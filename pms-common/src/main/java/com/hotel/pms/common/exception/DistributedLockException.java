package com.hotel.pms.common.exception;

import com.hotel.pms.common.result.ResultCode;

/**
 * 分布式锁异常类
 * <p>
 * 用于处理分布式锁获取失败
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
public class DistributedLockException extends BusinessException {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造分布式锁异常
     * 
     * @param message 错误消息
     */
    public DistributedLockException(String message) {
        super(ResultCode.DISTRIBUTED_LOCK_FAIL, message);
    }
    
    /**
     * 构造分布式锁异常
     */
    public DistributedLockException() {
        super(ResultCode.DISTRIBUTED_LOCK_FAIL);
    }
}