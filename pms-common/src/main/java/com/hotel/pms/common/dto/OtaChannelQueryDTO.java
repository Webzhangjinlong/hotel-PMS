package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * OTA渠道查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OtaChannelQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 渠道名称 */
    private String channelName;
    
    /** 渠道编码 */
    private String channelCode;
    
    /** 渠道类型：OTA-在线旅行社/DIRECT-直销/CORPORATE-协议单位 */
    private String channelType;
    
    /** 状态：ACTIVE-启用/INACTIVE-停用 */
    private String status;
}
