package com.fish.chat.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_channel_member")
public class ChannelMemberPO {
    private Long id;
    private Long channelId;
    private String userCode;
    private Integer role;
    private LocalDateTime joinTime;
    private Integer deleted;
}