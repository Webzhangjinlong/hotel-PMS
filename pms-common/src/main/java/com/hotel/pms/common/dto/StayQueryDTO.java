package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 入住查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StayQueryDTO extends PageRequest implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 入住状态 */
    private String status;
    
    /** 房间号 */
    private String roomNo;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 入住类型：TEAM-团队/WALK_IN-散客/RESERVATION-预订 */
    private String checkInType;
    
    /** 入住日期-开始 */
    private LocalDate checkInDateStart;
    
    /** 入住日期-结束 */
    private LocalDate checkInDateEnd;
}
