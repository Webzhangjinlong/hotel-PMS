package com.hotel.pms.common.constant;

/**
 * 会员常量类
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
public final class MemberConstants {

    private MemberConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========== 会员等级编码 ==========
    public static final String LEVEL_NORMAL = "NORMAL";
    public static final String LEVEL_SILVER = "SILVER";
    public static final String LEVEL_GOLD = "GOLD";
    public static final String LEVEL_DIAMOND = "DIAMOND";

    // ========== 会员状态 ==========
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_FROZEN = "FROZEN";

    // ========== 注册来源 ==========
    public static final String SOURCE_AUTO = "AUTO";
    public static final String SOURCE_MANUAL = "MANUAL";

    // ========== 积分变动类型 ==========
    public static final String POINTS_EARN = "EARN";
    public static final String POINTS_EXCHANGE = "EXCHANGE";
    public static final String POINTS_REFUND = "REFUND";
    public static final String POINTS_ADJUST = "ADJUST";

    // ========== 积分兑换比例 ==========
    /** 积分兑换比例：100积分 = 1元 */
    public static final int POINTS_EXCHANGE_RATE = 100;
}
