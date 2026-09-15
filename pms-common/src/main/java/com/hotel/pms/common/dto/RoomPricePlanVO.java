package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房价码响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPricePlanVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long hotelId;
    private String code;
    private String name;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /** 房型明细列表 */
    private List<DetailVO> details;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailVO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long id;
        private Long planId;
        private Long roomTypeId;
        private String roomTypeName;
        private BigDecimal basePrice;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal finalPrice;
    }
}