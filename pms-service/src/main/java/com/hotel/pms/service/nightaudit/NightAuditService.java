package com.hotel.pms.service.nightaudit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.entity.TeamReservation;
import com.hotel.pms.dao.entity.HotelConfig;
import com.hotel.pms.dao.mapper.*;
import com.hotel.pms.dao.mapper.GuestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;
import com.hotel.pms.common.constant.FolioConstants;
import com.hotel.pms.common.constant.StayConstants;
import com.hotel.pms.common.constant.RoomConstants;
import com.hotel.pms.service.price.RoomPricePlanService;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 夜审服务类
 */
@Slf4j
@Service
public class NightAuditService {
    
    @Autowired
    private NightAuditMapper nightAuditMapper;
    
    @Autowired
    private NightAuditStepMapper nightAuditStepMapper;
    
    @Autowired
    private StayMapper stayMapper;
    
    @Autowired
    private FolioMapper folioMapper;
    
    @Autowired
    private FinTransactionMapper finTransactionMapper;
    
    @Autowired
    private HotelMapper hotelMapper;
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private RoomPriceMapper roomPriceMapper;
    
    @Autowired
    private TeamFolioMapper teamFolioMapper;    
    @Autowired
    private TeamReservationMapper teamReservationMapper;    
    @Autowired
    private RoomPricePlanService roomPricePlanService;
    
    @Autowired
    private RoomTypeMapper roomTypeMapper;    
    @Autowired
    private GuestMapper guestMapper;    
    @Autowired
    private HotelConfigMapper hotelConfigMapper;
    
