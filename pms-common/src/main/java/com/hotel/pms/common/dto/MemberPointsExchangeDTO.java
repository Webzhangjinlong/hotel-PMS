package com.hotel.pms.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 积分兑换DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class MemberPointsExchangeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "积分数不能为空")
    @Min(value = 100, message = "最少兑换100积分")
    private Integer points;

    /** 关联入住单ID */
    private Long stayId;

    /** 备注 */
    private String remark;
}
