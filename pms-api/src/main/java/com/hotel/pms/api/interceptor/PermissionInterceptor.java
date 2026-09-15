package com.hotel.pms.api.interceptor;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.annotation.RequiresPermission;
import com.hotel.pms.common.annotation.RequiresRole;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

/**
 * 权限校验拦截器
 * <p>
 * 校验接口的权限和角色
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 【非Controller方法直接放行】
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        
        // 【获取当前用户】
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return true;  // 未登录，由AuthInterceptor处理
        }
        
        // 【校验角色】
        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (requiresRole != null) {
            checkRole(userId, requiresRole);
        }
        
        // 【校验权限】
        RequiresPermission requiresPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (requiresPermission != null) {
            checkPermission(userId, requiresPermission);
        }
        
        return true;
    }
    
    /**
     * 校验角色
     */
    private void checkRole(Long userId, RequiresRole requiresRole) {
        Set<String> userRoles = UserContext.getRoleCodes();
        if (userRoles == null || userRoles.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无角色权限");
        }
        
        // 【检查是否有任一角色】
        boolean hasRole = Arrays.stream(requiresRole.value())
                .anyMatch(userRoles::contains);
        
        if (!hasRole) {
            log.warn("角色校验失败：userId={}, required={}, actual={}", userId, 
                    Arrays.toString(requiresRole.value()), userRoles);
            throw new BusinessException(ResultCode.FORBIDDEN, "无访问权限");
        }
    }
    
    /**
     * 校验权限
     */
    private void checkPermission(Long userId, RequiresPermission requiresPermission) {
        Set<String> userPermissions = UserContext.getPermissionCodes();
        if (userPermissions == null || userPermissions.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限");
        }
        
        boolean hasPermission;
        
        if (requiresPermission.logical() == RequiresPermission.Logical.AND) {
            // 【AND逻辑】需要同时拥有所有权限
            hasPermission = Arrays.stream(requiresPermission.value())
                    .allMatch(userPermissions::contains);
        } else {
            // 【OR逻辑】拥有任一权限即可
            hasPermission = Arrays.stream(requiresPermission.value())
                    .anyMatch(userPermissions::contains);
        }
        
        if (!hasPermission) {
            log.warn("权限校验失败：userId={}, required={}, logical={}, actual={}", userId,
                    Arrays.toString(requiresPermission.value()), requiresPermission.logical(), userPermissions);
            throw new BusinessException(ResultCode.FORBIDDEN, "无操作权限");
        }
    }
}