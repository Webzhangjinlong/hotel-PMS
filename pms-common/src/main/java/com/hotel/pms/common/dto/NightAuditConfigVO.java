package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * 夜审配置VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NightAuditConfigVO implements Serializable {
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 夜审时间 */
    private LocalTime auditTime;
    
    /** 是否启用自动夜审 */
    private Boolean autoAuditEnabled;
    
    /** 启用的步骤列表 */
    private List<String> enabledSteps;
    
    /** 所有可用的步骤列表 */
    private List<StepInfo> availableSteps;
    
    /** 通知配置 */
    private NotificationConfig notification;
    
    /**
     * 步骤信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepInfo {
        
        /** 步骤名称 */
        private String stepName;
        
        /** 步骤显示名称 */
        private String displayName;
        
        /** 是否启用 */
        private Boolean enabled;
        
        /** 步骤描述 */
        private String description;
    }
    
    /**
     * 通知配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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