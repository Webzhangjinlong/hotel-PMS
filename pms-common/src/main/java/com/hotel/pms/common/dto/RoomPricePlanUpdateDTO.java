package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 房价码更新请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomPricePlanUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    
    private LocalDate validFrom;
    
    private LocalDate validTo;
    
    private String description;
    
    /** 房型明细列表（传入则整体替换） */
    private List<Detail> details;
    
    @Data
    public static class Detail implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 明细ID（更新时需要） */
        private Long id;
        
        private Long roomTypeId;
        
        private BigDecimal basePrice;
        
        private String discountType;
        
        private BigDecimal discountValue;
    }
}