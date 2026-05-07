package com.fish.chat.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_group_member")
public class GroupMemberPO {
    private Long id;
    private Long groupId;
    private Long userId;
    private Integer role;
    private LocalDateTime joinTime;
    private Integer deleted;
}