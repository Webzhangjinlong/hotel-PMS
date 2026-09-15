package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预付响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class PrepaymentVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 预付ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 散客预订ID */
    private Long reservationId;
    
    /** 团队预订ID */
    private Long teamReservationId;
    
    /** 预付类型：FULL-全额/PARTIAL-部分/DEPOSIT-押金 */
    private String prepaymentType;
    
    /** 预付类型名称 */
    private String prepaymentTypeName;
    
    /** 预付金额 */
    private BigDecimal amount;
    
    /** 支付方式 */
    private String paymentMethod;
    
    /** 支付方式名称 */
    private String paymentMethodName;
    
    /** 支付时间 */
    private LocalDateTime paymentTime;
    
    /** 关联的交易ID */
    private Long transactionId;
    
    /** 状态：PAID-已付/TRANSFERRED-已转入/REFUNDED-已退款 */
    private String status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 备注 */
    private String remark;
}
