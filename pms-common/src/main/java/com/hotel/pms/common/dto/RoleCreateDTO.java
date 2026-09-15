package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建角色请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoleCreateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过50个字符")
    private String roleName;
    
    /** 角色编码 */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码不能超过50个字符")
    private String roleCode;
    
    /** 角色描述 */
    @Size(max = 200, message = "角色描述不能超过200个字符")
    private String description;
    
    /** 排序号 */
    private Integer sortOrder;
    
    /** 权限ID列表 */
    private List<Long> permissionIds;
}