package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 身份证阅读器设备配置查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdcardReaderConfigQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 设备名称 */
    private String deviceName;
    
    /** 设备型号 */
    private String deviceModel;
    
    /** 设备状态：OFFLINE-离线/ONLINE-在线/ERROR-故障 */
    private String deviceStatus;
    
    /** 配置状态：ACTIVE-启用/INACTIVE-停用 */
    private String status;
}
