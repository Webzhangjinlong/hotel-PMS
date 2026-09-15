package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预订单实体类
 * <p>
 * 对应数据库表：reservation
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservation")
public class Reservation extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 预订号 */
    @TableField("reservation_no")
    private String reservationNo;
    
    /** 来源：WALK_IN/PHONE/OTA */
    @TableField("source")
    private String source;
    
    /** 客人姓名 */
    @TableField("guest_name")
    private String guestName;
    
    /** 客人电话 */
    @TableField("guest_phone")
    private String guestPhone;
    
    /** 身份证号 */
    @TableField("id_card_no")
    private String idCardNo;
    
    /** 房型ID */
    @TableField("room_type_id")
    private Long roomTypeId;
    
    /** 房间ID（预分房时填写） */
    @TableField("room_id")
    private Long roomId;
    
    /** 入住日期 */
    @TableField("check_in_date")
    private LocalDate checkInDate;
    
    /** 离店日期 */
    @TableField("check_out_date")
    private LocalDate checkOutDate;
    
    /** 房晚数 */
    @TableField("nights")
    private Integer nights;
    
    /** 总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /** 状态：PENDING/CONFIRMED/CHECKED_IN/CANCELLED/NO_SHOW */
    @TableField("status")
    private String status;
    
        
    /** 房价码ID */
    @TableField("price_plan_id")
    private Long pricePlanId;
    /** 关联会员ID */
    @TableField("member_id")
    private Long memberId;

    /** 特殊要求 */
    @TableField("special_requests")
    private String specialRequests;

    /** 每日房价（预订实际价格） */
    @TableField("daily_price")
    private BigDecimal dailyPrice;
    
    /** 价格来源：PRICE_PLAN/MANUAL/CUSTOM */
    @TableField("price_source")
    private String priceSource;
}
