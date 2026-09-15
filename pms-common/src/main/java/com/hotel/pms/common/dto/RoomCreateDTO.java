package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 房间创建请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomCreateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 房型ID */
    @NotNull(message = "房型ID不能为空")
    private Long roomTypeId;
    
    /** 楼层ID */
    @NotNull(message = "楼层ID不能为空")
    private Long floorId;
    
    /** 房间号 */
    @NotBlank(message = "房间号不能为空")
    private String roomNo;
    
    /** 房间描述 */
    private String description;
}
