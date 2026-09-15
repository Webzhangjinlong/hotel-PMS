package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分流水响应VO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPointsLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long memberId;
    private String changeType;
    private String changeTypeName;
    private Integer points;
    private Integer beforePoints;
    private Integer afterPoints;
    private Long relatedStayId;
    private String relatedStayNo;
    private Long relatedTransactionId;
    private String description;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
}
