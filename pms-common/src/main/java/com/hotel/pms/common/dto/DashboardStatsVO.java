package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台统计VO
 * <p>
 * 包含工作台页面所需的统计数据
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class DashboardStatsVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 总房间数 */
    private Integer totalRooms;
    
    /** 可用房间数 */
    private Integer availableRooms;
    
    /** 在住房间数 */
    private Integer occupiedRooms;
    
    /** 脏房数量 */
    private Integer dirtyRooms;
    
    /** 维修房间数 */
    private Integer maintenanceRooms;
    
    /** 预留房间数 */
    private Integer reservedRooms;
    
    /** 今日抵店数 */
    private Integer todayArrivals;
    
    /** 今日离店数 */
    private Integer todayDepartures;
    
    /** 待确认预订数 */
    private Integer pendingReservations;
    
    /** 今日入住数 */
    private Integer todayCheckIns;
}
