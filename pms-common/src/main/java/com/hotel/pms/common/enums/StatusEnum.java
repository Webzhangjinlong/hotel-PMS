package com.hotel.pms.common.enums;

import lombok.Getter;

/**
 * 状态枚举
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Getter
public enum StatusEnum {
    
    /** 启用 */
    ACTIVE("ACTIVE", "启用"),
    
    /** 停用 */
    INACTIVE("INACTIVE", "停用");
    
    /** 状态编码 */
    private final String code;
    
    /** 状态名称 */
    private final String name;
    
    /**
     * 构造函数
     * 
     * @param code 状态编码
     * @param name 状态名称
     */
    StatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据编码获取状态
     * 
     * @param code 状态编码
     * @return 状态枚举
     */
    public static StatusEnum getByCode(String code) {
        for (StatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}