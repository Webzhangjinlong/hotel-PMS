package com.hotel.pms.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 房态常量类
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
public final class RoomConstants {
    
    /** 私有构造函数 */
    private RoomConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ========== 房间状态 ==========
    /** 空闲 */
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    /** 在住 */
    public static final String STATUS_OCCUPIED = "OCCUPIED";
    /** 脏房 */
    public static final String STATUS_DIRTY = "DIRTY";
    /** 维修 */
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";
    /** 停用 */
    public static final String STATUS_OOO = "OOO";
    /** 预留 */
    public static final String STATUS_RESERVED = "RESERVED";
    
    // ========== 预订状态 ==========
    /** 待确认 */
    public static final String RESERVATION_PENDING = "PENDING";
    /** 已确认 */
    public static final String RESERVATION_CONFIRMED = "CONFIRMED";
    /** 已入住 */
    public static final String RESERVATION_CHECKED_IN = "CHECKED_IN";
    /** 已离店 */
    public static final String RESERVATION_CHECKED_OUT = "CHECKED_OUT";
    /** 已取消 */
    public static final String RESERVATION_CANCELLED = "CANCELLED";
    /** 未到店 */
    public static final String RESERVATION_NO_SHOW = "NO_SHOW";
    
    // ========== 入住状态 ==========
    /** 在住 */
    public static final String STAY_CHECKED_IN = "CHECKED_IN";
    /** 已离店 */
    public static final String STAY_CHECKED_OUT = "CHECKED_OUT";
    
    // ========== 状态名称映射 ==========
    private static final Map<String, String> STATUS_NAME_MAP = new HashMap<>();
    
    static {
        // 房间状态
        STATUS_NAME_MAP.put(STATUS_AVAILABLE, "空闲");
        STATUS_NAME_MAP.put(STATUS_OCCUPIED, "在住");
        STATUS_NAME_MAP.put(STATUS_DIRTY, "脏房");
        STATUS_NAME_MAP.put(STATUS_MAINTENANCE, "维修");
        STATUS_NAME_MAP.put(STATUS_OOO, "停用");
        STATUS_NAME_MAP.put(STATUS_RESERVED, "预留");
        
        // 预订状态
        STATUS_NAME_MAP.put(RESERVATION_PENDING, "待确认");
        STATUS_NAME_MAP.put(RESERVATION_CONFIRMED, "已确认");
        STATUS_NAME_MAP.put(RESERVATION_CHECKED_IN, "已入住");
        STATUS_NAME_MAP.put(RESERVATION_CHECKED_OUT, "已离店");
        STATUS_NAME_MAP.put(RESERVATION_CANCELLED, "已取消");
        STATUS_NAME_MAP.put(RESERVATION_NO_SHOW, "未到店");
    }
    
    /**
     * 获取状态名称
     * 
     * @param statusCode 状态编码
     * @return 状态名称
     */
    public static String getStatusName(String statusCode) {
        return STATUS_NAME_MAP.getOrDefault(statusCode, statusCode);
    }
    
    /**
     * 获取所有房间状态
     * 
     * @return 房间状态数组
     */
    public static String[] getAllRoomStatuses() {
        return new String[]{
            STATUS_AVAILABLE,
            STATUS_OCCUPIED,
            STATUS_DIRTY,
            STATUS_MAINTENANCE,
            STATUS_OOO,
            STATUS_RESERVED
        };
    }
}