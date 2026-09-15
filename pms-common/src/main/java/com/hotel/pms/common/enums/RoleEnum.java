package com.hotel.pms.common.enums;

import lombok.Getter;

/**
 * 角色枚举
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Getter
public enum RoleEnum {
    
    /** 管理员 */
    ADMIN("ADMIN", "管理员"),
    
    /** 店长 */
    MANAGER("MANAGER", "店长"),
    
    /** 前台 */
    RECEPTIONIST("RECEPTIONIST", "前台");
    
    /** 角色编码 */
    private final String code;
    
    /** 角色名称 */
    private final String name;
    
    /**
     * 构造函数
     * 
     * @param code 角色编码
     * @param name 角色名称
     */
    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据编码获取角色
     * 
     * @param code 角色编码
     * @return 角色枚举
     */
    public static RoleEnum getByCode(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}