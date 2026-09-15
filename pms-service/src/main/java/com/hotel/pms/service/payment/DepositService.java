package com.hotel.pms.service.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepositService {

    private final DepositMapper depositMapper;
    private final FinTransactionMapper finTransactionMapper;
    private final GuestMapper guestMapper;
    private final StayMapper stayMapper;
    private final FolioMapper folioMapper;

    /**
     * 生成押金单号
     * <p>
     * 格式：DEP + 日期(yyyyMMdd) + 4位序号
     * </p>
     *
     * @param hotelId 酒店ID
     * @return 押金单号
     */
    private String generateDepositNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "DEP" + dateStr;

        // 查询当天最大单号
        LambdaQueryWrapper<Deposit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deposit::getHotelId, hotelId)
               .likeRight(Deposit::getDepositNo, prefix)
               .orderByDesc(Deposit::getDepositNo)
               .last("LIMIT 1");

        Deposit lastDeposit = depositMapper.selectOne(wrapper);

        int seq = 1;
        if (lastDeposit != null && lastDeposit.getDepositNo() != null) {
            String lastNo = lastDeposit.getDepositNo();
            String seqStr = lastNo.substring(lastNo.length() - 4);
            try {
                seq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }

        return prefix + String.format("%04d", seq);
    }

    /**
     * 生成交易流水号
     * <p>
     * 格式：TXN + 日期(yyyyMMdd) + 6位序号
     * </p>
     *
     * @param hotelId 酒店ID
     * @return 交易流水号
     */
    private String generateTransactionNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TXN" + dateStr;

        // 查询当天最大单号
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getHotelId, hotelId)
               .likeRight(FinTransaction::getTransactionNo, prefix)
               .orderByDesc(FinTransaction::getTransactionNo)
               .last("LIMIT 1");

        FinTransaction lastTransaction = finTransactionMapper.selectOne(wrapper);

        int seq = 1;
        if (lastTransaction != null && lastTransaction.getTransactionNo() != null) {
            String lastNo = lastTransaction.getTransactionNo();
            String seqStr = lastNo.substring(lastNo.length() - 6);
            try {
                seq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }

        return prefix + String.format("%06d", seq);
    }

    /**
     * 收取押金
     * <p>
     * 为客人收取入住押金，生成押金单，并记录交易流水
     * </p>
     *
     * @param dto 押金收取请求
     * @return 押金信息
     */
    @Transactional(rollbackFor = Exception.class)
    public DepositVO collectDeposit(DepositCollectDTO dto) {
        // 1. 验证客人是否存在
        Guest guest = guestMapper.selectById(dto.getGuestId());
        if (guest == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "客人不存在");
        }

        // 2. 创建押金记录
        Deposit deposit = new Deposit();
        BeanUtils.copyProperties(dto, deposit);

        // 3. 生成押金单号
        deposit.setDepositNo(generateDepositNo(dto.getHotelId()));

        // 4. 设置押金状态和时间
        deposit.setStatus("COLLECTED");
        deposit.setCollectedAt(LocalDateTime.now());
        deposit.setCollectedBy(dto.getOperatorId());
        deposit.setRefundedAmount(BigDecimal.ZERO);

        // 5. 保存押金记录
        depositMapper.insert(deposit);

        // 6. 记录交易流水
        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(dto.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(dto.getHotelId()));
        transaction.setType("DEPOSIT");
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setDescription("押金收取 - " + dto.getGuestName());
        transaction.setOperatorId(dto.getOperatorId());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setFolioId(null); // 押金交易没有关联账务单
        finTransactionMapper.insert(transaction);


        log.info("押金收取成功：押金单号={}, 金额={}, 客人={}, 交易流水号={}",
                deposit.getDepositNo(), deposit.getAmount(), dto.getGuestName(), transaction.getTransactionNo());

        // 7. 返回押金信息
        return convertToVO(deposit);
    }

    /**
     * 退还押金
     * <p>
     * 对已收取的押金进行退还操作，支持部分退还
     * </p>
     *
     * @param id 押金ID
     * @param dto 退还请求
     * @return 更新后的押金信息
     */
    @Transactional(rollbackFor = Exception.class)
    public DepositVO refundDeposit(Long id, DepositRefundDTO dto) {
        // 1. 查询押金记录
        Deposit deposit = depositMapper.selectById(id);
        if (deposit == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "押金记录不存在");
        }

        // 2. 验证押金状态
        if ("REFUNDED".equals(deposit.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该押金已全额退还");
        }

        // 3. 计算可退金额
        BigDecimal refundableAmount = deposit.getAmount().subtract(deposit.getRefundedAmount());
        if (dto.getAmount().compareTo(refundableAmount) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                String.format("退还金额不能超过可退金额：%.2f", refundableAmount));
        }

        // 4. 更新退还金额
        deposit.setRefundedAmount(deposit.getRefundedAmount().add(dto.getAmount()));
        deposit.setRefundedAt(LocalDateTime.now());
        deposit.setRefundedBy(dto.getOperatorId());
        deposit.setRefundMethod(dto.getRefundMethod());
        deposit.setRemark(dto.getRemark());

        // 5. 更新押金状态
        if (deposit.getRefundedAmount().compareTo(deposit.getAmount()) >= 0) {
            deposit.setStatus("REFUNDED");
        } else {
            deposit.setStatus("PARTIAL_DEDUCT");
        }

        // 6. 保存更新
        depositMapper.updateById(deposit);

        log.info("押金退还成功：押金单号={}, 退还金额={}, 退还方式={}",
                deposit.getDepositNo(), dto.getAmount(), dto.getRefundMethod());

        return convertToVO(deposit);
    }

    /**
     * 查询押金列表
     * <p>
     * 根据条件分页查询押金记录
     * </p>
     *
     * @param queryDTO 查询条件
     * @return 押金列表
     */
    public Page<DepositVO> list(DepositQueryDTO queryDTO) {
        Page<Deposit> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<Deposit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deposit::getHotelId, queryDTO.getHotelId())
               .eq(Deposit::getDeleted, false)
               .in(Deposit::getStatus, "COLLECTED", "PARTIAL_REFUND", "PARTIAL_DEDUCT", "REFUNDED");

        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Deposit::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getGuestName())) {
            wrapper.like(Deposit::getGuestName, queryDTO.getGuestName());
        }
        if (StringUtils.hasText(queryDTO.getRoomNo())) {
            wrapper.eq(Deposit::getRoomNo, queryDTO.getRoomNo());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(Deposit::getCollectedAt, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(Deposit::getCollectedAt, queryDTO.getEndDate().atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(Deposit::getCreatedAt);

        Page<Deposit> result = depositMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<DepositVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<DepositVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询押金详情
     * <p>
     * 根据押金ID查询详细信息
     * </p>
     *
     * @param id 押金ID
     * @return 押金信息
     */
    public DepositVO getById(Long id) {
        Deposit deposit = depositMapper.selectById(id);
        if (deposit == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "押金记录不存在");
        }
        return convertToVO(deposit);
    }

    /**
     * 根据入住单ID查询押金
     * <p>
     * 查询指定入住单关联的所有押金记录
     * </p>
     *
     * @param stayId 入住单ID
     * @return 押金列表
     */
    public List<DepositVO> getByStayId(Long stayId) {
        LambdaQueryWrapper<Deposit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Deposit::getStayId, stayId)
               .eq(Deposit::getDeleted, false)
               .in(Deposit::getStatus, "COLLECTED", "PARTIAL_REFUND", "PARTIAL_DEDUCT", "REFUNDED")
               .orderByDesc(Deposit::getCreatedAt);

        List<Deposit> deposits = depositMapper.selectList(wrapper);
        return deposits.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将实体转换为VO
     *
     * @param deposit 押金实体
     * @return 押金VO
     */
    private DepositVO convertToVO(Deposit deposit) {
        DepositVO vo = new DepositVO();
        BeanUtils.copyProperties(deposit, vo);

        // 设置支付方式名称
        vo.setPaymentMethodName(getPaymentMethodName(deposit.getPaymentMethod()));

        // 设置状态名称
        vo.setStatusName(getStatusName(deposit.getStatus()));

        // 查询收取人姓名
        if (deposit.getCollectedBy() != null) {
            Guest collector = guestMapper.selectById(deposit.getCollectedBy());
            if (collector != null) {
                vo.setCollectedByName(collector.getName());
            }
        }

        // 查询退还人姓名
        if (deposit.getRefundedBy() != null) {
            Guest refunder = guestMapper.selectById(deposit.getRefundedBy());
            if (refunder != null) {
                vo.setRefundedByName(refunder.getName());
            }
        }

        return vo;
    }

    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(String method) {
        if (method == null) return "";
        switch (method) {
            case "CASH": return "现金";
            case "WECHAT": return "微信";
            case "ALIPAY": return "支付宝";
            case "POS": return "刷卡";
            case "BANK_TRANSFER": return "银行转账";
            default: return method;
        }
    }

    /**
     * 获取押金状态名称
     */
    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "COLLECTED": return "已收取";
            case "REFUNDED": return "已退还";
            case "PARTIAL_REFUND": return "部分退还";
            case "PARTIAL_DEDUCT": return "部分抵扣";
            default: return status;
        }
    }

    /**
     * 押金抵扣房费
     * <p>
     * 将押金转为房费支付，减少押金余额并记录房费支付交易流水
     * </p>
     *
     * @param id 押金ID
     * @param dto 抵扣请求
     * @return 更新后的押金信息
     */
    @Transactional(rollbackFor = Exception.class)
    public DepositVO deductDepositForRoomFee(Long id, DepositDeductDTO dto) {
        // 根据入住单号查询入住单
        Stay stay = null;
        if (StringUtils.hasText(dto.getStayNo())) {
            LambdaQueryWrapper<Stay> stayWrapper = new LambdaQueryWrapper<>();
            stayWrapper.eq(Stay::getStayNo, dto.getStayNo());
            stay = stayMapper.selectOne(stayWrapper);
        }
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }

        // 1. 查询押金记录
        Deposit deposit = depositMapper.selectById(id);
        if (deposit == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "押金记录不存在");
        }

        // 2. 验证押金状态
        if ("REFUNDED".equals(deposit.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该押金已全额退还，无法抵扣");
        }

        // 3. 计算可抵扣金额（押金总额 - 已退还金额 - 已抵扣金额）
        BigDecimal refundableAmount = deposit.getAmount()
                .subtract(deposit.getRefundedAmount() != null ? deposit.getRefundedAmount() : BigDecimal.ZERO)
                .subtract(deposit.getDeductedAmount() != null ? deposit.getDeductedAmount() : BigDecimal.ZERO);
        if (dto.getAmount().compareTo(refundableAmount) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                String.format("抵扣金额不能超过可抵扣金额：%.2f", refundableAmount));
        }

        // 4. 更新押金的已抵扣金额
        BigDecimal currentDeducted = deposit.getDeductedAmount() != null ? deposit.getDeductedAmount() : BigDecimal.ZERO;
        deposit.setDeductedAmount(currentDeducted.add(dto.getAmount()));
        deposit.setRemark(dto.getRemark() != null ?
            deposit.getRemark() + "；抵扣房费：" + dto.getAmount() :
            "抵扣房费：" + dto.getAmount());

        // 5. 更新押金状态
        BigDecimal totalUsed = (deposit.getRefundedAmount() != null ? deposit.getRefundedAmount() : BigDecimal.ZERO)
                .add(deposit.getDeductedAmount());
        if (totalUsed.compareTo(deposit.getAmount()) >= 0) {
            deposit.setStatus("REFUNDED");
        } else {
            deposit.setStatus("PARTIAL_DEDUCT");
        }

        // 6. 保存押金更新
        depositMapper.updateById(deposit);

        // 7. 查询账务单并记录房费支付交易流水
        LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
        folioWrapper.eq(Folio::getStayId, stay.getId());
        Folio folio = folioMapper.selectOne(folioWrapper);

        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(deposit.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(deposit.getHotelId()));
        if (folio != null) {
            transaction.setFolioId(folio.getId());
        }
        transaction.setType("ROOM_FEE");
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentMethod("DEPOSIT");
        transaction.setDescription("押金抵扣房费 - 押金单号：" + deposit.getDepositNo());
        transaction.setOperatorId(dto.getOperatorId());
        transaction.setCreatedAt(LocalDateTime.now());
        finTransactionMapper.insert(transaction);

        // 8. 更新入住单的已付金额
        BigDecimal currentPaid = stay.getPaidAmount() != null ? stay.getPaidAmount() : BigDecimal.ZERO;
        stay.setPaidAmount(currentPaid.add(dto.getAmount()));
        stayMapper.updateById(stay);

        // 9. 更新账务单的已付金额
        if (folio != null) {
            BigDecimal currentFolioPaid = folio.getPaidAmount() != null ? folio.getPaidAmount() : BigDecimal.ZERO;
            folio.setPaidAmount(currentFolioPaid.add(dto.getAmount()));
            BigDecimal currentBalance = folio.getBalance() != null ? folio.getBalance() : BigDecimal.ZERO;
            folio.setBalance(currentBalance.subtract(dto.getAmount()));
            folioMapper.updateById(folio);
        }

        log.info("押金抵扣房费成功：押金单号={}, 抵扣金额={}, 入住单号={}",
                deposit.getDepositNo(), dto.getAmount(), dto.getStayNo());

        return convertToVO(deposit);
    }
}




