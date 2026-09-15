package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_shift_message")
public class SysShiftMessage extends BaseEntity {
    
    @TableField("shift_id")
    private Long shiftId;
    
    @TableField("receiver_id")
    private Long receiverId;
    
    @TableField("message_type")
    private String messageType;
    
    @TableField("is_read")
    private Boolean isRead;
    
    @TableField("read_time")
    private LocalDateTime readTime;
}
