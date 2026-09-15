package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 房卡查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoomCardQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 设备ID */
    private Long deviceId;
    
    /** 卡号 */
    private String cardNo;
    
    /** 卡类型：GUEST_CARD-客人卡/STAFF_CARD-员工卡/MASTER_CARD-总控卡 */
    private String cardType;
    
    /** 房间ID */
    private Long roomId;
    
    /** 房间号 */
    private String roomNo;
    
    /** 客人ID */
    private Long guestId;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 卡状态：ACTIVE-有效/EXPIRED-过期/LOST-挂失/CANCELLED-注销 */
    private String cardStatus;
}
