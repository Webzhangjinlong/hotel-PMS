package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * <p>
 * 对应数据库表：sys_user_role
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    @TableField("user_id")
    private Long userId;
    
    /** 角色ID */
    @TableField("role_id")
    private Long roleId;
    
    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
