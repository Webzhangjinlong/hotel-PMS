package com.hotel.pms.common.constant;

/**
 * 入住常量类
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
public final class StayConstants {
    
    /** 私有构造函数 */
    private StayConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ========== 入住状态 ==========
    /** 在住 */
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    /** 已离店 */
    public static final String STATUS_CHECKED_OUT = "CHECKED_OUT";
    
    // ========== 客人证件类型 ==========
    /** 身份证 */
    public static final String ID_TYPE_ID_CARD = "ID_CARD";
    /** 护照 */
    public static final String ID_TYPE_PASSPORT = "PASSPORT";
    
    // ========== 账务单状态 ==========
    /** 开放 */
    public static final String FOLIO_STATUS_OPEN = "OPEN";
    /** 关闭 */
    public static final String FOLIO_STATUS_CLOSED = "CLOSED";
}