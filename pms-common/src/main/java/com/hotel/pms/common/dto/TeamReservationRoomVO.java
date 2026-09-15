package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团队预订房间明细VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class TeamReservationRoomVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** ID */
    private Long id;
    
    /** 团队预订ID */
    private Long teamReservationId;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 房型名称 */
    private String roomTypeName;
    
    /** 房间ID */
    private Long roomId;
    
    /** 房间号 */
    private String roomNo;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 客人电话 */
    private String guestPhone;
    
    /** 客人证件号 */
    private String guestIdNo;
    
    /** 客人性别 */
    private String guestGender;
    
    /** 金额 */
    private BigDecimal amount;
    
    /** 状态 */
    private String status;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 实际入住时间 */
    private LocalDateTime checkInTime;
    
    /** 实际退房时间 */
    private LocalDateTime checkOutTime;
}
