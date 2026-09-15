package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OTA事件日志查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OtaEventLogQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 渠道ID */
    private Long channelId;
    
    /** OTA订单ID */
    private Long otaOrderId;
    
    /** 事件类型：NEW_ORDER-新订单/CANCEL_ORDER-取消订单/UPDATE_ORDER-修改订单/ROOM_UPDATE-房态更新 */
    private String eventType;
    
    /** 处理状态：PENDING-待处理/PROCESSING-处理中/SUCCESS-成功/FAILED-失败 */
    private String processStatus;
    
    /** 事件时间开始 */
    private LocalDateTime eventTimeStart;
    
    /** 事件时间结束 */
    private LocalDateTime eventTimeEnd;
}
