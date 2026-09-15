package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 续住响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StayExtendVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 入住单号 */
    private String stayNo;
    
    /** 原离店时间 */
    private LocalDateTime oldCheckOutTime;
    
    /** 新离店时间 */
    private LocalDateTime newCheckOutTime;
    
    /** 续住天数 */
    private Integer extendedDays;
    
    /** 新增费用 */
    private BigDecimal additionalAmount;
    
    /** 新总金额 */
    private BigDecimal newTotalAmount;
}