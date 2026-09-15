package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * OTA订单查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OtaOrderQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 渠道ID */
    private Long channelId;
    
    /** OTA订单号 */
    private String otaOrderNo;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 订单状态：PENDING-待确认/CONFIRMED-已确认/CHECKED_IN-已入住/CANCELLED-已取消/NO_SHOW-未到店 */
    private String orderStatus;
    
    /** 支付状态：UNPAID-未支付/PAID-已支付/REFUNDED-已退款 */
    private String paymentStatus;
    
    /** 入住日期开始 */
    private LocalDate checkInDateStart;
    
    /** 入住日期结束 */
    private LocalDate checkInDateEnd;
}
