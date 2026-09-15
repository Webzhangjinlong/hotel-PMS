package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 发票明细请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class InvoiceItemDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 商品/服务名称 */
    @NotBlank(message = "商品/服务名称不能为空")
    private String itemName;
    
    /** 规格型号 */
    private String itemSpec;
    
    /** 单位 */
    private String itemUnit;
    
    /** 数量 */
    @NotNull(message = "数量不能为空")
    private BigDecimal itemQuantity;
    
    /** 单价 */
    @NotNull(message = "单价不能为空")
    private BigDecimal itemPrice;
    
    /** 税率 */
    @NotNull(message = "税率不能为空")
    private BigDecimal taxRate;
    
    /** 备注 */
    private String remark;
}
