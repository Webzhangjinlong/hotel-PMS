package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统账号实体类
 * <p>
 * 对应数据库表：sys_account
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_account")
public class SysAccount extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 登录用户名，酒店内唯一 */
    @TableField("username")
    private String username;
    
    /** 登录密码，BCrypt加密存储 */
    @TableField("password")
    private String password;
    
    /** 真实姓名 */
    @TableField("real_name")
    private String realName;
    
    /** 手机号 */
    @TableField("phone")
    private String phone;
    
    /** 邮箱 */
    @TableField("email")
    private String email;
    
    /**
     * 角色类型
     * @see com.hotel.pms.common.enums.RoleEnum
     */
    @TableField("role")
    private String role;
    
    /**
     * 账号状态
     * @see com.hotel.pms.common.enums.StatusEnum
     */
    @TableField("status")
    private String status;
    
    /** 最后登录时间 */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    
    /** 登录失败次数 */
    @TableField("login_fail_count")
    private Integer loginFailCount;
    
    /** 锁定时间 */
    @TableField("lock_time")
    private LocalDateTime lockTime;
}