package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 散客入住请求DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class StayCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;

    @NotNull(message = "房间不能为空")
    private Long roomId;

    @NotBlank(message = "客人姓名不能为空")
    private String guestName;

    private String guestIdNo;

    @NotBlank(message = "客人电话不能为空")
    private String guestPhone;

    private String guestGender;

    @NotNull(message = "预计离店日期不能为空")
    private LocalDate expectedCheckOutDate;

    /** 单日房价（可选，为空则使用房价码自动计算） */
    private BigDecimal dailyPrice;

    /** 房价码ID（可选，为空则使用默认价格） */
    private Long pricePlanId;

    /** 会员ID（可选） */
    private Long memberId;

    /** 同住人列表（可选，不含主客人） */
    private List<StayGuestDTO> coGuests;
}
