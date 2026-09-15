package com.hotel.pms.common.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 酒店VO
 */
@Data
@Builder
public class HotelVO {
    
    /** 酒店ID */
    private Long id;
    
    /** 酒店标识码 */
    private String hotelCode;
    
    /** 酒店名称 */
    private String name;
    
    /** 酒店地址 */
    private String address;
    
    /** 联系电话 */
    private String phone;
    
    /** 时区 */
    private String timezone;
    
    /** 酒店状态 */
    private String status;
    
    /** 夜审时间 */
    private LocalTime auditTime;
    
    /** 是否启用自动夜审 */
    private Boolean autoAuditEnabled;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
