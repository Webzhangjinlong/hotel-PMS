package com.hotel.pms.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 押金退还请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class DepositRefundDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 退还金额 */
    @NotNull(message = "退还金额不能为空")
    @DecimalMin(value = "0.01", message = "退还金额必须大于0")
    private BigDecimal amount;
    
    /** 退还方式：CASH/WECHAT/ALIPAY/POS */
    @NotBlank(message = "退还方式不能为空")
    private String refundMethod;
    
    /** 备注 */
    private String remark;
    
    /** 操作员ID */
    private Long operatorId;
}
