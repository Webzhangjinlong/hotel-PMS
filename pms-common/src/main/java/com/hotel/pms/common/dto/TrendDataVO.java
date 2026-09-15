package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 趋势数据VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 日期 */
    private LocalDate date;
    
    /** 收入 */
    private BigDecimal revenue;
    
    /** 入住率 */
    private BigDecimal occupancyRate;
    
    /** 平均房价（ADR） */
    private BigDecimal adr;
    
    /** 每间可售房收入（RevPAR） */
    private BigDecimal revpar;
    
    /** 入住间夜数 */
    private Integer roomNights;
    
    /** 可售房间数 */
    private Integer availableRooms;
}