    /**
     * 执行夜审
     */
    @Transactional(rollbackFor = Exception.class)
    public NightAuditVO executeNightAudit(Long hotelId) {
        // 1. 检查是否已执行过夜审
        LocalDate auditDate = LocalDate.now();
        NightAudit existingAudit = nightAuditMapper.selectOne(
            new LambdaQueryWrapper<NightAudit>()
                .eq(NightAudit::getHotelId, hotelId)
                .eq(NightAudit::getAuditDate, auditDate)
        );
        
        if (existingAudit != null) {
            if ("IN_PROGRESS".equals(existingAudit.getStatus())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "夜审正在执行中，请稍后再试");
            }
            if ("COMPLETED".equals(existingAudit.getStatus()) || "COMPLETED_WITH_ERRORS".equals(existingAudit.getStatus())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "今日夜审已执行");
            }
            // If status is FAILED or PENDING, delete old steps and reuse the record
            if ("FAILED".equals(existingAudit.getStatus()) || "PENDING".equals(existingAudit.getStatus())) {
                log.info("重用已有夜审记录: id={}, status={}", existingAudit.getId(), existingAudit.getStatus());
                // Delete old steps
                nightAuditStepMapper.delete(
                    new LambdaQueryWrapper<NightAuditStep>()
                        .eq(NightAuditStep::getNightAuditId, existingAudit.getId())
                );
                // Reset the audit record
                existingAudit.setStatus("IN_PROGRESS");
                existingAudit.setStartedAt(LocalDateTime.now());
                existingAudit.setCompletedAt(null);
                existingAudit.setErrorMessage(null);
                existingAudit.setVersion(existingAudit.getVersion() + 1);
                nightAuditMapper.updateById(existingAudit);
                // Create new steps and execute
                createNightAuditSteps(existingAudit.getId());
                executeNightAuditSteps(existingAudit);
                return convertToVO(nightAuditMapper.selectById(existingAudit.getId()));
            }
        }
        
        // 2. 创建夜审记录
        NightAudit nightAudit = new NightAudit();
        nightAudit.setHotelId(hotelId);
        nightAudit.setAuditDate(auditDate);
        nightAudit.setStatus("IN_PROGRESS");
        nightAudit.setStartedAt(LocalDateTime.now());
        nightAudit.setVersion(0);
        nightAuditMapper.insert(nightAudit);
        
        // 3. 创建夜审步骤
        createNightAuditSteps(nightAudit.getId());
        
        // 4. 分步执行夜审
        executeNightAuditSteps(nightAudit);
        
        // 重新查询以获取最新数据
        return convertToVO(nightAuditMapper.selectById(nightAudit.getId()));
    }
    
    /**
     * 创建夜审步骤
     */
    private void createNightAuditSteps(Long nightAuditId) {
        List<NightAuditStep> steps = Arrays.asList(
            createStep(nightAuditId, "PRE_CHECK", 1),
            createStep(nightAuditId, "AUTO_POST_ROOM_CHARGES", 2),
            createStep(nightAuditId, "HANDLE_OVERTIME", 3),
            createStep(nightAuditId, "TEAM_FOLIO_SUMMARY", 4),
            createStep(nightAuditId, "POLICE_UPLOAD_CHECK", 5),
            createStep(nightAuditId, "CALCULATE_STATISTICS", 6),
            createStep(nightAuditId, "GENERATE_DAILY_REPORT", 7),
            createStep(nightAuditId, "LOCK_DATA", 8)
        );
        
        steps.forEach(step -> nightAuditStepMapper.insert(step));
    }
    
    /**
     * 创建单个步骤
     */
    private NightAuditStep createStep(Long nightAuditId, String stepName, Integer stepOrder) {
        NightAuditStep step = new NightAuditStep();
        step.setNightAuditId(nightAuditId);
        step.setStepName(stepName);
        step.setStepOrder(stepOrder);
        step.setStatus("PENDING");
        step.setRetryCount(0);
        return step;
    }
    
    /**
     * 执行夜审步骤
     */
    private void executeNightAuditSteps(NightAudit nightAudit) {
        List<NightAuditStep> steps = nightAuditStepMapper.selectList(
            new LambdaQueryWrapper<NightAuditStep>()
                .eq(NightAuditStep::getNightAuditId, nightAudit.getId())
                .orderByAsc(NightAuditStep::getStepOrder)
        );
        
        boolean hasError = false;
        
        for (NightAuditStep step : steps) {
            try {
                executeStep(step, nightAudit);
            } catch (Exception e) {
                log.error("夜审步骤执行失败: {}", step.getStepName(), e);
                step.setStatus("FAILED");
                step.setErrorMessage(e.getMessage());
                step.setCompletedAt(LocalDateTime.now());
                nightAuditStepMapper.updateById(step);
                hasError = true;
                // 继续执行后续步骤
            }
        }
        
        // 更新夜审状态
        nightAudit.setStatus(hasError ? "COMPLETED_WITH_ERRORS" : "COMPLETED");
        nightAudit.setCompletedAt(LocalDateTime.now());
        nightAuditMapper.updateById(nightAudit);
        
        // 执行夜审后数据校验
        try {
            executePostCheck(nightAudit);
        } catch (Exception e) {
            log.warn("夜审后校验警告: {}", e.getMessage());
        }
    }
    
    /**
     * 执行单个步骤
     */
    private void executeStep(NightAuditStep step, NightAudit nightAudit) {
        step.setStatus("IN_PROGRESS");
        step.setStartedAt(LocalDateTime.now());
        nightAuditStepMapper.updateById(step);
        
        switch (step.getStepName()) {
            case "PRE_CHECK":
                executePreCheck(nightAudit);
                break;
            case "AUTO_POST_ROOM_CHARGES":
                executeAutoPostRoomCharges(nightAudit);
                break;
            case "HANDLE_OVERTIME":
                executeHandleOvertime(nightAudit);
                break;
            case "TEAM_FOLIO_SUMMARY":
                executeTeamFolioSummary(nightAudit);
                break;
            case "POLICE_UPLOAD_CHECK":
                executePoliceUploadCheck(nightAudit);
                break;
            case "CALCULATE_STATISTICS":
                executeCalculateStatistics(nightAudit);
                break;
            case "GENERATE_DAILY_REPORT":
                executeGenerateDailyReport(nightAudit);
                break;
            case "LOCK_DATA":
                executeLockData(nightAudit);
                break;
            default:
                throw new BusinessException(ResultCode.BAD_REQUEST, "未知的夜审步骤: " + step.getStepName());
        }
        
        step.setStatus("COMPLETED");
        step.setCompletedAt(LocalDateTime.now());
        nightAuditStepMapper.updateById(step);
    }
    
    /**
     * 预检查
     * <p>
     * 夜审前数据完整性校验：
     * 1. 检查酒店是否存在且启用
     * 2. 检查在住客人证件信息完整性
     * 3. 检查账务单数据一致性
     * 4. 检查房间状态一致性
     * 5. 记录校验结果和警告
     * </p>
     */
    private void executePreCheck(NightAudit nightAudit) {
        Long hotelId = nightAudit.getHotelId();
        log.info("执行预检查: hotelId={}", hotelId);
        
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // 1. 检查酒店是否存在且启用
        Hotel hotel = hotelMapper.selectById(hotelId);
        if (hotel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "酒店不存在");
        }
        if (!"ACTIVE".equals(hotel.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "酒店已停用，无法执行夜审");
        }
        
        // 2. 检查在住客人证件信息完整性
        List<Stay> stays = stayMapper.selectList(
            new LambdaQueryWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
        );
        
        int staysWithoutGuest = 0;
        int staysWithIncompleteGuest = 0;
        
        for (Stay stay : stays) {
            if (stay.getGuestId() == null) {
                staysWithoutGuest++;
                continue;
            }
            
            Guest guest = guestMapper.selectById(stay.getGuestId());
            if (guest == null) {
                staysWithoutGuest++;
            } else if (guest.getIdNo() == null || guest.getIdNo().isEmpty()) {
                staysWithIncompleteGuest++;
            }
        }
        
        if (staysWithoutGuest > 0) {
            warnings.add(String.format("有 %d 个入住单未关联客人信息", staysWithoutGuest));
        }
        if (staysWithIncompleteGuest > 0) {
            warnings.add(String.format("有 %d 个客人缺少证件号码", staysWithIncompleteGuest));
        }
        
        // 3. 检查账务单数据一致性
        int staysWithoutFolio = 0;
        for (Stay stay : stays) {
            Long folioCount = folioMapper.selectCount(
                new LambdaQueryWrapper<Folio>()
                    .eq(Folio::getStayId, stay.getId())
                    .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
            );
            if (folioCount == 0) {
                staysWithoutFolio++;
            }
        }
        
        if (staysWithoutFolio > 0) {
            warnings.add(String.format("有 %d 个入住单没有开放的账务单", staysWithoutFolio));
        }
        
        // 4. 检查房间状态一致性
        Long occupiedRoomCount = roomMapper.selectCount(
            new LambdaQueryWrapper<Room>()
                .eq(Room::getHotelId, hotelId)
                .eq(Room::getStatus, RoomConstants.STATUS_OCCUPIED)
        );
        
        if (occupiedRoomCount != stays.size()) {
            warnings.add(String.format("房间状态不一致：在住房间数(%d)与入住单数(%d)不匹配", 
                    occupiedRoomCount, stays.size()));
        }
        
        // 5. 检查是否有重复的入住单（同一房间多个在住）
        Map<Long, Long> roomStayCount = stays.stream()
            .collect(Collectors.groupingBy(Stay::getRoomId, Collectors.counting()));
        
        long duplicateRooms = roomStayCount.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .count();
        
        if (duplicateRooms > 0) {
            errors.add(String.format("有 %d 个房间存在多个在住入住单", duplicateRooms));
        }
        
        // 记录校验结果
        if (!warnings.isEmpty()) {
            log.warn("预检查警告: {}", String.join("; ", warnings));
        }
        if (!errors.isEmpty()) {
            log.error("预检查错误: {}", String.join("; ", errors));
            throw new BusinessException(ResultCode.BAD_REQUEST, 
                    "数据校验失败: " + String.join("; ", errors));
        }
        
        log.info("预检查完成: 在住入住单={}, 警告={}", stays.size(), warnings.size());
    }
    
    /**
     * 自动过房费
     */
    private void executeAutoPostRoomCharges(NightAudit nightAudit) {
        Long hotelId = nightAudit.getHotelId();
        LocalDate auditDate = nightAudit.getAuditDate();
        
        // 1. 查询所有在住且未锁定的入住单
        List<Stay> stays = stayMapper.selectList(
            new LambdaQueryWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
                .eq(Stay::getIsLocked, false)
        );
        
        BigDecimal totalRoomRevenue = BigDecimal.ZERO;
        int processedCount = 0;
        
        for (Stay stay : stays) {
            try {
                // 2. 获取房间信息
                Room room = roomMapper.selectById(stay.getRoomId());
                if (room == null) {
                    log.warn("房间不存在: roomId={}", stay.getRoomId());
                    continue;
                }
                
                // 3. 查询当日房价（无论是否已过账，都需要统计）
                BigDecimal dailyPrice = getDailyPrice(hotelId, room.getRoomTypeId(), auditDate, stay.getPricePlanId());
                
                // 4. 检查当日是否已过账（防重复）
                if (hasRoomFeeForDate(stay.getId(), auditDate)) {
                    // 已过账或已收款，只统计房费收入，不创建新的交易记录
                    totalRoomRevenue = totalRoomRevenue.add(dailyPrice);
                    processedCount++;
                    log.info("跳过过账（已过房费/已收款）：入住单={}, 房间={}, 金额={}", stay.getStayNo(), room.getRoomNo(), dailyPrice);
                } else {
                    // 5. 创建房费交易
                    createRoomFeeTransaction(stay, dailyPrice, auditDate);
                    
                    // 6. 更新账务单
                    updateFolioForRoomCharge(stay.getId(), dailyPrice);
                    
                    // 7. 更新入住单总金额
                    stay.setTotalAmount(stay.getTotalAmount().add(dailyPrice));
                    stayMapper.updateById(stay);
                    
                    totalRoomRevenue = totalRoomRevenue.add(dailyPrice);
                    processedCount++;
                    
                    log.info("过房费成功：入住单={}, 房间={}, 金额={}", stay.getStayNo(), room.getRoomNo(), dailyPrice);
                }
            } catch (Exception e) {
                log.error("过房费失败：入住单 {}", stay.getStayNo(), e);
                // 继续处理其他入住单，不中断
            }
        }
        
        // 8. 更新夜审统计
        nightAudit.setRoomRevenue(totalRoomRevenue);
        nightAudit.setOccupiedRooms(processedCount);
        nightAuditMapper.updateById(nightAudit);
        
        log.info("自动过房费完成：处理 {} 间，房费收入 {}", processedCount, totalRoomRevenue);
    }
    /**
     * 检查当日是否已过账（防重复）
     */
    /**
     * 检查当日是否已过账（防重复）
     * 检查逻辑：
     * 1. 如果当日有 ROOM_FEE 交易，说明已过账，跳过
     * 2. 如果有任何 PAYMENT 或 DEPOSIT 交易（不限日期），说明已收款，跳过
     */
    private boolean hasRoomFeeForDate(Long stayId, LocalDate date) {
        // 通过入住单找到账务单
        Folio folio = folioMapper.selectOne(
            new LambdaQueryWrapper<Folio>()
                .eq(Folio::getStayId, stayId)
                .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
        );
        if (folio == null) return false;
        
        // 检查当日是否有 ROOM_FEE 交易（房费已过账）
        Long roomFeeCount = finTransactionMapper.selectCount(
            new LambdaQueryWrapper<FinTransaction>()
                .eq(FinTransaction::getFolioId, folio.getId())
                .eq(FinTransaction::getType, FolioConstants.TRANSACTION_ROOM_FEE)
                .apply("DATE(created_at) = {0}", date)
        );
        if (roomFeeCount > 0) {
            return true; // 当日已过房费，跳过
        }
        
        // 检查是否有 PAYMENT 交易（已收款，不限日期）
        // 因为用户可能在入住前就付款了（如预订付款）
        Long paymentCount = finTransactionMapper.selectCount(
            new LambdaQueryWrapper<FinTransaction>()
                .eq(FinTransaction::getFolioId, folio.getId())
                .eq(FinTransaction::getType, FolioConstants.TRANSACTION_PAYMENT)
        );
        if (paymentCount > 0) {
            return true; // 已有收款记录，跳过（避免重复计算）
        }
        
        // 检查是否有 DEPOSIT 交易（押金已收款，不限日期）
        Long depositCount = finTransactionMapper.selectCount(
            new LambdaQueryWrapper<FinTransaction>()
                .eq(FinTransaction::getFolioId, folio.getId())
                .eq(FinTransaction::getType, FolioConstants.TRANSACTION_DEPOSIT)
        );
        if (depositCount > 0) {
            return true; // 已有押金收款记录，跳过（避免重复计算）
        }
        
        return false;
    }
    
    private BigDecimal getDailyPrice(Long hotelId, Long roomTypeId, LocalDate date, Long pricePlanId) {
        // 优先级1：房价码价格
        if (pricePlanId != null) {
            BigDecimal planPrice = roomPricePlanService.getPriceByPlanAndRoomType(pricePlanId, roomTypeId);
            if (planPrice != null) {
                return planPrice;
            }
        }
        
        // 优先级2：日历价
        RoomPrice roomPrice = roomPriceMapper.selectOne(
            new LambdaQueryWrapper<RoomPrice>()
                .eq(RoomPrice::getHotelId, hotelId)
                .eq(RoomPrice::getRoomTypeId, roomTypeId)
                .eq(RoomPrice::getPriceDate, date)
        );
        if (roomPrice != null) {
            return roomPrice.getPrice();
        }
        
        // 优先级3：房型基础价
        RoomType roomType = roomTypeMapper.selectById(roomTypeId);
        if (roomType != null && roomType.getBasePrice() != null) {
            return roomType.getBasePrice();
        }
        
        throw new BusinessException(ResultCode.DATA_NOT_FOUND, "无法获取房价：roomTypeId=" + roomTypeId);
    }
    
    /**
     * 创建房费交易记录
     */
    private void createRoomFeeTransaction(Stay stay, BigDecimal amount, LocalDate date) {
        Folio folio = folioMapper.selectOne(
            new LambdaQueryWrapper<Folio>()
                .eq(Folio::getStayId, stay.getId())
                .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
        );
        
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "账务单不存在：stayId=" + stay.getId());
        }
        
        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(stay.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(stay.getHotelId()));
        transaction.setFolioId(folio.getId());
        transaction.setType(FolioConstants.TRANSACTION_ROOM_FEE);
        transaction.setAmount(amount);
        transaction.setDescription(date + " 房费");
        transaction.setCreatedAt(date.atStartOfDay());
        finTransactionMapper.insert(transaction);
    }
    
    /**
     * 生成交易流水号
     */
    private String generateTransactionNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = System.currentTimeMillis() % 1000000;
        return "T" + dateStr + String.format("%06d", seq);
    }
    
    /**
     * 更新账务单金额
     */
    private void updateFolioForRoomCharge(Long stayId, BigDecimal amount) {
        Folio folio = folioMapper.selectOne(
            new LambdaQueryWrapper<Folio>()
                .eq(Folio::getStayId, stayId)
                .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
        );
        
        if (folio != null) {
            folio.setTotalAmount(folio.getTotalAmount().add(amount));
            folio.setBalance(folio.getBalance().add(amount));
            folioMapper.updateById(folio);
        }
    }
    
    /**
     * 处理超时离店
     * <p>
     * 业务规则：
     * - 默认离店时间 12:00
     * - 12:00-18:00 超时收半日租
     * - 18:00 后收全日租
     * </p>
     */
    private void executeHandleOvertime(NightAudit nightAudit) {
        Long hotelId = nightAudit.getHotelId();
        LocalDate auditDate = nightAudit.getAuditDate();
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 获取酒店配置
        LocalTime checkOutTime = getHotelConfigTime(hotelId, "CHECKOUT_TIME", LocalTime.of(12, 0));
        LocalTime halfDayDeadline = getHotelConfigTime(hotelId, "LATE_CHECKOUT_HALF_DAY_TIME", LocalTime.of(18, 0));
        BigDecimal halfDayRate = getHotelConfigRate(hotelId, "LATE_CHECKOUT_HALF_DAY_RATE", new BigDecimal("0.5"));
        BigDecimal fullDayRate = getHotelConfigRate(hotelId, "LATE_CHECKOUT_FULL_DAY_RATE", BigDecimal.ONE);
        
        // 2. 计算今日的离店截止时间
        LocalDateTime todayCheckOutDeadline = auditDate.atTime(checkOutTime);
        LocalDateTime todayHalfDayDeadline = auditDate.atTime(halfDayDeadline);
        
        // 3. 查询超时未离店的入住单
        // 条件：预计离店时间 < 当前时间，且状态为在住
        List<Stay> overtimeStays = stayMapper.selectList(
            new LambdaQueryWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
                .eq(Stay::getIsLocked, false)
                .lt(Stay::getCheckOutTime, now)
        );
        
        int processedCount = 0;
        BigDecimal totalLateFee = BigDecimal.ZERO;
        
        for (Stay stay : overtimeStays) {
            try {
                // 检查今日是否已收过超时费
                if (hasLateFeeForDate(stay.getId(), auditDate)) {
                    log.info("跳过：入住单 {} 今日已收超时费", stay.getStayNo());
                    continue;
                }
                
                // 获取房间信息和房价
                Room room = roomMapper.selectById(stay.getRoomId());
                if (room == null) {
                    log.warn("房间不存在: roomId={}", stay.getRoomId());
                    continue;
                }
                
                BigDecimal dailyPrice = getDailyPrice(hotelId, room.getRoomTypeId(), auditDate, stay.getPricePlanId());
                
                // 计算超时费
                BigDecimal lateFee = calculateLateFee(stay, dailyPrice, checkOutTime, halfDayDeadline, halfDayRate, fullDayRate, now);
                
                if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
                    // 创建超时费交易
                    createLateFeeTransaction(stay, lateFee, auditDate);
                    
                    // 更新账务单
                    updateFolioForRoomCharge(stay.getId(), lateFee);
                    
                    // 更新入住单总金额
                    stay.setTotalAmount(stay.getTotalAmount().add(lateFee));
                    stayMapper.updateById(stay);
                    
                    totalLateFee = totalLateFee.add(lateFee);
                    processedCount++;
                    
                    log.info("超时费收取成功：入住单={}, 房间={}, 金额={}", stay.getStayNo(), room.getRoomNo(), lateFee);
                }
                
            } catch (Exception e) {
                log.error("处理超时离店失败：入住单 {}", stay.getStayNo(), e);
            }
        }
        
        // 4. 更新夜审统计
        nightAudit.setExtraRevenue(nightAudit.getExtraRevenue() != null ? 
            nightAudit.getExtraRevenue().add(totalLateFee) : totalLateFee);
        nightAuditMapper.updateById(nightAudit);
        
        log.info("处理超时离店完成：处理 {} 间，超时费收入 {}", processedCount, totalLateFee);
    }
    
    /**
     * 计算超时费
     */
    private BigDecimal calculateLateFee(Stay stay, BigDecimal dailyPrice, 
            LocalTime checkOutTime, LocalTime halfDayDeadline, 
            BigDecimal halfDayRate, BigDecimal fullDayRate, LocalDateTime now) {
        
        LocalDateTime stayCheckOutTime = stay.getCheckOutTime();
        if (stayCheckOutTime == null) {
            return BigDecimal.ZERO;
        }
        
        // 计算超时小时数
        long hoursLate = java.time.Duration.between(stayCheckOutTime, now).toHours();
        
        if (hoursLate <= 0) {
            return BigDecimal.ZERO;
        }
        
        LocalTime currentTime = now.toLocalTime();
        
        // 判断收取半日租还是全日租
        if (currentTime.isBefore(halfDayDeadline)) {
            // 12:00-18:00 收半日租
            return dailyPrice.multiply(halfDayRate).setScale(2, RoundingMode.HALF_UP);
        } else {
            // 18:00后 收全日租
            return dailyPrice.multiply(fullDayRate).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * 检查当日是否已收过超时费
     */
    private boolean hasLateFeeForDate(Long stayId, LocalDate date) {
        Folio folio = folioMapper.selectOne(
            new LambdaQueryWrapper<Folio>()
                .eq(Folio::getStayId, stayId)
                .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
        );
        if (folio == null) return false;
        
        Long count = finTransactionMapper.selectCount(
            new LambdaQueryWrapper<FinTransaction>()
                .eq(FinTransaction::getFolioId, folio.getId())
                .eq(FinTransaction::getType, FolioConstants.TRANSACTION_LATE_FEE)
                .apply("DATE(created_at) = {0}", date)
        );
        return count > 0;
    }
    
    /**
     * 创建超时费交易记录
     */
    private void createLateFeeTransaction(Stay stay, BigDecimal amount, LocalDate date) {
        Folio folio = folioMapper.selectOne(
            new LambdaQueryWrapper<Folio>()
                .eq(Folio::getStayId, stay.getId())
                .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
        );
        
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "账务单不存在：stayId=" + stay.getId());
        }
        
        FinTransaction transaction = new FinTransaction();
        transaction.setHotelId(stay.getHotelId());
        transaction.setTransactionNo(generateTransactionNo(stay.getHotelId()));
        transaction.setFolioId(folio.getId());
        transaction.setType(FolioConstants.TRANSACTION_LATE_FEE);
        transaction.setAmount(amount);
        transaction.setDescription(date + " 超时离店费");
        transaction.setCreatedAt(date.atStartOfDay());
        finTransactionMapper.insert(transaction);
    }
    
    /**
     * 获取酒店配置（时间类型）
     */
    private LocalTime getHotelConfigTime(Long hotelId, String configKey, LocalTime defaultValue) {
        HotelConfig config = hotelConfigMapper.selectOne(
            new LambdaQueryWrapper<HotelConfig>()
                .eq(HotelConfig::getHotelId, hotelId)
                .eq(HotelConfig::getConfigKey, configKey)
        );
        
        if (config != null && config.getConfigValue() != null) {
            try {
                return LocalTime.parse(config.getConfigValue());
            } catch (Exception e) {
                log.warn("解析配置时间失败: key={}, value={}", configKey, config.getConfigValue());
            }
        }
        return defaultValue;
    }
    
    /**
     * 获取酒店配置（费率类型）
     */
    private BigDecimal getHotelConfigRate(Long hotelId, String configKey, BigDecimal defaultValue) {
        HotelConfig config = hotelConfigMapper.selectOne(
            new LambdaQueryWrapper<HotelConfig>()
                .eq(HotelConfig::getHotelId, hotelId)
                .eq(HotelConfig::getConfigKey, configKey)
        );
        
        if (config != null && config.getConfigValue() != null) {
            try {
                return new BigDecimal(config.getConfigValue());
            } catch (Exception e) {
                log.warn("解析配置费率失败: key={}, value={}", configKey, config.getConfigValue());
            }
        }
        return defaultValue;
    }
    
    /**
     * 团队账务汇总
     * <p>
     * 对在住的团队入住单进行账务汇总：
     * - 统一结算：汇总成员账务到团队账务单
     * - 分开结算：仅记录汇总信息
     * </p>
     */
    private void executeTeamFolioSummary(NightAudit nightAudit) {
        Long hotelId = nightAudit.getHotelId();
        LocalDate auditDate = nightAudit.getAuditDate();
        
        // 1. 查询所有在住的团队入住单
        List<Stay> teamStays = stayMapper.selectList(
            new LambdaQueryWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
                .eq(Stay::getCheckInType, "TEAM")
                .isNotNull(Stay::getTeamReservationId)
        );
        
        if (teamStays.isEmpty()) {
            log.info("无在住团队入住单，跳过团队账务汇总");
            return;
        }
        
        // 2. 按团队预订ID分组
        Map<Long, List<Stay>> teamGroups = teamStays.stream()
            .collect(Collectors.groupingBy(Stay::getTeamReservationId));
        
        int processedTeams = 0;
        BigDecimal totalTeamRevenue = BigDecimal.ZERO;
        
        for (Map.Entry<Long, List<Stay>> entry : teamGroups.entrySet()) {
            Long teamReservationId = entry.getKey();
            List<Stay> staysInTeam = entry.getValue();
            
            try {
                // 3. 查询团队预订信息
                TeamReservation teamReservation = teamReservationMapper.selectById(teamReservationId);
                if (teamReservation == null) {
                    log.warn("团队预订不存在: teamReservationId={}", teamReservationId);
                    continue;
                }
                
                // 4. 汇总团队成员的账务
                BigDecimal teamTotalAmount = BigDecimal.ZERO;
                BigDecimal teamPaidAmount = BigDecimal.ZERO;
                BigDecimal teamBalance = BigDecimal.ZERO;
                
                for (Stay stay : staysInTeam) {
                    // 查询入住单对应的账务单
                    Folio folio = folioMapper.selectOne(
                        new LambdaQueryWrapper<Folio>()
                            .eq(Folio::getStayId, stay.getId())
                            .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
                    );
                    
                    if (folio != null) {
                        teamTotalAmount = teamTotalAmount.add(
                            folio.getTotalAmount() != null ? folio.getTotalAmount() : BigDecimal.ZERO);
                        teamPaidAmount = teamPaidAmount.add(
                            folio.getPaidAmount() != null ? folio.getPaidAmount() : BigDecimal.ZERO);
                        teamBalance = teamBalance.add(
                            folio.getBalance() != null ? folio.getBalance() : BigDecimal.ZERO);
                    }
                }
                
                // 5. 根据结算方式处理
                if ("UNIFIED".equals(teamReservation.getSettlementType())) {
                    // 统一结算：更新团队账务单
                    updateTeamFolio(teamReservationId, teamTotalAmount, teamPaidAmount, teamBalance);
                    log.info("团队账务汇总（统一结算）: teamName={}, 成员数={}, 总金额={}", 
                            teamReservation.getTeamName(), staysInTeam.size(), teamTotalAmount);
                } else {
                    // 分开结算：仅记录汇总信息
                    log.info("团队账务汇总（分开结算）: teamName={}, 成员数={}, 总金额={}", 
                            teamReservation.getTeamName(), staysInTeam.size(), teamTotalAmount);
                }
                
                totalTeamRevenue = totalTeamRevenue.add(teamTotalAmount);
                processedTeams++;
                
            } catch (Exception e) {
                log.error("团队账务汇总失败: teamReservationId={}", teamReservationId, e);
            }
        }
        
        // 6. 更新夜审统计
        if (nightAudit.getExtraRevenue() == null) {
            nightAudit.setExtraRevenue(BigDecimal.ZERO);
        }
        nightAuditMapper.updateById(nightAudit);
        
        log.info("团队账务汇总完成: 处理 {} 个团队，总金额 {}", processedTeams, totalTeamRevenue);
    }
    
    /**
     * 更新团队账务单
     */
    private void updateTeamFolio(Long teamReservationId, BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal balance) {
        // 查询团队账务单
        TeamFolio teamFolio = teamFolioMapper.selectOne(
            new LambdaQueryWrapper<TeamFolio>()
                .eq(TeamFolio::getTeamReservationId, teamReservationId)
                .eq(TeamFolio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
        );
        
        if (teamFolio != null) {
            // 更新现有账务单
            teamFolio.setTotalAmount(totalAmount);
            teamFolio.setPaidAmount(paidAmount);
            teamFolio.setBalance(balance);
            teamFolioMapper.updateById(teamFolio);
        } else {
            // 创建新的团队账务单
            TeamFolio newTeamFolio = new TeamFolio();
            newTeamFolio.setHotelId(teamReservationMapper.selectById(teamReservationId).getHotelId());
            newTeamFolio.setTeamReservationId(teamReservationId);
            newTeamFolio.setFolioNo(generateTeamFolioNo(teamReservationId));
            newTeamFolio.setTotalAmount(totalAmount);
            newTeamFolio.setPaidAmount(paidAmount);
            newTeamFolio.setBalance(balance);
            newTeamFolio.setStatus(StayConstants.FOLIO_STATUS_OPEN);
            teamFolioMapper.insert(newTeamFolio);
        }
    }
    
    /**
     * 生成团队账务单号
     */
    private String generateTeamFolioNo(Long teamReservationId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = System.currentTimeMillis() % 1000000;
        return "TF" + dateStr + String.format("%06d", seq);
    }
    
    /**
     * 公安上传检查
     */
    private void executePoliceUploadCheck(NightAudit nightAudit) {
        log.info("公安上传检查: hotelId={}", nightAudit.getHotelId());
        // 实现公安上传检查逻辑
    }
    
    /**
     * 计算统计指标
     * <p>
     * 计算酒店经营核心指标：
     * - 总房间数、在住房间数、可用房间数
     * - 入住率 (Occupancy Rate)
     * - 平均每日房价 ADR (Average Daily Rate)
     * - 每间可售房收入 RevPAR (Revenue Per Available Room)
     * </p>
     */
    private void executeCalculateStatistics(NightAudit nightAudit) {
        Long hotelId = nightAudit.getHotelId();
        log.info("计算统计指标: hotelId={}", hotelId);
        
        // 1. 计算总房间数
        Long totalRooms = roomMapper.selectCount(
            new LambdaQueryWrapper<Room>()
                .eq(Room::getHotelId, hotelId)
                .eq(Room::getDeleted, false)
        );
        
        // 2. 计算在住房间数
        Long occupiedRooms = stayMapper.selectCount(
            new LambdaQueryWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
        );
        
        // 3. 计算可用房间数
        Integer availableRooms = (int)(totalRooms - occupiedRooms);
        
        // 4. 计算入住率 (Occupancy Rate = 在住房间数 / 总房间数 × 100)
        BigDecimal occupancyRate = BigDecimal.ZERO;
        if (totalRooms > 0) {
            occupancyRate = new BigDecimal(occupiedRooms)
                .divide(new BigDecimal(totalRooms), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP);
        }
        
        // 5. 获取房费收入（在自动过房费步骤中已计算）
        BigDecimal roomRevenue = nightAudit.getRoomRevenue() != null ? 
            nightAudit.getRoomRevenue() : BigDecimal.ZERO;
        
        // 6. 计算ADR (Average Daily Rate = 房费收入 / 在住房间数)
        BigDecimal adr = BigDecimal.ZERO;
        if (occupiedRooms > 0 && roomRevenue.compareTo(BigDecimal.ZERO) > 0) {
            adr = roomRevenue.divide(new BigDecimal(occupiedRooms), 2, RoundingMode.HALF_UP);
        }
        
        // 7. 计算RevPAR (Revenue Per Available Room = 房费收入 / 总房间数)
        BigDecimal revpar = BigDecimal.ZERO;
        if (totalRooms > 0 && roomRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revpar = roomRevenue.divide(new BigDecimal(totalRooms), 2, RoundingMode.HALF_UP);
        }
        
        // 8. 更新夜审记录
        nightAudit.setTotalRooms(totalRooms.intValue());
        nightAudit.setOccupiedRooms(occupiedRooms.intValue());
        nightAudit.setAvailableRooms(availableRooms);
        nightAudit.setOccupancyRate(occupancyRate);
        nightAudit.setAdr(adr);
        nightAudit.setRevpar(revpar);
        nightAuditMapper.updateById(nightAudit);
        
        log.info("统计指标计算完成: 总房间={}, 在住={}, 可用={}, 入住率={}%, ADR={}, RevPAR={}", 
                totalRooms, occupiedRooms, availableRooms, occupancyRate, adr, revpar);
    }
    
    
    /**
     * 夜审后数据校验
     * <p>
     * 验证夜审统计数据与实际数据一致：
     * 1. 验证房间统计与实际数据一致
     * 2. 验证收入统计与交易记录一致
     * 3. 验证账务单状态正确
     * </p>
     */
    private void executePostCheck(NightAudit nightAudit) {
        Long hotelId = nightAudit.getHotelId();
        log.info("执行夜审后校验: hotelId={}", hotelId);
        
        List<String> warnings = new ArrayList<>();
        
        // 1. 验证房间统计
        Long actualTotalRooms = roomMapper.selectCount(
            new LambdaQueryWrapper<Room>()
                .eq(Room::getHotelId, hotelId)
                .eq(Room::getDeleted, false)
        );
        
        Long actualOccupiedRooms = stayMapper.selectCount(
            new LambdaQueryWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
        );
        
        if (!actualTotalRooms.equals(nightAudit.getTotalRooms().longValue())) {
            warnings.add(String.format("总房间数不一致：统计=%d，实际=%d", 
                    nightAudit.getTotalRooms(), actualTotalRooms));
        }
        
        if (!actualOccupiedRooms.equals(nightAudit.getOccupiedRooms().longValue())) {
            warnings.add(String.format("在住房间数不一致：统计=%d，实际=%d", 
                    nightAudit.getOccupiedRooms(), actualOccupiedRooms));
        }
        
        // 2. 验证收入统计
        BigDecimal actualRoomRevenue = BigDecimal.ZERO;
        List<FinTransaction> transactions = finTransactionMapper.selectList(
            new LambdaQueryWrapper<FinTransaction>()
                .eq(FinTransaction::getHotelId, hotelId)
                .eq(FinTransaction::getType, FolioConstants.TRANSACTION_ROOM_FEE)
                .ge(FinTransaction::getCreatedAt, nightAudit.getAuditDate().atStartOfDay())
                .lt(FinTransaction::getCreatedAt, nightAudit.getAuditDate().plusDays(1).atStartOfDay())
        );
        
        for (FinTransaction transaction : transactions) {
            actualRoomRevenue = actualRoomRevenue.add(
                transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO);
        }
        
        if (nightAudit.getRoomRevenue() != null && 
            nightAudit.getRoomRevenue().compareTo(actualRoomRevenue) != 0) {
            warnings.add(String.format("房费收入不一致：统计=%s，实际=%s", 
                    nightAudit.getRoomRevenue(), actualRoomRevenue));
        }
        
        // 3. 验证账务单状态
        Long openFolioCount = folioMapper.selectCount(
            new LambdaQueryWrapper<Folio>()
                .eq(Folio::getHotelId, hotelId)
                .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
                .eq(Folio::getIsLocked, false)
        );
        
        if (openFolioCount > 0) {
            warnings.add(String.format("有 %d 个账务单未锁定", openFolioCount));
        }
        
        // 记录校验结果
        if (!warnings.isEmpty()) {
            log.warn("夜审后校验警告:\n{}", String.join("\n", warnings));
        } else {
            log.info("夜审后校验通过：数据一致性验证通过");
        }
    }

    /**
     * 生成营业日报
     * <p>
     * 基于夜审统计数据生成营业日报：
     * - 汇总当日经营数据
     * - 计算关键经营指标
     * - 记录日报生成日志
     * </p>
     */
    private void executeGenerateDailyReport(NightAudit nightAudit) {
        Long hotelId = nightAudit.getHotelId();
        LocalDate auditDate = nightAudit.getAuditDate();
        log.info("生成营业日报: hotelId={}, auditDate={}", hotelId, auditDate);
        
        // 1. 获取酒店信息
        Hotel hotel = hotelMapper.selectById(hotelId);
        String hotelName = hotel != null ? hotel.getName() : "未知酒店";
        
        // 2. 获取统计数据
        Integer totalRooms = nightAudit.getTotalRooms() != null ? nightAudit.getTotalRooms() : 0;
        Integer occupiedRooms = nightAudit.getOccupiedRooms() != null ? nightAudit.getOccupiedRooms() : 0;
        Integer availableRooms = nightAudit.getAvailableRooms() != null ? nightAudit.getAvailableRooms() : 0;
        BigDecimal occupancyRate = nightAudit.getOccupancyRate() != null ? nightAudit.getOccupancyRate() : BigDecimal.ZERO;
        BigDecimal adr = nightAudit.getAdr() != null ? nightAudit.getAdr() : BigDecimal.ZERO;
        BigDecimal revpar = nightAudit.getRevpar() != null ? nightAudit.getRevpar() : BigDecimal.ZERO;
        BigDecimal roomRevenue = nightAudit.getRoomRevenue() != null ? nightAudit.getRoomRevenue() : BigDecimal.ZERO;
        BigDecimal extraRevenue = nightAudit.getExtraRevenue() != null ? nightAudit.getExtraRevenue() : BigDecimal.ZERO;
        BigDecimal totalRevenue = nightAudit.getTotalRevenue() != null ? nightAudit.getTotalRevenue() : BigDecimal.ZERO;
        
        // 3. 获取入住和退房统计
        Long todayCheckIns = stayMapper.selectCount(
            new LambdaQueryWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .ge(Stay::getCheckInTime, auditDate.atStartOfDay())
                .lt(Stay::getCheckInTime, auditDate.plusDays(1).atStartOfDay())
        );
        
        Long todayCheckOuts = stayMapper.selectCount(
            new LambdaQueryWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .ge(Stay::getActualCheckOutTime, auditDate.atStartOfDay())
                .lt(Stay::getActualCheckOutTime, auditDate.plusDays(1).atStartOfDay())
        );
        
        // 4. 生成日报摘要
        StringBuilder reportSummary = new StringBuilder();
        reportSummary.append(String.format("【%s】营业日报 - %s", hotelName, auditDate));
        reportSummary.append(String.format("\n房间统计: 总房间=%d, 在住=%d, 可用=%d", totalRooms, occupiedRooms, availableRooms));
        reportSummary.append(String.format("\n入住率: %s%%", occupancyRate));
        reportSummary.append(String.format("\n经营指标: ADR=¥%s, RevPAR=¥%s", adr, revpar));
        reportSummary.append(String.format("\n收入统计: 房费=¥%s, 其他=¥%s, 总计=¥%s", roomRevenue, extraRevenue, totalRevenue));
        reportSummary.append(String.format("\n当日入住: %d间, 当日退房: %d间", todayCheckIns, todayCheckOuts));
        
        // 5. 更新夜审记录（补充额外收入）
        if (extraRevenue.compareTo(BigDecimal.ZERO) > 0) {
            nightAudit.setTotalRevenue(roomRevenue.add(extraRevenue));
            nightAuditMapper.updateById(nightAudit);
        }
        
        // 6. 记录日报生成日志
        log.info("营业日报生成完成:\n{}", reportSummary.toString());
        log.info("日报数据: hotelId={}, auditDate={}, totalRooms={}, occupiedRooms={}, occupancyRate={}%, adr={}, revpar={}, roomRevenue={}, totalRevenue={}", 
                hotelId, auditDate, totalRooms, occupiedRooms, occupancyRate, adr, revpar, roomRevenue, totalRevenue);
    }
    
    /**
     * 锁定数据
     * <p>
     * 夜审完成后锁定当日数据，防止修改：
     * - 锁定在住的入住单（is_locked = true）
     * - 锁定开放的账务单（is_locked = true）
     * - 记录锁定的夜审ID
     * </p>
     */
    private void executeLockData(NightAudit nightAudit) {
        Long hotelId = nightAudit.getHotelId();
        Long auditId = nightAudit.getId();
        log.info("锁定数据: hotelId={}, auditId={}", hotelId, auditId);
        
        // 1. 锁定在住的入住单
        int lockedStayCount = stayMapper.update(null,
            new LambdaUpdateWrapper<Stay>()
                .eq(Stay::getHotelId, hotelId)
                .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
                .eq(Stay::getIsLocked, false)
                .set(Stay::getIsLocked, true)
                .set(Stay::getLockedByAuditId, auditId)
        );
        
        // 2. 锁定开放的账务单
        int lockedFolioCount = folioMapper.update(null,
            new LambdaUpdateWrapper<Folio>()
                .eq(Folio::getHotelId, hotelId)
                .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
                .eq(Folio::getIsLocked, false)
                .set(Folio::getIsLocked, true)
                .set(Folio::getLockedByAuditId, auditId)
        );
        
        // 3. 锁定开放的团队账务单
        int lockedTeamFolioCount = teamFolioMapper.update(null,
            new LambdaUpdateWrapper<TeamFolio>()
                .eq(TeamFolio::getHotelId, hotelId)
                .eq(TeamFolio::getStatus, StayConstants.FOLIO_STATUS_OPEN)
                .eq(TeamFolio::getIsLocked, false)
                .set(TeamFolio::getIsLocked, true)
                .set(TeamFolio::getLockedByAuditId, auditId)
        );
        
        log.info("数据锁定完成: 锁定入住单={}, 锁定账务单={}, 锁定团队账务单={}", 
                lockedStayCount, lockedFolioCount, lockedTeamFolioCount);
    }
    
    /**
     * 重试失败的夜审步骤
     */
    @Transactional(rollbackFor = Exception.class)
    public void retryNightAuditStep(Long auditId, Long stepId) {
        // 1. 验证夜审记录存在
        NightAudit nightAudit = nightAuditMapper.selectById(auditId);
        if (nightAudit == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "夜审记录不存在");
        }
        
        // 2. 验证夜审状态为 FAILED 或 COMPLETED_WITH_ERRORS
        if (!"FAILED".equals(nightAudit.getStatus()) && !"COMPLETED_WITH_ERRORS".equals(nightAudit.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能重试失败的夜审步骤");
        }
        
        // 3. 验证步骤存在且状态为 FAILED
        NightAuditStep step = nightAuditStepMapper.selectById(stepId);
        if (step == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "夜审步骤不存在");
        }
        if (!"FAILED".equals(step.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能重试失败的步骤");
        }
        
        // 4. 更新夜审状态为 IN_PROGRESS
        nightAudit.setStatus("IN_PROGRESS");
        nightAuditMapper.updateById(nightAudit);
        
        // 5. 重试该步骤
        try {
            step.setRetryCount(step.getRetryCount() + 1);
            executeStep(step, nightAudit);
            
            // 检查是否所有步骤都完成了
            List<NightAuditStep> allSteps = nightAuditStepMapper.selectList(
                new LambdaQueryWrapper<NightAuditStep>()
                    .eq(NightAuditStep::getNightAuditId, auditId)
            );
            
            boolean hasFailed = allSteps.stream().anyMatch(s -> "FAILED".equals(s.getStatus()));
            nightAudit.setStatus(hasFailed ? "COMPLETED_WITH_ERRORS" : "COMPLETED");
            nightAudit.setCompletedAt(LocalDateTime.now());
            nightAuditMapper.updateById(nightAudit);
            
        } catch (Exception e) {
            log.error("重试步骤失败: {}", step.getStepName(), e);
            step.setStatus("FAILED");
            step.setErrorMessage(e.getMessage());
            step.setCompletedAt(LocalDateTime.now());
            nightAuditStepMapper.updateById(step);
            
            nightAudit.setStatus("COMPLETED_WITH_ERRORS");
            nightAudit.setCompletedAt(LocalDateTime.now());
            nightAuditMapper.updateById(nightAudit);
            
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "重试步骤失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置夜审状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetNightAuditStatus(Long auditId) {
        NightAudit nightAudit = nightAuditMapper.selectById(auditId);
        if (nightAudit == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "夜审记录不存在");
        }
        
        if (!"IN_PROGRESS".equals(nightAudit.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能重置执行中的夜审状态");
        }
        
        // 更新状态为 FAILED，允许重新执行
        nightAudit.setStatus("FAILED");
        nightAudit.setErrorMessage("手动重置状态");
        nightAudit.setCompletedAt(LocalDateTime.now());
        nightAuditMapper.updateById(nightAudit);
        
        log.info("夜审状态已重置: auditId={}", auditId);
    }
    
    /**
     * 查询夜审记录列表
     */
    public PageResponse<NightAuditVO> getNightAuditList(NightAuditQueryDTO queryDTO) {
        LambdaQueryWrapper<NightAudit> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getHotelId() != null) {
            wrapper.eq(NightAudit::getHotelId, queryDTO.getHotelId());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq(NightAudit::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(NightAudit::getAuditDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(NightAudit::getAuditDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(NightAudit::getAuditDate);
        
        Page<NightAudit> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<NightAudit> result = nightAuditMapper.selectPage(page, wrapper);
        
        // 转换为VO
        List<NightAuditVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 查询夜审详情
     */
    public NightAuditDetailVO getNightAuditDetail(Long nightAuditId) {
        NightAudit nightAudit = nightAuditMapper.selectById(nightAuditId);
        if (nightAudit == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "夜审记录不存在");
        }
        
        // 查询夜审步骤
        List<NightAuditStep> steps = nightAuditStepMapper.selectList(
            new LambdaQueryWrapper<NightAuditStep>()
                .eq(NightAuditStep::getNightAuditId, nightAuditId)
                .orderByAsc(NightAuditStep::getStepOrder)
        );
        
        return convertToDetailVO(nightAudit, steps);
    }
    
    /**
     * 手动执行夜审
     */
    public NightAuditVO manualExecuteNightAudit(Long hotelId) {
        return executeNightAudit(hotelId);
    }
    
    /**
     * 自动执行夜审（定时任务）
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void autoExecuteNightAudit() {
        log.info("开始自动执行夜审任务");
        
        // 获取所有启用自动夜审的酒店
        List<Hotel> hotels = hotelMapper.selectList(
            new LambdaQueryWrapper<Hotel>()
                .eq(Hotel::getAutoAuditEnabled, true)
        );
        
        for (Hotel hotel : hotels) {
            try {
                executeNightAudit(hotel.getId());
                log.info("酒店 {} 夜审执行成功", hotel.getName());
            } catch (Exception e) {
                log.error("酒店 {} 夜审执行失败", hotel.getName(), e);
            }
        }
    }
    
    /**
     * 转换为VO
     */
    private NightAuditVO convertToVO(NightAudit nightAudit) {
        return NightAuditVO.builder()
            .id(nightAudit.getId())
            .hotelId(nightAudit.getHotelId())
            .auditDate(nightAudit.getAuditDate())
            .status(nightAudit.getStatus())
            .totalRooms(nightAudit.getTotalRooms())
            .occupiedRooms(nightAudit.getOccupiedRooms())
            .availableRooms(nightAudit.getAvailableRooms())
            .totalRevenue(nightAudit.getTotalRevenue())
            .roomRevenue(nightAudit.getRoomRevenue())
            .extraRevenue(nightAudit.getExtraRevenue())
            .occupancyRate(nightAudit.getOccupancyRate())
            .adr(nightAudit.getAdr())
            .revpar(nightAudit.getRevpar())
            .startedAt(nightAudit.getStartedAt())
            .completedAt(nightAudit.getCompletedAt())
            .errorMessage(nightAudit.getErrorMessage())
            .build();
    }
    
    /**
     * 转换为详情VO
     */
    private NightAuditDetailVO convertToDetailVO(NightAudit nightAudit, List<NightAuditStep> steps) {
        List<NightAuditStepVO> stepVOs = steps.stream()
            .map(this::convertToStepVO)
            .collect(Collectors.toList());
        
        return NightAuditDetailVO.builder()
            .id(nightAudit.getId())
            .hotelId(nightAudit.getHotelId())
            .auditDate(nightAudit.getAuditDate())
            .status(nightAudit.getStatus())
            .totalRooms(nightAudit.getTotalRooms())
            .occupiedRooms(nightAudit.getOccupiedRooms())
            .availableRooms(nightAudit.getAvailableRooms())
            .totalRevenue(nightAudit.getTotalRevenue())
            .roomRevenue(nightAudit.getRoomRevenue())
            .extraRevenue(nightAudit.getExtraRevenue())
            .occupancyRate(nightAudit.getOccupancyRate())
            .adr(nightAudit.getAdr())
            .revpar(nightAudit.getRevpar())
            .startedAt(nightAudit.getStartedAt())
            .completedAt(nightAudit.getCompletedAt())
            .errorMessage(nightAudit.getErrorMessage())
            .steps(stepVOs)
            .build();
    }
    
    /**
     * 转换步骤为VO
     */
    private NightAuditStepVO convertToStepVO(NightAuditStep step) {
        return NightAuditStepVO.builder()
            .id(step.getId())
            .stepName(step.getStepName())
            .stepOrder(step.getStepOrder())
            .status(step.getStatus())
            .startedAt(step.getStartedAt())
            .completedAt(step.getCompletedAt())
            .errorMessage(step.getErrorMessage())
            .retryCount(step.getRetryCount())
            .build();
    }
}


