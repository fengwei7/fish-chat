package com.fish.chat.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fish.chat.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_group_member")
public class GroupMemberPO extends BasePO {
    private String groupCode;
    private String userCode;
    private Integer role;
}