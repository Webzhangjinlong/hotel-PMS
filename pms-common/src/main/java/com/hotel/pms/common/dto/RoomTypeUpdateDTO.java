package com.hotel.pms.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房型更新请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomTypeUpdateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 房型名称 */
    @NotBlank(message = "房型名称不能为空")
    private String name;
    
    /** 床型 */
    private String bedType;
    
    /** 最大入住人数 */
    @Min(value = 1, message = "最大入住人数必须大于0")
    private Integer maxGuests;
    
    /** 基础价格 */
    @Min(value = 0, message = "基础价格不能为负数")
    private BigDecimal basePrice;
    
    /** 房型描述 */
    private String description;
}
