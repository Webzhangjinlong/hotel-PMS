package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OTA事件日志实体类
 * <p>
 * 对应数据库表：ota_event_log
 * 存储OTA事件的日志信息，用于追踪和调试
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ota_event_log")
public class OtaEventLog extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 渠道ID */
    @TableField("channel_id")
    private Long channelId;
    
    /** OTA订单ID */
    @TableField("ota_order_id")
    private Long otaOrderId;
    
    /** 事件类型：NEW_ORDER-新订单/CANCEL_ORDER-取消订单/UPDATE_ORDER-修改订单/ROOM_UPDATE-房态更新 */
    @TableField("event_type")
    private String eventType;
    
    /** 事件来源 */
    @TableField("event_source")
    private String eventSource;
    
    /** 事件时间 */
    @TableField("event_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;
    
    /** 请求数据（JSON格式） */
    @TableField("request_data")
    private String requestData;
    
    /** 响应数据（JSON格式） */
    @TableField("response_data")
    private String responseData;
    
    /** 处理状态：PENDING-待处理/PROCESSING-处理中/SUCCESS-成功/FAILED-失败 */
    @TableField("process_status")
    private String processStatus;
    
    /** 处理结果 */
    @TableField("process_result")
    private String processResult;
    
    /** 错误信息 */
    @TableField("error_message")
    private String errorMessage;
}
