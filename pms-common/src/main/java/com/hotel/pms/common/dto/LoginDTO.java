package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求DTO
 * <p>
 * 用于接收前端登录请求参数
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class LoginDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 用户名，必填，长度3-50 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50个字符")
    private String username;
    
    /** 酒店标识码，必填 */
    @NotBlank(message = "酒店标识码不能为空")
    @Size(min = 2, max = 50, message = "酒店标识码长度2-50个字符")
    private String hotelCode;
    
    /** 密码，必填，长度6-100 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100个字符")
    private String password;
    
    /** 验证码，必填 */
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    
    /** 验证码key，必填 */
    @NotBlank(message = "验证码key不能为空")
    private String captchaKey;
}