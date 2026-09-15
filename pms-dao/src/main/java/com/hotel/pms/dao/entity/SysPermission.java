package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体类
 * <p>
 * 对应数据库表：sys_permission
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {
    
    /** 权限名称 */
    @TableField("permission_name")
    private String permissionName;
    
    /** 权限编码，如：user:create、menu:dashboard */
    @TableField("permission_code")
    private String permissionCode;
    
    /**
     * 资源类型
     * MENU-菜单/BUTTON-按钮/API-接口
     */
    @TableField("resource_type")
    private String resourceType;
    
    /** 资源路径：菜单路径或API路径 */
    @TableField("resource_path")
    private String resourcePath;
    
    /** 父权限ID，用于菜单层级 */
    @TableField("parent_id")
    private Long parentId;
    
    /** 排序号 */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /** 菜单图标 */
    @TableField("icon")
    private String icon;
    
    /** 是否可见：TRUE-可见/FALSE-隐藏 */
    @TableField("is_visible")
    private Boolean isVisible;
    
    /** 状态：ACTIVE-启用/INACTIVE-停用 */
    @TableField("status")
    private String status;
}