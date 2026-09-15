package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 团队入住请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class TeamCheckInDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 需要入住的房间明细ID列表（为空则全部入住） */
    private List<Long> roomIds;
    
    /** 强制分配房间（如果预订时未分配房间） */
    private List<RoomAssignment> assignments;
    
    @Data
    public static class RoomAssignment implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 房间明细ID */
        @NotNull(message = "房间明细ID不能为空")
        private Long roomDetailId;
        
        /** 分配的房间ID */
        @NotNull(message = "房间ID不能为空")
        private Long roomId;
    }
}
