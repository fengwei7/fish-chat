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

        long count = friendRepository.selectCount(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, me.getCode())
                .eq(FriendPO::getFriendCode, friend.getCode()));
        if (count > 0) throw new BusinessException("已发送好友请求");

        FriendPO po = new FriendPO();
        po.setUserCode(me.getCode());
        po.setFriendCode(friend.getCode());
        po.setRemark(remark);
        po.setStatus(FriendStatus.PENDING.getValue());
        friendRepository.save(po);
    }

    @Transactional
    @Override
    public void acceptFriend(String friendCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);
        UserPO friend = userRepository.selectByCode(friendCode);
        if (friend == null) throw new BusinessException("用户不存在");

        // 更新对方发来的请求
        FriendPO po = friendRepository.selectOne(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, friend.getCode())
                .eq(FriendPO::getFriendCode, me.getCode())
                .eq(FriendPO::getStatus, FriendStatus.PENDING.getValue()));
        if (po == null) throw new BusinessException("没有待确认的好友请求");
        po.setStatus(FriendStatus.CONFIRMED.getValue());
        friendRepository.updateById(po);

        // 反向添加
        FriendPO reverse = new FriendPO();
        reverse.setUserCode(me.getCode());
        reverse.setFriendCode(friend.getCode());
        reverse.setStatus(FriendStatus.CONFIRMED.getValue());
        friendRepository.save(reverse);
    }

    @Transactional
    @Override
    public void removeFriend(String friendCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);
        UserPO friend = userRepository.selectByCode(friendCode);
        if (friend == null) throw new BusinessException("用户不存在");

        friendRepository.delete(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, me.getCode()).eq(FriendPO::getFriendCode, friend.getCode()));
        friendRepository.delete(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, friend.getCode()).eq(FriendPO::getFriendCode, me.getCode()));
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
