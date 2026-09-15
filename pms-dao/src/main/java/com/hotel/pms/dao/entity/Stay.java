package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入住单实体类
 * <p>
 * 对应数据库表：stay
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stay")
public class Stay extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 入住单号 */
    @TableField("stay_no")
    private String stayNo;
    
    /** 预订ID */
    @TableField("reservation_id")
    private Long reservationId;
    
    /** 团队预订ID（团队入住时关联） */
    @TableField("team_reservation_id")
    private Long teamReservationId;
    
    /** 房间ID */
    @TableField("room_id")
    private Long roomId;
    
    /** 客人ID */
    @TableField("guest_id")
    private Long guestId;
    
    /** 入住时间 */
    @TableField("check_in_time")
    private LocalDateTime checkInTime;
    
    /** 预计离店时间 */
    @TableField("check_out_time")
    private LocalDateTime checkOutTime;
    
    /** 实际离店时间 */
    @TableField("actual_check_out_time")
    private LocalDateTime actualCheckOutTime;
    
    /** 状态：CHECKED_IN-在住/CHECKED_OUT-已离店 */
    @TableField("status")
    private String status;
    
    /** 总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /** 已付金额 */
    @TableField("paid_amount")
    private BigDecimal paidAmount;
    
    /** 房价码ID */
    @TableField("price_plan_id")
    private Long pricePlanId;
    
    /** 关联会员ID */
    @TableField("member_id")
    private Long memberId;

    /** 入住类型：INDIVIDUAL-散客/TEAM-团队 */
    @TableField("check_in_type")
    private String checkInType;
    
    /** 是否锁定 */
    @TableField("is_locked")
    private Boolean isLocked;
    
    /** 锁定的夜审ID */
    @TableField("locked_by_audit_id")
    private Long lockedByAuditId;
}
