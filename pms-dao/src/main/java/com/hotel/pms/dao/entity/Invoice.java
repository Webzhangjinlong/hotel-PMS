package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 发票主表实体类
 * <p>
 * 对应数据库表：invoice
 * 存储发票的基本信息、购买方信息、销售方信息等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("invoice")
public class Invoice extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 发票号码 */
    @TableField("invoice_no")
    private String invoiceNo;
    
    /** 发票代码 */
    @TableField("invoice_code")
    private String invoiceCode;
    
    /** 发票类型：NORMAL-普通发票/SPECIAL-增值税专用发票/ELECTRONIC-电子发票 */
    @TableField("invoice_type")
    private String invoiceType;
    
    /** 发票状态：NORMAL-正常/VOID-作废/RED-红冲 */
    @TableField("invoice_status")
    private String invoiceStatus;
    
    /** 开票日期 */
    @TableField("invoice_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;
    
    /** 购买方名称 */
    @TableField("buyer_name")
    private String buyerName;
    
    /** 购买方纳税人识别号 */
    @TableField("buyer_tax_no")
    private String buyerTaxNo;
    
    /** 购买方地址 */
    @TableField("buyer_address")
    private String buyerAddress;
    
    /** 购买方电话 */
    @TableField("buyer_phone")
    private String buyerPhone;
    
    /** 购买方开户行 */
    @TableField("buyer_bank")
    private String buyerBank;
    
    /** 购买方银行账号 */
    @TableField("buyer_bank_account")
    private String buyerBankAccount;
    
    /** 销售方名称 */
    @TableField("seller_name")
    private String sellerName;
    
    /** 销售方纳税人识别号 */
    @TableField("seller_tax_no")
    private String sellerTaxNo;
    
    /** 销售方地址 */
    @TableField("seller_address")
    private String sellerAddress;
    
    /** 销售方电话 */
    @TableField("seller_phone")
    private String sellerPhone;
    
    /** 销售方开户行 */
    @TableField("seller_bank")
    private String sellerBank;
    
    /** 销售方银行账号 */
    @TableField("seller_bank_account")
    private String sellerBankAccount;
    
    /** 金额（不含税） */
    @TableField("amount")
    private BigDecimal amount;
    
    /** 税额 */
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    
    /** 价税合计 */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /** 关联入住单ID */
    @TableField("stay_id")
    private Long stayId;
    
    /** 关联入住单号 */
    @TableField("stay_no")
    private String stayNo;
    
    /** 客人姓名 */
    @TableField("guest_name")
    private String guestName;
    
    /** 客人电话 */
    @TableField("guest_phone")
    private String guestPhone;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
    
    /** 作废原因 */
    @TableField("void_reason")
    private String voidReason;
    
    /** 操作人ID */
    @TableField("operator_id")
    private Long operatorId;
    
    /** 操作人姓名 */
    @TableField("operator_name")
    private String operatorName;
}
