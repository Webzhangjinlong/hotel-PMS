package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_shift_notify_config")
public class SysShiftNotifyConfig extends BaseEntity {
    
    @TableField("hotel_id")
    private Long hotelId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("notify_type")
    private String notifyType;
    
    @TableField("is_active")
    private Boolean isActive;
}
