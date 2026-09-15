package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 房型实体类
 * <p>
 * 对应数据库表：room_type
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_type")
public class RoomType extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 房型名称 */
    @TableField("name")
    private String name;
    
    /** 房型编码 */
    @TableField("code")
    private String code;
    
    /** 床型 */
    @TableField("bed_type")
    private String bedType;
    
    /** 最大入住人数 */
    @TableField("max_guests")
    private Integer maxGuests;
    
    /** 基础价格 */
    @TableField("base_price")
    private BigDecimal basePrice;
    
    /** 房型描述 */
    @TableField("description")
    private String description;
    
    /**
     * 状态
     * @see com.hotel.pms.common.enums.StatusEnum
     */
    @TableField("status")
    private String status;
}
