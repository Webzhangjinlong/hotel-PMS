package com.hotel.pms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 身份证阅读器设备日志响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcardReaderLogVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 日志ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 设备ID */
    private Long deviceId;
    
    /** 设备名称 */
    private String deviceName;
    
    /** 日志类型：CONNECT-连接/READ-读取/ERROR-错误/INFO-信息 */
    private String logType;
    
    /** 日志类型名称 */
    private String logTypeName;
    
    /** 日志内容 */
    private String logContent;
    
    /** 日志级别：INFO-信息/WARN-警告/ERROR-错误 */
    private String logLevel;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
