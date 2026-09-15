package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房间响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 房间ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 酒店名称 */
    private String hotelName;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 房型名称 */
    private String roomTypeName;
    
    /** 房型编码 */
    private String roomTypeCode;
    
    /** 基础价格 */
    private BigDecimal basePrice;
    
    /** 楼层ID */
    private Long floorId;
    
    /** 楼层名称 */
    private String floorName;
    
    /** 房间号 */
    private String roomNo;
    
    /** 状态 */
    private String status;
    
    /** 房间描述 */
    private String description;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
