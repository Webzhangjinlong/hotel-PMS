package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeamFolioPaymentVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long teamFolioId;
    private String paymentNo;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String operatorName;
    private String remark;
}