package com.fish.chat.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fish.chat.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_channel_member")
public class ChannelMemberPO extends BasePO {
    private String channelCode;
    private String userCode;
    private Integer role;
}