package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预订响应VO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationVO implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 预订ID
     */
    private Long id;

    /**
     * 酒店ID
     */
    private Long hotelId;

    /**
     * 酒店名称
     */
    private String hotelName;

    /**
     * 预订号
     */
    private String reservationNo;

    /**
     * 来源
     */
    private String source;

    /**
     * 客人姓名
     */
    private String guestName;

    /**
     * 客人电话
     */
    private String guestPhone;

    /**
     * 房型ID
     */
    private Long roomTypeId;

    /**
     * 房型名称
     */
    private String roomTypeName;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 房间号
     */
    private String roomNo;

    /**
     * 入住日期
     */
    private LocalDate checkInDate;

    /**
     * 离店日期
     */
    private LocalDate checkOutDate;

    /**
     * 房晚数
     */
    private Integer nights;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态
     */
    private String status;

    /**
     * 房价码ID
     */
    private Long pricePlanId;

    /**
     * 特殊要求
     */
    private String specialRequests;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 每日房价（预订实际价格）
     */
    private BigDecimal dailyPrice;

    /**
     * 价格来源：PRICE_PLAN/MANUAL/CUSTOM
     */
    private String priceSource;

    /**
     * 房价码名称
     */
    private String pricePlanName;

    /**
     * 房价码每日价格（只读参考价）
     */
    private BigDecimal planDailyPrice;
}
