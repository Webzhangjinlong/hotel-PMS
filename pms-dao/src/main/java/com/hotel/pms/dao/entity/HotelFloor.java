package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼层实体类
 * <p>
 * 对应数据库表：hotel_floor
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hotel_floor")
public class HotelFloor extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 楼层号 */
    @TableField("floor_no")
    private Integer floorNo;
    
    /** 楼层名称 */
    @TableField("name")
    private String name;
    
    /**
     * 状态
     * @see com.hotel.pms.common.enums.StatusEnum
     */
    @TableField("status")
    private String status;
}
