package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 客人查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GuestQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 客人姓名（模糊查询） */
    private String name;
    
    /** 手机号 */
    private String phone;
    
    /** 证件号码 */
    private String idNo;
    
    /** 是否常住客人 */
    private Boolean isVip;
    
    /** 是否黑名单 */
    private Boolean isBlacklisted;
}
