package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 房态看板响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDashboardVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 总房间数 */
    private Long totalRooms;
    
    /** 各状态房间数量统计 */
    private Map<String, Long> statusCounts;
    
    /** 按状态分组的房间列表 */
    private List<StatusGroupVO> statusGroups;
    
    /**
     * 状态分组VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusGroupVO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /** 状态编码 */
        private String statusCode;
        
        /** 状态名称 */
        private String statusName;
        
        /** 房间数量 */
        private Long roomCount;
        
        /** 该状态下的房间列表 */
        private List<RoomDetailVO> rooms;
    }
}