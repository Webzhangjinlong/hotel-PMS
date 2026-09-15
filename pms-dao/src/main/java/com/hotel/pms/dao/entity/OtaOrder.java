package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OTA订单实体类
 * <p>
 * 对应数据库表：ota_order
 * 存储从OTA渠道接收的订单信息
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ota_order")
public class OtaOrder extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 渠道ID */
    @TableField("channel_id")
    private Long channelId;
    
    /** OTA订单号 */
    @TableField("ota_order_no")
    private String otaOrderNo;
    
    /** OTA预订号 */
    @TableField("ota_reservation_no")
    private String otaReservationNo;
    
    /** 关联预订ID */
    @TableField("reservation_id")
    private Long reservationId;
    
    /** 关联入住单ID */
    @TableField("stay_id")
    private Long stayId;
    
    /** 客人姓名 */
    @TableField("guest_name")
    private String guestName;
    
    /** 客人电话 */
    @TableField("guest_phone")
    private String guestPhone;
    
    /** 客人邮箱 */
    @TableField("guest_email")
    private String guestEmail;
    
    /** 证件类型 */
    @TableField("guest_id_type")
    private String guestIdType;
    
    /** 证件号码 */
    @TableField("guest_id_no")
    private String guestIdNo;
    
    /** 房型ID */
    @TableField("room_type_id")
    private Long roomTypeId;
    
    /** 房型名称 */
    @TableField("room_type_name")
    private String roomTypeName;
    
    /** 房间数量 */
    @TableField("room_count")
    private Integer roomCount;
    
    /** 入住日期 */
    @TableField("check_in_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    
    /** 离店日期 */
    @TableField("check_out_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    
    /** 入住天数 */
    @TableField("nights")
    private Integer nights;
    
    /** 房价 */
    @TableField("room_price")
    private BigDecimal roomPrice;
    
    /** 总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /** 佣金比例 */
    @TableField("commission_rate")
    private BigDecimal commissionRate;
    
    /** 佣金金额 */
    @TableField("commission_amount")
    private BigDecimal commissionAmount;
    
    /** 净收入 */
    @TableField("net_amount")
    private BigDecimal netAmount;
    
    /** 订单状态：PENDING-待确认/CONFIRMED-已确认/CHECKED_IN-已入住/CANCELLED-已取消/NO_SHOW-未到店 */
    @TableField("order_status")
    private String orderStatus;
    
    /** 支付状态：UNPAID-未支付/PAID-已支付/REFUNDED-已退款 */
    @TableField("payment_status")
    private String paymentStatus;
    
    /** 特殊要求 */
    @TableField("special_requests")
    private String specialRequests;
    
    /** 原始数据（JSON格式） */
    @TableField("raw_data")
    private String rawData;
}
