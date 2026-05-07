package com.fish.chat.core.service;

import com.fish.chat.core.entity.dto.GroupDTO;
import java.util.List;

public interface GroupService {
    GroupDTO createGroup(String name, String avatar);
    GroupDTO getGroup(String code);
    void dismissGroup(String code);
    void addMember(String groupCode, String userCode);
    void removeMember(String groupCode, String userCode);
    List<GroupDTO> listMyGroups();
    List<GroupDTO> searchGroups(String keyword);
}