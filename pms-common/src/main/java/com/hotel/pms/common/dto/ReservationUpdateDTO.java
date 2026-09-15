package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预订更新请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class ReservationUpdateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 客人电话 */
    private String guestPhone;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 房间ID */
    private Long roomId;
    
    /** 入住日期 */
    private LocalDate checkInDate;
    
    /** 离店日期 */
    private LocalDate checkOutDate;
    
    /** 总金额 */
    private BigDecimal totalAmount;
    
    /** 特殊要求 */
    private String specialRequests;

    /** 每日房价（可选，不填则保持原价） */
    private BigDecimal dailyPrice;
    
    /** 价格来源：PRICE_PLAN/MANUAL/CUSTOM */
    private String priceSource;
}
