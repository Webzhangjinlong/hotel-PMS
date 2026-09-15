package com.hotel.pms.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 预付统计VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class PrepaymentStatisticsVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 预付总额 */
    private BigDecimal totalAmount;
    
    /** 预付笔数 */
    private Integer totalCount;
    
    /** 已转入金额 */
    private BigDecimal transferredAmount;
    
    /** 已退款金额 */
    private BigDecimal refundedAmount;
    
    /** 待处理金额（已付未转入） */
    private BigDecimal pendingAmount;
    
    /** 平均预付金额 */
    private BigDecimal averageAmount;
    
    /** 按日汇总 */
    private List<DailySummary> dailySummaries;
    
    /** 按预付类型汇总 */
    private List<TypeSummary> typeSummaries;
    
    /** 按支付方式汇总 */
    private List<PaymentMethodSummary> paymentMethodSummaries;
    
    @Data
    public static class DailySummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private String date;
        private Integer count;
        private BigDecimal amount;
        private BigDecimal transferredAmount;
        private BigDecimal refundedAmount;
        private BigDecimal pendingAmount;
    }
    
    @Data
    public static class TypeSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private String prepaymentType;
        private String prepaymentTypeName;
        private Integer count;
        private BigDecimal amount;
        private BigDecimal percentage;
    }
    
    @Data
    public static class PaymentMethodSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private String paymentMethod;
        private String paymentMethodName;
        private Integer count;
        private BigDecimal amount;
        private BigDecimal percentage;
    }
}
