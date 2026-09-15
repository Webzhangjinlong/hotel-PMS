package com.hotel.pms.common.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 用户信息响应VO
 * <p>
 * 用于返回用户基本信息，包含角色和权限
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
public class UserInfoVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 手机号 */
    private String phone;
    
    /** 邮箱 */
    private String email;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 酒店名称 */
    private String hotelName;
    
    /** 角色列表 */
    private List<RoleVO> roles;
    
    /** 角色编码集合（用于快速判断） */
    private Set<String> roleCodes;
    
    /** 权限编码集合（用于快速判断） */
    private Set<String> permissionCodes;
    
    /** 菜单权限列表（用于前端路由） */
    private List<MenuVO> menus;
}