package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 发票明细表实体类
 * <p>
 * 对应数据库表：invoice_item
 * 存储发票的商品/服务明细信息
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("invoice_item")
public class InvoiceItem extends BaseEntity {
    
    /** 发票ID */
    @TableField("invoice_id")
    private Long invoiceId;
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 商品/服务名称 */
    @TableField("item_name")
    private String itemName;
    
    /** 规格型号 */
    @TableField("item_spec")
    private String itemSpec;
    
    /** 单位 */
    @TableField("item_unit")
    private String itemUnit;
    
    /** 数量 */
    @TableField("item_quantity")
    private BigDecimal itemQuantity;
    
    /** 单价 */
    @TableField("item_price")
    private BigDecimal itemPrice;
    
    /** 金额（不含税） */
    @TableField("amount")
    private BigDecimal amount;
    
    /** 税率 */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    
    /** 税额 */
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    
    /** 价税合计 */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
}
