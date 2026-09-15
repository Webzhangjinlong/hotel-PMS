package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team_folio")
public class TeamFolio extends BaseEntity {
    
    @TableField("hotel_id")
    private Long hotelId;
    
    @TableField("team_reservation_id")
    private Long teamReservationId;
    
    @TableField("folio_no")
    private String folioNo;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("paid_amount")
    private BigDecimal paidAmount;
    
    @TableField("balance")
    private BigDecimal balance;
    
    @TableField("status")
    private String status;
    
    @TableField("payment_method")
    private String paymentMethod;
    
    @TableField("remark")
    private String remark;
    
    /** 是否锁定 */
    @TableField("is_locked")
    private Boolean isLocked;
    
    /** 锁定的夜审ID */
    @TableField("locked_by_audit_id")
    private Long lockedByAuditId;
}