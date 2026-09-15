package com.hotel.pms.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 押金收取请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class DepositCollectDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 预订ID */
    private Long reservationId;
    
    /** 客人ID */
    @NotNull(message = "客人ID不能为空")
    private Long guestId;
    
    /** 客人姓名 */
    @NotBlank(message = "客人姓名不能为空")
    private String guestName;
    
    /** 房间号 */
    private String roomNo;
    
    /** 押金金额 */
    @NotNull(message = "押金金额不能为空")
    @DecimalMin(value = "0.01", message = "押金金额必须大于0")
    private BigDecimal amount;
    
    /** 支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    /** 备注 */
    private String remark;
    
    /** 操作员ID */
    private Long operatorId;
}
