package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 * <p>
 * 对应数据库表：sys_role_permission
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 角色ID */
    @TableField("role_id")
    private Long roleId;
    
    /** 权限ID */
    @TableField("permission_id")
    private Long permissionId;
    
    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
