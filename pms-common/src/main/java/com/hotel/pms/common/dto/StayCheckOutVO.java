package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退房响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StayCheckOutVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 入住单号 */
    private String stayNo;
    
    /** 房间号 */
    private String roomNo;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 入住时间 */
    private LocalDateTime checkInTime;
    
    /** 实际离店时间 */
    private LocalDateTime actualCheckOutTime;
    
    /** 总费用 */
    private BigDecimal totalAmount;
    
    /** 已付金额 */
    private BigDecimal paidAmount;
    
    /** 待结金额 */
    private BigDecimal outstandingAmount;
}
