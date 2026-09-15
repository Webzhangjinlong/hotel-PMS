package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 挂账公司/协议单位查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreditCompanyQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 公司名称（模糊查询） */
    private String companyName;
    
    /** 状态：ACTIVE-活跃/INACTIVE-停用 */
    private String status;
    
    /** 公司类型：ENTERPRISE-企业/TRAVEL_AGENCY-旅行社/GOVERNMENT-政府/OTHER-其他 */
    private String companyType;
    
    /** 协议状态：ACTIVE-有效/EXPIRED-过期/TERMINATED-终止 */
    private String agreementStatus;
    
    /** 协议编号 */
    private String agreementNo;
}
