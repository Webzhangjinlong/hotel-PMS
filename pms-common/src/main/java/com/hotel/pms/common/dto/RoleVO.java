package com.hotel.pms.common.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
public class RoleVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 角色ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 角色名称 */
    private String roleName;
    
    /** 角色编码 */
    private String roleCode;
    
    /** 角色描述 */
    private String description;
    
    /** 排序号 */
    private Integer sortOrder;
    
    /** 状态 */
    private String status;
    
    /** 权限数量 */
    private Integer permissionCount;
    
    /** 用户数量 */
    private Integer userCount;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}