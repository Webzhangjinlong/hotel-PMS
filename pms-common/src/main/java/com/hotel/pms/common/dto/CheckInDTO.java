package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 预订入住请求DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class CheckInDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "预订ID不能为空")
    private Long reservationId;

    @NotNull(message = "房间不能为空")
    private Long roomId;

    @NotBlank(message = "客人姓名不能为空")
    private String guestName;

    private String guestIdNo;

    @NotBlank(message = "客人电话不能为空")
    private String guestPhone;

    private String guestGender;

    /** 会员ID（可选） */
    private Long memberId;

    /** 同住人列表（可选，不含主客人） */
    private List<StayGuestDTO> coGuests;
}
