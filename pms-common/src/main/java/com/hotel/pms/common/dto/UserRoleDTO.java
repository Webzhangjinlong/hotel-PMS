package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户角色分配DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class UserRoleDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /** 角色ID列表 */
    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}