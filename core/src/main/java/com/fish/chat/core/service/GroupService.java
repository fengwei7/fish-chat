package com.fish.chat.core.service;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.GroupDTO;

public interface GroupService {
    GroupDTO createGroup(String name, String avatar);
    GroupDTO getGroup(String code);
    void dismissGroup(String code);
    void addMember(String groupCode, String userCode);
    void removeMember(String groupCode, String userCode);
    PageResult<GroupDTO> listMyGroups(int pageNum, int pageSize);
    PageResult<GroupDTO> searchGroups(String keyword, int pageNum, int pageSize);
}