package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统账号数据传输对象（创建/更新用户）
 */
@Data
public class SysAccountDTO {

    /** 登录用户名，酒店内唯一 */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    /** 登录密码，BCrypt加密存储（创建必填，更新忽略） */
    @Size(min = 6, max = 100, message = "密码长度需在6-100之间")
    private String password;

    /** 真实姓名 */
    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    /** 手机号 */
    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    /** 邮箱 */
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    /** 角色类型（枚举 RoleEnum） */
    private String role;
}
