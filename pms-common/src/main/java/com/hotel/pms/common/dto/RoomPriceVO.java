package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 房价响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPriceVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 房价ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 房型名称 */
    private String roomTypeName;
    
    /** 价格日期 */
    private LocalDate priceDate;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 状态 */
    private String status;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
