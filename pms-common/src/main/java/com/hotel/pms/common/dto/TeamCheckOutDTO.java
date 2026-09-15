package com.hotel.pms.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 团队退房请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class TeamCheckOutDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 需要退房的房间明细ID列表（为空则全部退房） */
    private List<Long> roomIds;
}
