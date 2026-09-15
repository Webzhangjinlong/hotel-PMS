package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * 夜审配置DTO
 */
@Data
public class NightAuditConfigDTO implements Serializable {
    
    /** 夜审时间 */
    private LocalTime auditTime;
    
    /** 是否启用自动夜审 */
    private Boolean autoAuditEnabled;
    
    /** 启用的步骤列表 */
    private List<String> enabledSteps;
    
    /** 通知配置 */
    private NotificationConfig notification;
    
    /**
     * 通知配置
     */
    @Data
    public static class NotificationConfig {
        
        /** 是否启用通知 */
        private Boolean enabled;
        
        /** 夜审完成时是否通知 */
        private Boolean notifyOnComplete;
        
        /** 夜审失败时是否通知 */
        private Boolean notifyOnFailure;
        
        /** 通知邮箱列表 */
        private List<String> emailRecipients;
    }
}