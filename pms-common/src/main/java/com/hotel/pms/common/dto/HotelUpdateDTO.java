package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalTime;

/**
 * 酒店更新DTO
 */
@Data
public class HotelUpdateDTO {
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long id;
    
    /** 酒店名称 */
    @Size(max = 100, message = "酒店名称不能超过100个字符")
    private String name;
    
    /** 酒店地址 */
    @Size(max = 500, message = "地址不能超过500个字符")
    private String address;
    
    /** 联系电话 */
    @Size(max = 20, message = "电话不能超过20个字符")
    private String phone;
    
    /** 时区 */
    private String timezone;
    
    /** 酒店状态 */
    private String status;
    
    /** 夜审时间 */
    private LocalTime auditTime;
    
    /** 是否启用自动夜审 */
    private Boolean autoAuditEnabled;
}