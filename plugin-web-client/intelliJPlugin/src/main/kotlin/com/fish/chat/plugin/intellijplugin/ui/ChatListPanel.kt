package com.fish.chat.plugin.intellijplugin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fish.chat.plugin.intellijplugin.AppState
import com.fish.chat.plugin.intellijplugin.Screen
import com.fish.chat.plugin.intellijplugin.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

private val SectionColor = Color(0xFF888888)
private val SubtitleColor = Color(0xFF777777)
private val OnlineColor = Color(0xFF4CAF50)

@Composable
fun ChatListPanel(state: AppState) {
    var loaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!loaded) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val friends = state.api.getFriends().map { f ->
                        Conversation(
                            code = f.code ?: "",
                            name = f.nickname?.ifEmpty { f.username ?: "" } ?: f.username ?: "",
                            avatar = f.avatarUrl ?: "",
                            type = RoomType.PRIVATE,
                            roomCode = RoomCodeBuilder.privateRoom(state.currentUser.code ?: "", f.code ?: ""),
                            online = f.online,
                            remark = f.remark ?: ""
                        )
                    }
                    val groups = state.api.getMyGroups().map { g ->
                        Conversation(
                            code = g.code ?: "",
                            name = g.name ?: "",
                            avatar = g.avatar ?: "",
                            type = RoomType.GROUP,
                            roomCode = RoomCodeBuilder.groupRoom(g.code ?: ""),
                            memberCount = g.memberCount
                        )
                    }
                    val channels = state.api.getMyChannels().map { c ->
                        Conversation(
                            code = c.code ?: "",
                            name = c.name ?: "",
                            avatar = c.avatar ?: "",
                            type = RoomType.CHANNEL,
                            roomCode = RoomCodeBuilder.channelRoom(c.code ?: ""),
                            memberCount = c.subscriberCount
                        )
                    }
                    state.conversations = friends + groups + channels
                }
                loaded = true
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
//                Text("Fish Chat", fontWeight = FontWeight.Bold)
                Text(
                    state.currentUser.nickname?.ifEmpty { state.currentUser.username ?: "" } ?: state.currentUser.username ?: "",
                    color = SubtitleColor
                )
            }
            OutlinedButton(onClick = {
                state.error = null
                state.ws.disconnect()
                state.clearCredentials()
                state.conversations = emptyList()
                state.clearMessages()
                state.currentConversation = null
                state.screen = Screen.LOGIN
            }) {
                Text("Logout")
            }
        }

        // 会话列表
        if (!loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...")
            }
        } else if (state.conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No conversations yet", color = SubtitleColor)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // 私聊
                val privates = state.conversations.filter { it.type == RoomType.PRIVATE }
                if (privates.isNotEmpty()) {
                    item {
                        Text("Direct Messages", modifier = Modifier.padding(12.dp, 8.dp), color = SectionColor)
                    }
                    items(privates) { conv -> ConversationItem(conv, state) }
                }

                // 群聊
                val groups = state.conversations.filter { it.type == RoomType.GROUP }
                if (groups.isNotEmpty()) {
                    item {
                        Text("Groups", modifier = Modifier.padding(12.dp, 8.dp), color = SectionColor)
                    }
                    items(groups) { conv -> ConversationItem(conv, state) }
                }

                // 频道
                val channels = state.conversations.filter { it.type == RoomType.CHANNEL }
                if (channels.isNotEmpty()) {
                    item {
                        Text("Channels", modifier = Modifier.padding(12.dp, 8.dp), color = SectionColor)
                    }
                    items(channels) { conv -> ConversationItem(conv, state) }
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(conv: Conversation, state: AppState) {
    val icon = when (conv.type) {
        RoomType.PRIVATE -> if (conv.online) "[*]" else "[ ]"
        RoomType.GROUP -> "[#]"
        RoomType.CHANNEL -> "[+]"
    }
    val subtitle = when (conv.type) {
        RoomType.PRIVATE -> if (conv.online) "online" else "offline"
        RoomType.GROUP -> "${conv.memberCount} members"
        RoomType.CHANNEL -> "${conv.memberCount} subscribers"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                state.currentConversation = conv
                state.screen = Screen.CHAT
            }
            .padding(12.dp, 8.dp)
    ) {
        Text(
            icon,
            color = when (conv.type) {
                RoomType.PRIVATE -> if (conv.online) OnlineColor else SubtitleColor
                RoomType.GROUP -> Color(0xFF64B5F6)
                RoomType.CHANNEL -> Color(0xFFCE93D8)
            }
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(conv.name, fontWeight = FontWeight.Medium)
            Text(subtitle, color = SubtitleColor)
        }
    }
}
