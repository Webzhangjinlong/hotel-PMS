package com.hotel.pms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OTA订单响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtaOrderVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 订单ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 渠道ID */
    private Long channelId;
    
    /** 渠道名称 */
    private String channelName;
    
    /** OTA订单号 */
    private String otaOrderNo;
    
    /** OTA预订号 */
    private String otaReservationNo;
    
    /** 关联预订ID */
    private Long reservationId;
    
    /** 关联入住单ID */
    private Long stayId;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 客人电话 */
    private String guestPhone;
    
    /** 客人邮箱 */
    private String guestEmail;
    
    /** 证件类型 */
    private String guestIdType;
    
    /** 证件号码 */
    private String guestIdNo;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 房型名称 */
    private String roomTypeName;
    
    /** 房间数量 */
    private Integer roomCount;
    
    /** 入住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    
    /** 离店日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    
    /** 入住天数 */
    private Integer nights;
    
    /** 房价 */
    private BigDecimal roomPrice;
    
    /** 总金额 */
    private BigDecimal totalAmount;
    
    /** 佣金比例 */
    private BigDecimal commissionRate;
    
    /** 佣金金额 */
    private BigDecimal commissionAmount;
    
    /** 净收入 */
    private BigDecimal netAmount;
    
    /** 订单状态：PENDING-待确认/CONFIRMED-已确认/CHECKED_IN-已入住/CANCELLED-已取消/NO_SHOW-未到店 */
    private String orderStatus;
    
    /** 订单状态名称 */
    private String orderStatusName;
    
    /** 支付状态：UNPAID-未支付/PAID-已支付/REFUNDED-已退款 */
    private String paymentStatus;
    
    /** 支付状态名称 */
    private String paymentStatusName;
    
    /** 特殊要求 */
    private String specialRequests;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
