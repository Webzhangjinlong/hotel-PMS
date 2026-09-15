package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 门锁设备配置请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class DoorLockConfigDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 设备名称 */
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;
    
    /** 设备型号 */
    private String deviceModel;
    
    /** 设备IP地址 */
    private String deviceIp;
    
    /** 设备端口 */
    private String devicePort;
    
    /** 配置信息（JSON格式） */
    private String configJson;
    
    /** 配置状态：ACTIVE-启用/INACTIVE-停用 */
    private String status;
    
    /** 备注 */
    private String remark;
}
