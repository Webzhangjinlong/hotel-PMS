package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 夜审查询DTO
 */
@Data
public class NightAuditQueryDTO implements Serializable {
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 状态 */
    private String status;
    
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 结束日期 */
    private LocalDate endDate;
    
    /** 页码 */
    private Integer pageNum = 1;
    
    /** 每页大小 */
    private Integer pageSize = 10;
}