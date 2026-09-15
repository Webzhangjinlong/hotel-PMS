package com.hotel.pms.api.config;

import java.util.Set;

/**
 * 登录用户上下文
 * <p>
 * 使用ThreadLocal存储当前登录用户信息，包含角色和权限
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
public class UserContext {
    
    /** 用户ID */
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    
    /** 用户名 */
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    
    /** 角色编码（逗号分隔） */
    private static final ThreadLocal<String> ROLES = new ThreadLocal<>();
    
    /** 角色编码集合 */
    private static final ThreadLocal<Set<String>> ROLE_CODES = new ThreadLocal<>();
    
    /** 权限编码集合 */
    private static final ThreadLocal<Set<String>> PERMISSION_CODES = new ThreadLocal<>();
    
    /** 酒店ID */
    private static final ThreadLocal<Long> HOTEL_ID = new ThreadLocal<>();
    
    /**
     * 设置用户信息
     */
    public static void set(Long userId, String username, String roles, Long hotelId) {
        USER_ID.set(userId);
        USERNAME.set(username);
        ROLES.set(roles);
        HOTEL_ID.set(hotelId);
    }
    
    /**
     * 设置用户信息（包含角色和权限）
     */
    public static void set(Long userId, String username, String roles, Long hotelId,
                          Set<String> roleCodes, Set<String> permissionCodes) {
        USER_ID.set(userId);
        USERNAME.set(username);
        ROLES.set(roles);
        HOTEL_ID.set(hotelId);
        ROLE_CODES.set(roleCodes);
        PERMISSION_CODES.set(permissionCodes);
    }
    
    /**
     * 清除用户信息
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLES.remove();
        HOTEL_ID.remove();
        ROLE_CODES.remove();
        PERMISSION_CODES.remove();
    }
    
    /** 获取用户ID */
    public static Long getUserId() {
        return USER_ID.get();
    }
    
    /** 获取用户名 */
    public static String getUsername() {
        return USERNAME.get();
    }
    
    /** 获取角色编码（逗号分隔） */
    public static String getRoles() {
        return ROLES.get();
    }
    
    /** 获取角色编码集合 */
    public static Set<String> getRoleCodes() {
        return ROLE_CODES.get();
    }
    
    /** 获取权限编码集合 */
    public static Set<String> getPermissionCodes() {
        return PERMISSION_CODES.get();
    }
    
    /** 获取酒店ID */
    public static Long getHotelId() {
        return HOTEL_ID.get();
    }
}