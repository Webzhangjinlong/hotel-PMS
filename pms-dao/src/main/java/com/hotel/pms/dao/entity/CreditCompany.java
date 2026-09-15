package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 挂账公司/协议单位实体类
 * <p>
 * 对应数据库表：credit_company
 * 支持协议客户管理和协议价管理
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("credit_company")
public class CreditCompany extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 公司名称 */
    @TableField("company_name")
    private String companyName;
    
    /** 联系人姓名 */
    @TableField("contact_name")
    private String contactName;
    
    /** 联系人电话 */
    @TableField("contact_phone")
    private String contactPhone;
    
    /** 信用额度 */
    @TableField("credit_limit")
    private BigDecimal creditLimit;
    
    /** 当前余额（挂账金额） */
    @TableField("current_balance")
    private BigDecimal currentBalance;
    
    /** 状态：ACTIVE-活跃/INACTIVE-停用 */
    @TableField("status")
    private String status;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
    
    /** 协议编号 */
    @TableField("agreement_no")
    private String agreementNo;
    
    /** 公司类型：ENTERPRISE-企业/TRAVEL_AGENCY-旅行社/GOVERNMENT-政府/OTHER-其他 */
    @TableField("company_type")
    private String companyType;
    
    /** 协议开始日期 */
    @TableField("agreement_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate agreementStartDate;
    
    /** 协议结束日期 */
    @TableField("agreement_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate agreementEndDate;
    
    /** 协议状态：ACTIVE-有效/EXPIRED-过期/TERMINATED-终止 */
    @TableField("agreement_status")
    private String agreementStatus;
    
    /** 公司地址 */
    @TableField("address")
    private String address;
    
    /** 联系邮箱 */
    @TableField("email")
    private String email;
    
    /** 传真号码 */
    @TableField("fax")
    private String fax;
}
