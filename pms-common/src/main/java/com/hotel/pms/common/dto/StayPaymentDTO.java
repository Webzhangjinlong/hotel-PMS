package com.hotel.pms.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 散客收款请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class StayPaymentDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 收款金额 */
    @NotNull(message = "收款金额不能为空")
    @DecimalMin(value = "0.01", message = "收款金额必须大于0")
    private BigDecimal amount;
    
    /** 支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER/CREDIT */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    /** 交易类型：DEPOSIT-押金/ROOM_FEE-房费/EXTRA-杂费 */
    @NotBlank(message = "交易类型不能为空")
    private String transactionType;
    
    /** 挂账公司ID（挂账时使用） */
    private Long creditCompanyId;
    
    /** 挂账客人ID（挂账时使用） */
    private Long creditGuestId;
    
    /** 备注 */
    private String remark;
    
    /** 操作员ID */
    private Long operatorId;
}
