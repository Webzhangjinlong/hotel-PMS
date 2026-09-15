package com.hotel.pms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OTA渠道配置响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtaChannelVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 渠道ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 渠道名称 */
    private String channelName;
    
    /** 渠道编码 */
    private String channelCode;
    
    /** 渠道类型：OTA-在线旅行社/DIRECT-直销/CORPORATE-协议单位 */
    private String channelType;
    
    /** 渠道类型名称 */
    private String channelTypeName;
    
    /** API Key */
    private String appKey;
    
    /** API地址 */
    private String apiUrl;
    
    /** 回调地址 */
    private String callbackUrl;
    
    /** 认证类型：API_KEY/OAUTH2/BASIC */
    private String authType;
    
    /** 认证类型名称 */
    private String authTypeName;
    
    /** Token过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tokenExpireTime;
    
    /** 配置信息（JSON格式） */
    private String configJson;
    
    /** 状态：ACTIVE-启用/INACTIVE-停用 */
    private String status;
    
    /** 状态名称 */
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
