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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.ui.component.Text

private val LOG = Logger.getInstance("FishChat")

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
    var messages by mutableStateOf<MutableMap<String, MutableList<ChatMessageDTO>>>(mutableMapOf())
    var error by mutableStateOf<String?>(null)
    var autoLoginChecked by mutableStateOf(false)

    val api = FishChatApiClient()
    val ws = FishChatWebSocketClient()

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
        println("[FishChat] Credentials cleared")
        LOG.info("Credentials cleared")
    }
}

// ==================== 主 Composable ====================

@Composable
fun FishChatApp() {
    val state = remember { AppState() }
    val scope = rememberCoroutineScope()

    // 自动登录检查
    LaunchedEffect(Unit) {
        if (!state.autoLoginChecked && state.api.token.isNotEmpty()) {
            state.autoLoginChecked = true
            println("[FishChat] Auto-login: attempting with saved token...")
            LOG.info("Auto-login: attempting with saved token")
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val host = state.api.serverUrl
                            .replace("http://", "")
                            .replace("https://", "")
                            .split(":")[0]
                        val wsUrl = "ws://$host:8081"
                        println("[FishChat] Auto-login: connecting WS to $wsUrl")
                        state.ws.connect(wsUrl, state.api.token)
                        setupWsListener(state)
                    }
                    println("[FishChat] Auto-login: success, showing chat list")
                    LOG.info("Auto-login success")
                    state.screen = Screen.CHAT_LIST
                } catch (e: Exception) {
                    println("[FishChat] Auto-login FAILED: ${e.message}")
                    LOG.warn("Auto-login failed", e)
                    state.clearCredentials()
                    state.screen = Screen.LOGIN
                }
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
        state.error?.let { err ->
            println("[FishChat] ERROR displayed: $err")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF424242))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(err, fontSize = 12.sp)
            }
        }
    }
}

fun setupWsListener(state: AppState) {
    state.ws.onMessage = { packet ->
        if (packet.cmd == "MSG" && packet.body != null) {
            val body = packet.body
            val rc = body.roomCode
            val list = state.messages.getOrPut(rc) { mutableListOf() }
            list.add(ChatMessageDTO(
                id = body.msgId ?: "",
                type = body.msgType,
                from = body.senderCode ?: "",
                senderName = body.senderName ?: "",
                senderAvatar = body.senderAvatar ?: "",
                roomCode = rc,
                roomType = body.roomType,
                content = body.content,
                timestamp = body.timestamp ?: System.currentTimeMillis()
            ))
        }
    }
    state.ws.onDisconnected = { reason ->
        state.error = "WebSocket disconnected: $reason"
    }
}
