package com.hotel.pms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * PoliceUploadRecordVO 视图对象
 */
@Data
public class PoliceUploadRecordVO {

    /** 主键ID */
    private Long id;

    /** 创建时间 */
    private java.time.LocalDateTime createdAt;

    /** 更新时间 */
    private java.time.LocalDateTime updatedAt;

    private Long hotelId;

    private Long stayId;

    private String stayNo;

    private Long guestId;

    private String guestName;

    private String idType;

    private String idNo;

    private String phone;

    private String gender;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private String roomNo;

    private String status;

    private LocalDateTime uploadTime;

    private String errorMessage;

    private Integer retryCount;

    private Boolean isManual;

    private Long operatorId;

    private String remark;

}