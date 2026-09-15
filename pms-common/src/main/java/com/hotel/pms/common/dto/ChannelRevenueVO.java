package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 渠道收入占比VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRevenueVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 渠道名称 */
    private String channelName;
    
    /** 渠道代码 */
    private String channelCode;
    
    /** 收入金额 */
    private BigDecimal revenue;
    
    /** 占比百分比 */
    private BigDecimal percentage;
    
    /** 订单数量 */
    private Integer orderCount;
    
    /** 平均房价 */
    private BigDecimal avgRate;
}
