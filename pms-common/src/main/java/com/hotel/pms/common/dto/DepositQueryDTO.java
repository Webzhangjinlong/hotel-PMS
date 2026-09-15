package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 押金查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class DepositQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 页码 */
    private Integer page = 1;
    
    /** 每页条数 */
    private Integer size = 10;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 押金状态 */
    private String status;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 房间号 */
    private String roomNo;
    
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 结束日期 */
    private LocalDate endDate;
}
