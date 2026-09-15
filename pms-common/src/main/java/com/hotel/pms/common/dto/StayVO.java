package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入住响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StayVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 入住单ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 酒店名称 */
    private String hotelName;
    
    /** 入住单号 */
    private String stayNo;
    
    /** 预订ID */
    private Long reservationId;
    
    /** 预订号 */
    private String reservationNo;
    
    /** 房间ID */
    private Long roomId;
    
    /** 房间号 */
    private String roomNo;
    
    /** 房型ID */
    private Long roomTypeId;

    /** 房型名称 */
    private String roomTypeName;
    
    /** 客人ID */
    private Long guestId;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 客人电话 */
    private String guestPhone;
    
    /** 入住时间 */
    private LocalDateTime checkInTime;
    
    /** 预计离店时间 */
    private LocalDateTime checkOutTime;
    
    /** 实际离店时间 */
    private LocalDateTime actualCheckOutTime;
    
    /** 状态 */
    private String status;
    
    /** 总金额 */
    private BigDecimal totalAmount;
    
    /** 已付金额 */
    private BigDecimal paidAmount;

    /** 支付状态：PAID-已付清/PARTIAL-部分支付/UNPAID-未支付/NO_FEE-无费用 */
    private String paymentStatus;
    
    /** 待付金额 */
    private BigDecimal unpaidAmount;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 关联会员ID */
    private Long memberId;

    /** 入住类型：INDIVIDUAL-散客/RESERVATION-预订/TEAM-团队 */
    private String checkInType;
    
    /** 团队预订ID */
    private Long teamReservationId;
    
    /** 团队预订号 */
    private String teamReservationNo;
    
    /** 团队名称 */
    private String teamName;
    
    /** 结算方式：UNIFIED-统一结算/SEPARATE-分开结算 */
    private String settlementType;
    
    /** 团队联系人姓名 */
    private String contactName;
    
    /** 团队联系人电话 */
    private String contactPhone;
    
    /** 团队账务单ID（统一结算时使用） */
    private Long teamFolioId;
    
    
    /** 预订来源/渠道 */
    private String source;

    /** 同住人数量（不包括主客人） */
    private Integer coGuestCount;

    /** 同住人姓名列表（JSON数组格式） */
    private String coGuestNames;
    
    /** 押金金额 */
    private BigDecimal depositAmount;
}
