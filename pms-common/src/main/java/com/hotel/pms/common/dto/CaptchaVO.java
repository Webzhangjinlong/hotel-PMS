package com.hotel.pms.common.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 验证码响应VO
 * <p>
 * 用于返回验证码信息
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
public class CaptchaVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 验证码Key */
    private String key;
    
    /** 验证码图片（Base64编码） */
    private String image;
}