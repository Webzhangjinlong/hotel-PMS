package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 团队预订更新请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class TeamReservationUpdateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 团队/公司名称 */
    private String teamName;
    
    /** 主联系人姓名 */
    private String contactName;
    
    /** 主联系人电话 */
    private String contactPhone;
    
    /** 主联系人证件号 */
    private String contactIdNo;
    
    /** 结算方式 */
    private String settlementType;
    
    /** 特殊要求 */
    private String specialRequests;
    
    /** 房间明细列表（可选，提供则替换原有明细） */
    private List<TeamReservationCreateDTO.RoomDetail> rooms;
}
