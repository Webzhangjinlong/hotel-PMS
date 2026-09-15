package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 协议价实体类
 * <p>
 * 对应数据库表：agreement_price
 * 存储协议单位的房型协议价格
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agreement_price")
public class AgreementPrice extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 协议公司ID */
    @TableField("credit_company_id")
    private Long creditCompanyId;
    
    /** 房型ID */
    @TableField("room_type_id")
    private Long roomTypeId;
    
    /** 协议价格 */
    @TableField("price")
    private BigDecimal price;
    
    /** 折扣比例（100表示原价，90表示9折） */
    @TableField("discount_rate")
    private BigDecimal discountRate;
    
    /** 有效期开始 */
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    /** 有效期结束 */
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    /** 状态：ACTIVE-有效/INACTIVE-停用 */
    @TableField("status")
    private String status;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
}
