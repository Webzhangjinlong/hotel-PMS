package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 账务单实体类
 * <p>
 * 对应数据库表：folio
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("folio")
public class Folio extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 账务单号 */
    @TableField("folio_no")
    private String folioNo;
    
    /** 入住单ID */
    @TableField("stay_id")
    private Long stayId;
    
    /** 客人ID */
    @TableField("guest_id")
    private Long guestId;
    
    /** 总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /** 已付金额 */
    @TableField("paid_amount")
    private BigDecimal paidAmount;
    
    /** 余额 */
    @TableField("balance")
    private BigDecimal balance;
    
    /** 状态：OPEN-开放/CLOSED-关闭 */
    @TableField("status")
    private String status;
    
    /** 是否锁定 */
    @TableField("is_locked")
    private Boolean isLocked;
    
    /** 锁定的夜审ID */
    @TableField("locked_by_audit_id")
    private Long lockedByAuditId;
}
