package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 房间更新请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomUpdateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 楼层ID */
    private Long floorId;
    
    /** 房间号 */
    @NotBlank(message = "房间号不能为空")
    private String roomNo;
    
    /** 房间描述 */
    private String description;
}
