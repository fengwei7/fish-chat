package com.fish.chat.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_friend")
public class FriendPO {
    private Long id;
    @com.baomidou.mybatisplus.annotation.TableField("user_id")
    private String userCode;
    @com.baomidou.mybatisplus.annotation.TableField("friend_id")
    private String friendCode;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}