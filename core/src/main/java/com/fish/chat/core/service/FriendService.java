package com.fish.chat.core.service;

import com.fish.chat.core.entity.dto.FriendDTO;
import java.util.List;

public interface FriendService {
    void addFriend(String friendCode, String remark);
    void acceptFriend(String friendCode);
    void removeFriend(String friendCode);
    List<FriendDTO> listFriends();
    List<FriendDTO> searchUsers(String keyword);
}