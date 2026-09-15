package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 夜审步骤实体类
 */
@Data
@TableName("night_audit_step")
public class NightAuditStep extends BaseEntity {
    
    /** 夜审ID */
    private Long nightAuditId;
    
    /** 步骤名称 */
    private String stepName;
    
    /** 步骤顺序 */
    private Integer stepOrder;
    
    /** 状态: PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED */
    private String status;
    
    /** 开始时间 */
    private LocalDateTime startedAt;
    
    /** 完成时间 */
    private LocalDateTime completedAt;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 重试次数 */
    private Integer retryCount;
}
