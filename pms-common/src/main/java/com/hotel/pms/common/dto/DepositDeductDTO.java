package com.hotel.pms.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 押金抵扣房费请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class DepositDeductDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 抵扣金额 */
    @NotNull(message = "抵扣金额不能为空")
    @DecimalMin(value = "0.01", message = "抵扣金额必须大于0")
    private BigDecimal amount;
    
    /** 关联的入住单ID */
    @NotNull(message = "入住单ID不能为空")
    private String stayNo;
    
    /** 备注 */
    private String remark;
    
    /** 操作员ID */
    private Long operatorId = 1L;
}
