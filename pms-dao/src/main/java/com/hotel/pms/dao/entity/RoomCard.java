package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 房卡记录实体类
 * <p>
 * 对应数据库表：room_card
 * 存储房卡的信息，包括卡号、关联房间、有效期等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_card")
public class RoomCard extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 设备ID */
    @TableField("device_id")
    private Long deviceId;
    
    /** 卡号 */
    @TableField("card_no")
    private String cardNo;
    
    /** 卡类型：GUEST_CARD-客人卡/STAFF_CARD-员工卡/MASTER_CARD-总控卡 */
    @TableField("card_type")
    private String cardType;
    
    /** 房间ID */
    @TableField("room_id")
    private Long roomId;
    
    /** 房间号 */
    @TableField("room_no")
    private String roomNo;
    
    /** 客人ID */
    @TableField("guest_id")
    private Long guestId;
    
    /** 客人姓名 */
    @TableField("guest_name")
    private String guestName;
    
    /** 入住单ID */
    @TableField("stay_id")
    private Long stayId;
    
    /** 入住单号 */
    @TableField("stay_no")
    private String stayNo;
    
    /** 有效期开始 */
    @TableField("valid_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validStart;
    
    /** 有效期结束 */
    @TableField("valid_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validEnd;
    
    /** 卡状态：ACTIVE-有效/EXPIRED-过期/LOST-挂失/CANCELLED-注销 */
    @TableField("card_status")
    private String cardStatus;
    
    /** 操作员ID */
    @TableField("operator_id")
    private Long operatorId;
    
    /** 操作员姓名 */
    @TableField("operator_name")
    private String operatorName;
}
