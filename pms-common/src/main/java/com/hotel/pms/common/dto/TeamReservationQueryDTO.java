package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 团队预订查询DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class TeamReservationQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 团队预订号 */
    private String teamReservationNo;
    
    /** 团队名称（模糊查询） */
    private String teamName;
    
    /** 联系人姓名 */
    private String contactName;
    
    /** 联系人电话 */
    private String contactPhone;
    
    /** 状态 */
    private String status;
    
    /** 入住日期-开始 */
    private LocalDate checkInDateStart;
    
    /** 入住日期-结束 */
    private LocalDate checkInDateEnd;
    
    /** 页码 */
    private Integer page = 1;
    
    /** 每页大小 */
    private Integer size = 10;
}
