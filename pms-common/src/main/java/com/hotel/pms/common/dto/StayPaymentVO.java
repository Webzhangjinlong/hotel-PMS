package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 散客收款响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class StayPaymentVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 交易ID */
    private Long transactionId;
    
    /** 交易号 */
    private String transactionNo;
    
    /** 账务单ID */
    private Long folioId;
    
    /** 交易类型 */
    private String transactionType;
    
    /** 交易金额 */
    private BigDecimal amount;
    
    /** 支付方式 */
    private String paymentMethod;
    
    /** 支付方式名称 */
    private String paymentMethodName;
    
    /** 交易时间 */
    private LocalDateTime transactionTime;
    
    /** 账务单总金额 */
    private BigDecimal folioTotalAmount;
    
    /** 账务单已付金额 */
    private BigDecimal folioPaidAmount;
    
    /** 账务单余额 */
    private BigDecimal folioBalance;
}
