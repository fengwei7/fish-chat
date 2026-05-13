package com.fish.chat.plugin.intellijplugin.model

// ==================== API 通用响应 ====================

data class ApiResult<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null,
    val timestamp: Long = 0
)

data class PageResult<T>(
    val data: List<T> = emptyList(),
    val pageNum: Long = 0,
    val pageSize: Long = 0,
    val total: Long = 0
)

// ==================== 认证 ====================

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null,
    val mobile: String? = null,
    val nickname: String? = null
)

data class AuthDTO(
    val token: String? = null,
    val code: String? = null,
    val username: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null
)

// ==================== 用户 ====================

data class UserDTO(
    val code: String? = null,
    val username: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val profile: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val online: Boolean = false
)

data class UpdateProfileRequest(
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val profile: String? = null,
    val email: String? = null,
    val mobile: String? = null
)

// ==================== 好友 ====================

data class FriendRequest(
    val friendCode: String,
    val remark: String? = null
)

data class FriendAcceptRequest(
    val friendCode: String
)

data class FriendRemoveRequest(
    val friendCode: String
)

// ==================== 群组 ====================

data class CreateGroupRequest(
    val name: String,
    val avatar: String? = null
)

data class GroupMemberRequest(
    val userCode: String
)

// ==================== 频道 ====================

data class CreateChannelRequest(
    val name: String,
    val avatar: String? = null,
    val description: String? = null
)

// ==================== 文件 ====================

data class UploadFileResponse(
    val fileName: String? = null,
    val accessUrl: String? = null
)

// ==================== 会话（统一模型） ====================

enum class RoomType(val value: String) {
    PRIVATE("PRIVATE"),
    GROUP("GROUP"),
    CHANNEL("CHANNEL");

    companion object {
        fun fromValue(value: String): RoomType = when (value.uppercase()) {
            "PRIVATE" -> PRIVATE
            "GROUP" -> GROUP
            "CHANNEL" -> CHANNEL
            else -> PRIVATE
        }
    }
}

data class Conversation(
    val code: String,           // 好友code / 群code / 频道code
    val name: String,           // 显示名称
    val avatar: String = "",    // 头像
    val type: RoomType,         // 会话类型
    val roomCode: String,       // 房间ID（WebSocket 用）
    val online: Boolean = false,// 是否在线（仅私聊）
    val memberCount: Int = 0,   // 成员数（群聊/频道）
    val remark: String = ""     // 备注（私聊）
)

// ==================== 好友/群组/频道 DTO ====================

data class FriendDTO(
    val code: String? = null,
    val username: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val remark: String? = null,
    val status: Int = 0,
    val online: Boolean = false
)

data class GroupDTO(
    val code: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val ownerCode: String? = null,
    val notice: String? = null,
    val maxMembers: Int = 0,
    val memberCount: Int = 0,
    val status: Int = 0
)

data class ChannelDTO(
    val code: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val ownerCode: String? = null,
    val description: String? = null,
    val subscriberCount: Int = 0,
    val status: Int = 0
)

// ==================== 消息 ====================

data class ChatMessageDTO(
    val id: String? = null,
    val type: String? = "TEXT",
    val from: String? = null,
    val senderName: String? = null,
    val senderAvatar: String? = null,
    val to: String? = null,
    val content: String? = null,
    val timestamp: Long = 0,
    val roomCode: String? = null,
    val roomType: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null
)

data class MessageHistoryData(
    val messages: List<ChatMessageDTO> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val total: Long = 0
)

// ==================== WebSocket 协议包 ====================

data class WsPacket(
    val cmd: String = "",
    val code: Int = 0,
    val reqCode: String = "",
    val body: WsBody? = null
)

data class WsBody(
    val msgId: String? = null,
    val senderCode: String? = null,
    val senderName: String? = null,
    val senderAvatar: String? = null,
    val roomCode: String = "",
    val roomType: String = "PRIVATE",
    val msgType: String = "TEXT",
    val content: String = "",
    val fileName: String? = null,
    val fileSize: Long? = null,
    val timestamp: Long? = null,
    val extra: Map<String, Any>? = null
)

// ==================== 房间 code 工具 ====================

object RoomCodeBuilder {
    fun privateRoom(user1: String, user2: String): String {
        val prefix = RoomType.PRIVATE.value
        return if (user1 < user2) "$prefix:$user1:$user2" else "$prefix:$user2:$user1"
    }

    fun groupRoom(groupCode: String): String = "${RoomType.GROUP.value}:$groupCode"
    fun channelRoom(channelCode: String): String = "${RoomType.CHANNEL.value}:$channelCode"
}
