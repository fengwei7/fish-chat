package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.chat.SessionManager;
import com.fish.chat.core.entity.dto.FriendDTO;
import com.fish.chat.core.entity.po.FriendPO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.mapper.FriendMapper;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.FriendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendServiceImpl implements FriendService {

    @Resource private FriendMapper friendMapper;
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

        Long count = friendMapper.selectCount(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, me.getId())
                .eq(FriendPO::getFriendCode, friend.getId()));
        if (count > 0) throw new BusinessException("已发送好友请求");

        FriendPO po = new FriendPO();
        po.setUserCode(me.getCode());
        po.setFriendCode(friend.getCode());
        po.setRemark(remark);
        po.setStatus(0); // 待确认
        friendMapper.insert(po);
    }

    @Transactional
    @Override
    public void acceptFriend(String friendCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);
        UserPO friend = userRepository.selectByCode(friendCode);
        if (friend == null) throw new BusinessException("用户不存在");

        // 更新对方发来的请求
        FriendPO po = friendMapper.selectOne(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, friend.getId())
                .eq(FriendPO::getFriendCode, me.getId())
                .eq(FriendPO::getStatus, 0));
        if (po == null) throw new BusinessException("没有待确认的好友请求");
        po.setStatus(1);
        friendMapper.updateById(po);

        // 反向添加
        FriendPO reverse = new FriendPO();
        reverse.setUserCode(me.getCode());
        reverse.setFriendCode(friend.getCode());
        reverse.setStatus(1);
        friendMapper.insert(reverse);
    }

    @Transactional
    @Override
    public void removeFriend(String friendCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);
        UserPO friend = userRepository.selectByCode(friendCode);
        if (friend == null) throw new BusinessException("用户不存在");

        friendMapper.delete(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, me.getId()).eq(FriendPO::getFriendCode, friend.getId()));
        friendMapper.delete(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, friend.getId()).eq(FriendPO::getFriendCode, me.getId()));
    }

    @Override
    public List<FriendDTO> listFriends() {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO me = resolveUser(userCode);

        List<FriendPO> friends = friendMapper.selectList(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, me.getId())
                .eq(FriendPO::getStatus, 1));

        List<FriendDTO> result = new ArrayList<>();
        for (FriendPO f : friends) {
            UserPO user = userRepository.selectById(String.valueOf(f.getFriendCode()));
            if (user != null) {
                FriendDTO dto = new FriendDTO();
                dto.setCode(user.getCode());
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatarUrl(user.getAvatarUrl());
                dto.setRemark(f.getRemark());
                dto.setStatus(1);
                dto.setOnline(sessionManager.isOnline(user.getCode()));
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<FriendDTO> searchUsers(String keyword) {
        // 简单：按code或username搜索
        List<UserPO> users = new ArrayList<>();
        UserPO byCode = userRepository.selectByCode(keyword);
        if (byCode != null) users.add(byCode);
        UserPO byName = userRepository.selectByUsername(keyword);
        if (byName != null && !users.contains(byName)) users.add(byName);

        List<FriendDTO> result = new ArrayList<>();
        for (UserPO u : users) {
            FriendDTO dto = new FriendDTO();
            dto.setCode(u.getCode());
            dto.setUsername(u.getUsername());
            dto.setNickname(u.getNickname());
            dto.setAvatarUrl(u.getAvatarUrl());
            dto.setOnline(sessionManager.isOnline(u.getCode()));
            result.add(dto);
        }
        return result;
    }

    private UserPO resolveUser(String loginId) {
        UserPO u = userRepository.selectByCode(loginId);
        if (u == null) u = userRepository.selectById(loginId);
        if (u == null) throw new BusinessException("用户不存在");
        return u;
    }
}
