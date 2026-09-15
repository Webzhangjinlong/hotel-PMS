package com.hotel.pms.common.dto;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 夜审详情VO
 */
@Data
@Builder
public class NightAuditDetailVO implements Serializable {
    
    private Long id;
    private Long hotelId;
    private LocalDate auditDate;
    private String status;
    private Integer totalRooms;
    private Integer occupiedRooms;
    private Integer availableRooms;
    private BigDecimal totalRevenue;
    private BigDecimal roomRevenue;
    private BigDecimal extraRevenue;
    private BigDecimal occupancyRate;
    private BigDecimal adr;
    private BigDecimal revpar;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private List<NightAuditStepVO> steps;
}