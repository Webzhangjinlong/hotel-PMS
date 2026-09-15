package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人工补传DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class PoliceManualUploadDTO {

    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;

    /** 入住单号 */
    @NotBlank(message = "入住单号不能为空")
    private String stayNo;

    /** 客人姓名 */
    @NotBlank(message = "客人姓名不能为空")
    private String guestName;

    /** 证件号码 */
    @NotBlank(message = "证件号码不能为空")
    private String guestIdNo;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    private String guestPhone;

    /** 入住时间 */
    @NotNull(message = "入住时间不能为空")
    private LocalDateTime checkInTime;

    /** 备注 */
    private String remark;
}
