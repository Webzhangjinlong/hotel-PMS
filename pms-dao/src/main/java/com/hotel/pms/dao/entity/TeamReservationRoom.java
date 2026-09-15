package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团队预订房间明细实体类
 * <p>
 * 对应数据库表：team_reservation_room
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team_reservation_room")
public class TeamReservationRoom extends BaseEntity {
    
    /** 团队预订ID */
    @TableField("team_reservation_id")
    private Long teamReservationId;
    
    /** 房型ID */
    @TableField("room_type_id")
    private Long roomTypeId;
    
    /** 房间ID（可为空，入住时分配） */
    @TableField("room_id")
    private Long roomId;
    
    /** 客人姓名 */
    @TableField("guest_name")
    private String guestName;
    
    /** 客人电话 */
    @TableField("guest_phone")
    private String guestPhone;
    
    /** 客人证件号 */
    @TableField("guest_id_no")
    private String guestIdNo;
    
    /** 客人性别 */
    @TableField("guest_gender")
    private String guestGender;
    
    /** 该房间金额 */
    @TableField("amount")
    private BigDecimal amount;
    
    /** 状态：PENDING/CHECKED_IN/CHECKED_OUT */
    @TableField("status")
    private String status;
    
    /** 入住单ID（入住后关联） */
    @TableField("stay_id")
    private Long stayId;
    
    /** 实际入住时间 */
    @TableField("check_in_time")
    private LocalDateTime checkInTime;
    
    /** 实际退房时间 */
    @TableField("check_out_time")
    private LocalDateTime checkOutTime;
}
