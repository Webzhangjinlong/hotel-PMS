package com.hotel.pms.service.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务实现类
 */
@Service
public class ReportServiceImpl implements ReportService {
    
    @Autowired
    private FinTransactionMapper transactionMapper;
    
    @Autowired
    private FolioMapper folioMapper;
    
    @Autowired
    private StayMapper stayMapper;
    
    @Autowired
    private DepositMapper depositMapper;
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private SysShiftMapper shiftMapper;
    
    @Override
    public Map<String, Object> getShiftReport(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建时间范围
        LocalDateTime startTime = buildStartTime(businessDate, shift);
        LocalDateTime endTime = buildEndTime(businessDate, shift);
        
        // 构建查询条件
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, 1L) // 默认酒店ID
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime);
        
        if (operatorId != null) {
            wrapper.eq(FinTransaction::getOperatorId, operatorId);
        }
        
        // 查询所有交易记录
        List<FinTransaction> transactions = transactionMapper.selectList(wrapper);
        
        // 1. 上缴汇总
        Map<String, Object> depositSummary = calculateDepositSummary(transactions);
        result.put("depositSummary", depositSummary);
        
        // 2. 收入总计
        Map<String, Object> incomeSummary = calculateIncomeSummary(transactions);
        result.put("incomeSummary", incomeSummary);
        
        // 3. 总收款
        Map<String, Object> paymentSummary = calculatePaymentSummary(transactions);
        result.put("paymentSummary", paymentSummary);
        
        // 4. 预授权
        Map<String, Object> preAuth = calculatePreAuth(businessDate, shift);
        result.put("preAuth", preAuth);
        
        // 5. 会员冻结
        Map<String, Object> memberFreeze = calculateMemberFreeze(businessDate, shift);
        result.put("memberFreeze", memberFreeze);
        
        // 6. 会员售卡
        Map<String, Object> memberCard = calculateMemberCard(businessDate, shift);
        result.put("memberCard", memberCard);
        
        // 7. 会员充值
        Map<String, Object> memberRecharge = calculateMemberRecharge(businessDate, shift);
        result.put("memberRecharge", memberRecharge);
        
        // 8. 协议回款
        Map<String, Object> agreementPayment = calculateAgreementPayment(businessDate, shift);
        result.put("agreementPayment", agreementPayment);
        
        return result;
    }
    
    /**
     * 计算上缴汇总
     */
    private Map<String, Object> calculateDepositSummary(List<FinTransaction> transactions) {
        Map<String, Object> summary = new HashMap<>();
        
        BigDecimal cash = BigDecimal.ZERO;
        BigDecimal wechat = BigDecimal.ZERO;
        BigDecimal alipay = BigDecimal.ZERO;
        int preAuthCount = 0;
        
        for (FinTransaction tx : transactions) {
            if ("PAYMENT".equals(tx.getType())) {
                switch (tx.getPaymentMethod()) {
                    case "CASH":
                        cash = cash.add(tx.getAmount());
                        break;
                    case "WECHAT":
                        wechat = wechat.add(tx.getAmount());
                        break;
                    case "ALIPAY":
                        alipay = alipay.add(tx.getAmount());
                        break;
                }
            }
        }
        
        summary.put("cash", cash.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        summary.put("wechat", wechat.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        summary.put("alipay", alipay.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        summary.put("preAuth", preAuthCount);
        
        return summary;
    }
    
    /**
     * 计算收入总计
     */
    private Map<String, Object> calculateIncomeSummary(List<FinTransaction> transactions) {
        Map<String, Object> summary = new HashMap<>();
        
        BigDecimal roomFee = BigDecimal.ZERO;
        BigDecimal goods = BigDecimal.ZERO;
        BigDecimal meal = BigDecimal.ZERO;
        
        for (FinTransaction tx : transactions) {
            if ("DEPOSIT".equals(tx.getType()) || "EXTRA".equals(tx.getType())) {
                String desc = tx.getDescription() != null ? tx.getDescription() : "";
                if (desc.contains("房费")) {
                    roomFee = roomFee.add(tx.getAmount());
                } else if (desc.contains("商品")) {
                    goods = goods.add(tx.getAmount());
                } else if (desc.contains("餐费")) {
                    meal = meal.add(tx.getAmount());
                } else {
                    // 默认计入房费
                    roomFee = roomFee.add(tx.getAmount());
                }
            }
        }
        
        BigDecimal total = roomFee.add(goods).add(meal);
        
        summary.put("roomFee", roomFee.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        summary.put("goods", goods.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        summary.put("meal", meal.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        summary.put("total", total.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        
        return summary;
    }
    
    /**
     * 计算总收款
     */
    private Map<String, Object> calculatePaymentSummary(List<FinTransaction> transactions) {
        Map<String, Object> summary = new HashMap<>();
        
        Map<String, BigDecimal> paymentMap = new HashMap<>();
        paymentMap.put("cash", BigDecimal.ZERO);
        paymentMap.put("bankCard", BigDecimal.ZERO);
        paymentMap.put("wechat", BigDecimal.ZERO);
        paymentMap.put("alipay", BigDecimal.ZERO);
        paymentMap.put("memberPay", BigDecimal.ZERO);
        paymentMap.put("arAccount", BigDecimal.ZERO);
        paymentMap.put("coupon", BigDecimal.ZERO);
        paymentMap.put("internal", BigDecimal.ZERO);
        paymentMap.put("thirdParty", BigDecimal.ZERO);
        paymentMap.put("credit", BigDecimal.ZERO);
        paymentMap.put("rounding", BigDecimal.ZERO);
        
        for (FinTransaction tx : transactions) {
            if ("PAYMENT".equals(tx.getType())) {
                String method = tx.getPaymentMethod();
                switch (method) {
                    case "CASH":
                        paymentMap.put("cash", paymentMap.get("cash").add(tx.getAmount()));
                        break;
                    case "POS":
                    case "BANK_CARD":
                        paymentMap.put("bankCard", paymentMap.get("bankCard").add(tx.getAmount()));
                        break;
                    case "WECHAT":
                        paymentMap.put("wechat", paymentMap.get("wechat").add(tx.getAmount()));
                        break;
                    case "ALIPAY":
                        paymentMap.put("alipay", paymentMap.get("alipay").add(tx.getAmount()));
                        break;
                    case "MEMBER":
                        paymentMap.put("memberPay", paymentMap.get("memberPay").add(tx.getAmount()));
                        break;
                    case "CREDIT":
                        paymentMap.put("arAccount", paymentMap.get("arAccount").add(tx.getAmount()));
                        break;
                    case "COUPON":
                        paymentMap.put("coupon", paymentMap.get("coupon").add(tx.getAmount()));
                        break;
                    case "INTERNAL":
                        paymentMap.put("internal", paymentMap.get("internal").add(tx.getAmount()));
                        break;
                    default:
                        paymentMap.put("thirdParty", paymentMap.get("thirdParty").add(tx.getAmount()));
                        break;
                }
            }
        }
        
        // 计算总收款
        BigDecimal total = paymentMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 设置格式化后的值
        paymentMap.forEach((key, value) -> 
            summary.put(key, value.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
        summary.put("total", total.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        
        return summary;
    }
    
    /**
     * 计算预授权
     */
    private Map<String, Object> calculatePreAuth(LocalDate businessDate, String shift) {
        Map<String, Object> preAuth = new HashMap<>();
        // TODO: 实现预授权逻辑
        preAuth.put("newCount", 0);
        preAuth.put("completedCount", 0);
        return preAuth;
    }
    
    /**
     * 计算会员冻结
     */
    private Map<String, Object> calculateMemberFreeze(LocalDate businessDate, String shift) {
        Map<String, Object> memberFreeze = new HashMap<>();
        // TODO: 实现会员冻结逻辑
        memberFreeze.put("newAmount", "0.00");
        memberFreeze.put("completedAmount", "0");
        return memberFreeze;
    }
    
    /**
     * 计算会员售卡
     */
    private Map<String, Object> calculateMemberCard(LocalDate businessDate, String shift) {
        Map<String, Object> memberCard = new HashMap<>();
        // TODO: 实现会员售卡逻辑
        memberCard.put("newCount", 0);
        memberCard.put("totalAmount", "0");
        Map<String, String> payment = new HashMap<>();
        payment.put("cash", "0");
        payment.put("wechat", "0");
        payment.put("alipay", "0");
        payment.put("bankCard", "0");
        payment.put("other", "0");
        memberCard.put("payment", payment);
        return memberCard;
    }
    
    /**
     * 计算会员充值
     */
    private Map<String, Object> calculateMemberRecharge(LocalDate businessDate, String shift) {
        Map<String, Object> memberRecharge = new HashMap<>();
        // TODO: 实现会员充值逻辑
        memberRecharge.put("totalAmount", "0");
        Map<String, String> payment = new HashMap<>();
        payment.put("cash", "0");
        payment.put("wechat", "0");
        payment.put("alipay", "0");
        payment.put("bankCard", "0");
        payment.put("other", "0");
        memberRecharge.put("payment", payment);
        return memberRecharge;
    }
    
    /**
     * 计算协议回款
     */
    private Map<String, Object> calculateAgreementPayment(LocalDate businessDate, String shift) {
        Map<String, Object> agreementPayment = new HashMap<>();
        // TODO: 实现协议回款逻辑
        agreementPayment.put("totalAmount", "0");
        Map<String, String> payment = new HashMap<>();
        payment.put("cash", "0");
        payment.put("wechat", "0");
        payment.put("alipay", "0");
        payment.put("bankCard", "0");
        payment.put("other", "0");
        agreementPayment.put("payment", payment);
        return agreementPayment;
    }
    
    /**
     * 构建开始时间
     */
    private LocalDateTime buildStartTime(LocalDate businessDate, String shift) {
        LocalTime time;
        switch (shift) {
            case "MORNING":
                time = LocalTime.of(6, 0);
                break;
            case "MIDDLE":
                time = LocalTime.of(14, 0);
                break;
            case "NIGHT":
                time = LocalTime.of(22, 0);
                break;
            default:
                time = LocalTime.of(0, 0);
        }
        return LocalDateTime.of(businessDate, time);
    }
    
    /**
     * 构建结束时间
     */
    private LocalDateTime buildEndTime(LocalDate businessDate, String shift) {
        LocalTime time;
        switch (shift) {
            case "MORNING":
                time = LocalTime.of(14, 0);
                break;
            case "MIDDLE":
                time = LocalTime.of(22, 0);
                break;
            case "NIGHT":
                time = LocalTime.of(6, 0);
                businessDate = businessDate.plusDays(1);
                break;
            default:
                time = LocalTime.of(23, 59, 59);
        }
        return LocalDateTime.of(businessDate, time);
    }
    
    // ==================== 其他报表方法 ====================
    
    @Override
    public Map<String, Object> getEntryDetail(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = buildStartTime(businessDate, shift);
        LocalDateTime endTime = buildEndTime(businessDate, shift);
        
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, 1L)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime)
               .in(FinTransaction::getType, "DEPOSIT", "EXTRA");
        
        if (operatorId != null) {
            wrapper.eq(FinTransaction::getOperatorId, operatorId);
        }
        wrapper.orderByDesc(FinTransaction::getCreatedAt);
        
        List<FinTransaction> list = transactionMapper.selectList(wrapper);
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }
    
    @Override
    public Map<String, Object> getEntrySummary(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = buildStartTime(businessDate, shift);
        LocalDateTime endTime = buildEndTime(businessDate, shift);
        
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, 1L)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime)
               .in(FinTransaction::getType, "DEPOSIT", "EXTRA");
        
        if (operatorId != null) {
            wrapper.eq(FinTransaction::getOperatorId, operatorId);
        }
        
        List<FinTransaction> list = transactionMapper.selectList(wrapper);
        
        // 按类型汇总
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("roomFee", BigDecimal.ZERO);
        summary.put("goods", BigDecimal.ZERO);
        summary.put("meal", BigDecimal.ZERO);
        summary.put("other", BigDecimal.ZERO);
        
        for (FinTransaction tx : list) {
            String desc = tx.getDescription() != null ? tx.getDescription() : "";
            if (desc.contains("房费")) {
                summary.put("roomFee", summary.get("roomFee").add(tx.getAmount()));
            } else if (desc.contains("商品")) {
                summary.put("goods", summary.get("goods").add(tx.getAmount()));
            } else if (desc.contains("餐费")) {
                summary.put("meal", summary.get("meal").add(tx.getAmount()));
            } else {
                summary.put("other", summary.get("other").add(tx.getAmount()));
            }
        }
        
        result.put("summary", summary);
        result.put("total", list.size());
        return result;
    }
    
    @Override
    public Map<String, Object> getEntryTotal(LocalDate businessDate, String shift, Long operatorId) {
        return getEntrySummary(businessDate, shift, operatorId);
    }
    
    @Override
    public Map<String, Object> getPaymentDetail(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = buildStartTime(businessDate, shift);
        LocalDateTime endTime = buildEndTime(businessDate, shift);
        
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, 1L)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime)
               .eq(FinTransaction::getType, "PAYMENT");
        
        if (operatorId != null) {
            wrapper.eq(FinTransaction::getOperatorId, operatorId);
        }
        wrapper.orderByDesc(FinTransaction::getCreatedAt);
        
        List<FinTransaction> list = transactionMapper.selectList(wrapper);
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }
    
    @Override
    public Map<String, Object> getPaymentSummary(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = buildStartTime(businessDate, shift);
        LocalDateTime endTime = buildEndTime(businessDate, shift);
        
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, 1L)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime)
               .eq(FinTransaction::getType, "PAYMENT");
        
        if (operatorId != null) {
            wrapper.eq(FinTransaction::getOperatorId, operatorId);
        }
        
        List<FinTransaction> list = transactionMapper.selectList(wrapper);
        
        // 按支付方式汇总
        Map<String, BigDecimal> summary = new HashMap<>();
        for (FinTransaction tx : list) {
            String method = tx.getPaymentMethod();
            summary.putIfAbsent(method, BigDecimal.ZERO);
            summary.put(method, summary.get(method).add(tx.getAmount()));
        }
        
        result.put("summary", summary);
        result.put("totalAmount", list.stream()
            .map(FinTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        return result;
    }
    
    @Override
    public Map<String, Object> getTransferReport(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = buildStartTime(businessDate, shift);
        LocalDateTime endTime = buildEndTime(businessDate, shift);
        
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, 1L)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime)
               .eq(FinTransaction::getType, "TRANSFER");
        
        if (operatorId != null) {
            wrapper.eq(FinTransaction::getOperatorId, operatorId);
        }
        wrapper.orderByDesc(FinTransaction::getCreatedAt);
        
        List<FinTransaction> list = transactionMapper.selectList(wrapper);
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }
    
    @Override
    public Map<String, Object> getChargeBackAdjust(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = buildStartTime(businessDate, shift);
        LocalDateTime endTime = buildEndTime(businessDate, shift);
        
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, 1L)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime)
               .in(FinTransaction::getType, "REVERSAL", "REFUND");
        
        if (operatorId != null) {
            wrapper.eq(FinTransaction::getOperatorId, operatorId);
        }
        wrapper.orderByDesc(FinTransaction::getCreatedAt);
        
        List<FinTransaction> list = transactionMapper.selectList(wrapper);
        result.put("list", list);
        result.put("total", list.size());
        return result;
    }
    
    @Override
    public Map<String, Object> getCheckoutActualStats(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime startTime = buildStartTime(businessDate, shift);
        LocalDateTime endTime = buildEndTime(businessDate, shift);
        
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, 1L)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime)
               .eq(FinTransaction::getType, "PAYMENT");
        
        if (operatorId != null) {
            wrapper.eq(FinTransaction::getOperatorId, operatorId);
        }
        
        List<FinTransaction> list = transactionMapper.selectList(wrapper);
        
        BigDecimal totalAmount = list.stream()
            .map(FinTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        result.put("totalCount", list.size());
        result.put("totalAmount", totalAmount);
        return result;
    }
    
    @Override
    public Map<String, Object> getCheckoutActualDetail(LocalDate businessDate, String shift, Long operatorId) {
        return getPaymentDetail(businessDate, shift, operatorId);
    }
    
    @Override
    public Map<String, Object> getProductSalesSummary(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        // TODO: 实现商品销售汇总逻辑
        result.put("summary", new ArrayList<>());
        result.put("total", 0);
        return result;
    }
    
    @Override
    public Map<String, Object> getProductSalesDetail(LocalDate businessDate, String shift, Long operatorId) {
        Map<String, Object> result = new HashMap<>();
        // TODO: 实现商品销售明细逻辑
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        return result;
    }
}
