package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员创建DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class MemberCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String gender;

    private String idNo;

    private String remark;
}
