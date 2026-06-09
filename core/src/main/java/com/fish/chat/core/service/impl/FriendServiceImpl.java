package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.chat.SessionManager;
import com.fish.chat.core.entity.dto.FriendDTO;
import com.fish.chat.core.entity.po.FriendPO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.enums.FriendStatus;
import com.fish.chat.core.repository.FriendRepository;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.FriendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    @Resource private FriendRepository friendRepository;
    @Resource private UserRepository userRepository;
    @Resource private SessionManager sessionManager;

    @Transactional
    @Override
    public void addFriend(String friendCode, String remark) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);
        UserPO friend = userRepository.selectByCode(friendCode);
        if (friend == null) throw new BusinessException("用户不存在");
        if (me.getId().equals(friend.getId())) throw new BusinessException("不能添加自己为好友");

        if (friendRepository.existsFriendRelation(me.getCode(), friend.getCode())) {
            throw new BusinessException("已发送好友请求");
        }

        friendRepository.addFriendRequest(me.getCode(), friend.getCode(), remark);
    }

    @Transactional
    @Override
    public void acceptFriend(String friendCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);
        UserPO friend = userRepository.selectByCode(friendCode);
        if (friend == null) throw new BusinessException("用户不存在");

        // 更新对方发来的请求
        FriendPO po = friendRepository.selectPendingRequest(friend.getCode(), me.getCode());
        if (po == null) throw new BusinessException("没有待确认的好友请求");
        friendRepository.acceptFriendRequest(po);

        // 反向添加
        friendRepository.addConfirmedFriend(me.getCode(), friend.getCode());
    }

    @Transactional
    @Override
    public void rejectFriend(String friendCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);
        UserPO friend = userRepository.selectByCode(friendCode);
        if (friend == null) throw new BusinessException("用户不存在");

        // 查找对方发来的好友请求
        FriendPO po = friendRepository.selectPendingRequest(friend.getCode(), me.getCode());
        if (po == null) throw new BusinessException("没有待确认的好友请求");
        
        // 更新状态为已拒绝
        friendRepository.rejectFriendRequest(po);
    }

    @Transactional
    @Override
    public void removeFriend(String friendCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);
        UserPO friend = userRepository.selectByCode(friendCode);
        if (friend == null) throw new BusinessException("用户不存在");

        friendRepository.deleteBidirectionalFriend(me.getCode(), friend.getCode());
    }

    @Override
    public PageResult<FriendDTO> listFriends(int pageNum, int pageSize) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);

        Page<FriendPO> friendPage = friendRepository.selectFriendPage(
                me.getCode(), new Page<>(pageNum, pageSize));

        List<String> friendCodes = friendPage.getRecords().stream()
                .map(FriendPO::getFriendCode)
                .collect(Collectors.toList());

        if (friendCodes.isEmpty()) {
            return PageResult.of(new ArrayList<>(), pageNum, pageSize, friendPage.getTotal());
        }

        List<UserPO> users = userRepository.selectByCodes(friendCodes);
        Map<String, UserPO> userMap = users.stream()
                .collect(Collectors.toMap(UserPO::getCode, u -> u));

        List<FriendDTO> result = new ArrayList<>();
        for (FriendPO f : friendPage.getRecords()) {
            UserPO user = userMap.get(f.getFriendCode());
            if (user != null) {
                FriendDTO dto = new FriendDTO();
                dto.setCode(user.getCode());
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatarUrl(user.getAvatarUrl());
                dto.setRemark(f.getRemark());
                dto.setStatus(FriendStatus.CONFIRMED.getValue());
                dto.setOnline(sessionManager.isOnline(user.getCode()));
                result.add(dto);
            }
        }
        return PageResult.of(result, pageNum, pageSize, friendPage.getTotal());
    }

    @Override
    public PageResult<FriendDTO> listFriendRequests(int pageNum, int pageSize) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);

        Page<FriendPO> requestPage = friendRepository.selectRequestPage(
                me.getCode(), new Page<>(pageNum, pageSize));

        List<String> userCodes = requestPage.getRecords().stream()
                .map(FriendPO::getUserCode)
                .collect(Collectors.toList());

        if (userCodes.isEmpty()) {
            return PageResult.of(new ArrayList<>(), pageNum, pageSize, requestPage.getTotal());
        }

        List<UserPO> users = userRepository.selectByCodes(userCodes);
        Map<String, UserPO> userMap = users.stream()
                .collect(Collectors.toMap(UserPO::getCode, u -> u));

        List<FriendDTO> result = new ArrayList<>();
        for (FriendPO req : requestPage.getRecords()) {
            UserPO user = userMap.get(req.getUserCode());
            if (user != null) {
                FriendDTO dto = new FriendDTO();
                dto.setCode(user.getCode());
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatarUrl(user.getAvatarUrl());
                dto.setRemark(req.getRemark());
                dto.setStatus(FriendStatus.PENDING.getValue());
                dto.setOnline(sessionManager.isOnline(user.getCode()));
                result.add(dto);
            }
        }
        return PageResult.of(result, pageNum, pageSize, requestPage.getTotal());
    }

    @Override
    public PageResult<FriendDTO> searchUsers(String keyword, int pageNum, int pageSize) {
        Page<UserPO> userPage = userRepository.selectByCodeOrUsernamePage(
                keyword, new Page<>(pageNum, pageSize));

        List<FriendDTO> result = new ArrayList<>();
        for (UserPO u : userPage.getRecords()) {
            FriendDTO dto = new FriendDTO();
            dto.setCode(u.getCode());
            dto.setUsername(u.getUsername());
            dto.setNickname(u.getNickname());
            dto.setAvatarUrl(u.getAvatarUrl());
            dto.setOnline(sessionManager.isOnline(u.getCode()));
            result.add(dto);
        }
        return PageResult.of(result, pageNum, pageSize, userPage.getTotal());
    }

    private UserPO resolveUser(String loginId) {
        UserPO u = userRepository.selectByCode(loginId);
        if (u == null) u = userRepository.selectById(loginId);
        if (u == null) throw new BusinessException("用户不存在");
        return u;
    }
}
