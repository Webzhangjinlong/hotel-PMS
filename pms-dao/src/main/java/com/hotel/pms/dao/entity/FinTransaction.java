package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易流水实体类
 * <p>
 * 对应数据库表：fin_transaction
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_transaction")
public class FinTransaction extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 交易号 */
    @TableField("transaction_no")
    private String transactionNo;
    
    /** 账务单ID */
    @TableField("folio_id")
    private Long folioId;
    
    /** 交易类型：DEPOSIT-押金/ROOM_FEE-房费/EXTRA-杂费/PAYMENT-付款/REFUND-退款/REVERSAL-冲账 */
    @TableField("type")
    private String type;
    
    /** 交易金额 */
    @TableField("amount")
    private BigDecimal amount;
    
    /** 支付方式：CASH/WECHAT/ALIPAY/POS/BANK_TRANSFER/CREDIT */
    @TableField("payment_method")
    private String paymentMethod;
    
    /** 交易描述 */
    @TableField("description")
    private String description;
    
    /** 冲账关联的原交易ID */
    @TableField("reversal_transaction_id")
    private Long reversalTransactionId;
    
    /** 退款关联的原交易ID */
    @TableField("refund_transaction_id")
    private Long refundTransactionId;
    
    /** 挂账公司ID */
    @TableField("credit_company_id")
    private Long creditCompanyId;
    
    /** 挂账客人ID */
    @TableField("credit_guest_id")
    private Long creditGuestId;
    
    /** 积分抵扣使用的积分数 */
    @TableField("points_used")
    private Integer pointsUsed;

    /** 操作员ID */
    @TableField("operator_id")
    private Long operatorId;
    
    /** 操作时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
