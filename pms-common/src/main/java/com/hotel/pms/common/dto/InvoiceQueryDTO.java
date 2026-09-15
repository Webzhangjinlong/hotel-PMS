package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 发票查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvoiceQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 发票号码 */
    private String invoiceNo;
    
    /** 发票类型：NORMAL-普通发票/SPECIAL-增值税专用发票/ELECTRONIC-电子发票 */
    private String invoiceType;
    
    /** 发票状态：NORMAL-正常/VOID-作废/RED-红冲 */
    private String invoiceStatus;
    
    /** 购买方名称 */
    private String buyerName;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 开票日期开始 */
    private LocalDate invoiceDateStart;
    
    /** 开票日期结束 */
    private LocalDate invoiceDateEnd;
    
    /** 关联入住单号 */
    private String stayNo;
}
