package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class TransactionVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 交易ID */
    private Long id;
    
    /** 交易号 */
    private String transactionNo;
    
    /** 账务单ID */
    private Long folioId;
    
    /** 入住单号 */
    private String stayNo;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 房间号 */
    private String roomNo;
    
    /** 交易类型 */
    private String type;
    
    /** 交易类型名称 */
    private String typeName;
    
    /** 交易金额 */
    private BigDecimal amount;
    
    /** 支付方式 */
    private String paymentMethod;
    
    /** 支付方式名称 */
    private String paymentMethodName;
    
    /** 交易描述 */
    private String description;
    
    /** 挂账公司ID */
    private Long creditCompanyId;
    
    /** 挂账公司名称 */
    private String creditCompanyName;
    
    /** 退款原交易ID */
    private Long refundTransactionId;
    
    /** 冲账原交易ID */
    private Long reversalTransactionId;
    
    /** 是否已被冲账 */
    private Boolean isReversed;
    
    /** 操作员ID */
    private Long operatorId;
    
    /** 操作员姓名 */
    private String operatorName;
    
    /** 交易时间 */
    private LocalDateTime createdAt;
}


