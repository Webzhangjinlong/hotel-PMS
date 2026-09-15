package com.hotel.pms.api.config;

import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.exception.ConcurrentUpdateException;
import com.hotel.pms.common.exception.DistributedLockException;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理系统异常，返回标准的Result响应格式
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     * 
     * @param e 业务异常
     * @return 错误结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理并发更新异常
     * 
     * @param e 并发更新异常
     * @return 错误结果
     */
    @ExceptionHandler(ConcurrentUpdateException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleConcurrentUpdateException(ConcurrentUpdateException e) {
        log.warn("并发更新异常：message={}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理分布式锁异常
     * 
     * @param e 分布式锁异常
     * @return 错误结果
     */
    @ExceptionHandler(DistributedLockException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleDistributedLockException(DistributedLockException e) {
        log.warn("分布式锁异常：message={}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常（@Valid）
     * 
     * @param e 参数校验异常
     * @return 错误结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", message);
        return Result.error(ResultCode.BAD_REQUEST, message);
    }
    
    /**
     * 处理参数绑定异常
     * 
     * @param e 参数绑定异常
     * @return 错误结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败：{}", message);
        return Result.error(ResultCode.BAD_REQUEST, message);
    }
    
    /**
     * 处理约束违反异常
     * 
     * @param e 约束违反异常
     * @return 错误结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束违反：{}", message);
        return Result.error(ResultCode.BAD_REQUEST, message);
    }
    
    /**
     * 处理其他未知异常
     * 
     * @param e 异常
     * @return 错误结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(ResultCode.INTERNAL_ERROR);
    }
}