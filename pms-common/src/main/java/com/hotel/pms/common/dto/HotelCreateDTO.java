package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 酒店创建DTO
 */
@Data
public class HotelCreateDTO {
    
    /** 酒店名称 */
    @NotBlank(message = "酒店名称不能为空")
    @Size(max = 100, message = "酒店名称不能超过100个字符")
    private String name;
    
    /** 酒店标识码 */
    @NotBlank(message = "酒店标识码不能为空")
    @Size(min = 2, max = 50, message = "酒店标识码长度2-50个字符")
    private String hotelCode;
    
    /** 酒店地址 */
    @Size(max = 500, message = "地址不能超过500个字符")
    private String address;
    
    /** 联系电话 */
    @Size(max = 20, message = "电话不能超过20个字符")
    private String phone;
    
    /** 时区 */
    private String timezone = "Asia/Shanghai";
}