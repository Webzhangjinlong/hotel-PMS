package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamFolioVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long hotelId;
    private Long teamReservationId;
    private String teamReservationNo;
    private String teamName;
    private String settlementType;
    private String folioNo;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private String status;
    private String paymentMethod;
    private String remark;
    private LocalDateTime createdAt;
    private List<TeamFolioPaymentVO> payments;
}
