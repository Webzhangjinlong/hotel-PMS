package com.hotel.pms.service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * 事务管理工具类
 * <p>
 * 提供编程式事务管理，支持复杂的事务传播场景
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionHelper {
    
    /** 事务模板 */
    private final TransactionTemplate transactionTemplate;
    
    /**
     * 在新事务中执行业务逻辑
     * <p>
     * 使用场景：需要独立事务的操作，如日志记录、事件发布等
     * </p>
     * 
     * @param action 业务逻辑
     * @param <T> 返回类型
     * @return 业务执行结果
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T executeInNewTransaction(Supplier<T> action) {
        return action.get();
    }
    
    /**
     * 在新事务中执行无返回值的业务逻辑
     * 
     * @param action 业务逻辑
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeInNewTransaction(Runnable action) {
        action.run();
    }
    
    /**
     * 使用编程式事务执行业务逻辑
     * <p>
     * 使用场景：需要精确控制事务边界的复杂业务
     * </p>
     * 
     * @param action 业务逻辑
     * @param <T> 返回类型
     * @return 业务执行结果
     */
    public <T> T executeWithTransaction(Supplier<T> action) {
        return transactionTemplate.execute(status -> {
            try {
                return action.get();
            } catch (Exception e) {
                // 【标记回滚】手动标记事务回滚
                status.setRollbackOnly();
                throw e;
            }
        });
    }
    
    /**
     * 使用编程式事务执行无返回值的业务逻辑
     * 
     * @param action 业务逻辑
     */
    public void executeWithTransaction(Runnable action) {
        transactionTemplate.execute(status -> {
            try {
                action.run();
                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
        });
    }
}