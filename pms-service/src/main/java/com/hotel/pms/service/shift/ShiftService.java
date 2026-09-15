package com.hotel.pms.service.shift;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftService {
    
    private static final DateTimeFormatter SHIFT_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    private final SysShiftMapper shiftMapper;
    private final SysShiftMessageMapper messageMapper;
    private final SysShiftNotifyConfigMapper notifyConfigMapper;
    private final FinTransactionMapper finTransactionMapper;
    private final SysAccountMapper accountMapper;
    
    public SysShift createShift(SysShift shift, Long hotelId, Long userId) {
        shift.setHotelId(hotelId);
        shift.setOperatorId(userId);
        shift.setStatus("DRAFT");
        shift.setShiftNo(generateShiftNo(hotelId));
        
        // 统计当班数据
        if (shift.getStartTime() != null && shift.getEndTime() != null) {
            statisticsShiftData(shift, hotelId, shift.getStartTime(), shift.getEndTime());
        }
        
        shiftMapper.insert(shift);
        return shift;
    }
    
    public SysShift updateShift(Long id, SysShift shift) {
        SysShift existing = shiftMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能编辑草稿状态的交班记录");
        }
        shift.setId(id);
        shiftMapper.updateById(shift);
        return shiftMapper.selectById(id);
    }
    
    public SysShift submitShift(Long id) {
        SysShift shift = shiftMapper.selectById(id);
        if (shift == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        if (!"DRAFT".equals(shift.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能提交草稿状态的交班记录");
        }
        shift.setStatus("PENDING");
        shift.setSubmitTime(LocalDateTime.now());
        shiftMapper.updateById(shift);
        sendNotification(shift, "SHIFT_SUBMIT");
        return shift;
    }
    
    public void deleteShift(Long id) {
        SysShift shift = shiftMapper.selectById(id);
        if (shift == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        if (!"DRAFT".equals(shift.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能删除草稿状态的交班记录");
        }
        shiftMapper.deleteById(id);
    }
    
    public SysShift acceptShift(Long id, Long userId) {
        SysShift shift = shiftMapper.selectById(id);
        if (shift == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        if (!"PENDING".equals(shift.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能接收待接收状态的交班记录");
        }
        shift.setReceiverId(userId);
        shift.setStatus("ACCEPTED");
        shift.setAcceptTime(LocalDateTime.now());
        shiftMapper.updateById(shift);
        sendNotification(shift, "SHIFT_ACCEPT");
        return shift;
    }
    
    public SysShift confirmShift(Long id) {
        SysShift shift = shiftMapper.selectById(id);
        if (shift == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        if (!"ACCEPTED".equals(shift.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能确认已接收状态的交班记录");
        }
        shift.setStatus("CONFIRMED");
        shift.setConfirmTime(LocalDateTime.now());
        shiftMapper.updateById(shift);
        sendNotification(shift, "SHIFT_CONFIRM");
        return shift;
    }
    
    public SysShift rejectShift(Long id, String reason) {
        SysShift shift = shiftMapper.selectById(id);
        if (shift == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        if (!"PENDING".equals(shift.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能驳回待接收状态的交班记录");
        }
        shift.setStatus("REJECTED");
        shift.setRejectTime(LocalDateTime.now());
        shift.setRejectReason(reason);
        shiftMapper.updateById(shift);
        sendNotification(shift, "SHIFT_REJECT");
        return shift;
    }
    
    public List<SysShift> listShifts(Long hotelId, String status) {
        LambdaQueryWrapper<SysShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysShift::getHotelId, hotelId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SysShift::getStatus, status);
        }
        wrapper.orderByDesc(SysShift::getCreatedAt);
        return shiftMapper.selectList(wrapper);
    }
    
    public SysShift getShift(Long id) {
        return shiftMapper.selectById(id);
    }
    
    public List<SysShift> listPendingShifts(Long hotelId) {
        LambdaQueryWrapper<SysShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysShift::getHotelId, hotelId)
               .eq(SysShift::getStatus, "PENDING")
               .orderByDesc(SysShift::getCreatedAt);
        return shiftMapper.selectList(wrapper);
    }
    
    /**
     * 获取当班统计数据
     */
    public SysShift getStatistics(Long hotelId, Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        SysShift stats = new SysShift();
        stats.setHotelId(hotelId);
        stats.setOperatorId(userId);
        
        // 如果没有指定时间，默认查询今天的
        if (startTime == null) {
            startTime = LocalDateTime.now().toLocalDate().atStartOfDay();
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        // 查询时间范围内的交易数据
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, hotelId)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime);
        
        List<FinTransaction> transactions = finTransactionMapper.selectList(wrapper);
        
        // 统计各支付方式金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal cashAmount = BigDecimal.ZERO;
        BigDecimal posAmount = BigDecimal.ZERO;
        BigDecimal wechatAmount = BigDecimal.ZERO;
        BigDecimal alipayAmount = BigDecimal.ZERO;
        BigDecimal creditAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        
        for (FinTransaction tx : transactions) {
            BigDecimal amount = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
            String type = tx.getType();
            String paymentMethod = tx.getPaymentMethod();
            
            // 退款和冲账是负数
            if ("REFUND".equals(type) || "REVERSAL".equals(type)) {
                refundAmount = refundAmount.add(amount.abs());
                continue;
            }
            
            // 挂账单独统计
            if ("CREDIT".equals(paymentMethod)) {
                creditAmount = creditAmount.add(amount);
                continue;
            }
            
            // 按支付方式统计
            if ("CASH".equals(paymentMethod)) {
                cashAmount = cashAmount.add(amount);
            } else if ("POS".equals(paymentMethod)) {
                posAmount = posAmount.add(amount);
            } else if ("WECHAT".equals(paymentMethod)) {
                wechatAmount = wechatAmount.add(amount);
            } else if ("ALIPAY".equals(paymentMethod)) {
                alipayAmount = alipayAmount.add(amount);
            }
            
            totalAmount = totalAmount.add(amount);
        }
        
        stats.setTotalAmount(totalAmount);
        stats.setCashAmount(cashAmount);
        stats.setPosAmount(posAmount);
        stats.setWechatAmount(wechatAmount);
        stats.setAlipayAmount(alipayAmount);
        stats.setCreditAmount(creditAmount);
        stats.setRefundAmount(refundAmount);
        stats.setTransactionCount(transactions.size());
        
        return stats;
    }
    
    public List<SysShiftMessage> listMessages(Long userId) {
        LambdaQueryWrapper<SysShiftMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysShiftMessage::getReceiverId, userId)
               .orderByDesc(SysShiftMessage::getCreatedAt);
        return messageMapper.selectList(wrapper);
    }
    
    public void markMessageRead(Long id) {
        SysShiftMessage message = messageMapper.selectById(id);
        if (message != null) {
            message.setIsRead(true);
            message.setReadTime(LocalDateTime.now());
            messageMapper.updateById(message);
        }
    }
    
    public List<SysShiftNotifyConfig> getNotifyConfig(Long hotelId) {
        LambdaQueryWrapper<SysShiftNotifyConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysShiftNotifyConfig::getHotelId, hotelId)
               .eq(SysShiftNotifyConfig::getIsActive, true);
        return notifyConfigMapper.selectList(wrapper);
    }
    
    public void saveNotifyConfig(Long hotelId, List<Long> userIds) {
        LambdaQueryWrapper<SysShiftNotifyConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysShiftNotifyConfig::getHotelId, hotelId);
        notifyConfigMapper.delete(wrapper);
        for (Long userId : userIds) {
            SysShiftNotifyConfig config = new SysShiftNotifyConfig();
            config.setHotelId(hotelId);
            config.setUserId(userId);
            config.setNotifyType("ALL");
            config.setIsActive(true);
            notifyConfigMapper.insert(config);
        }
    }
    
    /**
     * 统计当班数据
     */
    private void statisticsShiftData(SysShift shift, Long hotelId, LocalDateTime startTime, LocalDateTime endTime) {
        // 查询时间范围内的交易数据
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, hotelId)
               .ge(FinTransaction::getCreatedAt, startTime)
               .le(FinTransaction::getCreatedAt, endTime);
        
        List<FinTransaction> transactions = finTransactionMapper.selectList(wrapper);
        
        // 统计各支付方式金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal cashAmount = BigDecimal.ZERO;
        BigDecimal posAmount = BigDecimal.ZERO;
        BigDecimal wechatAmount = BigDecimal.ZERO;
        BigDecimal alipayAmount = BigDecimal.ZERO;
        BigDecimal creditAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        int checkinCount = 0;
        int checkoutCount = 0;
        
        for (FinTransaction tx : transactions) {
            BigDecimal amount = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
            String type = tx.getType();
            String paymentMethod = tx.getPaymentMethod();
            
            // 统计入住和退房
            if ("ROOM_FEE".equals(type) || "DEPOSIT".equals(type)) {
                checkinCount++;
            }
            if ("REFUND".equals(type)) {
                checkoutCount++;
                refundAmount = refundAmount.add(amount.abs());
                continue;
            }
            if ("REVERSAL".equals(type)) {
                refundAmount = refundAmount.add(amount.abs());
                continue;
            }
            
            // 挂账单独统计
            if ("CREDIT".equals(paymentMethod)) {
                creditAmount = creditAmount.add(amount);
                continue;
            }
            
            // 按支付方式统计
            if ("CASH".equals(paymentMethod)) {
                cashAmount = cashAmount.add(amount);
            } else if ("POS".equals(paymentMethod)) {
                posAmount = posAmount.add(amount);
            } else if ("WECHAT".equals(paymentMethod)) {
                wechatAmount = wechatAmount.add(amount);
            } else if ("ALIPAY".equals(paymentMethod)) {
                alipayAmount = alipayAmount.add(amount);
            }
            
            totalAmount = totalAmount.add(amount);
        }
        
        shift.setTotalAmount(totalAmount);
        shift.setCashAmount(cashAmount);
        shift.setPosAmount(posAmount);
        shift.setWechatAmount(wechatAmount);
        shift.setAlipayAmount(alipayAmount);
        shift.setCreditAmount(creditAmount);
        shift.setRefundAmount(refundAmount);
        shift.setCheckinCount(checkinCount);
        shift.setCheckoutCount(checkoutCount);
        shift.setTransactionCount(transactions.size());
    }
    
    private void sendNotification(SysShift shift, String messageType) {
        List<SysShiftNotifyConfig> configs = getNotifyConfig(shift.getHotelId());
        List<Long> receiverIds = new ArrayList<>();
        if (shift.getReceiverId() != null) {
            receiverIds.add(shift.getReceiverId());
        }
        for (SysShiftNotifyConfig config : configs) {
            if (!receiverIds.contains(config.getUserId())) {
                receiverIds.add(config.getUserId());
            }
        }
        for (Long receiverId : receiverIds) {
            SysShiftMessage message = new SysShiftMessage();
            message.setShiftId(shift.getId());
            message.setReceiverId(receiverId);
            message.setMessageType(messageType);
            message.setIsRead(false);
            messageMapper.insert(message);
        }
    }
    
    private String generateShiftNo(Long hotelId) {
        String dateStr = LocalDateTime.now().format(SHIFT_NO_FORMAT);
        String prefix = "SH" + dateStr;
        LambdaQueryWrapper<SysShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysShift::getHotelId, hotelId)
               .likeRight(SysShift::getShiftNo, prefix)
               .orderByDesc(SysShift::getShiftNo)
               .last("LIMIT 1");
        SysShift latest = shiftMapper.selectOne(wrapper);
        int nextSeq = 1;
        if (latest != null && latest.getShiftNo() != null) {
            String lastNo = latest.getShiftNo();
            String seqStr = lastNo.substring(lastNo.length() - 4);
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        }
        return prefix + String.format("%04d", nextSeq);
    }
    /**
     * 交班核对
     * <p>
     * 核对系统统计数据与实际交接金额，计算差异
     * </p>
     * 
     * @param shiftId 交班记录ID
     * @param actualCash 实际现金金额
     * @param actualPos 实际POS金额
     * @param actualWechat 实际微信金额
     * @param actualAlipay 实际支付宝金额
     * @return 核对结果
     */
    public SysShift verifyShift(Long shiftId, BigDecimal actualCash, BigDecimal actualPos, 
                               BigDecimal actualWechat, BigDecimal actualAlipay) {
        // 1. 查询交班记录
        SysShift shift = shiftMapper.selectById(shiftId);
        if (shift == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        
        // 2. 设置实际交接金额
        shift.setActualCash(actualCash);
        shift.setActualPos(actualPos);
        shift.setActualWechat(actualWechat);
        shift.setActualAlipay(actualAlipay);
        
        // 3. 计算差异金额
        BigDecimal cashDiff = calculateDifference(shift.getCashAmount(), actualCash);
        BigDecimal posDiff = calculateDifference(shift.getPosAmount(), actualPos);
        BigDecimal wechatDiff = calculateDifference(shift.getWechatAmount(), actualWechat);
        BigDecimal alipayDiff = calculateDifference(shift.getAlipayAmount(), actualAlipay);
        
        // 4. 记录核对结果
        log.info("交班核对完成: shiftId={}, 现金差异={}, POS差异={}, 微信差异={}, 支付宝差异={}", 
                shiftId, cashDiff, posDiff, wechatDiff, alipayDiff);
        
        // 5. 更新交班记录
        shiftMapper.updateById(shift);
        
        return shift;
    }
    
    /**
     * 保存实际交接金额
     * <p>
     * 保存实际交接金额，用于交班核对
     * </p>
     * 
     * @param shiftId 交班记录ID
     * @param actualCash 实际现金金额
     * @param actualPos 实际POS金额
     * @param actualWechat 实际微信金额
     * @param actualAlipay 实际支付宝金额
     * @return 更新后的交班记录
     */
    public SysShift saveActualAmounts(Long shiftId, BigDecimal actualCash, BigDecimal actualPos,
                                     BigDecimal actualWechat, BigDecimal actualAlipay) {
        // 1. 查询交班记录
        SysShift shift = shiftMapper.selectById(shiftId);
        if (shift == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        
        // 2. 设置实际交接金额
        shift.setActualCash(actualCash);
        shift.setActualPos(actualPos);
        shift.setActualWechat(actualWechat);
        shift.setActualAlipay(actualAlipay);
        
        // 3. 更新交班记录
        shiftMapper.updateById(shift);
        
        log.info("保存实际交接金额: shiftId={}, 现金={}, POS={}, 微信={}, 支付宝={}", 
                shiftId, actualCash, actualPos, actualWechat, actualAlipay);
        
        return shift;
    }
    
    /**
     * 获取交班报表数据
     * <p>
     * 生成交班报表，包含详细统计信息
     * </p>
     * 
     * @param shiftId 交班记录ID
     * @return 交班报表数据
     */
    public SysShift getShiftReport(Long shiftId) {
        // 1. 查询交班记录
        SysShift shift = shiftMapper.selectById(shiftId);
        if (shift == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交班记录不存在");
        }
        
        // 2. 计算差异金额
        BigDecimal cashDiff = calculateDifference(shift.getCashAmount(), shift.getActualCash());
        BigDecimal posDiff = calculateDifference(shift.getPosAmount(), shift.getActualPos());
        BigDecimal wechatDiff = calculateDifference(shift.getWechatAmount(), shift.getActualWechat());
        BigDecimal alipayDiff = calculateDifference(shift.getAlipayAmount(), shift.getActualAlipay());
        
        // 3. 记录报表生成日志
        log.info("生成交班报表: shiftId={}, 总金额={}, 现金差异={}, POS差异={}, 微信差异={}, 支付宝差异={}", 
                shiftId, shift.getTotalAmount(), cashDiff, posDiff, wechatDiff, alipayDiff);
        
        return shift;
    }
    
    /**
     * 计算差异金额
     * <p>
     * 计算系统金额与实际金额的差异
     * </p>
     * 
     * @param systemAmount 系统金额
     * @param actualAmount 实际金额
     * @return 差异金额（实际金额 - 系统金额）
     */
    private BigDecimal calculateDifference(BigDecimal systemAmount, BigDecimal actualAmount) {
        // 1. 处理空值
        BigDecimal system = systemAmount != null ? systemAmount : BigDecimal.ZERO;
        BigDecimal actual = actualAmount != null ? actualAmount : BigDecimal.ZERO;
        
        // 2. 计算差异
        return actual.subtract(system);
    }
}
