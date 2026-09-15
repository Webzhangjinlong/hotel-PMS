package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 房价更新请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomPriceUpdateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 房型ID */
    @NotNull(message = "房型ID不能为空")
    private Long roomTypeId;
    
    /** 价格日期 */
    @NotNull(message = "价格日期不能为空")
    private LocalDate priceDate;
    
    /** 价格 */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
}
