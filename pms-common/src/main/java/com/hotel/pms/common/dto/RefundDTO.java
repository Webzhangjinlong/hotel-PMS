package com.hotel.pms.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RefundDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 退款金额 */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal amount;
    
    /** 退款方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER */
    @NotBlank(message = "退款方式不能为空")
    private String refundMethod;
    
    /** 原交易ID（用于原路退回） */
    private Long originalTransactionId;
    
    /** 退款原因 */
    @NotBlank(message = "退款原因不能为空")
    private String reason;
    
    /** 操作员ID */
    private Long operatorId;
}
