package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 换房请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class ChangeRoomDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 入住单ID */
    @NotNull(message = "入住单ID不能为空")
    private Long stayId;
    
    /** 新房间ID */
    @NotNull(message = "新房间ID不能为空")
    private Long newRoomId;
    
    /** 换房原因 */
    private String reason;
}
