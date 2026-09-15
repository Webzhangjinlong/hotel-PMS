package com.hotel.pms.service.stay;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.constant.FolioConstants;
import com.hotel.pms.common.constant.RoomConstants;
import com.hotel.pms.common.constant.ReservationConstants;
import com.hotel.pms.common.constant.StayConstants;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.dto.RoomPriceVO;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.entity.TeamReservation;
import com.hotel.pms.dao.mapper.*;
import com.hotel.pms.service.config.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 入住服务类
 * <p>
 * 负责入住的业务逻辑处理
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class StayService extends BaseService<Stay, StayMapper> {

    /** 日期格式 */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private GuestMapper guestMapper;

    @Autowired
    private FolioMapper folioMapper;

    @Autowired
    private TeamReservationMapper teamReservationMapper;

    @Autowired
    private TeamReservationRoomMapper teamReservationRoomMapper;

    @Autowired
    private TeamFolioMapper teamFolioMapper;

    @Autowired
    private com.hotel.pms.service.price.RoomPriceService roomPriceService;

    @Autowired
    private FinTransactionMapper finTransactionMapper;

    @Autowired
    private StayGuestMapper stayGuestMapper;


    @Autowired
    private DepositMapper depositMapper;
    /**
     * 分页查询入住列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResponse<StayVO> pageList(StayQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, Stay::getHotelId, queryDTO.getHotelId())
               .eq(StringUtils.hasText(queryDTO.getStatus()), Stay::getStatus, queryDTO.getStatus())
               .eq(StringUtils.hasText(queryDTO.getCheckInType()), Stay::getCheckInType, queryDTO.getCheckInType())
               .orderByDesc(Stay::getCreatedAt);

        // 日期条件需要单独处理，避免空指针
        if (queryDTO.getCheckInDateStart() != null) {
            wrapper.ge(Stay::getCheckInTime, queryDTO.getCheckInDateStart().atStartOfDay());
        }
        if (queryDTO.getCheckInDateEnd() != null) {
            wrapper.le(Stay::getCheckInTime, queryDTO.getCheckInDateEnd().atTime(LocalTime.MAX));
        }

        // 执行分页查询
        Page<Stay> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Stay> result = mapper.selectPage(page, wrapper);

        // 转换为VO
        List<StayVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 批量查询同住人信息
        if (!voList.isEmpty()) {
            List<Long> stayIds = voList.stream()
                    .map(StayVO::getId)
                    .collect(Collectors.toList());

            // 使用StayGuestMapper批量查询同住人（不包括主客人）
            List<StayGuest> coGuests = stayGuestMapper.selectByStayIds(stayIds);

            // 按stayId分组
            Map<Long, List<StayGuest>> coGuestMap = coGuests.stream()
                    .collect(Collectors.groupingBy(StayGuest::getStayId));

            // 填充同住人信息到StayVO
            for (StayVO vo : voList) {
                List<StayGuest> guestList = coGuestMap.get(vo.getId());
                if (guestList != null && !guestList.isEmpty()) {
                    vo.setCoGuestCount(guestList.size());
                    // 生成同住人姓名JSON数组
                    String names = guestList.stream()
                            .map(g -> "{\"name\":\""+ g.getGuestName() + "\"}")
                            .collect(Collectors.joining(","));
                    vo.setCoGuestNames("[" + names + "]");
                } else {
                    vo.setCoGuestCount(0);
                    vo.setCoGuestNames("[]");
                }
            }
        }

        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 根据ID查询入住详情
     *
     * @param id 入住单ID
     * @return 入住信息
     */
    public StayVO getById(Long id) {
        Stay stay = mapper.selectById(id);
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }
        return convertToVO(stay);
    }


    /**
     * 分页查询团队入住汇总
     *
     * @param queryDTO 查询条件
     * @return 团队汇总分页结果
     */
    public PageResponse<TeamStaySummaryVO> pageTeamSummary(StayQueryDTO queryDTO) {
        // 构建查询条件 - 查询团队入住单
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, Stay::getHotelId, queryDTO.getHotelId())
               .eq(Stay::getCheckInType, "TEAM")
               .isNotNull(Stay::getTeamReservationId)
               .orderByDesc(Stay::getCreatedAt);

        // 日期条件
        if (queryDTO.getCheckInDateStart() != null) {
            wrapper.ge(Stay::getCheckInTime, queryDTO.getCheckInDateStart().atStartOfDay());
        }
        if (queryDTO.getCheckInDateEnd() != null) {
            wrapper.le(Stay::getCheckInTime, queryDTO.getCheckInDateEnd().atTime(LocalTime.MAX));
        }

        // 查询所有符合条件的团队入住单
        List<Stay> allTeamStays = mapper.selectList(wrapper);

        // 按团队预订ID分组
        Map<Long, List<Stay>> groupedByTeam = allTeamStays.stream()
                .filter(stay -> stay.getTeamReservationId() != null)
                .collect(Collectors.groupingBy(Stay::getTeamReservationId));

        // 转换为团队汇总VO列表
        List<TeamStaySummaryVO> allTeamSummaries = groupedByTeam.entrySet().stream()
                .map(entry -> {
                    Long teamReservationId = entry.getKey();
                    List<Stay> teamStays = entry.getValue();

                    // 查询团队预订信息
                    TeamReservation teamReservation = teamReservationMapper.selectById(teamReservationId);

                    // 计算汇总信息
                    BigDecimal totalAmount = teamStays.stream()
                            .map(Stay::getTotalAmount)
                            .filter(java.util.Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal paidAmount = teamStays.stream()
                            .map(Stay::getPaidAmount)
                            .filter(java.util.Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // 转换入住单为VO
                    List<StayVO> stayVOs = teamStays.stream()
                            .map(this::convertToVO)
                            .collect(Collectors.toList());

                    // 计算支付状态
                    String paymentStatus = "UNPAID";
                    if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                        if (paidAmount.compareTo(totalAmount) >= 0) {
                            paymentStatus = "PAID";
                        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                            paymentStatus = "PARTIAL";
                        }
                    }

                    return TeamStaySummaryVO.builder()
                            .teamReservationId(teamReservationId)
                            .teamReservationNo(teamReservation != null ? teamReservation.getTeamReservationNo() : "")
                            .teamName(teamReservation != null ? teamReservation.getTeamName() : "")
                            .settlementType(teamReservation != null ? teamReservation.getSettlementType() : "")
                            .contactName(teamReservation != null ? teamReservation.getContactName() : "")
                            .contactPhone(teamReservation != null ? teamReservation.getContactPhone() : "")
                            .source(teamReservation != null ? teamReservation.getSource() : "")
                            .totalRooms(teamStays.size())
                            .totalAmount(totalAmount)
                            .paidAmount(paidAmount)
                            .status(teamStays.stream().allMatch(s -> "CHECKED_OUT".equals(s.getStatus())) ? "CHECKED_OUT" : "CHECKED_IN")
                            .paymentStatus(paymentStatus)
                            .stays(stayVOs)
                            .build();
                })
                .collect(Collectors.toList());

        // 手动分页
        int total = allTeamSummaries.size();
        int page = queryDTO.getPage();
        int size = queryDTO.getSize();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<TeamStaySummaryVO> pageRecords = fromIndex < total
                ? allTeamSummaries.subList(fromIndex, toIndex)
                : new java.util.ArrayList<>();

        return new PageResponse<>(pageRecords, total, page, size);
    }
    /**
     * 散客入住
     *
     * @param dto 散客入住参数
     * @return 入住信息
     */
    @Transactional(rollbackFor = Exception.class)
    public StayVO walkInCheckIn(StayCreateDTO dto) {
        // 1. 验证酒店存在
        Hotel hotel = hotelMapper.selectById(dto.getHotelId());
        if (hotel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "酒店不存在");
        }

        // 2. 验证房间存在且状态为AVAILABLE
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房间不存在");
        }
        if (!RoomConstants.STATUS_AVAILABLE.equals(room.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "房间状态不是空闲，无法入住");
        }

         // 3. 创建客人记录
        Guest guest = findOrCreateGuest(dto.getHotelId(), dto.getGuestName(),
                dto.getGuestIdNo(), dto.getGuestPhone(), dto.getGuestGender());

        // 4. 计算入住天数
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dto.getExpectedCheckOutDate());
        if (days <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "离店日期必须晚于入住日期");
        }

        // 5. 计算房费（优先使用手动输入的房价）
        BigDecimal totalAmount;
        if (dto.getDailyPrice() != null && dto.getDailyPrice().compareTo(BigDecimal.ZERO) > 0) {
            // 使用手动输入的单日房价
            totalAmount = dto.getDailyPrice().multiply(BigDecimal.valueOf(days));
        } else {
            // 使用房价码自动计算
            totalAmount = calculateStayAmount(dto.getHotelId(), room.getRoomTypeId(), LocalDate.now(), dto.getExpectedCheckOutDate(), dto.getPricePlanId());
        }

        // 7. 创建入住单
        Stay stay = new Stay();
        stay.setHotelId(dto.getHotelId());
        stay.setStayNo(generateStayNo(dto.getHotelId()));
        stay.setRoomId(dto.getRoomId());
        stay.setGuestId(guest.getId());
        stay.setCheckInTime(LocalDateTime.now());
        stay.setCheckOutTime(dto.getExpectedCheckOutDate().atTime(12, 0));
        stay.setStatus(StayConstants.STATUS_CHECKED_IN);
        stay.setTotalAmount(totalAmount);
        stay.setPaidAmount(BigDecimal.ZERO);
        stay.setCheckInType("WALK_IN");
        stay.setPricePlanId(dto.getPricePlanId());
        mapper.insert(stay);

        // 8. 更新房间状态为OCCUPIED
        room.setStatus(RoomConstants.STATUS_OCCUPIED);
        roomMapper.updateById(room);

        // 8. 创建账务单
        createFolio(stay);

        // 9. 保存主客人到stay_guest表
        StayGuest primaryGuest = new StayGuest();
        primaryGuest.setStayId(stay.getId());
        primaryGuest.setGuestId(guest.getId());
        primaryGuest.setGuestName(dto.getGuestName());
        primaryGuest.setIdType("ID_CARD");
        primaryGuest.setIdNo(dto.getGuestIdNo());
        primaryGuest.setPhone(dto.getGuestPhone());
        primaryGuest.setGender(dto.getGuestGender());
        primaryGuest.setIsPrimary(true);
        primaryGuest.setCreatedAt(LocalDateTime.now());
        primaryGuest.setUpdatedAt(LocalDateTime.now());
        stayGuestMapper.insert(primaryGuest);

        // 10. 保存同住人到stay_guest表
        if (dto.getCoGuests() != null && !dto.getCoGuests().isEmpty()) {
            for (StayGuestDTO coGuestDto : dto.getCoGuests()) {
                if (!StringUtils.hasText(coGuestDto.getGuestName())) {
                    continue; // 跳过空的同住人
                }
                StayGuest coGuest = new StayGuest();
                coGuest.setStayId(stay.getId());
                coGuest.setGuestId(null); // 同住人可以没有客人档案
                coGuest.setGuestName(coGuestDto.getGuestName());
                coGuest.setIdType(coGuestDto.getIdType() != null ? coGuestDto.getIdType() : "ID_CARD");
                coGuest.setIdNo(coGuestDto.getIdNo());
                coGuest.setPhone(coGuestDto.getPhone());
                coGuest.setGender(coGuestDto.getGender());
                coGuest.setIsPrimary(false);
                coGuest.setCreatedAt(LocalDateTime.now());
                coGuest.setUpdatedAt(LocalDateTime.now());
                stayGuestMapper.insert(coGuest);
            }
        }

        log.info("散客入住成功：stayId={}, stayNo={}, roomId={}", stay.getId(), stay.getStayNo(), dto.getRoomId());

        return convertToVO(stay);
    }

    /**
     * 预订入住
     *
     * @param dto 预订入住参数
     * @return 入住信息
     */
    @Transactional(rollbackFor = Exception.class)
    public StayVO reservationCheckIn(CheckInDTO dto) {
        // 1. 验证预订是否存在
        Reservation reservation = reservationMapper.selectById(dto.getReservationId());
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预订不存在");
        }

        // 2. 验证预订状态
        if (!ReservationConstants.STATUS_CONFIRMED.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "预订状态不是已确认，无法入住");
        }

        // 3. 验证房间
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房间不存在");
        }
        if (!RoomConstants.STATUS_AVAILABLE.equals(room.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "房间状态不是空闲，无法入住");
        }

         // 4. 创建客人记录
        Guest guest = findOrCreateGuest(reservation.getHotelId(), dto.getGuestName(),
                dto.getGuestIdNo(), dto.getGuestPhone(), dto.getGuestGender());

        // 5. 检查房间是否有在住记录
        LambdaQueryWrapper<Stay> stayCheckWrapper = new LambdaQueryWrapper<>();
        stayCheckWrapper.eq(Stay::getRoomId, dto.getRoomId())
                       .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN);
        Long existingStayCount = mapper.selectCount(stayCheckWrapper);
        if (existingStayCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该房间已有在住客人，无法入住");
        }

        // 6. 使用预订金额（已包含房价码价格）
        BigDecimal totalAmount = reservation.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            totalAmount = calculateStayAmount(reservation.getHotelId(), room.getRoomTypeId(),
                    reservation.getCheckInDate(), reservation.getCheckOutDate(), reservation.getPricePlanId());
        }

        // 7. 创建入住单
        Stay stay = new Stay();
        stay.setHotelId(reservation.getHotelId());
        stay.setStayNo(generateStayNo(reservation.getHotelId()));
        stay.setReservationId(reservation.getId());
        stay.setRoomId(dto.getRoomId());
        stay.setGuestId(guest.getId());
        stay.setCheckInTime(LocalDateTime.now());
        stay.setCheckOutTime(reservation.getCheckOutDate().atTime(12, 0));
        stay.setStatus(StayConstants.STATUS_CHECKED_IN);
        stay.setTotalAmount(totalAmount);
        stay.setPaidAmount(BigDecimal.ZERO);
        stay.setCheckInType("RESERVATION");
        mapper.insert(stay);

        // 8. 更新房间状态
        room.setStatus(RoomConstants.STATUS_OCCUPIED);
        roomMapper.updateById(room);

        // 9. 更新预订单状态
        reservation.setStatus(ReservationConstants.STATUS_CHECKED_IN);
        reservation.setRoomId(dto.getRoomId());
        reservationMapper.updateById(reservation);

        // 9. 创建账务单
        createFolio(stay);

        // 10. 保存主客人到stay_guest表
        StayGuest primaryGuest = new StayGuest();
        primaryGuest.setStayId(stay.getId());
        primaryGuest.setGuestId(guest.getId());
        primaryGuest.setGuestName(dto.getGuestName());
        primaryGuest.setIdType("ID_CARD");
        primaryGuest.setIdNo(dto.getGuestIdNo());
        primaryGuest.setPhone(dto.getGuestPhone());
        primaryGuest.setGender(dto.getGuestGender());
        primaryGuest.setIsPrimary(true);
        primaryGuest.setCreatedAt(LocalDateTime.now());
        primaryGuest.setUpdatedAt(LocalDateTime.now());
        stayGuestMapper.insert(primaryGuest);

        // 11. 保存同住人到stay_guest表
        if (dto.getCoGuests() != null && !dto.getCoGuests().isEmpty()) {
            for (StayGuestDTO coGuestDto : dto.getCoGuests()) {
                if (!StringUtils.hasText(coGuestDto.getGuestName())) {
                    continue; // 跳过空的同住人
                }
                StayGuest coGuest = new StayGuest();
                coGuest.setStayId(stay.getId());
                coGuest.setGuestId(null); // 同住人可以没有客人档案
                coGuest.setGuestName(coGuestDto.getGuestName());
                coGuest.setIdType(coGuestDto.getIdType() != null ? coGuestDto.getIdType() : "ID_CARD");
                coGuest.setIdNo(coGuestDto.getIdNo());
                coGuest.setPhone(coGuestDto.getPhone());
                coGuest.setGender(coGuestDto.getGender());
                coGuest.setIsPrimary(false);
                coGuest.setCreatedAt(LocalDateTime.now());
                coGuest.setUpdatedAt(LocalDateTime.now());
                stayGuestMapper.insert(coGuest);
            }
        }

        log.info("预订入住成功：stayId={}, stayNo={}, reservationId={}", stay.getId(), stay.getStayNo(), reservation.getId());

        return convertToVO(stay);
    }

    /**
     * 查询今日入住列表
     *
     * @param hotelId 酒店ID
     * @return 今日入住列表
     */
    public List<StayVO> getTodayCheckIns(Long hotelId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getHotelId, hotelId)
               .ge(Stay::getCheckInTime, today.atStartOfDay())
               .le(Stay::getCheckInTime, today.atTime(LocalTime.MAX))
               .orderByDesc(Stay::getCreatedAt);

        List<Stay> list = mapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 续住
     *
     * @param dto 续住参数
     * @return 续住信息
     */

    public StayExtendVO extendStay(StayExtendDTO dto) {
        // 1. 验证入住单
        Stay stay = mapper.selectById(dto.getStayId());
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }
        if (!StayConstants.STATUS_CHECKED_IN.equals(stay.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该入住单状态不是在住，无法续住");
        }

        // 2. 验证新离店日期
        LocalDateTime oldCheckOutTime = stay.getCheckOutTime();
        LocalDateTime newCheckOutTime = dto.getNewCheckOutDate().atTime(12, 0);
        if (!newCheckOutTime.isAfter(oldCheckOutTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新离店日期必须晚于当前离店日期");
        }

        // 3. 检查房间可用性
        checkRoomAvailabilityForExtend(stay.getRoomId(), oldCheckOutTime, newCheckOutTime, stay.getId());

        // 4. 计算续住费用
        int extendedDays = (int) ChronoUnit.DAYS.between(oldCheckOutTime.toLocalDate(), dto.getNewCheckOutDate());
        BigDecimal additionalAmount = calculateExtendAmount(stay, extendedDays);

        // 5. 更新入住单
        stay.setCheckOutTime(newCheckOutTime);
        stay.setTotalAmount(stay.getTotalAmount().add(additionalAmount));
        mapper.updateById(stay);

        // 6. 更新账务单
        updateFolioForExtend(stay.getId(), additionalAmount);

        // 7. 如果是团队入住，同步更新团队账务
        if (stay.getTeamReservationId() != null) {
            updateTeamFolioForExtend(stay, additionalAmount);
        }

        log.info("续住成功：stayId={}, stayNo={}, extendedDays={}, additionalAmount={}",
                stay.getId(), stay.getStayNo(), extendedDays, additionalAmount);

        return StayExtendVO.builder()
                .stayId(stay.getId())
                .stayNo(stay.getStayNo())
                .oldCheckOutTime(oldCheckOutTime)
                .newCheckOutTime(newCheckOutTime)
                .extendedDays(extendedDays)
                .additionalAmount(additionalAmount)
                .newTotalAmount(stay.getTotalAmount())
                .build();
    }

    /**
     * 检查房间在续住期间的可用性
     */
    private void checkRoomAvailabilityForExtend(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeStayId) {
        // 检查入住冲突
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getRoomId, roomId)
               .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
               .ne(Stay::getId, excludeStayId)
               .lt(Stay::getCheckInTime, endTime)
               .gt(Stay::getCheckOutTime, startTime);

        Long count = mapper.selectCount(wrapper);
        if (count > 0) {
             throw new BusinessException(ResultCode.BAD_REQUEST, "该房间在续住期间已被占用");
        }

        // 检查预订冲突
        LambdaQueryWrapper<Reservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.eq(Reservation::getRoomId, roomId)
                          .in(Reservation::getStatus,
                              ReservationConstants.STATUS_PENDING,
                              ReservationConstants.STATUS_CONFIRMED)
                          .lt(Reservation::getCheckInDate, endTime.toLocalDate())
                          .ge(Reservation::getCheckOutDate, startTime.toLocalDate());

        Long reservationCount = reservationMapper.selectCount(reservationWrapper);
        if (reservationCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该房间在续住期间已有预订");
        }
    }

    /**
      * 计算续住费用
     */
    private BigDecimal calculateExtendAmount(Stay stay, int extendedDays) {
        // 查询房间信息获取房型ID
        Room room = roomMapper.selectById(stay.getRoomId());
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房间不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDate startDate = stay.getCheckOutTime().toLocalDate();

        for (int i = 0; i < extendedDays; i++) {
            LocalDate date = startDate.plusDays(i);
            RoomPriceVO priceVO = roomPriceService.getPriceByDate(
                stay.getHotelId(), room.getRoomTypeId(), date, stay.getPricePlanId());
            totalAmount = totalAmount.add(priceVO.getPrice());
        }

        return totalAmount;
    }

    /**
     * 更新账务单
     */
    private void updateFolioForExtend(Long stayId, BigDecimal additionalAmount) {
        LambdaQueryWrapper<Folio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folio::getStayId, stayId)
               .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN);

        Folio folio = folioMapper.selectOne(wrapper);
        if (folio != null) {
            folio.setTotalAmount(folio.getTotalAmount().add(additionalAmount));
            folio.setBalance(folio.getBalance().add(additionalAmount));
            folioMapper.updateById(folio);
        }
    }

    /**
     * 续住时同步更新团队账务
     */
    private void updateTeamFolioForExtend(Stay stay, BigDecimal additionalAmount) {
         // 更新团队预订房间明细的金额
        LambdaQueryWrapper<TeamReservationRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(TeamReservationRoom::getStayId, stay.getId());
        TeamReservationRoom teamRoom = teamReservationRoomMapper.selectOne(roomWrapper);
        if (teamRoom != null) {
            teamRoom.setAmount(teamRoom.getAmount().add(additionalAmount));
            teamReservationRoomMapper.updateById(teamRoom);
        }

         // 更新团队预订总金额
        TeamReservation reservation = teamReservationMapper.selectById(stay.getTeamReservationId());
        if (reservation != null) {
            reservation.setTotalAmount(reservation.getTotalAmount().add(additionalAmount));
            teamReservationMapper.updateById(reservation);
        }

         // 如果是统一结算，更新团队财务单
        if (reservation != null && ReservationConstants.SETTLEMENT_UNIFIED.equals(reservation.getSettlementType())) {
            LambdaQueryWrapper<TeamFolio> folioWrapper = new LambdaQueryWrapper<>();
            folioWrapper.eq(TeamFolio::getTeamReservationId, stay.getTeamReservationId())
                       .eq(TeamFolio::getStatus, StayConstants.FOLIO_STATUS_OPEN);
            TeamFolio teamFolio = teamFolioMapper.selectOne(folioWrapper);
            if (teamFolio != null) {
                teamFolio.setTotalAmount(teamFolio.getTotalAmount().add(additionalAmount));
                teamFolio.setBalance(teamFolio.getBalance().add(additionalAmount));
                teamFolioMapper.updateById(teamFolio);
            }
        }
    }


    /**
     * 退房
     *
     * @param dto 退房参数
     * @return 退房信息
     */
    @Transactional(rollbackFor = Exception.class)
    public StayCheckOutVO checkOut(StayCheckOutDTO dto) {
        // 1. 验证入住单存在
        Stay stay = mapper.selectById(dto.getStayId());
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }

        // 2. 验证状态必须是 CHECKED_IN
        if (!StayConstants.STATUS_CHECKED_IN.equals(stay.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该入住单状态不是在住，无法退房");
        }

        // 3. 设置实际离店时间为当前时间
        LocalDateTime now = LocalDateTime.now();
        stay.setActualCheckOutTime(now);

        // 4. 更新入住单状态为 CHECKED_OUT
        stay.setStatus(StayConstants.STATUS_CHECKED_OUT);
        mapper.updateById(stay);

        // 5. 更新房间状态为 DIRTY（待清洁）
        Room room = roomMapper.selectById(stay.getRoomId());
        if (room != null) {
            room.setStatus(RoomConstants.STATUS_DIRTY);
            roomMapper.updateById(room);
        }

         // 6. 更新预订单状态（如有）
        if (stay.getReservationId() != null) {
            Reservation reservation = reservationMapper.selectById(stay.getReservationId());
            if (reservation != null) {
                reservation.setStatus(ReservationConstants.STATUS_CHECKED_OUT);
                reservationMapper.updateById(reservation);
            }
        }

        // 7. 关闭账单

         // 6.1 如果是团队入住，更新团队房间明细状态
        if (stay.getTeamReservationId() != null) {
            LambdaQueryWrapper<TeamReservationRoom> teamRoomWrapper = new LambdaQueryWrapper<>();
            teamRoomWrapper.eq(TeamReservationRoom::getStayId, stay.getId());
            TeamReservationRoom teamRoom = teamReservationRoomMapper.selectOne(teamRoomWrapper);
            if (teamRoom != null) {
                teamRoom.setCheckOutTime(now);
                teamRoom.setStatus(ReservationConstants.ROOM_STATUS_CHECKED_OUT);
                teamReservationRoomMapper.updateById(teamRoom);
            }
        }
        closeFolioForStay(stay.getId());

         // 8. 查询客人信息
        Guest guest = guestMapper.selectById(stay.getGuestId());

        // 9. 计算待结金额
        BigDecimal outstandingAmount = stay.getTotalAmount().subtract(
            stay.getPaidAmount() != null ? stay.getPaidAmount() : BigDecimal.ZERO
        );

        log.info("退房成功：stayId={}, stayNo={}, roomNo={}",
                stay.getId(), stay.getStayNo(), room != null ? room.getRoomNo() : "unknown");

        // 10. 返回退房信息
        return StayCheckOutVO.builder()
                .stayId(stay.getId())
                .stayNo(stay.getStayNo())
                .roomNo(room != null ? room.getRoomNo() : null)
                .guestName(guest != null ? guest.getName() : null)
                .checkInTime(stay.getCheckInTime())
                .actualCheckOutTime(now)
                .totalAmount(stay.getTotalAmount())
                .paidAmount(stay.getPaidAmount() != null ? stay.getPaidAmount() : BigDecimal.ZERO)
                .outstandingAmount(outstandingAmount)
                .build();
    }

    /**
     * 关闭入住单的账单
     */
    private void closeFolioForStay(Long stayId) {
        LambdaQueryWrapper<Folio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folio::getStayId, stayId)
              .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN);

        Folio folio = folioMapper.selectOne(wrapper);
        if (folio != null) {
            folio.setStatus(StayConstants.FOLIO_STATUS_CLOSED);
            folioMapper.updateById(folio);
        }
    }

    /**
     * 计算入住费用
     */
    private BigDecimal calculateStayAmount(Long hotelId, Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, Long pricePlanId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDate currentDate = checkInDate;

        while (currentDate.isBefore(checkOutDate)) {
            RoomPriceVO priceVO = roomPriceService.getPriceByDate(hotelId, roomTypeId, currentDate, pricePlanId);
            totalAmount = totalAmount.add(priceVO.getPrice());
            currentDate = currentDate.plusDays(1);
        }

        return totalAmount;
    }

    /**
      * 查找或创建客人
     */
    private Guest findOrCreateGuest(Long hotelId, String name, String idNo, String phone, String gender) {
        // 先尝试通过证件号查找
        if (StringUtils.hasText(idNo)) {
            LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Guest::getHotelId, hotelId)
                   .eq(Guest::getIdNo, idNo);
            Guest existingGuest = guestMapper.selectOne(wrapper);
            if (existingGuest != null) {
                return existingGuest;
            }
        }

        // 再尝试通过手机号查找
        if (StringUtils.hasText(phone)) {
            LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Guest::getHotelId, hotelId)
                   .eq(Guest::getPhone, phone);
            Guest existingGuest = guestMapper.selectOne(wrapper);
            if (existingGuest != null) {
                return existingGuest;
            }
        }

         // 创建新客人
        Guest guest = new Guest();
        guest.setHotelId(hotelId);
        guest.setName(name);
        guest.setIdNo(idNo);
        guest.setPhone(phone);
        guest.setGender(gender);
        guest.setNationality("中国");
        guestMapper.insert(guest);

        return guest;
    }

    /**
     * 创建账务单
     */
    private void createFolio(Stay stay) {
        Folio folio = new Folio();
        folio.setHotelId(stay.getHotelId());
        folio.setFolioNo(generateFolioNo(stay.getHotelId()));
        folio.setStayId(stay.getId());
        folio.setGuestId(stay.getGuestId());
        folio.setTotalAmount(stay.getTotalAmount());
        folio.setPaidAmount(BigDecimal.ZERO);
        folio.setBalance(stay.getTotalAmount());
        folio.setStatus(StayConstants.FOLIO_STATUS_OPEN);
        folioMapper.insert(folio);
    }

    /**
     * 生成账务单号
     */
    private String generateFolioNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        long seq = System.currentTimeMillis() % 1000000;
        return "F" + dateStr + String.format("%06d", seq);
    }

    /**
     * 生成入住单号
     */
    private String generateStayNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
         // 简单实现：使用时间戳后6位
        long seq = System.currentTimeMillis() % 1000000;
        return "S" + dateStr + String.format("%06d", seq);
    }

    /**
     * 转换为VO
     */
    private StayVO convertToVO(Stay stay) {
        StayVO vo = new StayVO();
        BeanUtils.copyProperties(stay, vo);

        // 查询酒店信息
        Hotel hotel = hotelMapper.selectById(stay.getHotelId());
        if (hotel != null) {
            vo.setHotelName(hotel.getName());
        }

        // 查询房间信息
        Room room = roomMapper.selectById(stay.getRoomId());
        if (room != null) {
            vo.setRoomNo(room.getRoomNo());
            // 查询房型
            RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
            if (roomType != null) {
                vo.setRoomTypeId(roomType.getId());
                vo.setRoomTypeName(roomType.getName());
            }
        }

         // 查询客人信息
        Guest guest = guestMapper.selectById(stay.getGuestId());
        if (guest != null) {
            vo.setGuestName(guest.getName());
            vo.setGuestPhone(guest.getPhone());
        }

        // 查询预订信息
        if (stay.getReservationId() != null) {
            Reservation reservation = reservationMapper.selectById(stay.getReservationId());
            if (reservation != null) {
                vo.setReservationNo(reservation.getReservationNo());
            }
        }

         // 查询团队预订信息
        if (stay.getTeamReservationId() != null) {
            TeamReservation teamReservation = teamReservationMapper.selectById(stay.getTeamReservationId());
            if (teamReservation != null) {
                vo.setTeamReservationId(teamReservation.getId());
                vo.setTeamReservationNo(teamReservation.getTeamReservationNo());
                vo.setTeamName(teamReservation.getTeamName());
                vo.setSettlementType(teamReservation.getSettlementType());
                vo.setContactName(teamReservation.getContactName());
                vo.setContactPhone(teamReservation.getContactPhone());
                vo.setSource(teamReservation.getSource());
            }
        }

         // 设置入住类型（仅在为空时根据关联信息推断，已明确设置的不覆盖）
        if (vo.getCheckInType() == null) {
            if (stay.getTeamReservationId() != null) {
                vo.setCheckInType("TEAM");
            } else if (stay.getReservationId() != null) {
                vo.setCheckInType("RESERVATION");
            } else {
                vo.setCheckInType("WALK_IN");
            }
        }

        // 查询押金信息（包括已收取、部分退还、部分抵扣的押金）
        LambdaQueryWrapper<Deposit> depositWrapper = new LambdaQueryWrapper<>();
        depositWrapper.eq(Deposit::getStayId, stay.getId())
                     .in(Deposit::getStatus, "COLLECTED", "PARTIAL_REFUND", "PARTIAL_DEDUCT", "REFUNDED");
        List<Deposit> deposits = depositMapper.selectList(depositWrapper);
        if (deposits != null && !deposits.isEmpty()) {
            BigDecimal totalDeposit = deposits.stream()
                    .map(Deposit::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRefunded = deposits.stream()
                    .map(d -> d.getRefundedAmount() != null ? d.getRefundedAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDeducted = deposits.stream()
                    .map(d -> d.getDeductedAmount() != null ? d.getDeductedAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setDepositAmount(totalDeposit);
        }
                 // 计算支付状态
         // 统一结算的团队：使用团队账务单的支付状态
        if ("TEAM".equals(vo.getCheckInType()) && "UNIFIED".equals(vo.getSettlementType())) {
            LambdaQueryWrapper<TeamFolio> teamFolioWrapper = new LambdaQueryWrapper<>();
            teamFolioWrapper.eq(TeamFolio::getTeamReservationId, stay.getTeamReservationId())
                           .eq(TeamFolio::getStatus, StayConstants.FOLIO_STATUS_OPEN);
            TeamFolio teamFolio = teamFolioMapper.selectOne(teamFolioWrapper);
            if (teamFolio == null) {
                teamFolioWrapper = new LambdaQueryWrapper<>();
                teamFolioWrapper.eq(TeamFolio::getTeamReservationId, stay.getTeamReservationId());
                teamFolio = teamFolioMapper.selectOne(teamFolioWrapper);
            }
            if (teamFolio != null) {
                vo.setTeamFolioId(teamFolio.getId());
                BigDecimal teamTotal = teamFolio.getTotalAmount() != null ? teamFolio.getTotalAmount() : BigDecimal.ZERO;
                BigDecimal teamPaid = teamFolio.getPaidAmount() != null ? teamFolio.getPaidAmount() : BigDecimal.ZERO;
                if (teamTotal.compareTo(BigDecimal.ZERO) > 0) {
                    if (teamPaid.compareTo(teamTotal) >= 0) {
                        vo.setPaymentStatus("PAID");
                        vo.setUnpaidAmount(BigDecimal.ZERO);
                    } else if (teamPaid.compareTo(BigDecimal.ZERO) > 0) {
                        vo.setPaymentStatus("PARTIAL");
                        vo.setUnpaidAmount(teamTotal.subtract(teamPaid));
                    } else {
                        vo.setPaymentStatus("UNPAID");
                        vo.setUnpaidAmount(teamTotal);
                    }
                } else {
                    vo.setPaymentStatus("NO_FEE");
                    vo.setUnpaidAmount(BigDecimal.ZERO);
                }
            } else {
                vo.setPaymentStatus("UNPAID");
                vo.setUnpaidAmount(vo.getTotalAmount() != null ? vo.getTotalAmount() : BigDecimal.ZERO);
            }
        } else {
             // 分开结算或散客：使用入住单号自身的支付状态（押金也视为已付）
            BigDecimal effectivePaid = (vo.getPaidAmount() != null ? vo.getPaidAmount() : BigDecimal.ZERO)
                    .add(vo.getDepositAmount() != null ? vo.getDepositAmount() : BigDecimal.ZERO);
            if (vo.getTotalAmount() != null && vo.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                if (effectivePaid.compareTo(vo.getTotalAmount()) >= 0) {
                    vo.setPaymentStatus("PAID");
                    vo.setUnpaidAmount(BigDecimal.ZERO);
                } else if (effectivePaid.compareTo(BigDecimal.ZERO) > 0) {
                    vo.setPaymentStatus("PARTIAL");
                    vo.setUnpaidAmount(vo.getTotalAmount().subtract(effectivePaid));
                } else {
                    vo.setPaymentStatus("UNPAID");
                    vo.setUnpaidAmount(vo.getTotalAmount());
                }
            } else {
                vo.setPaymentStatus("NO_FEE");
                vo.setUnpaidAmount(BigDecimal.ZERO);
            }

        }

        return vo;
    }


    /**
      * 散客入住单转入团队预订
     * <p>
      * 将已入住的散客入住单关联到指定的团队预订，并处理账务合并
     * </p>
     *
      * @param stayId 散客入住单ID
      * @param dto    转入请求参数
      * @return 更新后的入住信息
     */

    /**
     * 换房
     * <p>
     * 将客人从当前房间换到新房间，自动调整账务
     * </p>
     *
     * @param dto 换房请求参数
     * @return 换房结果信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ChangeRoomVO changeRoom(ChangeRoomDTO dto) {
        // 1. 验证入住单存在且状态为在住
        Stay stay = mapper.selectById(dto.getStayId());
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }
        if (!StayConstants.STATUS_CHECKED_IN.equals(stay.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该入住单状态不是在住，无法换房");
        }

        // 2. 验证新房间存在且状态为空闲
        Room newRoom = roomMapper.selectById(dto.getNewRoomId());
        if (newRoom == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "新房间不存在");
        }

        // 2.1 Check if target room has other checked-in stays
        LambdaQueryWrapper<Stay> stayWrapper = new LambdaQueryWrapper<>();
        stayWrapper.eq(Stay::getRoomId, dto.getNewRoomId())
               .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
               .ne(Stay::getId, dto.getStayId());
        Long existingStayCount = mapper.selectCount(stayWrapper);
        if (existingStayCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新房间状态不是空闲，无法换房");
        }

        // 3. 验证新房间不是当前房间
        if (stay.getRoomId().equals(dto.getNewRoomId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新房间不能是当前房间");
        }

        // 4. 获取原房间信息
        Room oldRoom = roomMapper.selectById(stay.getRoomId());
        String oldRoomNo = oldRoom != null ? oldRoom.getRoomNo() : "未知";

        // 5. 获取新房型信息
        RoomType newRoomType = roomTypeMapper.selectById(newRoom.getRoomTypeId());

        // 6. 计算费用调整（如果房型不同，需要调整价格）
        BigDecimal amountAdjustment = BigDecimal.ZERO;
        if (!stay.getRoomId().equals(dto.getNewRoomId())) {
            // 计算原房间剩余天数的费用
            long remainingDays = ChronoUnit.DAYS.between(
                LocalDate.now(), stay.getCheckOutTime().toLocalDate());
            if (remainingDays <= 0) {
                remainingDays = 1;
            }

            // 获取原房型和新房型的价格
            RoomType oldRoomType = roomTypeMapper.selectById(oldRoom.getRoomTypeId());
            BigDecimal oldPrice = getRoomPrice(stay.getHotelId(), oldRoom.getId(), stay.getCheckInTime().toLocalDate());
            BigDecimal newPrice = getRoomPrice(stay.getHotelId(), newRoom.getId(), LocalDate.now());

            // 计算费用差额
            amountAdjustment = newPrice.subtract(oldPrice).multiply(BigDecimal.valueOf(remainingDays));
        }

        // 7. 更新原房间状态为脏房
        if (oldRoom != null) {
            oldRoom.setStatus(RoomConstants.STATUS_DIRTY);
            roomMapper.updateById(oldRoom);
        }

        // 8. 更新新房间状态为在住
        newRoom.setStatus(RoomConstants.STATUS_OCCUPIED);
        roomMapper.updateById(newRoom);

        // 9. 更新入住单
        stay.setRoomId(dto.getNewRoomId());
        if (amountAdjustment.compareTo(BigDecimal.ZERO) != 0) {
            stay.setTotalAmount(stay.getTotalAmount().add(amountAdjustment));
        }
        mapper.updateById(stay);

        // 10. 更新账务单金额（如果有费用调整）
        if (amountAdjustment.compareTo(BigDecimal.ZERO) != 0) {
            updateFolioForChangeRoom(stay.getId(), amountAdjustment);
        }

        // 11. 记录换房日志
        log.info("换房成功：stayId={}, stayNo={}, 从房间{}换到房间{}, 费用调整={}",
                stay.getId(), stay.getStayNo(), oldRoomNo, newRoom.getRoomNo(), amountAdjustment);

        // 12. 返回换房结果
        return ChangeRoomVO.builder()
                .stayId(stay.getId())
                .stayNo(stay.getStayNo())
                .oldRoomId(oldRoom != null ? oldRoom.getId() : null)
                .oldRoomNo(oldRoomNo)
                .newRoomId(newRoom.getId())
                .newRoomNo(newRoom.getRoomNo())
                .newRoomTypeId(newRoom.getRoomTypeId())
                .newRoomTypeName(newRoomType != null ? newRoomType.getName() : null)
                .reason(dto.getReason())
                .amountAdjustment(amountAdjustment)
                .newTotalAmount(stay.getTotalAmount())
                .build();
    }

    /**
     * 获取房间价格
     */
    private BigDecimal getRoomPrice(Long hotelId, Long roomId, LocalDate date) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            return BigDecimal.ZERO;
        }

        // 先查询房价码价格
        RoomPriceVO priceVO = roomPriceService.getPriceByDate(hotelId, room.getRoomTypeId(), date);
        if (priceVO != null && priceVO.getPrice() != null) {
            return priceVO.getPrice();
        }

        // 如果没有房价码价格，查询房型基础价
        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        return roomType != null && roomType.getBasePrice() != null ? roomType.getBasePrice() : BigDecimal.ZERO;
    }

    /**
     * 更新账务单金额（换房费用调整）
     */
    private void updateFolioForChangeRoom(Long stayId, BigDecimal amountAdjustment) {
        LambdaQueryWrapper<Folio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folio::getStayId, stayId)
               .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN);
        Folio folio = folioMapper.selectOne(wrapper);

        if (folio != null) {
            folio.setTotalAmount(folio.getTotalAmount().add(amountAdjustment));
            folio.setBalance(folio.getBalance().add(amountAdjustment));
            folioMapper.updateById(folio);

            // 创建调整交易记录
            FinTransaction transaction = new FinTransaction();
            transaction.setHotelId(folio.getHotelId());
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String prefix = "T" + dateStr;
            long seq = System.currentTimeMillis() % 10000;
            transaction.setTransactionNo(prefix + String.format("%04d", seq));
            transaction.setFolioId(folio.getId());
            transaction.setType(FolioConstants.TRANSACTION_ROOM_FEE);
            transaction.setAmount(amountAdjustment);
            transaction.setDescription("换房费用调整");
            transaction.setCreatedAt(LocalDateTime.now());
            finTransactionMapper.insert(transaction);

            log.info("换房账务调整：folioId={}, amountAdjustment={}", folio.getId(), amountAdjustment);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public StayVO transferToTeam(Long stayId, StayTransferDTO dto) {
        // 1. 验证散客入住单
        Stay stay = mapper.selectById(stayId);
        if (stay == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "入住单不存在");
        }
        if (!StayConstants.STATUS_CHECKED_IN.equals(stay.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只有在住状态的入住单才能转入团队");
        }
        if (stay.getTeamReservationId() != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该入住单已属于团队，不能重复转入");
        }

        // 2. 验证团队预订
        TeamReservation teamReservation = teamReservationMapper.selectById(dto.getTeamReservationId());
        if (teamReservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }
        if (ReservationConstants.STATUS_CANCELLED.equals(teamReservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已取消的团队预订不能接受散客入住单");
        }
        if (ReservationConstants.STATUS_CHECKED_OUT.equals(teamReservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已离店的团队预订不能接受散客入住单");
        }

        // 3. 更新入住单，关联团队预订
        stay.setTeamReservationId(teamReservation.getId());
        stay.setCheckInType("TEAM");
        mapper.updateById(stay);
        log.info("散客入住单已关联团队预订：stayId={}, teamReservationId={}", stayId, teamReservation.getId());

        // 4. 新增团队预订房间明细
        Room room = roomMapper.selectById(stay.getRoomId());
        TeamReservationRoom teamRoom = new TeamReservationRoom();
        teamRoom.setTeamReservationId(teamReservation.getId());
        teamRoom.setRoomTypeId(room != null ? room.getRoomTypeId() : null);
        teamRoom.setRoomId(stay.getRoomId());
        teamRoom.setAmount(stay.getTotalAmount());
        teamRoom.setStatus(ReservationConstants.ROOM_STATUS_CHECKED_IN);
        teamRoom.setStayId(stay.getId());
        teamRoom.setCheckInTime(stay.getCheckInTime());

        // 获取客人信息
        if (stay.getGuestId() != null) {
            Guest guest = guestMapper.selectById(stay.getGuestId());
            if (guest != null) {
                teamRoom.setGuestName(guest.getName());
                teamRoom.setGuestPhone(guest.getPhone());
                teamRoom.setGuestIdNo(guest.getIdNo());
                teamRoom.setGuestGender(guest.getGender());
            }
        }
        teamReservationRoomMapper.insert(teamRoom);
        log.info("已创建团队订房明细：teamReservationRoomId={}, stayId={}", teamRoom.getId(), stayId);

        // 5. 更新团队预订汇总信息
        teamReservation.setTotalRooms(teamReservation.getTotalRooms() + 1);
        teamReservation.setTotalAmount(teamReservation.getTotalAmount().add(
                stay.getTotalAmount() != null ? stay.getTotalAmount() : BigDecimal.ZERO));
        teamReservationMapper.updateById(teamReservation);

        // 6. 账务处理
        handleFolioTransfer(stay, teamReservation);

        log.info("散客入住单号转团队成功：stayId={}, teamReservationId={}", stayId, teamReservation.getId());
        return convertToVO(stay);
    }

    /**
     * 处理散客账务转入团队
     * <p>
     * 统一结算：将散客Folio金额合并到TeamFolio
     * 分开结算：散客Folio保持独立，通过stay关联团队
     * </p>
     */
    private void handleFolioTransfer(Stay stay, TeamReservation teamReservation) {
        // 查询散客的开放账务单
        LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
        folioWrapper.eq(Folio::getStayId, stay.getId())
                   .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN);
        Folio folio = folioMapper.selectOne(folioWrapper);

        if (folio == null) {
            log.warn("散客入住单没有开放的账务单，跳过账务合并：stayId={}", stay.getId());
            return;
        }

        if (ReservationConstants.SETTLEMENT_UNIFIED.equals(teamReservation.getSettlementType())) {
            // 统一结算：合并到团队账务单
            LambdaQueryWrapper<TeamFolio> teamFolioWrapper = new LambdaQueryWrapper<>();
            teamFolioWrapper.eq(TeamFolio::getTeamReservationId, teamReservation.getId())
                           .eq(TeamFolio::getStatus, StayConstants.FOLIO_STATUS_OPEN);
            TeamFolio teamFolio = teamFolioMapper.selectOne(teamFolioWrapper);

            if (teamFolio != null) {
                // 合并金额
                teamFolio.setTotalAmount(teamFolio.getTotalAmount().add(
                        folio.getTotalAmount() != null ? folio.getTotalAmount() : BigDecimal.ZERO));
                teamFolio.setPaidAmount(teamFolio.getPaidAmount().add(
                        folio.getPaidAmount() != null ? folio.getPaidAmount() : BigDecimal.ZERO));
                teamFolio.setBalance(teamFolio.getBalance().add(
                        folio.getBalance() != null ? folio.getBalance() : BigDecimal.ZERO));
                teamFolioMapper.updateById(teamFolio);

                 // 关闭散客账务单
                folio.setStatus(StayConstants.FOLIO_STATUS_CLOSED);
                folioMapper.updateById(folio);

                 log.info("散客房务已合并到团队账务单：folioId={}, teamFolioId={}, 合并金额={}",
                        folio.getId(), teamFolio.getId(), folio.getBalance());
            } else {
                 log.warn("团队预订没有开放的账务单，散客账务保持独立：teamReservationId={}", teamReservation.getId());
            }
        } else {
             // 分开结算：散客账务保持独立，仅记录关联
            folioMapper.updateById(folio);
             log.info("散客账务保持独立（分开结算）：folioId={}, teamReservationId={}",
                    folio.getId(), teamReservation.getId());
        }
    }
}



