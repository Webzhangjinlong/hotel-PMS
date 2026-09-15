package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退房请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class StayCheckOutDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 入住单ID */
    @NotNull(message = "入住单ID不能为空")
    private Long stayId;
    
    /** 补收金额 */
    private BigDecimal additionalPayment;
    
    /** 补收支付方式 */
    private String paymentMethod;
    
    /** 退款方式 */
    private String refundMethod;
    
    /** 退房备注 */
    private String remark;
    
    /** 操作员ID */
    private Long operatorId;
}
