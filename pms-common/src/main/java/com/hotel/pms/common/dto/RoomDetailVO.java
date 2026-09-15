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
 * 房间详情VO（房态看板用）
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 房间ID */
    private Long roomId;
    
    /** 房间号 */
    private String roomNo;
    
    /** 楼层ID */
    private Long floorId;
    
    /** 楼层名称 */
    private String floorName;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 房型名称 */
    private String roomTypeName;
    
    /** 房型编码 */
    private String roomTypeCode;
    
    /** 房间状态 */
    private String status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 基础价格 */
    private BigDecimal basePrice;
    
    /** 今日房价 */
    private BigDecimal todayPrice;
    /** 当前入住信息 */
    private CurrentStayInfo currentStay;
    
    /** 未来预订列表 */
    private List<FutureReservation> futureReservations;
    
    /**
     * 当前入住信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentStayInfo implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /** 预订ID */
        private Long reservationId;
        
        /** 客人姓名 */
        private String guestName;
        
        /** 入住时间 */
        private LocalDateTime checkInTime;
        
        /** 预计离店时间 */
        private LocalDateTime checkOutTime;
        
        /** 入住天数 */
        private Integer stayDays;

        /** 付款状态 */
        private String paymentStatus;

        /** 已付金额 */
        private java.math.BigDecimal paidAmount;

        /** 总金额 */
        private java.math.BigDecimal totalAmount;
    }
    
    /**
     * 未来预订信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FutureReservation implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /** 预订ID */
        private Long reservationId;
        
        /** 客人姓名 */
        private String guestName;
        
        /** 入住日期 */
        private LocalDate checkInDate;
        
        /** 离店日期 */
        private LocalDate checkOutDate;
        
        /** 预订状态 */
        private String status;
    }
}

