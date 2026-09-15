package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员等级创建/编辑DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class MemberLevelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "等级编码不能为空")
    private String levelCode;

    @NotBlank(message = "等级名称不能为空")
    private String levelName;

    @NotNull(message = "折扣率不能为空")
    private BigDecimal discountRate;

    @NotNull(message = "积分倍率不能为空")
    private BigDecimal pointsMultiplier;

    @NotNull(message = "最低消费金额不能为空")
    private BigDecimal minTotalConsumption;

    private Integer minStayCount;

    private String benefitsDesc;

    private Integer sortOrder;

    private String status;
}
