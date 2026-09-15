package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 经营指标响应VO
 */
@Data
public class MetricsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal occupancyRate;      // 入住率 (%)
    private BigDecimal adr;                // 平均房价
    private BigDecimal revpar;             // 平均客房收益
    private BigDecimal totalRevenue;       // 总收入
    private Integer roomNightsSold;        // 售出夜数
    private Integer totalAvailableRooms;   // 总可售房数
    private BigDecimal avgLengthOfStay;    // 平均入住天数

    // 趋势数据 (用于折线图)
    private List<String> dates;
    private List<BigDecimal> revenueTrend;
    private List<BigDecimal> occTrend;
    private List<BigDecimal> adrTrend;
}
