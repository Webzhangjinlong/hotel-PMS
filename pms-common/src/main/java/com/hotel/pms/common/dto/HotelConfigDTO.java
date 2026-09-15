package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 酒店配置DTO
 */
@Data
public class HotelConfigDTO implements Serializable {
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 配置项Map */
    private Map<String, String> configs;
}
