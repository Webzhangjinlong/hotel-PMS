package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房间实体类
 * <p>
 * 对应数据库表：room
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room")
public class Room extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 房型ID */
    @TableField("room_type_id")
    private Long roomTypeId;
    
    /** 楼层ID */
    @TableField("floor_id")
    private Long floorId;
    
    /** 房间号 */
    @TableField("room_no")
    private String roomNo;
    
    /**
     * 状态
     * @see com.hotel.pms.common.constant.RoomConstants
     */
    @TableField("status")
    private String status;
    
    /** 房间描述 */
    @TableField("description")
    private String description;
}
