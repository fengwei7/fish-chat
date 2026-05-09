package com.fish.chat.plugin.intellijplugin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fish.chat.plugin.intellijplugin.AppState
import com.fish.chat.plugin.intellijplugin.Screen
import com.fish.chat.plugin.intellijplugin.model.ChatMessageDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import java.text.SimpleDateFormat
import java.util.*

// 配色
private val TimestampColor = Color(0xFF666666)
private val MyMsgColor = Color(0xFF64B5F6)
private val OtherMsgColor = Color(0xFFFFB74D)
private val SystemMsgColor = Color(0xFF8BC34A)
private val OnlineColor = Color(0xFF4CAF50)
private val OfflineColor = Color(0xFFF44336)
private val InputBg = Color(0xFF3C3F41)
private val InputBorder = Color(0xFF555555)
private val PromptColor = Color(0xFF4CAF50)
private val DividerColor = Color(0xFF3C3F41)
private val EmptyColor = Color(0xFF555555)

@Composable
fun ChatPanel(state: AppState) {
    val conv = state.currentConversation ?: return
    val roomCode = conv.roomCode
    var historyLoaded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val messages = state.messages[roomCode] ?: emptyList()

    // 加载历史消息
    LaunchedEffect(roomCode) {
        if (!historyLoaded) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val data = state.api.getHistory(roomCode, 0, 50)
                    if (data != null) {
                        val list = state.messages.getOrPut(roomCode) { mutableListOf() }
                        list.clear()
                        list.addAll(data.messages.reversed())
                    }
                }
                historyLoaded = true
            }
        }
    }

    // 自动滚动到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val inputStyle = TextStyle(color = Color(0xFFBBBBBB), fontSize = 13.sp)

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题栏
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    state.screen = Screen.CHAT_LIST
                }) {
                    Text("<")
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(conv.name, fontWeight = FontWeight.Bold)
                    Text(conv.roomCode, fontSize = 10.sp, color = TimestampColor)
                }
            }
            
            // 显示对方用户在线状态（仅对私聊）
            if (conv.type == RoomType.PRIVATE) {
                // 解析私聊房间代码以获取对方用户code
                val parts = conv.roomCode.split(":")
                var otherUserCode = ""
                if (parts.size == 3 && parts[0] == "private") {
                    val myCode = state.currentUser.code ?: ""
                    otherUserCode = if (parts[1] == myCode) parts[2] else parts[1]
                }
                
                // 查找对方用户是否在线
                val isOtherOnline = state.conversations.firstOrNull { 
                    it.type == RoomType.PRIVATE && 
                    (it.code == otherUserCode || it.roomCode == conv.roomCode) 
                }?.online ?: false
                
                Text(
                    if (isOtherOnline) "[ONLINE]" else "[OFFLINE]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = if (isOtherOnline) OnlineColor else OfflineColor
                )
            } else {
                // 对于群聊和频道，显示WebSocket连接状态
                Text(
                    if (state.ws.isConnected) "[ONLINE]" else "[OFFLINE]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = if (state.ws.isConnected) OnlineColor else OfflineColor
                )
            }
        }

        // 分隔线
        Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

        // 消息列表（日志风格）
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "--- no messages yet ---",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = EmptyColor
                        )
                    }
                }
            }
            items(messages) { msg -> LogMessageItem(msg, state.currentUser.code ?: "") }
        }

        // 分隔线
        Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

        // 输入区域（命令行风格）
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(">", fontFamily = FontFamily.Monospace, color = PromptColor)
            Spacer(Modifier.width(6.dp))
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = inputStyle,
                modifier = Modifier
                    .weight(1f)
                    .background(InputBg, RoundedCornerShape(4.dp))
                    .border(1.dp, InputBorder, RoundedCornerShape(4.dp))
                    .padding(8.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter && !event.isShiftPressed) {
                            if (inputText.isNotBlank()) {
                                // 立即添加到本地消息列表
                                val localMsg = ChatMessageDTO(
                                    id = "temp_${System.currentTimeMillis()}",
                                    type = "TEXT",
                                    from = state.currentUser.code ?: "",
                                    senderName = state.currentUser.nickname ?: state.currentUser.username ?: "",
                                    senderAvatar = state.currentUser.avatarUrl ?: "",
                                    roomCode = roomCode,
                                    roomType = conv.type.name,
                                    content = inputText.trim(),
                                    timestamp = System.currentTimeMillis()
                                )
                                val list = state.messages.getOrPut(roomCode) { mutableListOf() }
                                list.add(localMsg)

                                // 发送消息到服务器
                                state.ws.sendMessage(
                                    roomCode = roomCode,
                                    roomType = conv.type.name,
                                    msgType = "TEXT",
                                    content = inputText.trim()
                                )
                                inputText = ""
                            }
                            true
                        } else {
                            false
                        }
                    }
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        // 立即添加到本地消息列表
                        val localMsg = ChatMessageDTO(
                            id = "temp_${System.currentTimeMillis()}",
                            type = "TEXT",
                            from = state.currentUser.code ?: "",
                            senderName = state.currentUser.nickname ?: state.currentUser.username ?: "",
                            senderAvatar = state.currentUser.avatarUrl ?: "",
                            roomCode = roomCode,
                            roomType = conv.type.name,
                            content = inputText.trim(),
                            timestamp = System.currentTimeMillis()
                        )
                        val list = state.messages.getOrPut(roomCode) { mutableListOf() }
                        list.add(localMsg)

                        // 发送消息到服务器
                        state.ws.sendMessage(
                            roomCode = roomCode,
                            roomType = conv.type.name,
                            msgType = "TEXT",
                            content = inputText.trim()
                        )
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && state.ws.isConnected
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun LogMessageItem(msg: ChatMessageDTO, myCode: String) {
    val senderCode = msg.from ?: ""
    val senderName = msg.senderName ?: ""
    val msgType = msg.type ?: "TEXT"
    val msgContent = msg.content ?: ""
    val isMine = senderCode == myCode
    val isSystem = msgType == "SYSTEM"
    val timeStr = remember(msg.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(msg.timestamp))
    }

    val (name, content, lineColor) = when {
        isSystem -> Triple("SYSTEM", msgContent, SystemMsgColor)
        isMine -> Triple("you", msgContent, MyMsgColor)
        msgType == "IMAGE" -> Triple(
            senderName.ifEmpty { senderCode.take(8) },
            "[IMAGE]",
            OtherMsgColor
        )
        msgType == "FILE" -> Triple(
            senderName.ifEmpty { senderCode.take(8) },
            "[FILE: ${msg.fileName ?: "unknown"}]",
            OtherMsgColor
        )
        else -> Triple(
            senderName.ifEmpty { senderCode.take(8) },
            msgContent,
            OtherMsgColor
        )
    }

    val textColor = Color(0xFFBBBBBB)
    val systemTextColor = Color(0xFF888888)

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Text(
            "[$timeStr] ",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TimestampColor
        )
        Text(
            "$name: ",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = lineColor
        )
        Text(
            content,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = if (isSystem) systemTextColor else textColor
        )
    }
}
