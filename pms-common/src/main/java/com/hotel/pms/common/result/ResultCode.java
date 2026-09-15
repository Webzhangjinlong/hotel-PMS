package com.hotel.pms.common.result;

import lombok.Getter;

/**
 * 结果码枚举
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Getter
public enum ResultCode {
    
    // ========== 成功 ==========
    SUCCESS(200, "操作成功"),
    
    // ========== 客户端错误 ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    
    // ========== 服务器错误 ==========
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // ========== 认证错误 AUTH ==========
    AUTH_USERNAME_PASSWORD_ERROR(1001, "用户名或密码错误"),
    AUTH_CAPTCHA_ERROR(1002, "验证码错误或已过期"),
    AUTH_ACCOUNT_LOCKED(1003, "账号已被锁定"),
    AUTH_ACCOUNT_DISABLED(1004, "账号已被停用"),
    AUTH_TOKEN_EXPIRED(1005, "Token已过期"),
    AUTH_TOKEN_INVALID(1006, "Token无效"),
    
    // ========== 业务错误 ACCOUNT ==========
    ACCOUNT_USERNAME_EXISTS(2001, "用户名已存在"),
    ACCOUNT_HOTEL_NOT_EXISTS(2002, "酒店不存在"),
    
    // ========== 业务错误 ROOM ==========
    ROOM_NO_EXISTS(3001, "房间号已存在"),
    ROOM_TYPE_NOT_EXISTS(3002, "房型不存在"),
    ROOM_FLOOR_NOT_EXISTS(3003, "楼层不存在"),
    ROOM_TYPE_CODE_EXISTS(3004, "房型编码已存在"),
    
    // ========== 业务错误 RESERVATION ==========
    RESERVATION_ROOM_TYPE_NOT_EXISTS(4001, "房型不存在"),
    RESERVATION_DATE_INVALID(4002, "日期无效"),
    RESERVATION_NO_AVAILABLE_ROOM(4003, "该日期无可用房间"),
    RESERVATION_NOT_EXISTS(4004, "预订不存在"),
    RESERVATION_STATUS_ERROR(4005, "预订状态不允许此操作"),
    
    // ========== 业务错误 STAY ==========
    STAY_HAS_UNPAID(5001, "存在未结账务"),
    STAY_NOT_EXISTS(5002, "入住单不存在"),
    STAY_ALREADY_CHECKED_OUT(5003, "已经退房"),
    STAY_ROOM_OCCUPIED(5004, "房间已被占用"),
    
    // ========== 业务错误 FOLIO ==========
    FOLIO_NOT_EXISTS(6001, "账务单不存在"),
    FOLIO_ALREADY_CLOSED(6002, "账务单已关闭"),
    FOLIO_AMOUNT_ERROR(6003, "金额错误"),
    
    // ========== 业务错误 NIGHT_AUDIT ==========
    NIGHT_AUDIT_ALREADY_EXECUTED(7001, "该日期夜审已执行"),
    NIGHT_AUDIT_HAS_UNPAID(7002, "存在未结账务"),
    NIGHT_AUDIT_EXECUTING(7003, "夜审正在执行中"),
    
    // ========== 业务错误 INTEGRATION ==========
    INTEGRATION_CHANNEL_NOT_EXISTS(8001, "渠道不存在或已停用"),
    INTEGRATION_ROOM_TYPE_NOT_EXISTS(8002, "房型编码不存在"),
    INTEGRATION_ORDER_NO_EXISTS(8003, "订单号已存在"),
    
    
    // ========== 业务错误 DATA ==========
    DATA_NOT_FOUND(10001, "数据不存在"),
    DATA_DUPLICATE(10002, "数据已存在"),
    // ========== 并发错误 ==========
    CONCURRENT_UPDATE(9001, "并发更新冲突，请重试"),
    DISTRIBUTED_LOCK_FAIL(9002, "获取分布式锁失败");
    
    /** 结果码 */
    private final int code;
    
    /** 结果消息 */
    private final String message;
    
    /**
     * 构造函数
     * 
     * @param code 结果码
     * @param message 结果消息
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}