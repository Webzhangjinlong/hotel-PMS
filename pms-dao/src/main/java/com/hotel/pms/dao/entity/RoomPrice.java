package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 房价实体类
 * <p>
 * 对应数据库表：room_price
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_price")
public class RoomPrice extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 房型ID */
    @TableField("room_type_id")
    private Long roomTypeId;
    
    /** 价格日期 */
    @TableField("price_date")
    private LocalDate priceDate;
    
    /** 价格 */
    @TableField("price")
    private BigDecimal price;
    
    /** 状态 */
    @TableField("status")
    private String status;
}
