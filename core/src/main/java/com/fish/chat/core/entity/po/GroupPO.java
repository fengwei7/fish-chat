package com.fish.chat.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fish.chat.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_group")
public class GroupPO extends BasePO {
    private String name;
    private String avatar;
    private String ownerCode;
    private String notice;
    private Integer maxMembers;
}
