package com.hotel.pms.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * <p>
 * 用于接口方法的权限校验
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    
    /**
     * 权限编码
     * <p>
     * 支持多个权限编码，满足任一即可
     * </p>
     * 
     * @return 权限编码数组
     */
    String[] value();
    
    /**
     * 逻辑关系
     * <p>
     * AND-同时满足所有权限，OR-满足任一权限（默认）
     * </p>
     * 
     * @return 逻辑关系
     */
    Logical logical() default Logical.OR;
    
    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /** 与 */
        AND,
        /** 或 */
        OR
    }
}