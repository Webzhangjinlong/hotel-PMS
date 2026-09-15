package com.hotel.pms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SysPermissionVO 视图对象
 */
@Data
public class SysPermissionVO {

    /** 主键ID */
    private Long id;

    /** 创建时间 */
    private java.time.LocalDateTime createdAt;

    /** 更新时间 */
    private java.time.LocalDateTime updatedAt;

    private String permissionName;

    private String permissionCode;

    private String resourceType;

    private String resourcePath;

    private Long parentId;

    private Integer sortOrder;

    private String icon;

    private Boolean isVisible;

    private String status;

}