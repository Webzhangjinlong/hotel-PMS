package com.hotel.pms.common.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应VO
 * <p>
 * 用于返回登录成功后的数据
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
public class LoginVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** JWT令牌 */
    private String token;
    
    /** 令牌类型，固定值：Bearer */
    private String tokenType;
    
    /** 过期时间（秒） */
    private Long expiresIn;
    
    /** 用户信息 */
    private UserInfoVO userInfo;
}