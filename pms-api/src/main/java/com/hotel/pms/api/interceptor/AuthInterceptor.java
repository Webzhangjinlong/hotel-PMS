package com.hotel.pms.api.interceptor;

import com.hotel.pms.common.utils.JwtUtil;
import com.hotel.pms.api.config.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * <p>
 * 拦截请求，验证JWT令牌，设置用户上下文
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    
    /** JWT工具类 */
    private final JwtUtil jwtUtil;
    
    /**
     * 请求前处理
     * <p>
     * 验证JWT令牌，设置用户上下文
     * </p>
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 【获取Token】从请求头中获取
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 【无Token】放行，由后续处理器判断是否需要认证
            return true;
        }
        
        String token = authHeader.substring(7);
        
        // 【验证Token】
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }
        
        // 【设置用户上下文】
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        Long hotelId = jwtUtil.getHotelId(token);
        
        UserContext.set(userId, username, role, hotelId);
        
        log.debug("用户认证成功：userId={}, username={}, role={}", userId, username, role);
        return true;
    }
    
    /**
     * 请求后处理
     * <p>
     * 清除用户上下文，防止内存泄漏
     * </p>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}