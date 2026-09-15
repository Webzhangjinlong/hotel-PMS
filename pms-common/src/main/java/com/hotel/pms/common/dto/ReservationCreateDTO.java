package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预订创建请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class ReservationCreateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 来源 */
    private String source;
    
    /** 客人姓名 */
    @NotBlank(message = "客人姓名不能为空")
    private String guestName;
    
    /** 客人电话 */
    @NotBlank(message = "客人电话不能为空")
    private String guestPhone;
    
    /** 身份证号 */
    private String idCardNo;
    
    /** 房型ID */
    @NotNull(message = "房型不能为空")
    private Long roomTypeId;
    
    /** 房间ID（预分房时填写） */
    private Long roomId;
    
    /** 入住日期 */
    @NotNull(message = "入住日期不能为空")
    private LocalDate checkInDate;
    
    /** 离店日期 */
    @NotNull(message = "离店日期不能为空")
    private LocalDate checkOutDate;
    
    /** 总金额（可选，不填则自动计算） */
    private BigDecimal totalAmount;
    
    /** 房价码ID */
    private Long pricePlanId;
    
    /** 特殊要求 */
    private String specialRequests;

    /** 每日房价（可选，不填则使用房价码价格） */
    private BigDecimal dailyPrice;
    
    /** 价格来源：PRICE_PLAN/MANUAL/CUSTOM */
    private String priceSource;
}
