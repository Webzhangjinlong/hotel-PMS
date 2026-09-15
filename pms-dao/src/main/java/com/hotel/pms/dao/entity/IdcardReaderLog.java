package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 身份证阅读器设备日志实体类
 * <p>
 * 对应数据库表：idcard_reader_log
 * 存储身份证阅读器的设备日志
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("idcard_reader_log")
public class IdcardReaderLog extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 设备ID */
    @TableField("device_id")
    private Long deviceId;
    
    /** 日志类型：CONNECT-连接/READ-读取/ERROR-错误/INFO-信息 */
    @TableField("log_type")
    private String logType;
    
    /** 日志内容 */
    @TableField("log_content")
    private String logContent;
    
    /** 日志级别：INFO-信息/WARN-警告/ERROR-错误 */
    @TableField("log_level")
    private String logLevel;
}
