package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 * <p>
 * 对应数据库表：sys_role
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 角色名称 */
    @TableField("role_name")
    private String roleName;
    
    /** 角色编码 */
    @TableField("role_code")
    private String roleCode;
    
    /** 角色描述 */
    @TableField("description")
    private String description;
    
    /** 排序号 */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /** 状态：ACTIVE-启用/INACTIVE-停用 */
    @TableField("status")
    private String status;
}