package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 协议价请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class AgreementPriceDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 协议公司ID */
    private Long creditCompanyId;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 协议价格 */
    private BigDecimal price;
    
    /** 折扣比例（100表示原价，90表示9折） */
    private BigDecimal discountRate;
    
    /** 有效期开始 */
    private LocalDate startDate;
    
    /** 有效期结束 */
    private LocalDate endDate;
    
    /** 状态：ACTIVE-有效/INACTIVE-停用 */
    private String status;
    
    /** 备注 */
    private String remark;
}
