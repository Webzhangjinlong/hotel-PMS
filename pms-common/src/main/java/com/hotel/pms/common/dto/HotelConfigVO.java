package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 酒店配置VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelConfigVO implements Serializable {
    
    /** 配置ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 配置键 */
    private String configKey;
    
    /** 配置值 */
    private String configValue;
    
    /** 配置描述 */
    private String description;
}
