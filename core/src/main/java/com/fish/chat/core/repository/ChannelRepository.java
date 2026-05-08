package com.fish.chat.core.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.common.repository.BaseRepository;
import com.fish.chat.core.entity.po.ChannelMemberPO;
import com.fish.chat.core.entity.po.ChannelPO;
import com.fish.chat.core.mapper.ChannelMapper;
import com.fish.chat.core.mapper.ChannelMemberMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 频道数据访问层
 */
@Repository
public class ChannelRepository extends BaseRepository<ChannelPO> {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ChannelMemberMapper channelMemberMapper;

    @Override
    protected BaseMapper<ChannelPO> getBaseMapper() {
        return channelMapper;
    }

    /**
     * 分页查询用户的频道订阅关系
     */
    public Page<ChannelMemberPO> selectMemberPage(String userCode, Page<ChannelMemberPO> page) {
        return channelMemberMapper.selectPage(page,
                Wrappers.<ChannelMemberPO>lambdaQuery()
                        .eq(ChannelMemberPO::getUserCode, userCode));
    }

    /**
     * 根据 code 列表批量查询频道
     */
    public List<ChannelPO> selectByCodes(List<String> codes) {
        return channelMapper.selectList(
                Wrappers.<ChannelPO>lambdaQuery()
                        .in(ChannelPO::getCode, codes)
                        .eq(ChannelPO::getStatus, 1));
    }

    /**
     * 统计频道成员数
     */
    public long countMembers(String channelCode) {
        return channelMemberMapper.selectCount(
                Wrappers.<ChannelMemberPO>lambdaQuery()
                        .eq(ChannelMemberPO::getChannelCode, channelCode));
    }

    /**
     * 根据 code 查询频道
     */
    public ChannelPO selectByCode(String code) {
        return channelMapper.selectOne(
                Wrappers.<ChannelPO>lambdaQuery().eq(ChannelPO::getCode, code));
    }

    /**
     * 查询用户是否已订阅频道
     */
    public boolean isMember(String channelCode, String userCode) {
        return channelMemberMapper.selectCount(
                Wrappers.<ChannelMemberPO>lambdaQuery()
                        .eq(ChannelMemberPO::getChannelCode, channelCode)
                        .eq(ChannelMemberPO::getUserCode, userCode)) > 0;
    }

    /**
     * 插入频道成员
     */
    public void insertMember(ChannelMemberPO member) {
        channelMemberMapper.insert(member);
    }

    /**
     * 删除频道成员
     */
    public void deleteMember(String channelCode, String userCode) {
        channelMemberMapper.delete(Wrappers.<ChannelMemberPO>lambdaQuery()
                .eq(ChannelMemberPO::getChannelCode, channelCode)
                .eq(ChannelMemberPO::getUserCode, userCode));
    }
}
