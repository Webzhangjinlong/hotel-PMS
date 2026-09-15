package com.hotel.pms.service.config;

import com.hotel.pms.common.constant.SystemConstants;
import com.hotel.pms.common.exception.DistributedLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * <p>
 * 基于Redisson实现分布式锁，解决高并发场景下的数据一致性问题
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockHelper {
    
    /** Redisson客户端 */
    private final RedissonClient redissonClient;
    
    /**
     * 获取分布式锁并执行业务逻辑
     * <p>
     * 使用示例：
     * <pre>
     * distributedLockHelper.executeWithLock("lock:reservation:room:101", () -> {
     *     // 业务逻辑
     *     return reservationService.createReservation(dto);
     * });
     * </pre>
     * </p>
     * 
     * @param lockKey 锁的Key
     * @param action 业务逻辑
     * @param <T> 返回类型
     * @return 业务执行结果
     * @throws DistributedLockException 获取锁失败时抛出
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return executeWithLock(lockKey, SystemConstants.LOCK_WAIT_TIME, SystemConstants.LOCK_LEASE_TIME, action);
    }
    
    /**
     * 获取分布式锁并执行业务逻辑（自定义超时时间）
     * 
     * @param lockKey 锁的Key
     * @param waitTime 等待获取锁的时间（秒）
     * @param leaseTime 持有锁的时间（秒）
     * @param action 业务逻辑
     * @param <T> 返回类型
     * @return 业务执行结果
     * @throws DistributedLockException 获取锁失败时抛出
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 【尝试获取锁】等待指定时间
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            
            if (!acquired) {
                // 【获取锁失败】抛出异常
                log.warn("获取分布式锁失败，lockKey={}", lockKey);
                throw new DistributedLockException("获取分布式锁失败，请稍后重试");
            }
            
            // 【获取锁成功】执行业务逻辑
            log.debug("获取分布式锁成功，lockKey={}", lockKey);
            return action.get();
            
        } catch (InterruptedException e) {
            // 【中断异常】
            Thread.currentThread().interrupt();
            throw new DistributedLockException("获取分布式锁被中断");
            
        } finally {
            // 【释放锁】确保锁被释放
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁，lockKey={}", lockKey);
            }
        }
    }
    
    /**
     * 获取分布式锁并执行无返回值的业务逻辑
     * 
     * @param lockKey 锁的Key
     * @param action 业务逻辑
     */
    public void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(lockKey, () -> {
            action.run();
            return null;
        });
    }
    
    /**
     * 尝试获取分布式锁（非阻塞）
     * 
     * @param lockKey 锁的Key
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.tryLock();
    }
    
    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁的Key
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}