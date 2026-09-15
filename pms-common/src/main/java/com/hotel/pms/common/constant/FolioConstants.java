package com.hotel.pms.common.constant;

/**
 * 账务常量类
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
public final class FolioConstants {
    
    /** 私有构造函数 */
    private FolioConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ========== 账务状态 ==========
    /** 未结 */
    public static final String FOLIO_OPEN = "OPEN";
    /** 已结 */
    public static final String FOLIO_CLOSED = "CLOSED";
    
    // ========== 交易类型 ==========
    /** 押金 */
    public static final String TRANSACTION_DEPOSIT = "DEPOSIT";
    /** 房费 */
    public static final String TRANSACTION_ROOM_FEE = "ROOM_FEE";
    /** 杂费 */
    public static final String TRANSACTION_EXTRA = "EXTRA";
    /** 付款 */
    public static final String TRANSACTION_PAYMENT = "PAYMENT";
    /** 退款 */
    public static final String TRANSACTION_REFUND = "REFUND";
    /** 冲账 */
    public static final String TRANSACTION_REVERSAL = "REVERSAL";
    /** 超时费 */
    public static final String TRANSACTION_LATE_FEE = "LATE_FEE";
    
    // ========== 支付方式 ==========
    /** 现金 */
    public static final String PAYMENT_CASH = "CASH";
    /** 微信 */
    public static final String PAYMENT_WECHAT = "WECHAT";
    /** 支付宝 */
    public static final String PAYMENT_ALIPAY = "ALIPAY";
    /** POS刷卡 */
    public static final String PAYMENT_POS = "POS";
    /** 银行转账 */
    public static final String PAYMENT_BANK_TRANSFER = "BANK_TRANSFER";
    /** 挂账 */
    public static final String PAYMENT_CREDIT = "CREDIT";

    /** 积分抵扣 */
    public static final String PAYMENT_POINTS = "POINTS";
    
    // ========== 预付类型 ==========
    /** 全额预付 */
    public static final String PREPAYMENT_FULL = "FULL";
    /** 部分预付 */
    public static final String PREPAYMENT_PARTIAL = "PARTIAL";
    /** 押金 */
    public static final String PREPAYMENT_DEPOSIT = "DEPOSIT";
    
    // ========== 预付状态 ==========
    /** 已付 */
    public static final String PREPAYMENT_STATUS_PAID = "PAID";
    /** 已转入 */
    public static final String PREPAYMENT_STATUS_TRANSFERRED = "TRANSFERRED";
    /** 已退款 */
    public static final String PREPAYMENT_STATUS_REFUNDED = "REFUNDED";
    
    // ========== 挂账公司状态 ==========
    /** 活跃 */
    public static final String CREDIT_COMPANY_ACTIVE = "ACTIVE";
    /** 停用 */
    public static final String CREDIT_COMPANY_INACTIVE = "INACTIVE";
}
