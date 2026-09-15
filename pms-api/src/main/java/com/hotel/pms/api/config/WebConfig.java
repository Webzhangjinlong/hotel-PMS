package com.hotel.pms.api.config;

import com.hotel.pms.api.interceptor.AuthInterceptor;
import com.hotel.pms.api.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * <p>
 * 配置拦截器、CORS等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    /** 认证拦截器 */
    private final AuthInterceptor authInterceptor;
    
    /** 权限拦截器 */
    private final PermissionInterceptor permissionInterceptor;
    
    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 【认证拦截器】
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/captcha",
                        "/api/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                )
                .order(1);
        
        // 【权限拦截器】在认证之后执行
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/api/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                )
                .order(2);
    }
    
    /**
     * 配置CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}