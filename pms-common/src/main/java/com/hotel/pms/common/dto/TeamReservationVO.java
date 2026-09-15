package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 团队预订响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class TeamReservationVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 酒店名称 */
    private String hotelName;
    
    /** 团队预订号 */
    private String teamReservationNo;
    
    /** 团队/公司名称 */
    private String teamName;
    
    /** 主联系人姓名 */
    private String contactName;
    
    /** 主联系人电话 */
    private String contactPhone;
    
    /** 主联系人证件号 */
    private String contactIdNo;
    
    /** 入住日期 */
    private LocalDate checkInDate;
    
    /** 离店日期 */
    private LocalDate checkOutDate;
    
    /** 房晚数 */
    private Integer nights;
    
    /** 总房间数 */
    private Integer totalRooms;
    
    /** 总金额 */
    private BigDecimal totalAmount;
    
    /** 结算方式 */
    private String settlementType;
    
    /** 状态 */
    private String status;
    
    /** 特殊要求 */
    private String specialRequests;
    
    /** 房价码ID */
    private Long pricePlanId;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 房间明细 */
    private List<TeamReservationRoomVO> rooms;
}
