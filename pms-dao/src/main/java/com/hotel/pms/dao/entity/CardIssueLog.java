package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 发卡记录实体类
 * <p>
 * 对应数据库表：card_issue_log
 * 存储发卡操作的日志记录
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("card_issue_log")
public class CardIssueLog extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 设备ID */
    @TableField("device_id")
    private Long deviceId;
    
    /** 房卡ID */
    @TableField("card_id")
    private Long cardId;
    
    /** 发卡类型：NEW-新卡/REPLACE-补卡/EXTEND-续卡/CANCEL-注销 */
    @TableField("issue_type")
    private String issueType;
    
    /** 发卡时间 */
    @TableField("issue_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issueTime;
    
    /** 结果状态：SUCCESS-成功/FAILED-失败 */
    @TableField("result_status")
    private String resultStatus;
    
    /** 结果信息 */
    @TableField("result_message")
    private String resultMessage;
    
    /** 操作员ID */
    @TableField("operator_id")
    private Long operatorId;
    
    /** 操作员姓名 */
    @TableField("operator_name")
    private String operatorName;
}
