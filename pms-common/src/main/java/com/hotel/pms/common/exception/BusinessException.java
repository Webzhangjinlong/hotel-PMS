package com.hotel.pms.common.exception;

import com.hotel.pms.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 * <p>
 * 用于处理业务逻辑中的异常情况
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 错误码 */
    private final int code;
    
    /**
     * 构造业务异常
     * 
     * @param resultCode 结果码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
    
    /**
     * 构造业务异常（自定义消息）
     * 
     * @param resultCode 结果码枚举
     * @param message 自定义错误消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
    
    /**
     * 构造业务异常（自定义错误码和消息）
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造业务异常（自定义消息）
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }
}