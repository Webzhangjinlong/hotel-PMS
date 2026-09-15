package com.hotel.pms.common.dto;

import lombok.Data;

/**
 * 酒店查询DTO
 */
@Data
public class HotelQueryDTO {
    
    /** 酒店名称（模糊查询） */
    private String name;
    
    /** 酒店状态 */
    private String status;
    
    /** 页码 */
    private Integer pageNum = 1;
    
    /** 每页数量 */
    private Integer pageSize = 10;
}