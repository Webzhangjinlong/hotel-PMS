package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 散客入住单转入团队预订请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class StayTransferDTO implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 目标团队预订ID */
    @NotNull(message = "团队预订ID不能为空")
    private Long teamReservationId;
}