package com.hotel.pms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OTA事件日志响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtaEventLogVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 日志ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 渠道ID */
    private Long channelId;
    
    /** 渠道名称 */
    private String channelName;
    
    /** OTA订单ID */
    private Long otaOrderId;
    
    /** OTA订单号 */
    private String otaOrderNo;
    
    /** 事件类型：NEW_ORDER-新订单/CANCEL_ORDER-取消订单/UPDATE_ORDER-修改订单/ROOM_UPDATE-房态更新 */
    private String eventType;
    
    /** 事件类型名称 */
    private String eventTypeName;
    
    /** 事件来源 */
    private String eventSource;
    
    /** 事件时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;
    
    /** 请求数据（JSON格式） */
    private String requestData;
    
    /** 响应数据（JSON格式） */
    private String responseData;
    
    /** 处理状态：PENDING-待处理/PROCESSING-处理中/SUCCESS-成功/FAILED-失败 */
    private String processStatus;
    
    /** 处理状态名称 */
    private String processStatusName;
    
    /** 处理结果 */
    private String processResult;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
