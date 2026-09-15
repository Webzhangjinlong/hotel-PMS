package com.hotel.pms.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 挂账公司/协议单位请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class CreditCompanyDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 公司名称 */
    @NotBlank(message = "公司名称不能为空")
    private String companyName;
    
    /** 联系人姓名 */
    private String contactName;
    
    /** 联系人电话 */
    private String contactPhone;
    
    /** 信用额度 */
    @DecimalMin(value = "0", message = "信用额度不能为负数")
    private BigDecimal creditLimit;
    
    /** 状态：ACTIVE-活跃/INACTIVE-停用 */
    private String status;
    
    /** 备注 */
    private String remark;
    
    /** 协议编号 */
    private String agreementNo;
    
    /** 公司类型：ENTERPRISE-企业/TRAVEL_AGENCY-旅行社/GOVERNMENT-政府/OTHER-其他 */
    private String companyType;
    
    /** 协议开始日期 */
    private LocalDate agreementStartDate;
    
    /** 协议结束日期 */
    private LocalDate agreementEndDate;
    
    /** 协议状态：ACTIVE-有效/EXPIRED-过期/TERMINATED-终止 */
    private String agreementStatus;
    
    /** 公司地址 */
    private String address;
    
    /** 联系邮箱 */
    private String email;
    
    /** 传真号码 */
    private String fax;
}
