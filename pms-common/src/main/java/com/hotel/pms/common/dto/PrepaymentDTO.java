package com.hotel.pms.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预付请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class PrepaymentDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 散客预订ID */
    private Long reservationId;
    
    /** 团队预订ID */
    private Long teamReservationId;
    
    /** 预付类型：FULL-全额/PARTIAL-部分/DEPOSIT-押金 */
    @NotBlank(message = "预付类型不能为空")
    private String prepaymentType;
    
    /** 预付金额 */
    @NotNull(message = "预付金额不能为空")
    @DecimalMin(value = "0.01", message = "预付金额必须大于0")
    private BigDecimal amount;
    
    /** 支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    /** 备注 */
    private String remark;
    
    /** 操作员ID */
    private Long operatorId;
}
