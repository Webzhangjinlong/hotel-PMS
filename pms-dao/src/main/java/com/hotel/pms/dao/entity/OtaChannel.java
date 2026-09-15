package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OTA渠道配置实体类
 * <p>
 * 对应数据库表：ota_channel
 * 存储OTA渠道的配置信息，包括API配置、认证信息等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ota_channel")
public class OtaChannel extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 渠道名称 */
    @TableField("channel_name")
    private String channelName;
    
    /** 渠道编码 */
    @TableField("channel_code")
    private String channelCode;
    
    /** 渠道类型：OTA-在线旅行社/DIRECT-直销/CORPORATE-协议单位 */
    @TableField("channel_type")
    private String channelType;
    
    /** API Key */
    @TableField("app_key")
    private String appKey;
    
    /** API Secret */
    @TableField("app_secret")
    private String appSecret;
    
    /** API地址 */
    @TableField("api_url")
    private String apiUrl;
    
    /** 回调地址 */
    @TableField("callback_url")
    private String callbackUrl;
    
    /** 认证类型：API_KEY/OAUTH2/BASIC */
    @TableField("auth_type")
    private String authType;
    
    /** 认证Token */
    @TableField("auth_token")
    private String authToken;
    
    /** Token过期时间 */
    @TableField("token_expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tokenExpireTime;
    
    /** 配置信息（JSON格式） */
    @TableField("config_json")
    private String configJson;
    
    /** 状态：ACTIVE-启用/INACTIVE-停用 */
    @TableField("status")
    private String status;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
}
