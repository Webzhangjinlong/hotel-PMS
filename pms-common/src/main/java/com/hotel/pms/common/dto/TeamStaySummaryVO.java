package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 团队入住汇总VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStaySummaryVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 团队预订ID */
    private Long teamReservationId;
    
    /** 团队预订号 */
    private String teamReservationNo;
    
    /** 团队名称 */
    private String teamName;
    
    /** 结算方式：UNIFIED-统一结算/SEPARATE-分开结算 */
    private String settlementType;
    
    /** 联系人姓名 */
    private String contactName;
    
    /** 联系人电话 */
    private String contactPhone;
    
    /** 预订来源/渠道 */
    private String source;
    
    /** 总房间数 */
    private Integer totalRooms;
    
    /** 总金额 */
    private BigDecimal totalAmount;
    
    /** 已付金额 */
    private BigDecimal paidAmount;
    
    /** 支付状态 */
    private String paymentStatus;
    
    /** 团队状态：CHECKED_IN-在住/CHECKED_OUT-已离店 */
    private String status;
    
    /** 团队入住单列表 */
    private List<StayVO> stays;
}
