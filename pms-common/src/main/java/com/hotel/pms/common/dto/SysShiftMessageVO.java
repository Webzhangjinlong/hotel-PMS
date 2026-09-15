package com.hotel.pms.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** SysShiftMessageVO */
@Data
public class SysShiftMessageVO {

    private Long shiftId;

    private Long receiverId;

    private String messageType;

    private Boolean isRead;

    private LocalDateTime readTime;

}