package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类（用于 AOP 记录）
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@TableName("operation_log")
public class OperationLogEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 操作人ID */
    @TableField("operator_id")
    private Long operatorId;
    
    /** 操作人姓名 */
    @TableField("operator_name")
    private String operatorName;
    
    /** 所属模块 */
    @TableField("module")
    private String module;
    
    /** 操作类型 */
    @TableField("action")
    private String action;
    
    /** 目标类型 */
    @TableField("target_type")
    private String targetType;
    
    /** 目标ID */
    @TableField("target_id")
    private Long targetId;
    
    /** 操作内容 */
    @TableField("content")
    private String content;
    
    /** IP地址 */
    @TableField("ip_address")
    private String ipAddress;
    
    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
