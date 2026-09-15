package com.hotel.pms.common.constant;

/**
 * 预订常量类
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
public final class ReservationConstants {
    
    /** 私有构造函数 */
    private ReservationConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ========== 预订状态 ==========
    /** 待确认 */
    public static final String STATUS_PENDING = "PENDING";
    /** 已确认 */
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    /** 已入住 */
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    /** 已取消 */
    public static final String STATUS_CANCELLED = "CANCELLED";
    /** 未到店 */
    public static final String STATUS_NO_SHOW = "NO_SHOW";
    /** 已离店 */
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT";
    
    // ========== 预订来源 ==========
    /** 散客（直接到店） */
    public static final String SOURCE_WALK_IN = "WALK_IN";
    /** 电话预订 */
    public static final String SOURCE_PHONE = "PHONE";
    /** OTA渠道 */
    public static final String SOURCE_OTA = "OTA";
    
    // ========== 预订类型 ==========
    /** 个人预订 */
    public static final String TYPE_INDIVIDUAL = "INDIVIDUAL";
    /** 团队预订 */
    public static final String TYPE_TEAM = "TEAM";
    
    // ========== 结算方式 ==========
    /** 统一结算 */
    public static final String SETTLEMENT_UNIFIED = "UNIFIED";
    /** 分开结算 */
    public static final String SETTLEMENT_SEPARATE = "SEPARATE";
    
    // ========== 团队预订房间状态 ==========
    /** 待入住 */
    public static final String ROOM_STATUS_PENDING = "PENDING";
    /** 已入住 */
    public static final String ROOM_STATUS_CHECKED_IN = "CHECKED_IN";
    /** 已退房 */
    public static final String ROOM_STATUS_CHECKED_OUT = "CHECKED_OUT";
}
