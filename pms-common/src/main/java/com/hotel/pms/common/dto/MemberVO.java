package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员响应VO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long hotelId;
    private Long guestId;
    private String memberNo;
    private String phone;
    private String name;

    /** 等级信息 */
    private Long levelId;
    private String levelCode;
    private String levelName;
    private BigDecimal discountRate;
    private BigDecimal pointsMultiplier;

    /** 积分信息 */
    private Integer totalPoints;
    private Integer usedPoints;
    private Integer availablePoints;

    /** 消费统计 */
    private BigDecimal totalConsumption;
    private Integer totalStayCount;

    /** 注册信息 */
    private String registerSource;
    private String registerSourceName;
    private LocalDateTime registerTime;
    private LocalDateTime lastStayTime;

    /** 状态 */
    private String status;
    private String statusName;
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
