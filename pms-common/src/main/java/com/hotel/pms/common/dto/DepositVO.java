package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class DepositVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 押金单号 */
    private String depositNo;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 预订ID */
    private Long reservationId;
    
    /** 客人ID */
    private Long guestId;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 房间号 */
    private String roomNo;
    
    /** 押金金额 */
    private BigDecimal amount;
    
    /** 支付方式 */
    private String paymentMethod;
    
    /** 支付方式名称 */
    private String paymentMethodName;
    
    /** 押金状态 */
    private String status;
    
    /** 押金状态名称 */
    private String statusName;
    
    /** 收取时间 */
    private LocalDateTime collectedAt;
    
    /** 收取人姓名 */
    private String collectedByName;
    
    /** 已退金额 */
    private BigDecimal refundedAmount;

    /** 已抵扣金额 */
    private BigDecimal deductedAmount;
    
    /** 退还时间 */
    private LocalDateTime refundedAt;
    
    /** 退还人姓名 */
    private String refundedByName;
    
    /** 退还方式 */
    private String refundMethod;
    
    /** 备注 */
    private String remark;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}

