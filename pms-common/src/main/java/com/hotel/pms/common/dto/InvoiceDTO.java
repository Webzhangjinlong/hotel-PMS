package com.hotel.pms.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 发票请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class InvoiceDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 发票号码 */
    @NotBlank(message = "发票号码不能为空")
    private String invoiceNo;
    
    /** 发票代码 */
    private String invoiceCode;
    
    /** 发票类型：NORMAL-普通发票/SPECIAL-增值税专用发票/ELECTRONIC-电子发票 */
    @NotBlank(message = "发票类型不能为空")
    private String invoiceType;
    
    /** 开票日期 */
    @NotNull(message = "开票日期不能为空")
    private LocalDate invoiceDate;
    
    /** 购买方名称 */
    @NotBlank(message = "购买方名称不能为空")
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
    
    /** 发票明细列表 */
    @Valid
    @NotNull(message = "发票明细不能为空")
    private List<InvoiceItemDTO> items;
}
