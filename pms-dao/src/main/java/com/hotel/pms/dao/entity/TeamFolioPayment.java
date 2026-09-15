package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team_folio_payment")
public class TeamFolioPayment extends BaseEntity {
    
    @TableField("hotel_id")
    private Long hotelId;
    
    @TableField("team_folio_id")
    private Long teamFolioId;
    
    @TableField("payment_no")
    private String paymentNo;
    
    @TableField("amount")
    private BigDecimal amount;
    
    @TableField("payment_method")
    private String paymentMethod;
    
    @TableField("payment_time")
    private LocalDateTime paymentTime;
    
    @TableField("operator_id")
    private Long operatorId;
    
    @TableField("operator_name")
    private String operatorName;
    
    @TableField("remark")
    private String remark;
}