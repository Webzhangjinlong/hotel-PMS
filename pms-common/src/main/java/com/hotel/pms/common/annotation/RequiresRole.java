package com.hotel.pms.common.annotation;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * <p>
 * 用于接口方法的角色校验
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {
    
    /**
     * 角色编码
     * <p>
     * 支持多个角色编码，满足任一即可
     * </p>
     * 
     * @return 角色编码数组
     */
    String[] value();
}