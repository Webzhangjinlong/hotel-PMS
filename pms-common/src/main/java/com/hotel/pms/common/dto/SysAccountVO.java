package com.hotel.pms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统账号视图对象（用户管理，不含密码）
 */
@Data
public class SysAccountVO {

    /** 账号ID */
    private Long id;

    /** 酒店ID */
    private Long hotelId;

    /** 登录用户名，酒店内唯一 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色类型 */
    private String role;

    /** 账号状态 */
    private String status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 登录失败次数 */
    private Integer loginFailCount;

    /** 锁定时间 */
    private LocalDateTime lockTime;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
