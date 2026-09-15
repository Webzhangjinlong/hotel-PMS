package com.hotel.pms.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 楼层创建请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class FloorCreateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 楼层号 */
    @NotNull(message = "楼层号不能为空")
    @Min(value = 1, message = "楼层号必须大于0")
    private Integer floorNo;
    
    /** 楼层名称 */
    @NotBlank(message = "楼层名称不能为空")
    private String name;
}
