package com.hotel.pms.common.dto;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 夜审步骤VO
 */
@Data
@Builder
public class NightAuditStepVO implements Serializable {
    
    /** 步骤ID */
    private Long id;
    
    /** 步骤名称 */
    private String stepName;
    
    /** 步骤顺序 */
    private Integer stepOrder;
    
    /** 状态 */
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
