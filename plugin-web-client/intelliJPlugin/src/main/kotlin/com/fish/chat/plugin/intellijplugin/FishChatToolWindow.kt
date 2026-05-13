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
    var messages = mutableStateMapOf<String, MutableList<ChatMessageDTO>>()
    var error by mutableStateOf<String?>(null)
    var autoLoginChecked by mutableStateOf(false)

    val api = FishChatApiClient()
    val ws get() = sharedWs

    private val props = PropertiesComponent.getInstance()

    init {
        val savedServer = props.getValue("fishchat.server", "http://localhost:8080")
        val savedToken = props.getValue("fishchat.token", "")
        api.serverUrl = savedServer
        if (savedToken.isNotEmpty()) {
            api.token = savedToken
            println("[FishChat] Loaded saved token: server=$savedServer token=${savedToken.take(16)}...")
            LOG.info("Loaded saved token: server=$savedServer token=${savedToken.take(16)}...")
        } else {
            println("[FishChat] No saved token found, will show login")
            LOG.info("No saved token found")
        }
    }

    fun saveCredentials(server: String, token: String) {
        props.setValue("fishchat.server", server)
        props.setValue("fishchat.token", token)
        println("[FishChat] Credentials saved: server=$server token=${token.take(16)}...")
    }

    fun clearCredentials() {
        props.setValue("fishchat.token", "")
        api.token = ""
        autoLoginToken = null
        println("[FishChat] Credentials cleared")
        LOG.info("Credentials cleared")
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
    val host = state.api.serverUrl
        .replace("http://", "")
        .replace("https://", "")
        .split(":")[0]
    val wsUrl = "ws://$host:8081"
    sharedWs.connect(wsUrl, state.api.token)
    bindWsListeners(state)
}

fun bindWsListeners(state: AppState) {
    sharedWs.onMessage = { packet ->
        if (packet.cmd == "MSG" && packet.body != null) {
            val body = packet.body
            val rc = body.roomCode
            val msgId = body.msgId
            val myCode = state.currentUser.code ?: ""
            val list = state.messages.getOrPut(rc) { mutableListOf() }

            // dedup by msgId
            val isDup = msgId != null && list.any { it.id == msgId }

            if (!isDup) {
                // replace local temp message if this is our own message broadcast back
                var handled = false
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
                            timestamp = body.timestamp ?: System.currentTimeMillis()
                        )
                        state.messages[rc] = list
                        handled = true
                    }
                }

                if (!handled) {
                    val newMsg = ChatMessageDTO(
                        id = msgId ?: "",
                        type = body.msgType,
                        from = body.senderCode ?: "",
                        senderName = body.senderName ?: "",
                        senderAvatar = body.senderAvatar ?: "",
                        roomCode = rc,
                        roomType = body.roomType,
                        content = body.content,
                        timestamp = body.timestamp ?: System.currentTimeMillis()
                    )
                    list.add(newMsg)
                    state.messages[rc] = list
                }
            }
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
