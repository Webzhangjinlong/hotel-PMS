package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票明细响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 发票明细ID */
    private Long id;
    
    /** 发票ID */
    private Long invoiceId;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 商品/服务名称 */
    private String itemName;
    
    /** 规格型号 */
    private String itemSpec;
    
    /** 单位 */
    private String itemUnit;
    
    /** 数量 */
    private BigDecimal itemQuantity;
    
    /** 单价 */
    private BigDecimal itemPrice;
    
    /** 金额（不含税） */
    private BigDecimal amount;
    
    /** 税率 */
    private BigDecimal taxRate;
    
    /** 税额 */
    private BigDecimal taxAmount;
    
    /** 价税合计 */
    private BigDecimal totalAmount;
    
    /** 备注 */
    private String remark;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
