package com.hotel.pms.service.config;

import com.hotel.pms.common.constant.SystemConstants;
import com.hotel.pms.common.exception.ConcurrentUpdateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 乐观锁重试工具类
 * <p>
 * 处理乐观锁冲突时的自动重试机制
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class OptimisticLockRetryHelper {
    
    /**
     * 执行带乐观锁重试的业务逻辑
     * <p>
     * 当发生乐观锁冲突时自动重试，直到成功或达到最大重试次数
     * </p>
     * 
     * @param action 业务逻辑
     * @param <T> 返回类型
     * @return 业务执行结果
     * @throws ConcurrentUpdateException 达到最大重试次数时抛出
     */
    public <T> T executeWithRetry(Supplier<T> action) {
        return executeWithRetry(action, SystemConstants.OPTIMISTIC_LOCK_MAX_RETRY);
    }
    
    /**
     * 执行带乐观锁重试的业务逻辑（自定义重试次数）
     * 
     * @param action 业务逻辑
     * @param maxRetries 最大重试次数
     * @param <T> 返回类型
     * @return 业务执行结果
     * @throws ConcurrentUpdateException 达到最大重试次数时抛出
     */
    public <T> T executeWithRetry(Supplier<T> action, int maxRetries) {
        int retryCount = 0;
        
        while (true) {
            try {
                // 【执行业务逻辑】
                return action.get();
                
            } catch (ObjectOptimisticLockingFailureException e) {
                // 【乐观锁冲突】
                retryCount++;
                
                if (retryCount >= maxRetries) {
                    // 【达到最大重试次数】抛出异常
                    log.error("乐观锁重试{}次后仍然失败，放弃重试", maxRetries);
                    throw new ConcurrentUpdateException("并发更新冲突，请重试");
                }
                
                // 【等待重试】
                log.warn("乐观锁冲突，第{}次重试，等待{}ms", retryCount, SystemConstants.OPTIMISTIC_LOCK_RETRY_INTERVAL);
                try {
                    Thread.sleep(SystemConstants.OPTIMISTIC_LOCK_RETRY_INTERVAL);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ConcurrentUpdateException("操作被中断");
                }
            }
        }
    }
}