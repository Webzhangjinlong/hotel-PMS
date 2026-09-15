package com.hotel.pms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * OperationLogVO 视图对象
 */
@Data
public class OperationLogVO {

    /** 主键ID */
    private Long id;

    /** 创建时间 */
    private java.time.LocalDateTime createdAt;

    /** 更新时间 */
    private java.time.LocalDateTime updatedAt;

    private Long hotelId;

    private Long operatorId;

    private String operatorName;

    private String module;

    private String action;

    private String targetType;

    private Long targetId;

    private String content;

    private String ipAddress;

}