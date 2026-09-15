package com.hotel.pms.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 加床/杂费请求DTO
 * <p>
 * 用于住中服务的加床费和杂费入账
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class ExtraChargeDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 费用类型：BED-加床费/EXTRA-杂费 */
    @NotBlank(message = "费用类型不能为空")
    private String chargeType;
    
    /** 费用描述 */
    @NotBlank(message = "费用描述不能为空")
    private String description;
    
    /** 费用金额 */
    @NotNull(message = "费用金额不能为空")
    @DecimalMin(value = "0.01", message = "费用金额必须大于0")
    private BigDecimal amount;
    
    /** 支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER/CREDIT */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    /** 挂账公司ID（挂账时使用） */
    private Long creditCompanyId;
    
    /** 备注 */
    private String remark;
    
    /** 操作员ID */
    private Long operatorId;
}
