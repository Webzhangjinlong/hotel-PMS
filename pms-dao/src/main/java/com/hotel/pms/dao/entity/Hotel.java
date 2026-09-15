package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 酒店实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hotel")
public class Hotel extends BaseEntity {
    
    /** 酒店ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 酒店名称 */
    private String name;
    
    /** 酒店标识码 */
    @TableField("hotel_code")
    private String hotelCode;
    
    /** 酒店地址 */
    private String address;
    
    /** 联系电话 */
    private String phone;
    
    /** 时区 */
    private String timezone;
    
    /** 酒店状态: ACTIVE-启用, INACTIVE-停用 */
    private String status;
    
    /** 夜审时间 */
    private LocalTime auditTime;
    
    /** 是否启用自动夜审 */
    private Boolean autoAuditEnabled;
    
    /** 逻辑删除 */
    @TableLogic
    private Boolean deleted;
    
    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
