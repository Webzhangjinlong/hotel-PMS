package com.hotel.pms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 续住请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class StayExtendDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 入住单ID */
    @NotNull(message = "入住单ID不能为空")
    private Long stayId;
    
    /** 新离店日期 */
    @NotNull(message = "新离店日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newCheckOutDate;
}