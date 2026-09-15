package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TeamFolioPaymentDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotNull(message = "收款金额不能为空")
    private BigDecimal amount;
    
    @NotNull(message = "支付方式不能为空")
    private String paymentMethod;
    
    private String remark;
}