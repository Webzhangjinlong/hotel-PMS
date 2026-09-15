package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 房间统计响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSummaryVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 总房间数 */
    private Long totalRooms;
    
    /** 空闲房间数 */
    private Long availableRooms;
    
    /** 在住房间数 */
    private Long occupiedRooms;
    
    /** 脏房数 */
    private Long dirtyRooms;
    
    /** 维修房间数 */
    private Long maintenanceRooms;
    
    /** 停用房间数 */
    private Long oooRooms;
    
    /** 预留房间数 */
    private Long reservedRooms;
    
    /** 各状态统计 */
    private Map<String, Long> statusCounts;
}
