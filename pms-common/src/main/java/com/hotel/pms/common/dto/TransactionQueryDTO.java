package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 交易查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 交易类型：DEPOSIT/ROOM_FEE/EXTRA/PAYMENT/REFUND/REVERSAL */
    private String type;
    
    /** 支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER/CREDIT */
    private String paymentMethod;
    
    /** 账务单ID */
    private Long folioId;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 挂账公司ID */
    private Long creditCompanyId;
    
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 结束日期 */
    private LocalDate endDate;
    
    /** 交易号 */
    private String transactionNo;    
    /** 客人姓名（模糊查询） */
    private String guestName;
    
    /** 房间号 */
    private String roomNo;
}

