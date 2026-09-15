package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 同住人请求DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class StayGuestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 客人姓名 */
    @NotBlank(message = "客人姓名不能为空")
    private String guestName;

    /** 证件类型：ID_CARD-身份证/PASSPORT-护照 */
    private String idType;

    /** 证件号码 */
    private String idNo;

    /** 手机号 */
    private String phone;

    /** 性别 */
    private String gender;

    /** 是否主客人 */
    private Boolean isPrimary;
}
