package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级响应VO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLevelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long hotelId;
    private String levelCode;
    private String levelName;
    private BigDecimal discountRate;
    private BigDecimal pointsMultiplier;
    private BigDecimal minTotalConsumption;
    private Integer minStayCount;
    private String benefitsDesc;
    private Integer sortOrder;
    private String status;
    private String statusName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
