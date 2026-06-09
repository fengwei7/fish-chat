package com.fish.chat.core.service;

import com.fish.chat.core.entity.dto.ConversationDTO;
import java.util.List;

public interface ConversationService {
    
    /**
     * 获取用户会话列表（按最后消息时间倒序）
     *
     * @param userCode 用户唯一标识
     * @param limit 返回数量限制
     * @return 会话列表
     */
    List<ConversationDTO> listConversations(String userCode, int limit);
    
    /**
     * 更新会话（发送/接收消息时调用）
     *
     * @param userCode 用户唯一标识
     * @param roomCode 房间编码
     * @param roomType 房间类型
     * @param lastMsgContent 最后一条消息内容
     */
    void updateConversation(String userCode, String roomCode, String roomType, String lastMsgContent);
    
    /**
     * 增加未读消息数
     *
     * @param userCode 用户唯一标识
     * @param roomCode 房间编码
     */
    void incrementUnread(String userCode, String roomCode);
    
    /**
     * 清除未读消息数
     *
     * @param userCode 用户唯一标识
     * @param roomCode 房间编码
     */
    void clearUnread(String userCode, String roomCode);
    
    /**
     * 获取用户总未读消息数
     *
     * @param userCode 用户唯一标识
     * @return 未读总数
     */
    int getTotalUnreadCount(String userCode);
    
    /**
     * 删除会话
     *
     * @param userCode 用户唯一标识
     * @param roomCode 房间编码
     */
    void removeConversation(String userCode, String roomCode);
}
