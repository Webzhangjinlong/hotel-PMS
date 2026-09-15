package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * OTA渠道配置请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class OtaChannelDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 渠道名称 */
    @NotBlank(message = "渠道名称不能为空")
    private String channelName;
    
    /** 渠道编码 */
    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;
    
    /** 渠道类型：OTA-在线旅行社/DIRECT-直销/CORPORATE-协议单位 */
    private String channelType;
    
    /** API Key */
    private String appKey;
    
    /** API Secret */
    private String appSecret;
    
    /** API地址 */
    private String apiUrl;
    
    /** 回调地址 */
    private String callbackUrl;
    
    /** 认证类型：API_KEY/OAUTH2/BASIC */
    private String authType;
    
    /** 认证Token */
    private String authToken;
    
    /** 配置信息（JSON格式） */
    private String configJson;
    
    /** 状态：ACTIVE-启用/INACTIVE-停用 */
    private String status;
    
    /** 备注 */
    private String remark;
}
