package com.hotel.pms.common.dto;

import lombok.Data;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * NightAuditArchiveVO 视图对象
 */
@Data
public class NightAuditArchiveVO {

    /** 主键ID */
    private Long id;

    /** 创建时间 */
    private java.time.LocalDateTime createdAt;

    /** 更新时间 */
    private java.time.LocalDateTime updatedAt;

    /** 原始ID */
    private Long originalId;

    /** 酒店ID */
    private Long hotelId;

    /** 审计日期 */
    private LocalDate auditDate;

    /** 状态 */
    private String status;

    /** 总房间数 */
    private Integer totalRooms;

    /** 在住房间数 */
    private Integer occupiedRooms;

    /** 可用房间数 */
    private Integer availableRooms;

    /** 总收入 */
    private BigDecimal totalRevenue;

    /** 房费收入 */
    private BigDecimal roomRevenue;

    /** 其他收入 */
    private BigDecimal extraRevenue;

    /** 入住率 */
    private BigDecimal occupancyRate;

    /** 平均每日房价 */
    private BigDecimal adr;

    /** 每间可售房收入 */
    private BigDecimal revpar;

    /** 开始时间 */
    private LocalDateTime startedAt;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 错误信息 */
    private String errorMessage;

    /** 归档时间 */
    private LocalDateTime archivedAt;

    /** 归档人 */
    private String archivedBy;

}