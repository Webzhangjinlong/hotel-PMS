package com.hotel.pms.api.aspect;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.dao.entity.OperationLogEntity;
import com.hotel.pms.dao.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * <p>
 * 拦截带有 @OperationLog 注解的方法，自动记录操作日志
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {
    
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 方法正常返回后记录日志
     */
    @AfterReturning(pointcut = "@annotation(com.hotel.pms.common.annotation.OperationLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null);
    }
    
    /**
     * 方法抛出异常后记录日志
     */
    @AfterThrowing(pointcut = "@annotation(com.hotel.pms.common.annotation.OperationLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        handleLog(joinPoint, e);
    }
    
    /**
     * 处理日志记录
     */
    private void handleLog(JoinPoint joinPoint, Throwable e) {
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog operationLog = method.getAnnotation(OperationLog.class);
            
            if (operationLog == null) {
                return;
            }
            
            // 构建日志实体
            OperationLogEntity logEntity = new OperationLogEntity();
            logEntity.setHotelId(UserContext.getHotelId());
            logEntity.setOperatorId(UserContext.getUserId());
            logEntity.setOperatorName(UserContext.getUsername());
            logEntity.setModule(operationLog.module());
            logEntity.setAction(operationLog.action());
            logEntity.setTargetType(operationLog.targetType());
            logEntity.setIpAddress(getIpAddress());
            logEntity.setCreatedAt(LocalDateTime.now());
            
            // 获取目标ID
            Long targetId = getTargetId(joinPoint, operationLog.targetIdParam());
            if (targetId != null) {
                logEntity.setTargetId(targetId);
            }
            
            // 构建操作内容
            String content = buildContent(joinPoint, operationLog.content());
            if (e != null) {
                content += " [异常: " + e.getMessage() + "]";
            }
            logEntity.setContent(content);
            
            // 异步保存日志（这里简单处理，后续可改为异步）
            operationLogMapper.insert(logEntity);
            
        } catch (Exception ex) {
            log.error("记录操作日志失败", ex);
        }
    }
    
    /**
     * 获取目标ID
     */
    private Long getTargetId(JoinPoint joinPoint, String targetIdParam) {
        if (targetIdParam == null || targetIdParam.isEmpty()) {
            return null;
        }
        
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Parameter[] parameters = signature.getMethod().getParameters();
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(targetIdParam)) {
                    if (args[i] instanceof Long) {
                        return (Long) args[i];
                    } else if (args[i] instanceof Number) {
                        return ((Number) args[i]).longValue();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取目标ID失败", e);
        }
        
        return null;
    }
    
    /**
     * 构建操作内容
     */
    private String buildContent(JoinPoint joinPoint, String contentTemplate) {
        if (contentTemplate == null || contentTemplate.isEmpty()) {
            // 默认使用方法名作为操作内容
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getMethod().getName();
        }
        
        // 简单的参数替换（后续可扩展为 SpEL 表达式）
        String content = contentTemplate;
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Parameter[] parameters = signature.getMethod().getParameters();
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < parameters.length; i++) {
                String placeholder = "{" + parameters[i].getName() + "}";
                if (content.contains(placeholder)) {
                    content = content.replace(placeholder, String.valueOf(args[i]));
                }
            }
        } catch (Exception e) {
            log.warn("构建操作内容失败", e);
        }
        
        return content;
    }
    
    /**
     * 获取请求IP地址
     */
    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取IP地址失败", e);
        }
        return "unknown";
    }
}
