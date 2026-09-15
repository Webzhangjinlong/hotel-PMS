package com.hotel.pms.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 用于标记需要记录操作日志的方法
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作模块
     */
    String module() default "";
    
    /**
     * 操作类型
     */
    String action() default "";
    
    /**
     * 目标类型
     */
    String targetType() default "";
    
    /**
     * 目标ID的参数名（从方法参数中获取）
     */
    String targetIdParam() default "";
    
    /**
     * 操作内容模板（支持 SpEL 表达式）
     */
    String content() default "";
}
