package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 团队预订单实体类
 * <p>
 * 对应数据库表：team_reservation
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team_reservation")
public class TeamReservation extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 团队预订号 */
    @TableField("team_reservation_no")
    private String teamReservationNo;
    
    /** 团队/公司名称 */
    @TableField("team_name")
    private String teamName;
    
    /** 主联系人姓名 */
    @TableField("contact_name")
    private String contactName;
    
    /** 主联系人电话 */
    @TableField("contact_phone")
    private String contactPhone;
    
    /** 主联系人证件号 */
    @TableField("contact_id_no")
    private String contactIdNo;
    
    /** 入住日期 */
    @TableField("check_in_date")
    private LocalDate checkInDate;
    
    /** 离店日期 */
    @TableField("check_out_date")
    private LocalDate checkOutDate;
    
    /** 房晚数 */
    @TableField("nights")
    private Integer nights;
    
    /** 总房间数 */
    @TableField("total_rooms")
    private Integer totalRooms;
    
    /** 总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /** 结算方式：UNIFIED-统一结算/SEPARATE-分开结算 */
    @TableField("settlement_type")
    private String settlementType;
    
    /** 来源/渠道 */
    @TableField("source")
    private String source;
    
    /** 状态：PENDING/CONFIRMED/CHECKED_IN/CHECKED_OUT/CANCELLED */
    @TableField("status")
    private String status;
    
    /** 特殊要求 */
    @TableField("special_requests")
    private String specialRequests;
    
    /** 房价码ID */
    @TableField("price_plan_id")
    private Long pricePlanId;
}
