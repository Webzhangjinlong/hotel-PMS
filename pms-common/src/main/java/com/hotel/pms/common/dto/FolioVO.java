package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账务单响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class FolioVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 账务单ID */
    private Long id;
    
    /** 账务单号 */
    private String folioNo;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 入住单号 */
    private String stayNo;
    
    /** 客人ID */
    private Long guestId;
    
    /** 客人姓名 */
    private String guestName;

    /** 客人电话 */
    private String guestPhone;
    
    /** 总金额 */
    private BigDecimal totalAmount;
    
    /** 已付金额 */
    private BigDecimal paidAmount;
    
    /** 余额 */
    private BigDecimal balance;
    
    /** 状态：OPEN-开放/CLOSED-关闭 */
    private String status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 交易记录列表 */
    private List<TransactionVO> transactions;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}

