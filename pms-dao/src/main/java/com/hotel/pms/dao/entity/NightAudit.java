package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 夜审实体类
 */
@Data
@TableName("night_audit")
public class NightAudit extends BaseEntity {
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 审计日期 */
    private LocalDate auditDate;
    
    /** 状态: PENDING, IN_PROGRESS, COMPLETED, FAILED, COMPLETED_WITH_ERRORS */
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
    
    /** 平均每日房价 (ADR) */
    private BigDecimal adr;
    
    /** 每间可售房收入 (RevPAR) */
    private BigDecimal revpar;
    
    /** 开始时间 */
    private LocalDateTime startedAt;
    
    /** 完成时间 */
    private LocalDateTime completedAt;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 版本号 */
    private Integer version;
}
