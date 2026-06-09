package com.fish.chat.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.common.repository.BaseRepository;
import com.fish.chat.core.entity.po.GroupMemberPO;
import com.fish.chat.core.entity.po.GroupPO;
import com.fish.chat.core.enums.CommonStatus;
import com.fish.chat.core.mapper.GroupMapper;
import com.fish.chat.core.mapper.GroupMemberMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 群组数据访问层
 */
@Repository
public class GroupRepository extends BaseRepository<GroupPO> {

    @Resource
    private GroupMapper groupMapper;

    @Resource
    private GroupMemberMapper groupMemberMapper;

    @Override
    protected BaseMapper<GroupPO> getBaseMapper() {
        return groupMapper;
    }

    /**
     * 分页查询用户的群组成员关系
     */
    public Page<GroupMemberPO> selectMemberPage(String userCode, Page<GroupMemberPO> page) {
        return groupMemberMapper.selectPage(page,
                Wrappers.<GroupMemberPO>lambdaQuery()
                        .eq(GroupMemberPO::getUserCode, userCode));
    }

    /**
     * 根据 code 列表批量查询群组
     */
    public List<GroupPO> selectByCodes(List<String> codes) {
        return groupMapper.selectList(
                Wrappers.<GroupPO>lambdaQuery()
                        .in(GroupPO::getCode, codes)
                        .eq(GroupPO::getStatus, CommonStatus.NORMAL.getValue()));
    }

    /**
     * 统计群组成员数
     */
    public long countMembers(String groupCode) {
        return groupMemberMapper.selectCount(
                Wrappers.<GroupMemberPO>lambdaQuery()
                        .eq(GroupMemberPO::getGroupCode, groupCode));
    }

    /**
     * 根据 code 查询群组
     */
    public GroupPO selectByCode(String code) {
        return groupMapper.selectOne(
                Wrappers.<GroupPO>lambdaQuery().eq(GroupPO::getCode, code));
    }

    /**
     * 查询用户是否在群组中
     */
    public boolean isMember(String groupCode, String userCode) {
        return groupMemberMapper.selectCount(
                Wrappers.<GroupMemberPO>lambdaQuery()
                        .eq(GroupMemberPO::getGroupCode, groupCode)
                        .eq(GroupMemberPO::getUserCode, userCode)) > 0;
    }

    /**
     * 插入群组成员
     */
    public void insertMember(GroupMemberPO member) {
        groupMemberMapper.insert(member);
    }

    /**
     * 删除群组成员
     */
    public void deleteMember(String groupCode, String userCode) {
        groupMemberMapper.delete(Wrappers.<GroupMemberPO>lambdaQuery()
                .eq(GroupMemberPO::getGroupCode, groupCode)
                .eq(GroupMemberPO::getUserCode, userCode));
    }
    
    /**
     * 分页查询群组的成员列表
     */
    public Page<GroupMemberPO> selectGroupMemberPage(String groupCode, Page<GroupMemberPO> page) {
        return groupMemberMapper.selectPage(page,
                Wrappers.<GroupMemberPO>lambdaQuery()
                        .eq(GroupMemberPO::getGroupCode, groupCode));
    }
    
    /**
     * 查询用户在群组中的成员信息
     */
    public GroupMemberPO selectMember(String groupCode, String userCode) {
        return groupMemberMapper.selectOne(
                Wrappers.<GroupMemberPO>lambdaQuery()
                        .eq(GroupMemberPO::getGroupCode, groupCode)
                        .eq(GroupMemberPO::getUserCode, userCode));
    }
}
