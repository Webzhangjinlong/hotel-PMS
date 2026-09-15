package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 房间预订日历响应VO
 * 用于展示N天内房间的预订和排房情况
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCalendarVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 房间列表 */
    private List<RoomCalendarItem> rooms;
    
    /** 日期列表（用于表头显示） */
    private List<String> dates;
    
    /**
     * 房间日历项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomCalendarItem implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /** 房间ID */
        private Long roomId;
        
        /** 房间号 */
        private String roomNo;
        
        /** 房型名称 */
        private String roomTypeName;
        
        /** 房型ID */
        private Long roomTypeId;
        
        /** 楼层名称 */
        private String floorName;
        
        /** 楼层ID */
        private Long floorId;
        
        /** 基础价格 */
        private BigDecimal basePrice;
        
        /** 房间当前状态 */
        private String currentStatus;
        
        /** 每日状态映射：日期 -> 日历单元格信息 */
        private Map<String, CalendarCell> calendar;
        
        /** 该房间在查询范围内的预订列表 */
        private List<CalendarReservation> reservations;
    }
    
    /**
     * 日历单元格信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarCell implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /** 日期（yyyy-MM-dd） */
        private String date;
        
        /** 当日状态：AVAILABLE/RESERVED/OCCUPIED/DIRTY/MAINTENANCE/RESERVED */
        private String status;
        
        /** 关联的预订ID（如果有） */
        private Long reservationId;
        
        /** 客人姓名（如果有预订） */
        private String guestName;
        
        /** 预订状态 */
        private String reservationStatus;
        
        /** 当日价格 */
        private BigDecimal price;
    }
    
    /**
     * 日历预订信息（简化版）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarReservation implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /** 预订ID */
        private Long reservationId;
        
        /** 预订号 */
        private String reservationNo;
        
        /** 客人姓名 */
        private String guestName;
        
        /** 入住日期 */
        private String checkInDate;
        
        /** 离店日期 */
        private String checkOutDate;
        
        /** 预订状态 */
        private String status;
        
        /** 总金额 */
        private BigDecimal totalAmount;
    }
}
