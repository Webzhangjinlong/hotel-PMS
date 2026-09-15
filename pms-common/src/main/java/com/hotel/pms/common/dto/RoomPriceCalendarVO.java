package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 房价月历视图VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPriceCalendarVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 房型名称 */
    private String roomTypeName;
    
    /** 房型基础价 */
    private BigDecimal basePrice;
    
    /** 年份 */
    private Integer year;
    
    /** 月份 */
    private Integer month;
    
    /** 每日价格列表 */
    private List<DayPrice> dayPrices;
    
    /**
     * 每日价格
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayPrice implements Serializable {
        
        /** 序列化版本号 */
        private static final long serialVersionUID = 1L;
        
        /** 日期 */
        private LocalDate date;
        
        /** 价格（null表示使用基础价） */
        private BigDecimal price;
        
        /** 是否有特殊价格 */
        private Boolean hasSpecialPrice;
        
        /** 星期几（1-7） */
        private Integer dayOfWeek;
    }
}
