package com.hotel.pms.service.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.constant.FolioConstants;
import com.hotel.pms.common.dto.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.PrepaymentQueryDTO;

import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预付服务类
 * <p>
 * 负责预订时的预付款处理
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class PrepaymentService {

    private static final DateTimeFormatter TRANSACTION_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private ReservationPrepaymentMapper prepaymentMapper;

    @Autowired
    private FinTransactionMapper finTransactionMapper;

    @Autowired
    private FolioMapper folioMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private TeamReservationMapper teamReservationMapper;

    @Autowired
    private StayMapper stayMapper;

    /**
     * 预订时预付
     *
     * @param dto 预付请求
     * @return 预付结果
     */
    @Transactional(rollbackFor = Exception.class)
    public PrepaymentVO prepay(PrepaymentDTO dto) {
        // 1. 验证预订存在
        if (dto.getReservationId() == null && dto.getTeamReservationId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "预订ID不能为空");
        }

        // 2. 创建预付记录
        ReservationPrepayment prepayment = new ReservationPrepayment();
        prepayment.setHotelId(dto.getHotelId());
        prepayment.setReservationId(dto.getReservationId());
        prepayment.setTeamReservationId(dto.getTeamReservationId());
        prepayment.setPrepaymentType(dto.getPrepaymentType());
        prepayment.setAmount(dto.getAmount());
        prepayment.setPaymentMethod(dto.getPaymentMethod());
        prepayment.setPaymentTime(LocalDateTime.now());
        prepayment.setStatus(FolioConstants.PREPAYMENT_STATUS_PAID);
        prepayment.setRemark(dto.getRemark());
        prepaymentMapper.insert(prepayment);

        log.info("预付成功：预付ID={}, 金额={}, 预付类型={}, 支付方式={}",
                prepayment.getId(), dto.getAmount(), dto.getPrepaymentType(), dto.getPaymentMethod());

        return convertToVO(prepayment);
    }

    /**
     * 预付款转入住账务
     *
     * @param prepaymentId 预付ID
     * @param stayId 入住单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void transferToStayFolio(Long prepaymentId, Long stayId) {
        // 1. 查询预付记录
        ReservationPrepayment prepayment = prepaymentMapper.selectById(prepaymentId);
        if (prepayment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预付记录不存在");
        }

        if (!FolioConstants.PREPAYMENT_STATUS_PAID.equals(prepayment.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "预付记录状态不正确，无法转入");
        }

        // 2. 查询入住单
        Stay stay = stayMapper.selectById(stayId);
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }

        // 3. 查询账务单
        LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
        folioWrapper.eq(Folio::getStayId, stayId);
        Folio folio = folioMapper.selectOne(folioWrapper);

        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "账务单不存在");
        }

        // 4. 创建交易记录
        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(stay.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(stay.getHotelId()));
        transaction.setFolioId(folio.getId());
        transaction.setType(FolioConstants.TRANSACTION_PAYMENT);
        transaction.setAmount(prepayment.getAmount());
        transaction.setPaymentMethod(prepayment.getPaymentMethod());
        transaction.setDescription("预付款转入");
        transaction.setOperatorId(null);
        finTransactionMapper.insert(transaction);

        // 5. 更新账务单
        folio.setPaidAmount(folio.getPaidAmount().add(prepayment.getAmount()));
        folio.setBalance(folio.getBalance().subtract(prepayment.getAmount()));
        folioMapper.updateById(folio);

        // 6. 更新预付记录状态
        prepayment.setStatus(FolioConstants.PREPAYMENT_STATUS_TRANSFERRED);
        prepayment.setTransactionId(transaction.getId());
        prepaymentMapper.updateById(prepayment);

        log.info("预付款转入成功：预付ID={}, 入住单ID={}, 金额={}",
                prepaymentId, stayId, prepayment.getAmount());
    }

    /**
     * 预订取消退款
     *
     * @param prepaymentId 预付ID
     * @param refundMethod 退款方式
     * @return 退款结果
     */
    @Transactional(rollbackFor = Exception.class)
    public TransactionVO refundPrepayment(Long prepaymentId, String refundMethod) {
        // 1. 查询预付记录
        ReservationPrepayment prepayment = prepaymentMapper.selectById(prepaymentId);
        if (prepayment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预付记录不存在");
        }

        if (!FolioConstants.PREPAYMENT_STATUS_PAID.equals(prepayment.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "预付记录状态不正确，无法退款");
        }

        // 2. 创建退款交易记录
        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(prepayment.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(prepayment.getHotelId()));
        transaction.setFolioId(null); // 预付退款没有关联账务单
        transaction.setType(FolioConstants.TRANSACTION_REFUND);
        transaction.setAmount(prepayment.getAmount());
        transaction.setPaymentMethod(refundMethod);
        transaction.setDescription("预订取消退款");
        transaction.setRefundTransactionId(prepayment.getTransactionId());
        finTransactionMapper.insert(transaction);

        // 3. 更新预付记录状态
        prepayment.setStatus(FolioConstants.PREPAYMENT_STATUS_REFUNDED);
        prepaymentMapper.updateById(prepayment);

        log.info("预付退款成功：预付ID={}, 退款金额={}, 退款方式={}",
                prepaymentId, prepayment.getAmount(), refundMethod);

        return convertTransactionToVO(transaction);
    }

    /**
     * 根据散客预订ID查询预付款
     *
     * @param reservationId 散客预订ID
     * @return 预付款列表
     */
    public List<PrepaymentVO> getByReservationId(Long reservationId) {
        LambdaQueryWrapper<ReservationPrepayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationPrepayment::getReservationId, reservationId)
               .orderByDesc(ReservationPrepayment::getCreatedAt);

        List<ReservationPrepayment> list = prepaymentMapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据团队预订ID查询预付款
     *
     * @param teamReservationId 团队预订ID
     * @return 预付款列表
     */

    /**
     * 分页查询预付款列表
     *
     * @param queryDTO 查询条件
     * @return 预付款分页列表
     */
    public Page<PrepaymentVO> list(PrepaymentQueryDTO queryDTO) {
        LambdaQueryWrapper<ReservationPrepayment> wrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (queryDTO.getHotelId() != null) {
            wrapper.eq(ReservationPrepayment::getHotelId, queryDTO.getHotelId());
        }
        if (queryDTO.getReservationId() != null) {
            wrapper.eq(ReservationPrepayment::getReservationId, queryDTO.getReservationId());
        }
        if (queryDTO.getTeamReservationId() != null) {
            wrapper.eq(ReservationPrepayment::getTeamReservationId, queryDTO.getTeamReservationId());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq(ReservationPrepayment::getStatus, queryDTO.getStatus());
        }

        // 按创建时间降序排序
        wrapper.orderByDesc(ReservationPrepayment::getCreatedAt);

        // 执行分页查询
        Page<ReservationPrepayment> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<ReservationPrepayment> pageResult = prepaymentMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<PrepaymentVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<PrepaymentVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 预付统计
     *
     * @param queryDTO 查询条件
     * @return 统计结果
     */
    public PrepaymentStatisticsVO statistics(PrepaymentQueryDTO queryDTO) {
        PrepaymentStatisticsVO vo = new PrepaymentStatisticsVO();

        // 构建查询条件
        LambdaQueryWrapper<ReservationPrepayment> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getHotelId() != null) {
            wrapper.eq(ReservationPrepayment::getHotelId, queryDTO.getHotelId());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq(ReservationPrepayment::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(ReservationPrepayment::getCreatedAt);

        // 查询所有符合条件的预付记录
        List<ReservationPrepayment> allRecords = prepaymentMapper.selectList(wrapper);

        // 1. 计算核心指标
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal transferredAmount = BigDecimal.ZERO;
        BigDecimal refundedAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;

        for (ReservationPrepayment record : allRecords) {
            BigDecimal amount = record.getAmount() != null ? record.getAmount() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(amount);

            if (FolioConstants.PREPAYMENT_STATUS_TRANSFERRED.equals(record.getStatus())) {
                transferredAmount = transferredAmount.add(amount);
            } else if (FolioConstants.PREPAYMENT_STATUS_REFUNDED.equals(record.getStatus())) {
                refundedAmount = refundedAmount.add(amount);
            } else if (FolioConstants.PREPAYMENT_STATUS_PAID.equals(record.getStatus())) {
                pendingAmount = pendingAmount.add(amount);
            }
        }

        vo.setTotalAmount(totalAmount);
        vo.setTotalCount(allRecords.size());
        vo.setTransferredAmount(transferredAmount);
        vo.setRefundedAmount(refundedAmount);
        vo.setPendingAmount(pendingAmount);
        vo.setAverageAmount(allRecords.size() > 0 ? totalAmount.divide(BigDecimal.valueOf(allRecords.size()), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);

        // 2. 按日汇总
        Map<String, List<ReservationPrepayment>> dailyGroups = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate().toString()));

        List<PrepaymentStatisticsVO.DailySummary> dailySummaries = new ArrayList<>();
        for (Map.Entry<String, List<ReservationPrepayment>> entry : dailyGroups.entrySet()) {
            PrepaymentStatisticsVO.DailySummary summary = new PrepaymentStatisticsVO.DailySummary();
            summary.setDate(entry.getKey());
            summary.setCount(entry.getValue().size());

            BigDecimal dayAmount = BigDecimal.ZERO;
            BigDecimal dayTransferred = BigDecimal.ZERO;
            BigDecimal dayRefunded = BigDecimal.ZERO;
            BigDecimal dayPending = BigDecimal.ZERO;

            for (ReservationPrepayment r : entry.getValue()) {
                BigDecimal amount = r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO;
                dayAmount = dayAmount.add(amount);

                if (FolioConstants.PREPAYMENT_STATUS_TRANSFERRED.equals(r.getStatus())) {
                    dayTransferred = dayTransferred.add(amount);
                } else if (FolioConstants.PREPAYMENT_STATUS_REFUNDED.equals(r.getStatus())) {
                    dayRefunded = dayRefunded.add(amount);
                } else if (FolioConstants.PREPAYMENT_STATUS_PAID.equals(r.getStatus())) {
                    dayPending = dayPending.add(amount);
                }
            }

            summary.setAmount(dayAmount);
            summary.setTransferredAmount(dayTransferred);
            summary.setRefundedAmount(dayRefunded);
            summary.setPendingAmount(dayPending);
            dailySummaries.add(summary);
        }
        dailySummaries.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        vo.setDailySummaries(dailySummaries);

        // 3. 按预付类型汇总
        Map<String, List<ReservationPrepayment>> typeGroups = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getPrepaymentType() != null ? r.getPrepaymentType() : "UNKNOWN"));

        List<PrepaymentStatisticsVO.TypeSummary> typeSummaries = new ArrayList<>();
        for (Map.Entry<String, List<ReservationPrepayment>> entry : typeGroups.entrySet()) {
            PrepaymentStatisticsVO.TypeSummary summary = new PrepaymentStatisticsVO.TypeSummary();
            summary.setPrepaymentType(entry.getKey());
            summary.setPrepaymentTypeName(getPrepaymentTypeName(entry.getKey()));
            summary.setCount(entry.getValue().size());

            BigDecimal typeAmount = BigDecimal.ZERO;
            for (ReservationPrepayment r : entry.getValue()) {
                typeAmount = typeAmount.add(r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO);
            }
            summary.setAmount(typeAmount);
            summary.setPercentage(totalAmount.compareTo(BigDecimal.ZERO) > 0 ? typeAmount.multiply(BigDecimal.valueOf(100)).divide(totalAmount, 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            typeSummaries.add(summary);
        }
        typeSummaries.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        vo.setTypeSummaries(typeSummaries);

        // 4. 按支付方式汇总
        Map<String, List<ReservationPrepayment>> methodGroups = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getPaymentMethod() != null ? r.getPaymentMethod() : "UNKNOWN"));

        List<PrepaymentStatisticsVO.PaymentMethodSummary> methodSummaries = new ArrayList<>();
        for (Map.Entry<String, List<ReservationPrepayment>> entry : methodGroups.entrySet()) {
            PrepaymentStatisticsVO.PaymentMethodSummary summary = new PrepaymentStatisticsVO.PaymentMethodSummary();
            summary.setPaymentMethod(entry.getKey());
            summary.setPaymentMethodName(getPaymentMethodName(entry.getKey()));
            summary.setCount(entry.getValue().size());

            BigDecimal methodAmount = BigDecimal.ZERO;
            for (ReservationPrepayment r : entry.getValue()) {
                methodAmount = methodAmount.add(r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO);
            }
            summary.setAmount(methodAmount);
            summary.setPercentage(totalAmount.compareTo(BigDecimal.ZERO) > 0 ? methodAmount.multiply(BigDecimal.valueOf(100)).divide(totalAmount, 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            methodSummaries.add(summary);
        }
        methodSummaries.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        vo.setPaymentMethodSummaries(methodSummaries);

        return vo;
    }

    public List<PrepaymentVO> getByTeamReservationId(Long teamReservationId) {
        LambdaQueryWrapper<ReservationPrepayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationPrepayment::getTeamReservationId, teamReservationId)
               .orderByDesc(ReservationPrepayment::getCreatedAt);

        List<ReservationPrepayment> list = prepaymentMapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 生成交易号
     */
    private String generateTransactionNo(Long hotelId) {
        String dateStr = LocalDateTime.now().format(TRANSACTION_NO_FORMAT);
        String prefix = "T" + dateStr;

        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, hotelId)
               .likeRight(FinTransaction::getTransactionNo, prefix)
               .orderByDesc(FinTransaction::getTransactionNo)
               .last("LIMIT 1");

        FinTransaction latest = finTransactionMapper.selectOne(wrapper);

        int nextSeq = 1;
        if (latest != null && latest.getTransactionNo() != null) {
            String lastNo = latest.getTransactionNo();
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
     * 获取支付方式名称
     */
    private String getPaymentMethodName(String paymentMethod) {
        if (paymentMethod == null) return "";
        switch (paymentMethod) {
            case FolioConstants.PAYMENT_CASH: return "现金";
            case FolioConstants.PAYMENT_WECHAT: return "微信";
            case FolioConstants.PAYMENT_ALIPAY: return "支付宝";
            case FolioConstants.PAYMENT_POS: return "刷卡";
            case FolioConstants.PAYMENT_BANK_TRANSFER: return "银行转账";
            default: return paymentMethod;
        }
    }

    /**
     * 获取预付类型名称
     */
    private String getPrepaymentTypeName(String prepaymentType) {
        if (prepaymentType == null) return "";
        switch (prepaymentType) {
            case FolioConstants.PREPAYMENT_FULL: return "全额预付";
            case FolioConstants.PREPAYMENT_PARTIAL: return "部分预付";
            case FolioConstants.PREPAYMENT_DEPOSIT: return "押金";
            default: return prepaymentType;
        }
    }

    /**
     * 获取预付状态名称
     */
    private String getPrepaymentStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case FolioConstants.PREPAYMENT_STATUS_PAID: return "已付";
            case FolioConstants.PREPAYMENT_STATUS_TRANSFERRED: return "已转入";
            case FolioConstants.PREPAYMENT_STATUS_REFUNDED: return "已退款";
            default: return status;
        }
    }

    /**
     * 转换为VO
     */
    private PrepaymentVO convertToVO(ReservationPrepayment prepayment) {
        PrepaymentVO vo = new PrepaymentVO();
        vo.setId(prepayment.getId());
        vo.setHotelId(prepayment.getHotelId());
        vo.setReservationId(prepayment.getReservationId());
        vo.setTeamReservationId(prepayment.getTeamReservationId());
        vo.setPrepaymentType(prepayment.getPrepaymentType());
        vo.setPrepaymentTypeName(getPrepaymentTypeName(prepayment.getPrepaymentType()));
        vo.setAmount(prepayment.getAmount());
        vo.setPaymentMethod(prepayment.getPaymentMethod());
        vo.setPaymentMethodName(getPaymentMethodName(prepayment.getPaymentMethod()));
        vo.setPaymentTime(prepayment.getPaymentTime());
        vo.setTransactionId(prepayment.getTransactionId());
        vo.setStatus(prepayment.getStatus());
        vo.setStatusName(getPrepaymentStatusName(prepayment.getStatus()));
        vo.setRemark(prepayment.getRemark());
        return vo;
    }

    /**
     * 转换交易为VO
     */
    private TransactionVO convertTransactionToVO(FinTransaction transaction) {
        TransactionVO vo = new TransactionVO();
        vo.setId(transaction.getId());
        vo.setTransactionNo(transaction.getTransactionNo());
        vo.setFolioId(transaction.getFolioId());
        vo.setType(transaction.getType());
        vo.setAmount(transaction.getAmount());
        vo.setPaymentMethod(transaction.getPaymentMethod());
        vo.setPaymentMethodName(getPaymentMethodName(transaction.getPaymentMethod()));
        vo.setDescription(transaction.getDescription());
        vo.setCreatedAt(transaction.getCreatedAt());
        return vo;
    }
}

