package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 房价码房型明细实体类
 * <p>
 * 对应数据库表：room_price_plan_detail
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_price_plan_detail")
public class RoomPricePlanDetail extends BaseEntity {
    
    /** 房价码ID */
    @TableField("plan_id")
    private Long planId;
    
    /** 房型ID */
    @TableField("room_type_id")
    private Long roomTypeId;
    
    /** 基础价格 */
    @TableField("base_price")
    private BigDecimal basePrice;
    
    /** 折扣方式：NONE-无折扣/PERCENT-百分比/FIXED-固定价 */
    @TableField("discount_type")
    private String discountType;
    
    /** 折扣值 */
    @TableField("discount_value")
    private BigDecimal discountValue;
    
    /** 最终价格 */
    @TableField("final_price")
    private BigDecimal finalPrice;
}