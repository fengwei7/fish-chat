package com.fish.chat.plugin.intellijplugin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fish.chat.plugin.intellijplugin.model.*
import com.fish.chat.plugin.intellijplugin.service.FishChatApiClient
import com.fish.chat.plugin.intellijplugin.service.FishChatWebSocketClient
import com.fish.chat.plugin.intellijplugin.ui.ChatListPanel
import com.fish.chat.plugin.intellijplugin.ui.ChatPanel
import com.fish.chat.plugin.intellijplugin.ui.LoginPanel
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.ui.component.Text

private val LOG = Logger.getInstance("FishChat")

// WebSocket 单例 — 无论 ToolWindow 被 Jewel bridge 重复创建多少次，
// 始终只有一个 WS 连接，避免重复建连导致互踢
private val sharedWs = FishChatWebSocketClient()
private var autoLoginToken: String? = null

class FishChatToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        LOG.info("FishChat tool window opened")
        toolWindow.addComposeTab("Fish Chat", focusOnClickInside = true) {
            FishChatApp()
        }
    }
}

// ==================== 应用状态 ====================

enum class Screen { LOGIN, CHAT_LIST, CHAT }

class AppState {
    var screen by mutableStateOf(Screen.LOGIN)
    var currentUser by mutableStateOf(AuthDTO())
    var conversations by mutableStateOf<List<Conversation>>(emptyList())
    var currentConversation by mutableStateOf<Conversation?>(null)
    var messages by mutableStateOf<Map<String, List<ChatMessageDTO>>>(emptyMap())
    private val msgLock = Any()
    var error by mutableStateOf<String?>(null)
    var autoLoginChecked by mutableStateOf(false)

    val api = FishChatApiClient()
    val ws get() = sharedWs

    var wsUrl: String = "wss://fish-chat-wss.935577.xyz/ws-api"

    private val props = PropertiesComponent.getInstance()

    init {
        val savedServer = props.getValue("fishchat.server", "https://fish-chat-api.935577.xyz/api")
        val savedToken = props.getValue("fishchat.token", "")
        api.serverUrl = savedServer
        wsUrl = props.getValue("fishchat.wsUrl", "wss://fish-chat-wss.935577.xyz/ws-api")
        if (savedToken.isNotEmpty()) {
            api.token = savedToken
            currentUser = AuthDTO(
                token = savedToken,
                code = props.getValue("fishchat.userCode", ""),
                username = props.getValue("fishchat.username", ""),
                nickname = props.getValue("fishchat.nickname", ""),
                avatarUrl = props.getValue("fishchat.avatarUrl", "")
            )
            println("[FishChat] Loaded saved token: server=$savedServer token=${savedToken.take(16)}... user=${currentUser.nickname ?: currentUser.username}")
            LOG.info("Loaded saved token: server=$savedServer token=${savedToken.take(16)}...")
        } else {
            println("[FishChat] No saved token found, will show login")
            LOG.info("No saved token found")
        }
    }

    fun saveCredentials(server: String, token: String, user: AuthDTO) {
        props.setValue("fishchat.server", server)
        props.setValue("fishchat.token", token)
        props.setValue("fishchat.wsUrl", wsUrl)
        props.setValue("fishchat.userCode", user.code ?: "")
        props.setValue("fishchat.username", user.username ?: "")
        props.setValue("fishchat.nickname", user.nickname ?: "")
        props.setValue("fishchat.avatarUrl", user.avatarUrl ?: "")
        println("[FishChat] Credentials saved: server=$server token=${token.take(16)}... user=${user.nickname ?: user.username}")
    }

    fun clearCredentials() {
        props.setValue("fishchat.token", "")
        props.setValue("fishchat.userCode", "")
        props.setValue("fishchat.username", "")
        props.setValue("fishchat.nickname", "")
        props.setValue("fishchat.avatarUrl", "")
        api.token = ""
        autoLoginToken = null
        currentUser = AuthDTO()
        println("[FishChat] Credentials cleared")
        LOG.info("Credentials cleared")
    }

    fun addTempMessage(roomCode: String, msg: ChatMessageDTO) {
        synchronized(msgLock) {
            val cur = messages
            val list = (cur[roomCode] ?: emptyList()).toMutableList()
            list.add(msg)
            messages = cur + (roomCode to list)
        }
    }

    fun mergeHistory(roomCode: String, historyMsgs: List<ChatMessageDTO>) {
        if (historyMsgs.isEmpty()) return
        synchronized(msgLock) {
            val cur = messages
            val list = (cur[roomCode] ?: emptyList()).toMutableList()
            val existingIds = list.mapNotNull { it.id }.toSet()
            for (msg in historyMsgs) {
                val msgId = msg.id ?: ""
                if (msgId.isNotEmpty() && msgId in existingIds) continue
                val tempIdx = list.indexOfLast {
                    it.id?.startsWith("temp_") == true &&
                        it.content == msg.content &&
                        it.from == msg.from
                }
                if (tempIdx >= 0) list[tempIdx] = msg
                else list.add(msg)
            }
            messages = cur + (roomCode to list)
        }
    }

    fun clearMessages() {
        synchronized(msgLock) {
            messages = emptyMap()
        }
    }

