package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 换房响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoomVO implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 入住单号 */
    private String stayNo;
    
    /** 原房间ID */
    private Long oldRoomId;
    
    /** 原房间号 */
    private String oldRoomNo;
    
    /** 新房间ID */
    private Long newRoomId;
    
    /** 新房间号 */
    private String newRoomNo;
    
    /** 新房型ID */
    private Long newRoomTypeId;
    
    /** 新房型名称 */
    private String newRoomTypeName;
    
    /** 换房原因 */
    private String reason;
    
    /** 费用调整金额（正数为加收，负数为退还） */
    private BigDecimal amountAdjustment;
    
    /** 新总金额 */
    private BigDecimal newTotalAmount;
}
