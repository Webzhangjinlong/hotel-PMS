package com.hotel.pms.service.folio;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.constant.FolioConstants;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 散客账务服务类 * <p>
 * 负责散客入住的收款、退款、冲账等账务处理
 * </p>
 * 
 * @author PMS开发团队 * @since 1.0.0
 */
@Slf4j
@Service
public class FolioService {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TRANSACTION_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    @Autowired
    private FolioMapper folioMapper;
    
    @Autowired
    private FinTransactionMapper finTransactionMapper;
    
    @Autowired
    private StayMapper stayMapper;
    
    @Autowired
    private GuestMapper guestMapper;
    
    @Autowired
    private HotelMapper hotelMapper;
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private CreditCompanyMapper creditCompanyMapper;
    
    
    @Autowired
    private DepositMapper depositMapper;
    /**
     * 根据入住单ID查询账务单?     * 
     * @param stayId 入住单ID
     * @return 账务单信息?     */
    public FolioVO getFolioByStayId(Long stayId) {
        LambdaQueryWrapper<Folio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folio::getStayId, stayId);
        Folio folio = folioMapper.selectOne(wrapper);
        
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "账务单不存在");
        }
        
        return convertToVO(folio);
    }
    
    /**
     * 分页查询散客账务单列表
     * 
     * @param queryDTO 查询条件
     * @return 账务单分页列表
     */
    public PageResponse<FolioVO> getStayFolioList(FolioQueryDTO queryDTO) {
        LambdaQueryWrapper<Folio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, Folio::getHotelId, queryDTO.getHotelId())
               .eq(StringUtils.hasText(queryDTO.getStatus()), Folio::getStatus, queryDTO.getStatus())
               .eq(queryDTO.getStayId() != null, Folio::getStayId, queryDTO.getStayId())
               .orderByDesc(Folio::getCreatedAt);
        
        Page<Folio> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Folio> result = folioMapper.selectPage(page, wrapper);
        
        List<FolioVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 获取在住账务统计信息
     */
    public FolioStatsVO getStayFolioStats(FolioQueryDTO queryDTO) {
        LambdaQueryWrapper<Folio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getStayId() != null, Folio::getStayId, queryDTO.getStayId())
               .eq(Folio::getStatus, "OPEN");
        List<Folio> folios = folioMapper.selectList(wrapper);
        if (folios == null || folios.isEmpty()) {
            return new FolioStatsVO();
        }
        FolioStatsVO stats = new FolioStatsVO();
        BigDecimal totalAmount = folios.stream()
                .map(Folio::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAmount = folios.stream()
                .map(Folio::getPaidAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = folios.stream()
                .map(Folio::getBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalAmount(totalAmount);
        stats.setPaidAmount(paidAmount);
        stats.setBalance(balance);
        return stats;
    }
    
    /**
     * 入住时收款（押金/房费/杂费�?     * 
     * @param stayId 入住单ID
     * @param dto 收款请求
     * @return 收款结果
     */
    @Transactional(rollbackFor = Exception.class)
    public StayPaymentVO collectPayment(Long stayId, StayPaymentDTO dto) {
        // 1. 查询入住单
        Stay stay = stayMapper.selectById(stayId);
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }
        
        // 2. 查询账务单
        LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
        folioWrapper.eq(Folio::getStayId, stayId);
        Folio folio = folioMapper.selectOne(folioWrapper);
        
        if (folio == null) {
            // 如果账务单不存在，自动创建
            folio = createFolio(stay);
        }
        
        // 3. 验证账务单状态
        if (FolioConstants.FOLIO_CLOSED.equals(folio.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "账务单已关闭，无法收款");
        }
        
        // 4. 如果是挂账，验证挂账公司
        if (FolioConstants.PAYMENT_CREDIT.equals(dto.getPaymentMethod())) {
            validateCreditCompany(dto.getCreditCompanyId(), dto.getAmount());
        }
        
        // 5. 创建交易记录
        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(stay.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(stay.getHotelId()));
        transaction.setFolioId(folio.getId());
        transaction.setType(dto.getTransactionType());
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setDescription(dto.getRemark());
        transaction.setCreditCompanyId(dto.getCreditCompanyId());
        transaction.setCreditGuestId(dto.getCreditGuestId());
        transaction.setOperatorId(dto.getOperatorId());
        finTransactionMapper.insert(transaction);
        
        // 5.1 如果是押金类型，同时创建押金记录
        if (FolioConstants.TRANSACTION_DEPOSIT.equals(dto.getTransactionType())) {
            Deposit deposit = new Deposit();
            deposit.setHotelId(stay.getHotelId());
            deposit.setDepositNo(generateDepositNo(stay.getHotelId()));
            deposit.setStayId(stayId);
            deposit.setGuestId(stay.getGuestId());
            deposit.setAmount(dto.getAmount());
            deposit.setPaymentMethod(dto.getPaymentMethod());
            deposit.setStatus("COLLECTED");
            deposit.setCollectedBy(dto.getOperatorId());
            deposit.setCollectedAt(LocalDateTime.now());
            deposit.setRemark(dto.getRemark());
            deposit.setRefundedAmount(BigDecimal.ZERO);
            deposit.setDeductedAmount(BigDecimal.ZERO);
            
            // 查询房间号
            if (stay.getRoomId() != null) {
                Room room = roomMapper.selectById(stay.getRoomId());
                if (room != null) {
                    deposit.setRoomNo(room.getRoomNo());
                }
            }
            
            // 查询客人姓名
            if (stay.getGuestId() != null) {
                Guest guest = guestMapper.selectById(stay.getGuestId());
                if (guest != null) {
                    deposit.setGuestName(guest.getName());
                }
            }
            
            depositMapper.insert(deposit);
            log.info("押金记录创建成功：押金单号={}, 金额={}", deposit.getDepositNo(), deposit.getAmount());
        }
        
        // 6. 更新账务单（押金不计入，因为押金会通过deposit表单独计算）
        if (!FolioConstants.TRANSACTION_DEPOSIT.equals(dto.getTransactionType())) {
            folio.setPaidAmount(folio.getPaidAmount().add(dto.getAmount()));
            folio.setBalance(folio.getBalance().subtract(dto.getAmount()));
            folioMapper.updateById(folio);
        }
        
        // 7. 更新入住单的已付金额（押金不计入，因为押金会通过deposit表单独计算）
        if (!FolioConstants.TRANSACTION_DEPOSIT.equals(dto.getTransactionType())) {
            stay.setPaidAmount(stay.getPaidAmount().add(dto.getAmount()));
            stayMapper.updateById(stay);
        }
        
        // 7. 如果是挂账，更新挂账公司余额
        if (FolioConstants.PAYMENT_CREDIT.equals(dto.getPaymentMethod()) && dto.getCreditCompanyId() != null) {
            updateCreditCompanyBalance(dto.getCreditCompanyId(), dto.getAmount());
        }
        
        log.info("收款成功：入住单�?{}, 交易�?{}, 金额={}, 支付方式={}", 
                stay.getStayNo(), transaction.getTransactionNo(), dto.getAmount(), dto.getPaymentMethod());
        
        // 8. 构建响应
        StayPaymentVO vo = new StayPaymentVO();
        vo.setTransactionId(transaction.getId());
        vo.setTransactionNo(transaction.getTransactionNo());
        vo.setFolioId(folio.getId());
        vo.setTransactionType(dto.getTransactionType());
        vo.setAmount(dto.getAmount());
        vo.setPaymentMethod(dto.getPaymentMethod());
        vo.setPaymentMethodName(getPaymentMethodName(dto.getPaymentMethod()));
        vo.setTransactionTime(transaction.getCreatedAt());
        vo.setFolioTotalAmount(folio.getTotalAmount());
        vo.setFolioPaidAmount(folio.getPaidAmount());
        vo.setFolioBalance(folio.getBalance());
        
        return vo;
    }
    
    /**
     * 加床/杂费入账
     * 
     * @param stayId 入住单ID
     * @param dto 杂费请求
     * @return 收款结果
     */
    @Transactional(rollbackFor = Exception.class)
    public StayPaymentVO addExtraCharge(Long stayId, ExtraChargeDTO dto) {
        // 1. 查询入住单
        Stay stay = stayMapper.selectById(stayId);
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }
        
        // 2. 查询账务单
        LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
        folioWrapper.eq(Folio::getStayId, stayId);
        Folio folio = folioMapper.selectOne(folioWrapper);
        
        if (folio == null) {
            // 如果账务单不存在，自动创建
            folio = createFolio(stay);
        }
        
        // 3. 验证账务单状态
        if (FolioConstants.FOLIO_CLOSED.equals(folio.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "账务单已关闭，无法添加费用");
        }
        
        // 4. 如果是挂账，验证挂账公司
        if (FolioConstants.PAYMENT_CREDIT.equals(dto.getPaymentMethod())) {
            validateCreditCompany(dto.getCreditCompanyId(), dto.getAmount());
        }
        
        // 5. 创建交易记录
        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(stay.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(stay.getHotelId()));
        transaction.setFolioId(folio.getId());
        transaction.setType(FolioConstants.TRANSACTION_EXTRA);
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentMethod(dto.getPaymentMethod());
        transaction.setDescription(dto.getDescription());
        transaction.setCreditCompanyId(dto.getCreditCompanyId());
        transaction.setOperatorId(dto.getOperatorId());
        finTransactionMapper.insert(transaction);
        
        // 6. 更新账务单
        folio.setPaidAmount(folio.getPaidAmount().add(dto.getAmount()));
        folio.setBalance(folio.getBalance().subtract(dto.getAmount()));
        folioMapper.updateById(folio);
        
        // 7. 更新入住单的已付金额
        stay.setPaidAmount(stay.getPaidAmount().add(dto.getAmount()));
        stayMapper.updateById(stay);
        
        // 8. 如果是挂账，更新挂账公司余额
        if (FolioConstants.PAYMENT_CREDIT.equals(dto.getPaymentMethod()) && dto.getCreditCompanyId() != null) {
            updateCreditCompanyBalance(dto.getCreditCompanyId(), dto.getAmount());
        }
        
        log.info("杂费入账成功：入住单号={}, 交易号={}, 金额={}, 类型={}", 
                stay.getStayNo(), transaction.getTransactionNo(), dto.getAmount(), dto.getChargeType());
        
        // 9. 构建响应
        StayPaymentVO vo = new StayPaymentVO();
        vo.setTransactionId(transaction.getId());
        vo.setTransactionNo(transaction.getTransactionNo());
        vo.setAmount(transaction.getAmount());
        vo.setPaymentMethod(transaction.getPaymentMethod());
        vo.setPaymentMethodName(getPaymentMethodName(transaction.getPaymentMethod()));
        
        return vo;
    }

    /**
     * 查询入住单的加床/杂费记录
     * 
     * @param stayId 入住单ID
     * @return 杂费交易记录列表
     */
    public List<TransactionVO> getExtraChargesByStayId(Long stayId) {
        // 1. 查询账务单
        LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
        folioWrapper.eq(Folio::getStayId, stayId);
        Folio folio = folioMapper.selectOne(folioWrapper);
        
        if (folio == null) {
            return List.of();
        }
        
        // 2. 查询杂费交易记录
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getFolioId, folio.getId())
               .eq(FinTransaction::getType, FolioConstants.TRANSACTION_EXTRA)
               .orderByDesc(FinTransaction::getCreatedAt);
        
        List<FinTransaction> transactions = finTransactionMapper.selectList(wrapper);
        return transactions.stream()
                .map(this::convertTransactionToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询交易流水
     * 支持按客人姓名、房间号查询交易流水
     * 
     * @param queryDTO 查询条件
     * @return 交易流水分页列表
     */
    public PageResponse<TransactionVO> getTransactionFlow(TransactionQueryDTO queryDTO) {
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, FinTransaction::getHotelId, queryDTO.getHotelId())
               .eq(StringUtils.hasText(queryDTO.getType()), FinTransaction::getType, queryDTO.getType())
               .eq(StringUtils.hasText(queryDTO.getPaymentMethod()), FinTransaction::getPaymentMethod, queryDTO.getPaymentMethod())
               .eq(queryDTO.getFolioId() != null, FinTransaction::getFolioId, queryDTO.getFolioId())
               .eq(queryDTO.getCreditCompanyId() != null, FinTransaction::getCreditCompanyId, queryDTO.getCreditCompanyId())
               .eq(StringUtils.hasText(queryDTO.getTransactionNo()), FinTransaction::getTransactionNo, queryDTO.getTransactionNo())
               .orderByDesc(FinTransaction::getCreatedAt);
        
        // 日期范围查询
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(FinTransaction::getCreatedAt, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(FinTransaction::getCreatedAt, queryDTO.getEndDate().atTime(LocalTime.MAX));
        }
        
        // 如果有客人姓名或房间号条件，需要关联查询
        if (StringUtils.hasText(queryDTO.getGuestName()) || StringUtils.hasText(queryDTO.getRoomNo())) {
            // 先查询符合条件的入住单
            LambdaQueryWrapper<Stay> stayWrapper = new LambdaQueryWrapper<>();
            stayWrapper.eq(queryDTO.getHotelId() != null, Stay::getHotelId, queryDTO.getHotelId());
            
            if (StringUtils.hasText(queryDTO.getGuestName())) {
                // 查询客人
                LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
                guestWrapper.like(Guest::getName, queryDTO.getGuestName());
                List<Guest> guests = guestMapper.selectList(guestWrapper);
                if (!guests.isEmpty()) {
                    List<Long> guestIds = guests.stream().map(Guest::getId).collect(Collectors.toList());
                    stayWrapper.in(Stay::getGuestId, guestIds);
                } else {
                    return new PageResponse<>(List.of(), 0, queryDTO.getPage(), queryDTO.getSize());
                }
            }
            
            if (StringUtils.hasText(queryDTO.getRoomNo())) {
                // 查询房间
                LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
                roomWrapper.eq(Room::getRoomNo, queryDTO.getRoomNo());
                List<Room> rooms = roomMapper.selectList(roomWrapper);
                if (!rooms.isEmpty()) {
                    List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
                    stayWrapper.in(Stay::getRoomId, roomIds);
                } else {
                    return new PageResponse<>(List.of(), 0, queryDTO.getPage(), queryDTO.getSize());
                }
            }
            
            List<Stay> stays = stayMapper.selectList(stayWrapper);
            if (stays.isEmpty()) {
                return new PageResponse<>(List.of(), 0, queryDTO.getPage(), queryDTO.getSize());
            }
            
            // 查询这些入住单对应的账务单
            List<Long> stayIds = stays.stream().map(Stay::getId).collect(Collectors.toList());
            LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
            folioWrapper.in(Folio::getStayId, stayIds);
            List<Folio> folios = folioMapper.selectList(folioWrapper);
            
            if (folios.isEmpty()) {
                return new PageResponse<>(List.of(), 0, queryDTO.getPage(), queryDTO.getSize());
            }
            
            List<Long> folioIds = folios.stream().map(Folio::getId).collect(Collectors.toList());
            wrapper.in(FinTransaction::getFolioId, folioIds);
        }
        
        Page<FinTransaction> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<FinTransaction> result = finTransactionMapper.selectPage(page, wrapper);
        
        List<TransactionVO> voList = result.getRecords().stream()
                .map(this::convertTransactionToVO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 导出交易流水为CSV
     * 
     * @param queryDTO 查询条件
     * @return CSV文件字节数组
     */
    public byte[] exportTransactionFlow(TransactionQueryDTO queryDTO) {
        // 查询所有符合条件的交易记录（不分页）
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, FinTransaction::getHotelId, queryDTO.getHotelId())
               .eq(StringUtils.hasText(queryDTO.getType()), FinTransaction::getType, queryDTO.getType())
               .eq(StringUtils.hasText(queryDTO.getPaymentMethod()), FinTransaction::getPaymentMethod, queryDTO.getPaymentMethod())
               .eq(queryDTO.getFolioId() != null, FinTransaction::getFolioId, queryDTO.getFolioId())
               .orderByDesc(FinTransaction::getCreatedAt);
        
        // 日期范围查询
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(FinTransaction::getCreatedAt, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(FinTransaction::getCreatedAt, queryDTO.getEndDate().atTime(LocalTime.MAX));
        }
        
        List<FinTransaction> transactions = finTransactionMapper.selectList(wrapper);
        
        // 构建CSV内容
        StringBuilder csv = new StringBuilder();
        // CSV头
        csv.append("\uFEFF"); // BOM for UTF-8
        csv.append("交易号,账务单号,入住单号,交易类型,交易类型名称,金额,支付方式,支付方式名称,描述,操作时间\n");
        
        for (FinTransaction transaction : transactions) {
            TransactionVO vo = convertTransactionToVO(transaction);
            csv.append(vo.getTransactionNo()).append(",")
               .append(vo.getFolioId()).append(",")
               .append(vo.getStayNo() != null ? vo.getStayNo() : "").append(",")
               .append(vo.getType()).append(",")
               .append(vo.getTypeName()).append(",")
               .append(vo.getAmount()).append(",")
               .append(vo.getPaymentMethod()).append(",")
               .append(vo.getPaymentMethodName()).append(",")
               .append(vo.getDescription() != null ? vo.getDescription().replace(",", "，") : "").append(",")
               .append(vo.getCreatedAt()).append("\n");
        }
        
        try {
            return csv.toString().getBytes("UTF-8");
        } catch (Exception e) {
            log.error("导出交易流水失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "导出失败");
        }
    }
    
    /**
     * 退房时结算
     * 
     * @param stayId 入住单ID
     * @param dto 退房请�?     * @return 退房结�?     */
    @Transactional(rollbackFor = Exception.class)
    public StayCheckOutVO settleAndCheckOut(Long stayId, StayCheckOutDTO dto) {
        // 1. 查询入住单
        Stay stay = stayMapper.selectById(stayId);
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }
        
        // 2. 查询账务单
        LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
        folioWrapper.eq(Folio::getStayId, stayId);
        Folio folio = folioMapper.selectOne(folioWrapper);
        
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "账务单不存在");
        }
        
        // 3. 如果需要补收
        if (folio.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            if (dto.getAdditionalPayment() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "账务单有余额未结清，请先收款");
            }
            
            // 创建补收交易
            FinTransaction transaction = new FinTransaction();
            transaction.setHotelId(stay.getHotelId());
            transaction.setTransactionNo(generateTransactionNo(stay.getHotelId()));
            transaction.setFolioId(folio.getId());
            transaction.setType(FolioConstants.TRANSACTION_PAYMENT);
            transaction.setAmount(dto.getAdditionalPayment());
            transaction.setPaymentMethod(dto.getPaymentMethod());
            transaction.setDescription("退房补收");
            transaction.setOperatorId(dto.getOperatorId());
            finTransactionMapper.insert(transaction);
            
            folio.setPaidAmount(folio.getPaidAmount().add(dto.getAdditionalPayment()));
            folio.setBalance(folio.getBalance().subtract(dto.getAdditionalPayment()));
        }
        
        // 4. 如果需要退款
        if (folio.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal refundAmount = folio.getBalance().abs();
            
                        // 创建退款交易
            FinTransaction refundTransaction = new FinTransaction();
            refundTransaction.setHotelId(stay.getHotelId());
            refundTransaction.setTransactionNo(generateTransactionNo(stay.getHotelId()));
            refundTransaction.setFolioId(folio.getId());
            refundTransaction.setType(FolioConstants.TRANSACTION_REFUND);
            refundTransaction.setAmount(refundAmount);
            refundTransaction.setPaymentMethod(dto.getRefundMethod() != null ? dto.getRefundMethod() : FolioConstants.PAYMENT_CASH);
            refundTransaction.setDescription("退房退款");
            refundTransaction.setOperatorId(dto.getOperatorId());
            finTransactionMapper.insert(refundTransaction);
            
            folio.setPaidAmount(folio.getPaidAmount().subtract(refundAmount));
            folio.setBalance(BigDecimal.ZERO);
        }
        
        // 5. 关闭账务单
        folio.setStatus(FolioConstants.FOLIO_CLOSED);
        folioMapper.updateById(folio);
        
        log.info("退房结算成功：入住单号={}, 账务单号={}", stay.getStayNo(), folio.getFolioNo());
        
        // 6. 构建响应
        StayCheckOutVO vo = StayCheckOutVO.builder()
                .stayId(stayId)
                .stayNo(stay.getStayNo())
                .totalAmount(folio.getTotalAmount())
                .paidAmount(folio.getPaidAmount())
                .outstandingAmount(folio.getBalance())
                .build();
        
        return vo;
    }
    
    /**
     * 退�?     * 
     * @param folioId 账务单ID
     * @param dto 退款请�?     * @return 退款结�?     */
    @Transactional(rollbackFor = Exception.class)
    public TransactionVO refund(Long folioId, RefundDTO dto) {
        // 1. 查询账务单
        Folio folio = folioMapper.selectById(folioId);
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "账务单不存在");
        }
        
        // 2. 验证账务单状态
        if (FolioConstants.FOLIO_CLOSED.equals(folio.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "账务单已关闭，无法退款");
        }
        
        // 3. 验证退款金额
        if (dto.getAmount().compareTo(folio.getPaidAmount()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "退款金额不能大于已付金额");
        }
        
        // 4. 创建退款交易
        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(folio.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(folio.getHotelId()));
        transaction.setFolioId(folioId);
        transaction.setType(FolioConstants.TRANSACTION_REFUND);
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentMethod(dto.getRefundMethod());
        transaction.setDescription("退款：" + dto.getReason());
        transaction.setRefundTransactionId(dto.getOriginalTransactionId());
        transaction.setOperatorId(dto.getOperatorId());
        finTransactionMapper.insert(transaction);
        
        // 5. 更新账务单
        folio.setPaidAmount(folio.getPaidAmount().subtract(dto.getAmount()));
        folio.setBalance(folio.getBalance().add(dto.getAmount()));
        folioMapper.updateById(folio);
        
        log.info("退款成功：账务单号={}, 退款金�?{}, 退款方�?{}", 
                folio.getFolioNo(), dto.getAmount(), dto.getRefundMethod());
        
        return convertTransactionToVO(transaction);
    }
    
    /**
     * 查询交易记录
     * 
     * @param queryDTO 查询条件
     * @return 交易记录列表
     */
    public PageResponse<TransactionVO> getTransactions(TransactionQueryDTO queryDTO) {
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, FinTransaction::getHotelId, queryDTO.getHotelId())
               .eq(StringUtils.hasText(queryDTO.getType()), FinTransaction::getType, queryDTO.getType())
               .eq(StringUtils.hasText(queryDTO.getPaymentMethod()), FinTransaction::getPaymentMethod, queryDTO.getPaymentMethod())
               .eq(queryDTO.getFolioId() != null, FinTransaction::getFolioId, queryDTO.getFolioId())
               .eq(queryDTO.getCreditCompanyId() != null, FinTransaction::getCreditCompanyId, queryDTO.getCreditCompanyId())
               .eq(StringUtils.hasText(queryDTO.getTransactionNo()), FinTransaction::getTransactionNo, queryDTO.getTransactionNo())
               .orderByDesc(FinTransaction::getCreatedAt);
        
        // 日期范围查询
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(FinTransaction::getCreatedAt, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(FinTransaction::getCreatedAt, queryDTO.getEndDate().atTime(LocalTime.MAX));
        }
        
        Page<FinTransaction> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<FinTransaction> result = finTransactionMapper.selectPage(page, wrapper);
        
        List<TransactionVO> voList = result.getRecords().stream()
                .map(this::convertTransactionToVO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 冲账（撤销交易�?     * 
     * @param transactionId 交易ID
     * @param reason 冲账原因
     * @return 冲账结果
     */
    @Transactional(rollbackFor = Exception.class)
    public TransactionVO reverseTransaction(Long transactionId, String reason) {
        // 1. 查询原交易
        FinTransaction originalTransaction = finTransactionMapper.selectById(transactionId);
        if (originalTransaction == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交易记录不存在");
        }
        
        // 2. 查询账务单
        Folio folio = folioMapper.selectById(originalTransaction.getFolioId());
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "账务单不存在");
        }
        
        // 3. 验证账务单状态
        if (FolioConstants.FOLIO_CLOSED.equals(folio.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "账务单已关闭，无法冲账");
        }
        
        // 4. 创建冲账交易
        FinTransaction reversalTransaction = new FinTransaction();
        reversalTransaction.setHotelId(originalTransaction.getHotelId());
        reversalTransaction.setTransactionNo(generateTransactionNo(originalTransaction.getHotelId()));
        reversalTransaction.setFolioId(originalTransaction.getFolioId());
        reversalTransaction.setType(FolioConstants.TRANSACTION_REVERSAL);
        reversalTransaction.setAmount(originalTransaction.getAmount());
        reversalTransaction.setPaymentMethod(originalTransaction.getPaymentMethod());
        reversalTransaction.setDescription("冲账：" + reason);
        reversalTransaction.setReversalTransactionId(transactionId);
        reversalTransaction.setOperatorId(originalTransaction.getOperatorId());
        finTransactionMapper.insert(reversalTransaction);
        
        // 5. 更新账务单
        if (isPaymentType(originalTransaction.getType())) {
            // 原交易是付款，冲账后减少已付金额
            folio.setPaidAmount(folio.getPaidAmount().subtract(originalTransaction.getAmount()));
            folio.setBalance(folio.getBalance().add(originalTransaction.getAmount()));
        } else if (FolioConstants.TRANSACTION_REFUND.equals(originalTransaction.getType())) {
                        // 原交易是退款，冲账后增加已付金额
            folio.setPaidAmount(folio.getPaidAmount().add(originalTransaction.getAmount()));
            folio.setBalance(folio.getBalance().subtract(originalTransaction.getAmount()));
        }
        folioMapper.updateById(folio);
        
        log.info("冲账成功：原交易�?{}, 冲账交易�?{}, 原因={}", 
                originalTransaction.getTransactionNo(), reversalTransaction.getTransactionNo(), reason);
        
        return convertTransactionToVO(reversalTransaction);
    }
    
    /**
     * 查询账务单单交易记�?     * 
     * @param folioId 账务单ID
     * @return 交易记录列表
     */
    public List<TransactionVO> getTransactionsByFolioId(Long folioId) {
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getFolioId, folioId)
               .orderByDesc(FinTransaction::getCreatedAt);
        
        List<FinTransaction> transactions = finTransactionMapper.selectList(wrapper);
        return transactions.stream()
                .map(this::convertTransactionToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建账务�?     */
    private Folio createFolio(Stay stay) {
        Folio folio = new Folio();
        folio.setHotelId(stay.getHotelId());
        folio.setFolioNo(generateFolioNo(stay.getHotelId()));
        folio.setStayId(stay.getId());
        folio.setGuestId(stay.getGuestId());
        folio.setTotalAmount(stay.getTotalAmount() != null ? stay.getTotalAmount() : BigDecimal.ZERO);
        folio.setPaidAmount(BigDecimal.ZERO);
        folio.setBalance(stay.getTotalAmount() != null ? stay.getTotalAmount() : BigDecimal.ZERO);
        folio.setStatus(FolioConstants.FOLIO_OPEN);
        folioMapper.insert(folio);
        
        return folio;
    }
    
    /**
     * 验证挂账公司
     */
    private void validateCreditCompany(Long creditCompanyId, BigDecimal amount) {
        if (creditCompanyId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "挂账时必须选择挂账公司");
        }
        
        CreditCompany company = creditCompanyMapper.selectById(creditCompanyId);
        if (company == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "挂账公司不存在");
        }
        
        if (!FolioConstants.CREDIT_COMPANY_ACTIVE.equals(company.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "挂账公司已停用");
        }
        
        // 检查信用额度
        BigDecimal newBalance = company.getCurrentBalance().add(amount);
        if (company.getCreditLimit().compareTo(BigDecimal.ZERO) > 0 && 
            newBalance.compareTo(company.getCreditLimit()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "挂账金额超出信用额度");
        }
    }
    
    /**
     * 更新挂账公司余额
     */
    private void updateCreditCompanyBalance(Long creditCompanyId, BigDecimal amount) {
        CreditCompany company = creditCompanyMapper.selectById(creditCompanyId);
        if (company != null) {
            company.setCurrentBalance(company.getCurrentBalance().add(amount));
            creditCompanyMapper.updateById(company);
        }
    }
    
    /**
     * 判断是否是付款类�?     */
    private boolean isPaymentType(String type) {
        return FolioConstants.TRANSACTION_DEPOSIT.equals(type) ||
               FolioConstants.TRANSACTION_ROOM_FEE.equals(type) ||
               FolioConstants.TRANSACTION_EXTRA.equals(type) ||
               FolioConstants.TRANSACTION_PAYMENT.equals(type);
    }
    
    /**
     * 生成账务单号
     */
    private String generateFolioNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = "F" + dateStr;
        
        LambdaQueryWrapper<Folio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folio::getHotelId, hotelId)
               .likeRight(Folio::getFolioNo, prefix)
               .orderByDesc(Folio::getFolioNo)
               .last("LIMIT 1");
        
        Folio latest = folioMapper.selectOne(wrapper);
        
        int nextSeq = 1;
        if (latest != null && latest.getFolioNo() != null) {
            String lastNo = latest.getFolioNo();
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
     * 生成交易�?     */
    /**
     * 生成押金单号
     * <p>
     * 格式：DEP + 日期(yyyyMMdd) + 4位序号
     * </p>
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
            case FolioConstants.PAYMENT_CREDIT: return "挂账";
            default: return paymentMethod;
        }
    }
    
    /**
     * 获取交易类型名称
     */
    private String getTransactionTypeName(String type) {
        if (type == null) return "";
        switch (type) {
            case FolioConstants.TRANSACTION_DEPOSIT: return "押金";
            case FolioConstants.TRANSACTION_ROOM_FEE: return "房费";
            case FolioConstants.TRANSACTION_EXTRA: return "杂费";
            case FolioConstants.TRANSACTION_PAYMENT: return "付款";
            case FolioConstants.TRANSACTION_REFUND: return "退款";
            case FolioConstants.TRANSACTION_REVERSAL: return "冲账";
            default: return type;
        }
    }
    
    /**
     * 转换为VO
     */
    private FolioVO convertToVO(Folio folio) {
        FolioVO vo = new FolioVO();
        vo.setId(folio.getId());
        vo.setFolioNo(folio.getFolioNo());
        vo.setHotelId(folio.getHotelId());
        vo.setStayId(folio.getStayId());
        vo.setGuestId(folio.getGuestId());
        vo.setTotalAmount(folio.getTotalAmount());
        vo.setPaidAmount(folio.getPaidAmount());
        vo.setBalance(folio.getBalance());
        vo.setStatus(folio.getStatus());
        vo.setStatusName(FolioConstants.FOLIO_OPEN.equals(folio.getStatus()) ? "开启" : "关闭");
        vo.setCreatedAt(folio.getCreatedAt());
        vo.setUpdatedAt(folio.getUpdatedAt());
        
        // 查询入住单号
        if (folio.getStayId() != null) {
            Stay stay = stayMapper.selectById(folio.getStayId());
            if (stay != null) {
                vo.setStayNo(stay.getStayNo());
            }
        }
        
        // 查询客人姓名
        if (folio.getGuestId() != null) {
            Guest guest = guestMapper.selectById(folio.getGuestId());
            if (guest != null) {
                vo.setGuestName(guest.getName());
            }
        }
        
        // 查询交易记录
        vo.setTransactions(getTransactionsByFolioId(folio.getId()));
        
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
        vo.setTypeName(getTransactionTypeName(transaction.getType()));
        vo.setAmount(transaction.getAmount());
        vo.setPaymentMethod(transaction.getPaymentMethod());
        vo.setPaymentMethodName(getPaymentMethodName(transaction.getPaymentMethod()));
        vo.setDescription(transaction.getDescription());
        vo.setCreditCompanyId(transaction.getCreditCompanyId());
        vo.setRefundTransactionId(transaction.getRefundTransactionId());
        vo.setReversalTransactionId(transaction.getReversalTransactionId());
        vo.setOperatorId(transaction.getOperatorId());
        vo.setCreatedAt(transaction.getCreatedAt());
        
        // 查询入住单号
        if (transaction.getFolioId() != null) {
            Folio folio = folioMapper.selectById(transaction.getFolioId());
            if (folio != null && folio.getStayId() != null) {
                Stay stay = stayMapper.selectById(folio.getStayId());
                if (stay != null) {
                    vo.setStayNo(stay.getStayNo());
                }
            }
        }
        
        // 查询挂账公司名称
        if (transaction.getCreditCompanyId() != null) {
            CreditCompany company = creditCompanyMapper.selectById(transaction.getCreditCompanyId());
            if (company != null) {
                vo.setCreditCompanyName(company.getCompanyName());
            }
        }
        
        return vo;
    }
}



