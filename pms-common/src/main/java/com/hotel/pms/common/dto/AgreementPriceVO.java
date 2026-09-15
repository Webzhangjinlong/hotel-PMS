package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 协议价响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementPriceVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 协议价ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 协议公司ID */
    private Long creditCompanyId;
    
    /** 协议公司名称 */
    private String companyName;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 房型名称 */
    private String roomTypeName;
    
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
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
