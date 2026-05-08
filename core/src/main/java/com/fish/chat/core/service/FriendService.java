package com.fish.chat.core.service;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.FriendDTO;

public interface FriendService {
    void addFriend(String friendCode, String remark);
    void acceptFriend(String friendCode);
    void removeFriend(String friendCode);
    PageResult<FriendDTO> listFriends(int pageNum, int pageSize);
    PageResult<FriendDTO> listFriendRequests(int pageNum, int pageSize);
    PageResult<FriendDTO> searchUsers(String keyword, int pageNum, int pageSize);
}