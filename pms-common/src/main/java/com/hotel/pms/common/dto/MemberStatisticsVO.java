package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员统计VO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer totalMembers;
    private Integer normalCount;
    private Integer silverCount;
    private Integer goldCount;
    private Integer diamondCount;
    private Integer totalPoints;
    private BigDecimal totalConsumption;
}
