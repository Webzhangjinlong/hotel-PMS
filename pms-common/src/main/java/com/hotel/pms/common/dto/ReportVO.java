package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 报表数据VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 基础指标 */
    private MetricsVO metrics;
    
    /** 渠道收入占比 */
    private List<ChannelRevenueVO> channelRevenues;
    
    /** 趋势数据 */
    private List<TrendDataVO> trendData;
    
    /** 总收入 */
    private BigDecimal totalRevenue;
    
    /** 总订单数 */
    private Integer totalOrders;
    
    /** 平均入住率 */
    private BigDecimal avgOccupancyRate;
    
    /** 平均房价 */
    private BigDecimal avgAdr;
}
