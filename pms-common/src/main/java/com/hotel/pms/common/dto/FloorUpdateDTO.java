package com.hotel.pms.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 楼层更新请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class FloorUpdateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 楼层号 */
    @Min(value = 1, message = "楼层号必须大于0")
    private Integer floorNo;
    
    /** 楼层名称 */
    @NotBlank(message = "楼层名称不能为空")
    private String name;
}
