package com.hotel.pms.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一返回结果包装类
 * 
 * @param <T> 数据类型
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class Result<T> implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 响应码 */
    private int code;
    
    /** 响应消息 */
    private String message;
    
    /** 响应数据 */
    private T data;
    
    /** 响应时间戳 */
    private long timestamp;
    
    /**
     * 私有构造函数
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 成功响应（有数据）
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }
    
    /**
     * 失败响应
     * 
     * @param resultCode 结果码
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }
    
    /**
     * 失败响应（自定义消息）
     * 
     * @param resultCode 结果码
     * @param message 自定义消息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return error(resultCode.getCode(), message);
    }
    
    /**
     * 失败响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}