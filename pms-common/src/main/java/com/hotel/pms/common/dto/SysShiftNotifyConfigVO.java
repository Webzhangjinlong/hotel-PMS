package com.hotel.pms.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** SysShiftNotifyConfigVO */
@Data
public class SysShiftNotifyConfigVO {

    private Long hotelId;

    private Long userId;

    private String notifyType;

    private Boolean isActive;

}