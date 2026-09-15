package com.hotel.pms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 身份证阅读器设备配置响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcardReaderConfigVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 设备ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 设备名称 */
    private String deviceName;
    
    /** 设备型号 */
    private String deviceModel;
    
    /** 设备端口 */
    private String devicePort;
    
    /** 设备IP地址 */
    private String deviceIp;
    
    /** 设备状态：OFFLINE-离线/ONLINE-在线/ERROR-故障 */
    private String deviceStatus;
    
    /** 设备状态名称 */
    private String deviceStatusName;
    
    /** 配置信息（JSON格式） */
    private String configJson;
    
    /** 配置状态：ACTIVE-启用/INACTIVE-停用 */
    private String status;
    
    /** 配置状态名称 */
    private String statusName;
    
    /** 备注 */
    private String remark;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
