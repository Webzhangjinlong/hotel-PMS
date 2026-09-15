package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金实体类
 * <p>
 * 记录客人入住时收取的押金信息，支持收取和退还操作
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("deposit")
public class Deposit extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 押金单号 */
    @TableField("deposit_no")
    private String depositNo;
    
    /** 入住单ID */
    @TableField("stay_id")
    private Long stayId;
    
    /** 预订ID */
    @TableField("reservation_id")
    private Long reservationId;
    
    /** 客人ID */
    @TableField("guest_id")
    private Long guestId;
    
    /** 客人姓名 */
    @TableField("guest_name")
    private String guestName;
    
    /** 房间号 */
    @TableField("room_no")
    private String roomNo;
    
    /** 押金金额 */
    @TableField("amount")
    private BigDecimal amount;
    
    /** 支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER */
    @TableField("payment_method")
    private String paymentMethod;
    
    /** 押金状态：COLLECTED-已收取/REFUNDED-已退还/PARTIAL_REFUND-部分退还 */
    @TableField("status")
    private String status;
    
    /** 收取时间 */
    @TableField("collected_at")
    private LocalDateTime collectedAt;
    
    /** 收取人ID */
    @TableField("collected_by")
    private Long collectedBy;
    
    /** 已退金额 */
    @TableField("refunded_amount")
    private BigDecimal refundedAmount;

    /** 已抵扣金额 */
    @TableField("deducted_amount")
    private BigDecimal deductedAmount;
    
    /** 退还时间 */
    @TableField("refunded_at")
    private LocalDateTime refundedAt;
    
    /** 退还人ID */
    @TableField("refunded_by")
    private Long refundedBy;
    
    /** 退还方式 */
    @TableField("refund_method")
    private String refundMethod;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
}

