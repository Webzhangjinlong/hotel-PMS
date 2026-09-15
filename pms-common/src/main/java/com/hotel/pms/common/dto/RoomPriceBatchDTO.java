package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 房价批量调价请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomPriceBatchDTO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 房型ID列表 */
    @NotEmpty(message = "房型ID列表不能为空")
    private List<Long> roomTypeIds;
    
    /** 开始日期 */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    /** 结束日期 */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    /** 调价方式：FIXED-固定价/PERCENT-涨跌幅/AMOUNT-涨减额 */
    @NotNull(message = "调价方式不能为空")
    private String adjustType;
    
    /** 调价值 */
    @NotNull(message = "调价值不能为空")
    private BigDecimal adjustValue;
}
