package com.hotel.pms.common.exception;

import com.hotel.pms.common.result.ResultCode;

/**
 * 并发异常类
 * <p>
 * 用于处理并发更新冲突
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
public class ConcurrentUpdateException extends BusinessException {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造并发异常
     * 
     * @param message 错误消息
     */
    public ConcurrentUpdateException(String message) {
        super(ResultCode.CONCURRENT_UPDATE, message);
    }
    
    /**
     * 构造并发异常
     */
    public ConcurrentUpdateException() {
        super(ResultCode.CONCURRENT_UPDATE);
    }
}