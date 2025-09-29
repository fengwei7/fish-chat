package com.fish.chat.websocket.service;

import com.fish.chat.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GroupCacheService {

    @Autowired
    private GroupService groupService;

    // 群组成员缓存，key为groupId，value为该群组的成员userId集合
    private final Map<String, Set<String>> groupMembersCache = new ConcurrentHashMap<>();

    /**
     * 更新群组成员缓存
     *
     * @param groupId 群组ID
     */
    public void updateGroupMembersCache(String groupId) {
        // 从数据库获取群组成员列表并更新缓存
        List<com.fish.chat.entity.GroupMember> groupMembers = groupService.getGroupMembers(Long.valueOf(groupId));
        Set<String> members = groupMembers.stream()
                .map(m -> String.valueOf(m.getUserId()))
                .collect(Collectors.toSet());
        groupMembersCache.put(groupId, members);
    }

    /**
     * 获取群组成员（带缓存）
     *
     * @param groupId 群组ID
     * @return 群组成员ID集合
     */
    public Set<String> getGroupMembers(String groupId) {
        // 先从缓存获取
        Set<String> members = groupMembersCache.get(groupId);
        if (members == null) {
            // 缓存未命中，从数据库获取
            updateGroupMembersCache(groupId);
            members = groupMembersCache.get(groupId);
        }
        return members;
    }

    /**
     * 从缓存中移除群组
     *
     * @param groupId 群组ID
     */
    public void removeGroupFromCache(String groupId) {
        groupMembersCache.remove(groupId);
    }
}