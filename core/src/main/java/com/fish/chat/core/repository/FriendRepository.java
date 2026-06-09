package com.fish.chat.core.repository;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.core.entity.po.FriendPO;
import com.fish.chat.core.enums.FriendStatus;
import com.fish.chat.core.mapper.FriendMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 好友数据访问层
 */
@Repository
public class FriendRepository {

    @Resource
    private FriendMapper friendMapper;

    /**
     * 保存好友记录
     */
    public void save(FriendPO data) {
        friendMapper.insert(data);
    }

    /**
     * 分页查询用户的好友列表
     */
    public Page<FriendPO> selectFriendPage(String userCode, Page<FriendPO> page) {
        return friendMapper.selectPage(page,
                Wrappers.<FriendPO>lambdaQuery()
                        .eq(FriendPO::getUserCode, userCode)
                        .eq(FriendPO::getStatus, FriendStatus.CONFIRMED.getValue()));
    }

    /**
     * 分页查询收到的好友申请
     */
    public Page<FriendPO> selectRequestPage(String friendCode, Page<FriendPO> page) {
        return friendMapper.selectPage(page,
                Wrappers.<FriendPO>lambdaQuery()
                        .eq(FriendPO::getFriendCode, friendCode)
                        .eq(FriendPO::getStatus, FriendStatus.PENDING.getValue()));
    }

    /**
     * 根据 Wrapper 查询单条
     */
    public FriendPO selectOne(Wrapper<FriendPO> queryWrapper) {
        return friendMapper.selectOne(queryWrapper);
    }

    /**
     * 根据条件统计数量
     */
    public long selectCount(Wrapper<FriendPO> queryWrapper) {
        return friendMapper.selectCount(queryWrapper);
    }

    /**
     * 根据 ID 更新
     */
    public int updateById(FriendPO data) {
        return friendMapper.updateById(data);
    }

    /**
     * 根据 Wrapper 删除
     */
    public int delete(Wrapper<FriendPO> queryWrapper) {
        return friendMapper.delete(queryWrapper);
    }

    /**
     * 检查两个用户之间是否存在好友关系（任意状态）
     */
    public boolean existsFriendRelation(String userCode, String friendCode) {
        return selectCount(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, userCode)
                .eq(FriendPO::getFriendCode, friendCode)) > 0;
    }

    /**
     * 查询待处理的好友请求（由对方发起）
     */
    public FriendPO selectPendingRequest(String fromUserCode, String toUserCode) {
        return selectOne(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, fromUserCode)
                .eq(FriendPO::getFriendCode, toUserCode)
                .eq(FriendPO::getStatus, FriendStatus.PENDING.getValue()));
    }

    /**
     * 添加好友请求
     */
    public void addFriendRequest(String userCode, String friendCode, String remark) {
        FriendPO po = new FriendPO();
        po.setUserCode(userCode);
        po.setFriendCode(friendCode);
        po.setRemark(remark);
        po.setStatus(FriendStatus.PENDING.getValue());
        save(po);
    }

    /**
     * 接受好友请求（更新状态为已确认）
     */
    public void acceptFriendRequest(FriendPO friendRequest) {
        friendRequest.setStatus(FriendStatus.CONFIRMED.getValue());
        updateById(friendRequest);
    }

    /**
     * 拒绝好友请求（更新状态为已拒绝）
     */
    public void rejectFriendRequest(FriendPO friendRequest) {
        friendRequest.setStatus(FriendStatus.REJECTED.getValue());
        updateById(friendRequest);
    }

    /**
     * 添加已确认的好友关系
     */
    public void addConfirmedFriend(String userCode, String friendCode) {
        FriendPO po = new FriendPO();
        po.setUserCode(userCode);
        po.setFriendCode(friendCode);
        po.setStatus(FriendStatus.CONFIRMED.getValue());
        save(po);
    }

    /**
     * 删除双向好友关系
     */
    public void deleteBidirectionalFriend(String userCode, String friendCode) {
        delete(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, userCode)
                .eq(FriendPO::getFriendCode, friendCode));
        delete(Wrappers.<FriendPO>lambdaQuery()
                .eq(FriendPO::getUserCode, friendCode)
                .eq(FriendPO::getFriendCode, userCode));
    }
}
