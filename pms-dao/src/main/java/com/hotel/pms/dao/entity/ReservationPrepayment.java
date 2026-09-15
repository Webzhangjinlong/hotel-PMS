package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预订预付款实体类
 * <p>
 * 对应数据库表：reservation_prepayment
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservation_prepayment")
public class ReservationPrepayment extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 散客预订ID */
    @TableField("reservation_id")
    private Long reservationId;
    
    /** 团队预订ID */
    @TableField("team_reservation_id")
    private Long teamReservationId;
    
    /** 预付类型：FULL-全额/PARTIAL-部分/DEPOSIT-押金 */
    @TableField("prepayment_type")
    private String prepaymentType;
    
    /** 预付金额 */
    @TableField("amount")
    private BigDecimal amount;
    
    /** 支付方式 */
    @TableField("payment_method")
    private String paymentMethod;
    
    /** 支付时间 */
    @TableField("payment_time")
    private LocalDateTime paymentTime;
    
    /** 关联的交易ID */
    @TableField("transaction_id")
    private Long transactionId;
    
    /** 状态：PAID-已付/TRANSFERRED-已转入/REFUNDED-已退款 */
    @TableField("status")
    private String status;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
}
