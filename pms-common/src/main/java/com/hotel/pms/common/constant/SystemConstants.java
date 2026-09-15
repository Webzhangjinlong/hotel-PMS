package com.hotel.pms.common.constant;

/**
 * 系统常量类
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
public final class SystemConstants {
    
    /** 私有构造函数 */
    private SystemConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ========== 逻辑删除 ==========
    /** 未删除 */
    public static final Boolean NOT_DELETED = false;
    /** 已删除 */
    public static final Boolean DELETED = true;
    
    // ========== 状态 ==========
    /** 启用 */
    public static final String STATUS_ACTIVE = "ACTIVE";
    /** 停用 */
    public static final String STATUS_INACTIVE = "INACTIVE";
    
    // ========== 时间格式 ==========
    /** 日期格式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    /** 时间格式 */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    // ========== 分页 ==========
    /** 默认页码 */
    public static final int DEFAULT_PAGE = 1;
    /** 默认每页条数 */
    public static final int DEFAULT_SIZE = 10;
    /** 最大每页条数 */
    public static final int MAX_SIZE = 100;
    
    // ========== 并发控制 ==========
    /** 分布式锁默认等待时间（秒） */
    public static final long LOCK_WAIT_TIME = 5;
    /** 分布式锁默认持有时间（秒） */
    public static final long LOCK_LEASE_TIME = 30;
    /** 乐观锁最大重试次数 */
    public static final int OPTIMISTIC_LOCK_MAX_RETRY = 3;
    /** 乐观锁重试间隔（毫秒） */
    public static final long OPTIMISTIC_LOCK_RETRY_INTERVAL = 100;
}