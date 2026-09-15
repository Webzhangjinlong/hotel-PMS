package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房型响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 房型ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 酒店名称 */
    private String hotelName;
    
    /** 房型名称 */
    private String name;
    
    /** 房型编码 */
    private String code;
    
    /** 床型 */
    private String bedType;
    
    /** 最大入住人数 */
    private Integer maxGuests;
    
    /** 基础价格 */
    private BigDecimal basePrice;
    
    /** 房型描述 */
    private String description;
    
    /** 状态 */
    private String status;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