    fun handleWsMessage(body: WsBody) {
        val rc = body.roomCode
        val msgId = body.msgId
        val myCode = currentUser.code ?: ""

        synchronized(msgLock) {
            val cur = messages
            val list = (cur[rc] ?: emptyList()).toMutableList()

            // dedup by msgId
            if (msgId != null && list.any { it.id == msgId }) return

            val now = System.currentTimeMillis()
            var updated = false

            // if it's my own message echoed back, try to replace temp
            if (body.senderCode != null && body.senderCode == myCode) {
                val tempIdx = list.indexOfLast {
                    it.id?.startsWith("temp_") == true && it.content == body.content
                }
                if (tempIdx >= 0) {
                    list[tempIdx] = ChatMessageDTO(
                        id = msgId ?: list[tempIdx].id,
                        type = body.msgType,
                        from = body.senderCode ?: "",
                        senderName = body.senderName ?: "",
                        senderAvatar = body.senderAvatar ?: "",
                        roomCode = rc,
                        roomType = body.roomType,
                        content = body.content,
                        timestamp = body.timestamp ?: now
                    )
                    updated = true
                } else {
                    // fallback: match by sender + content + 10s window
                    val recentIdx = list.indexOfLast {
                        it.from == body.senderCode &&
                            it.content == body.content &&
                            (now - it.timestamp) < 10_000
                    }
                    if (recentIdx >= 0) {
                        list[recentIdx] = list[recentIdx].copy(
                            id = msgId ?: list[recentIdx].id,
                            senderName = body.senderName ?: list[recentIdx].senderName,
                            senderAvatar = body.senderAvatar ?: list[recentIdx].senderAvatar,
                            timestamp = body.timestamp ?: list[recentIdx].timestamp
                        )
                        updated = true
                    }
                }
            }

            if (!updated) {
                // dedup check before adding
                val alreadyExists = (msgId != null && list.any { it.id == msgId }) ||
                    list.any {
                        it.from == body.senderCode &&
                            it.content == body.content &&
                            (now - it.timestamp) < 10_000
                    }
                if (!alreadyExists) {
                    list.removeAll {
                        it.id?.startsWith("temp_") == true &&
                            it.from == body.senderCode &&
                            it.content == body.content
                    }
                    list.add(
                        ChatMessageDTO(
                            id = msgId ?: "",
                            type = body.msgType,
                            from = body.senderCode ?: "",
                            senderName = body.senderName ?: "",
                            senderAvatar = body.senderAvatar ?: "",
                            roomCode = rc,
                            roomType = body.roomType,
                            content = body.content,
                            timestamp = body.timestamp ?: now
                        )
                    )
                }
            }

            messages = cur + (rc to list)
        }
    }
}

// ==================== 主 Composable ====================

@Composable
fun FishChatApp() {
    val state = remember { AppState() }

    // 自动登录：只需一次，后续 composition 直接复用已有连接
    LaunchedEffect(Unit) {
        if (!state.autoLoginChecked && state.api.token.isNotEmpty()) {
            state.autoLoginChecked = true

            // 如果 WS 已经连着（第一个 composition 已经建连成功，第二个 composition 进来），
            // 只需要重新绑定回调到当前 state，不重复建连
            if (sharedWs.isConnected) {
                bindWsListeners(state)
                state.screen = Screen.CHAT_LIST
                println("[FishChat] Auto-login: reuse existing WS connection")
                return@LaunchedEffect
            }

            // 首次建连
            autoLoginToken = state.api.token
            println("[FishChat] Auto-login: connecting WS...")
            LOG.info("Auto-login: connecting WS")
            try {
                withContext(Dispatchers.IO) {
                    connectWs(state)
                }
                println("[FishChat] Auto-login: success")
                LOG.info("Auto-login: success")
                state.screen = Screen.CHAT_LIST
            } catch (e: CancellationException) {
                // Composition 被替换，WS 可能已经连上了
        // 不重置 autoLoginToken，让后续 composition 复用连接
                throw e
            } catch (e: Exception) {
                autoLoginToken = null
                println("[FishChat] Auto-login FAILED: ${e.message}")
                LOG.warn("Auto-login failed", e)
                state.clearCredentials()
                state.screen = Screen.LOGIN
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (state.screen) {
            Screen.LOGIN -> LoginPanel(state)
            Screen.CHAT_LIST -> ChatListPanel(state)
            Screen.CHAT -> ChatPanel(state)
        }

        // 错误提示
//        state.error?.let { err ->
//            println("[FishChat] ERROR displayed: $err")
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//                    .background(Color(0xFF424242))
//                    .padding(12.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(err, fontSize = 12.sp)
//            }
//        }
    }
}

private fun connectWs(state: AppState) {
    sharedWs.connect(state.wsUrl, state.api.token)
    bindWsListeners(state)
}

fun bindWsListeners(state: AppState) {
    sharedWs.onMessage = { packet ->
        if (packet.cmd == "MSG" && packet.body != null) {
            state.handleWsMessage(packet.body)
        }
    }
    sharedWs.onErrorPacket = { packet ->
        val errMsg = packet.body?.content ?: "Send failed"
        state.error = errMsg
    }
    sharedWs.onDisconnected = { reason ->
        state.error = "WebSocket disconnected: $reason"
    }
    sharedWs.onNotify = { packet ->
        val content = packet.body?.content ?: ""
        if (content.isNotEmpty()) {
            state.error = content
        }
    }
}
