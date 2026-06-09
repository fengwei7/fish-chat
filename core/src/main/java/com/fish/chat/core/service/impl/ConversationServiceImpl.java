package com.fish.chat.core.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fish.chat.common.redisutils.RedisUtil;
import com.fish.chat.core.chat.SessionManager;
import com.fish.chat.core.entity.dto.ConversationDTO;
import com.fish.chat.core.entity.po.ChatMessage;
import com.fish.chat.core.entity.po.GroupPO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.enums.RoomType;
import com.fish.chat.core.repository.ChatMessageRepository;
import com.fish.chat.core.repository.GroupRepository;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ConversationServiceImpl implements ConversationService {

    @Resource
    private RedisUtil redisUtil;
    
    @Resource
    private UserRepository userRepository;
    
    @Resource
    private GroupRepository groupRepository;
    
    @Resource
    private ChatMessageRepository messageRepository;
    
    @Resource
    private SessionManager sessionManager;

    private static final String CONVERSATION_KEY_PREFIX = "conversation:";
    private static final String UNREAD_KEY_PREFIX = "conversation:unread:";
    private static final long UNREAD_EXPIRE_DAYS = 30; // 未读数30天过期

    @Override
    public List<ConversationDTO> listConversations(String userCode, int limit) {
        String key = CONVERSATION_KEY_PREFIX + userCode;
        
        // 1. 检查 Redis 中是否存在会话数据
        long size = redisUtil.getSortedSetSize(key);
        
        // 2. 如果不存在，按需从 MongoDB 恢复
        if (size == 0) {
            log.info("用户 {} 会话列表为空，从 MongoDB 按需恢复", userCode);
            recoverUserConversations(userCode);
        }
        
        // 3. 从 Redis Sorted Set 获取会话列表（按时间倒序）
        Set<String> conversations = redisUtil.getSortedSetRange(key, 0, limit - 1);
        
        List<ConversationDTO> result = new ArrayList<>();
        for (String convJson : conversations) {
            try {
                // 4. 解析 JSON
                ConversationDTO dto = JSON.parseObject(convJson, ConversationDTO.class);
                
                // 5. 获取未读数
                Integer unread = getUnreadCount(userCode, dto.getRoomCode());
                dto.setUnreadCount(unread);
                
                // 6. 根据房间类型查询房间信息
                fillRoomInfo(dto, userCode);
                
                result.add(dto);
            } catch (Exception e) {
                log.error("解析会话数据失败: {}", convJson, e);
            }
        }
        
        return result;
    }

    @Override
    public void updateConversation(String userCode, String roomCode, String roomType, String lastMsgContent) {
        String key = CONVERSATION_KEY_PREFIX + userCode;
        
        // 构建会话信息
        Map<String, Object> conv = new HashMap<>();
        conv.put("roomCode", roomCode);
        conv.put("roomType", roomType);
        conv.put("lastMsgContent", lastMsgContent);
        conv.put("lastMsgTime", System.currentTimeMillis());
        conv.put("isTop", false);
        conv.put("isMute", false);
        
        // 添加到 Sorted Set，使用时间戳作为分数
        redisUtil.addToSortedSet(
            key,
            JSON.toJSONString(conv),
            System.currentTimeMillis()
        );
        
        log.debug("更新会话: userCode={}, roomCode={}", userCode, roomCode);
    }

    @Override
    public void incrementUnread(String userCode, String roomCode) {
        String key = UNREAD_KEY_PREFIX + userCode + ":" + roomCode;
        
        // 自增未读数，并设置30天过期时间
        Long count = redisUtil.incrementValueWithExpire(key, UNREAD_EXPIRE_DAYS, TimeUnit.DAYS);
        
        log.debug("增加未读数: userCode={}, roomCode={}, count={}", userCode, roomCode, count);
    }

    @Override
    public void clearUnread(String userCode, String roomCode) {
        String key = UNREAD_KEY_PREFIX + userCode + ":" + roomCode;
        redisUtil.deleteByKey(key);
        
        log.debug("清除未读数: userCode={}, roomCode={}", userCode, roomCode);
    }

    @Override
    public int getTotalUnreadCount(String userCode) {
        // 扫描用户所有未读key
        String pattern = UNREAD_KEY_PREFIX + userCode + ":*";
        Set<String> keys = redisUtil.scanKeys(pattern);
        
        int total = 0;
        for (String key : keys) {
            Object value = redisUtil.getByKey(key);
            if (value != null) {
                try {
                    total += Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    log.error("解析未读数失败: key={}", key, e);
                }
            }
        }
        
        return total;
    }

    @Override
    public void removeConversation(String userCode, String roomCode) {
        String key = CONVERSATION_KEY_PREFIX + userCode;
        
        // 从 Sorted Set 中移除所有包含该 roomCode 的记录
        Set<String> conversations = redisUtil.getSortedSetRange(key, 0, -1);
        for (String convJson : conversations) {
            try {
                ConversationDTO dto = JSON.parseObject(convJson, ConversationDTO.class);
                if (dto.getRoomCode().equals(roomCode)) {
                    redisUtil.removeFromSortedSet(key, convJson);
                    // 同时清除未读数
                    clearUnread(userCode, roomCode);
                    break;
                }
            } catch (Exception e) {
                log.error("删除会话失败: {}", convJson, e);
            }
        }
        
        log.info("删除会话: userCode={}, roomCode={}", userCode, roomCode);
    }
    
    /**
     * 按需恢复单个用户的会话列表
     * 当用户查询会话列表且 Redis 中不存在时调用
     * 
     * @param userCode 用户编码
     */
    private void recoverUserConversations(String userCode) {
        try {
            log.info("开始恢复用户 {} 的会话列表", userCode);
            
            // 1. 查询用户发送的所有消息（最近100条）
            PageRequest pageRequest = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp"));
            List<ChatMessage> sentMessages = messageRepository.findByFromOrderByTimestampDesc(userCode, pageRequest);
            
            // 2. 按 roomCode 去重，保留每个房间最新的消息
            Map<String, ChatMessage> latestByRoom = new LinkedHashMap<>();
            for (ChatMessage msg : sentMessages) {
                // 只保留第一次出现的（时间最新的）
                latestByRoom.putIfAbsent(msg.getRoomCode(), msg);
            }
            
            // 3. 重建会话列表
            int recoveredCount = 0;
            for (ChatMessage msg : latestByRoom.values()) {
                // 获取接收者列表
                Set<String> receivers = getReceivers(msg);
                
                for (String receiverCode : receivers) {
                    if (!receiverCode.equals(msg.getFrom())) {
                        updateConversation(receiverCode, msg.getRoomCode(), 
                            msg.getRoomType(), msg.getContent());
                        recoveredCount++;
                    }
                }
                
                // 也更新发送者的会话列表
                updateConversation(msg.getFrom(), msg.getRoomCode(), 
                    msg.getRoomType(), msg.getContent());
                recoveredCount++;
            }
            
            log.info("用户 {} 会话列表恢复完成，共恢复 {} 个会话", userCode, recoveredCount);
        } catch (Exception e) {
            log.error("恢复用户 {} 的会话列表失败", userCode, e);
        }
    }
    
    /**
     * 获取消息的接收者列表
     * 
     * @param msg 消息
     * @return 接收者用户编码集合
     */
    private Set<String> getReceivers(ChatMessage msg) {
        Set<String> receivers = new HashSet<>();
        
        if (RoomType.GROUP.getValue().equals(msg.getRoomType())) {
            // 群聊：需要获取群成员
            // 注意：这里简化处理，实际应该从 GroupRepository 获取群成员
            // 但由于恢复逻辑是按需的，这里暂时返回空
            // 群成员会在用户加入房间时自动创建会话
        } else if (RoomType.PRIVATE.getValue().equals(msg.getRoomType())) {
            // 私聊：接收者是消息的 to 字段
            if (msg.getTo() != null) {
                receivers.add(msg.getTo());
            }
        }
        
        return receivers;
    }

    /**
     * 填充房间信息
     */
    private void fillRoomInfo(ConversationDTO dto, String userCode) {
        try {
            if (RoomType.GROUP.getValue().equals(dto.getRoomType())) {
                // 群聊：查询群信息
                GroupPO group = groupRepository.selectByCode(dto.getRoomCode());
                if (group != null) {
                    dto.setRoomName(group.getName());
                    dto.setRoomAvatar(group.getAvatar());
                }
            } else if (RoomType.PRIVATE.getValue().equals(dto.getRoomType())) {
                // 私聊：查询对方用户信息
                String friendCode = extractFriendCode(dto.getRoomCode(), userCode);
                if (friendCode != null) {
                    UserPO friend = userRepository.selectByCode(friendCode);
                    if (friend != null) {
                        dto.setFriendCode(friend.getCode());
                        dto.setFriendNickname(friend.getNickname());
                        dto.setFriendAvatar(friend.getAvatarUrl());
                        dto.setRoomName(friend.getNickname());
                        dto.setRoomAvatar(friend.getAvatarUrl());
                        
                        // 查询在线状态
                        dto.setFriendOnline(sessionManager.isOnline(friend.getCode()));
                    }
                }
            }
        } catch (Exception e) {
            log.error("填充房间信息失败: roomCode={}", dto.getRoomCode(), e);
        }
    }

    /**
     * 从私聊 roomCode 中提取对方用户code
     * 格式: "private:userCode1:userCode2"
     */
    private String extractFriendCode(String roomCode, String userCode) {
        if (roomCode == null || !roomCode.startsWith("private:")) {
            return null;
        }
        
        String[] parts = roomCode.split(":");
        if (parts.length >= 3) {
            // 返回不是当前用户的另一个用户code
            if (parts[1].equals(userCode)) {
                return parts[2];
            } else {
                return parts[1];
            }
        }
        
        return null;
    }

    /**
     * 获取未读消息数
     */
    private Integer getUnreadCount(String userCode, String roomCode) {
        String key = UNREAD_KEY_PREFIX + userCode + ":" + roomCode;
        Object value = redisUtil.getByKey(key);
        
        if (value == null) {
            return 0;
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
