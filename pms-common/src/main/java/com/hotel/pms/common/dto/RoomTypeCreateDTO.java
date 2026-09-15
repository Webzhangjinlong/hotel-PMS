package com.hotel.pms.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房型创建请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomTypeCreateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 房型名称 */
    @NotBlank(message = "房型名称不能为空")
    private String name;
    
    /** 房型编码 */
    @NotBlank(message = "房型编码不能为空")
    private String code;
    
    /** 床型 */
    private String bedType;
    
    /** 最大入住人数 */
    @Min(value = 1, message = "最大入住人数必须大于0")
    private Integer maxGuests;
    
    /** 基础价格 */
    @NotNull(message = "基础价格不能为空")
    @Min(value = 0, message = "基础价格不能为负数")
    private BigDecimal basePrice;
    
    /** 房型描述 */
    private String description;
}
