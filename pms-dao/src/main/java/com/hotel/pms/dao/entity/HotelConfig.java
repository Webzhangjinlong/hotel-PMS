package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 酒店配置实体类
 * <p>
 * 对应数据库表：hotel_config
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hotel_config")
public class HotelConfig extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 配置键 */
    @TableField("config_key")
    private String configKey;
    
    /** 配置值 */
    @TableField("config_value")
    private String configValue;
    
    /** 配置描述 */
    @TableField("description")
    private String description;
}
