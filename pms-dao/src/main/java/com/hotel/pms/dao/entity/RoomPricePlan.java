package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 房价码实体类
 * <p>
 * 对应数据库表：room_price_plan
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_price_plan")
public class RoomPricePlan extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 房价码编码 */
    @TableField("code")
    private String code;
    
    /** 房价码名称 */
    @TableField("name")
    private String name;
    
    /** 有效期开始 */
    @TableField("valid_from")
    private LocalDate validFrom;
    
    /** 有效期结束 */
    @TableField("valid_to")
    private LocalDate validTo;
    
    /** 描述 */
    @TableField("description")
    private String description;
    
    /** 状态 */
    @TableField("status")
    private String status;
}