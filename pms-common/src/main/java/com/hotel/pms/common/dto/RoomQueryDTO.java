package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 房间查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoomQueryDTO extends PageRequest implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 房型ID */
    private Long roomTypeId;
    
    /** 楼层ID */
    private Long floorId;
    
    /** 房间号（模糊查询） */
    private String roomNo;
    
    /** 状态 */
    private String status;
}
