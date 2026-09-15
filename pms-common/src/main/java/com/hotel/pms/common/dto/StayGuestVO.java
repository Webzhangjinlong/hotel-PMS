package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 同住人响应VO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StayGuestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long stayId;
    private Long guestId;
    private String guestName;
    private String idType;
    private String idTypeName;
    private String idNo;
    private String phone;
    private String gender;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
