package com.hotel.pms.common.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 操作日志查询DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class OperationLogQueryDTO {
    
    /** 页码 */
    private Integer page = 1;
    
    /** 每页条数 */
    private Integer size = 20;
    
    /** 操作模块 */
    private String module;
    
    /** 操作类型 */
    private String action;
    
    /** 操作人姓名 */
    private String operatorName;
    
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 结束日期 */
    private LocalDate endDate;
}
