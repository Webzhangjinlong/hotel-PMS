package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发票响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 发票ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 发票号码 */
    private String invoiceNo;
    
    /** 发票代码 */
    private String invoiceCode;
    
    /** 发票类型：NORMAL-普通发票/SPECIAL-增值税专用发票/ELECTRONIC-电子发票 */
    private String invoiceType;
    
    /** 发票类型名称 */
    private String invoiceTypeName;
    
    /** 发票状态：NORMAL-正常/VOID-作废/RED-红冲 */
    private String invoiceStatus;
    
    /** 发票状态名称 */
    private String invoiceStatusName;
    
    /** 开票日期 */
    private LocalDate invoiceDate;
    
    /** 购买方名称 */
    private String buyerName;
    
    /** 购买方纳税人识别号 */
    private String buyerTaxNo;
    
    /** 购买方地址 */
    private String buyerAddress;
    
    /** 购买方电话 */
    private String buyerPhone;
    
    /** 购买方开户行 */
    private String buyerBank;
    
    /** 购买方银行账号 */
    private String buyerBankAccount;
    
    /** 销售方名称 */
    private String sellerName;
    
    /** 销售方纳税人识别号 */
    private String sellerTaxNo;
    
    /** 销售方地址 */
    private String sellerAddress;
    
    /** 销售方电话 */
    private String sellerPhone;
    
    /** 销售方开户行 */
    private String sellerBank;
    
    /** 销售方银行账号 */
    private String sellerBankAccount;
    
    /** 金额（不含税） */
    private BigDecimal amount;
    
    /** 税额 */
    private BigDecimal taxAmount;
    
    /** 价税合计 */
    private BigDecimal totalAmount;
    
    /** 关联入住单ID */
    private Long stayId;
    
    /** 关联入住单号 */
    private String stayNo;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 客人电话 */
    private String guestPhone;
    
    /** 备注 */
    private String remark;
    
    /** 作废原因 */
    private String voidReason;
    
    /** 操作人ID */
    private Long operatorId;
    
    /** 操作人姓名 */
    private String operatorName;
    
    /** 发票明细列表 */
    private List<InvoiceItemVO> items;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
