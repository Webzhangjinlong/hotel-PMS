package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 客人创建请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class GuestCreateDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 客人姓名 */
    @NotBlank(message = "客人姓名不能为空")
    private String name;
    
    /** 证件类型 */
    private String idType;
    
    /** 证件号码 */
    private String idNo;
    
    /** 手机号码 */
    @NotBlank(message = "手机号码不能为空")
    private String phone;
    
    /** 性别 */
    private String gender;
    
    /** 国籍 */
    private String nationality;
}
