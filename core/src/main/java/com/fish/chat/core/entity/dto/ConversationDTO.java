package com.fish.chat.core.entity.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ConversationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 房间信息
    private String roomCode;
    private String roomType;  // GROUP, PRIVATE, CHANNEL
    
    // 最后一条消息
    private String lastMsgContent;
    private Long lastMsgTime;
    
    // 未读和设置
    private Integer unreadCount;
    private Boolean isTop;
    private Boolean isMute;
    
    // 房间详情（从其他表查询）
    private String roomName;
    private String roomAvatar;
    
    // 私聊时的对方用户信息
    private String friendCode;
    private String friendNickname;
    private String friendAvatar;
    
    // 在线状态（仅私聊有效）
    private Boolean friendOnline;
}
