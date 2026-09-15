package com.hotel.pms.common.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/** SysShiftDTO */
@Data
public class SysShiftDTO {

    private Long hotelId;

    private String shiftNo;

    private Long operatorId;

    private Long receiverId;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal totalAmount;

    private BigDecimal cashAmount;

    private BigDecimal posAmount;

    private BigDecimal wechatAmount;

    private BigDecimal alipayAmount;

    private BigDecimal creditAmount;

    private BigDecimal refundAmount;

    private BigDecimal actualCash;

    private BigDecimal actualPos;

    private BigDecimal actualWechat;

    private BigDecimal actualAlipay;

    private Integer checkinCount;

    private Integer checkoutCount;

    private Integer transactionCount;

    private LocalDateTime submitTime;

    private LocalDateTime acceptTime;

    private LocalDateTime confirmTime;

    private LocalDateTime rejectTime;

    private String rejectReason;

    private String remark;

}