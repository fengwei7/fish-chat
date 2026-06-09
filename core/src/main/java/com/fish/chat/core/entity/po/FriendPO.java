package com.fish.chat.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fish.chat.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_friend")
public class FriendPO extends BasePO {
    private String userCode;
    private String friendCode;
    private String remark;
}