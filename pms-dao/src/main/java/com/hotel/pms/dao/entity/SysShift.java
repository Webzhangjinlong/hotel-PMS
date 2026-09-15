package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_shift")
public class SysShift extends BaseEntity {
    
    @TableField("hotel_id")
    private Long hotelId;
    
    @TableField("shift_no")
    private String shiftNo;
    
    @TableField("operator_id")
    private Long operatorId;
    
    @TableField("receiver_id")
    private Long receiverId;
    
    @TableField("status")
    private String status;
    
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("cash_amount")
    private BigDecimal cashAmount;
    
    @TableField("pos_amount")
    private BigDecimal posAmount;
    
    @TableField("wechat_amount")
    private BigDecimal wechatAmount;
    
    @TableField("alipay_amount")
    private BigDecimal alipayAmount;
    
    @TableField("credit_amount")
    private BigDecimal creditAmount;
    
    @TableField("refund_amount")
    private BigDecimal refundAmount;
    
    @TableField("actual_cash")
    private BigDecimal actualCash;
    
    @TableField("actual_pos")
    private BigDecimal actualPos;
    
    @TableField("actual_wechat")
    private BigDecimal actualWechat;
    
    @TableField("actual_alipay")
    private BigDecimal actualAlipay;
    
    @TableField("checkin_count")
    private Integer checkinCount;
    
    @TableField("checkout_count")
    private Integer checkoutCount;
    
    @TableField("transaction_count")
    private Integer transactionCount;
    
    @TableField("submit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    @TableField("accept_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;
    
    @TableField("confirm_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;
    
    @TableField("reject_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectTime;
    
    @TableField("reject_reason")
    private String rejectReason;
    
    @TableField("remark")
    private String remark;
}
