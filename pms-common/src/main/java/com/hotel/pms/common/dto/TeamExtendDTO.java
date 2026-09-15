package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class TeamExtendDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotNull(message = "新离店日期不能为空")
    private LocalDate newCheckOutDate;
}