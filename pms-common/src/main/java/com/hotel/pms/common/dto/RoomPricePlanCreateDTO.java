package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 房价码创建请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomPricePlanCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    @NotBlank(message = "房价码编码不能为空")
    private String code;
    
    @NotBlank(message = "房价码名称不能为空")
    private String name;
    
    /** 有效期开始 */
    private LocalDate validFrom;
    
    /** 有效期结束 */
    private LocalDate validTo;
    
    private String description;
    
    /** 房型明细列表 */
    @NotNull(message = "房型明细不能为空")
    private List<Detail> details;
    
    @Data
    public static class Detail implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @NotNull(message = "房型ID不能为空")
        private Long roomTypeId;
        
        private BigDecimal basePrice;
        
        /** 折扣方式：NONE/PERCENT/FIXED */
        private String discountType;
        
        private BigDecimal discountValue;
    }
}