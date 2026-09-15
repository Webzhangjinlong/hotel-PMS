package com.hotel.pms.common.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 账务统计VO
 */
@Data
public class FolioStatsVO {
    
    /** 总金额 */
    private BigDecimal totalAmount;
    
    /** 已收金额 */
    private BigDecimal paidAmount;
    
    /** 待收金额 */
    private BigDecimal balance;
    
    /** 账务笔数 */
    private Integer count;
}