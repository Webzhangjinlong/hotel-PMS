package com.hotel.pms.service.reservation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.constant.ReservationConstants;
import com.hotel.pms.common.constant.RoomConstants;
import com.hotel.pms.common.constant.StayConstants;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import com.hotel.pms.service.price.RoomPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 团队预订服务类
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class TeamReservationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private TeamReservationMapper teamReservationMapper;

    @Autowired
    private TeamReservationRoomMapper teamReservationRoomMapper;

    @Autowired
    private TeamFolioMapper teamFolioMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;
    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private StayMapper stayMapper;
    @Autowired
    private FolioMapper folioMapper;

    @Autowired
    private GuestMapper guestMapper;

    @Autowired
    private RoomPriceService roomPriceService;

    /**
     * 分页查询团队预订列表
     */
    public PageResponse<TeamReservationVO> pageList(TeamReservationQueryDTO queryDTO) {
        LambdaQueryWrapper<TeamReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, TeamReservation::getHotelId, queryDTO.getHotelId())
               .eq(StringUtils.hasText(queryDTO.getTeamReservationNo()), TeamReservation::getTeamReservationNo, queryDTO.getTeamReservationNo())
               .like(StringUtils.hasText(queryDTO.getTeamName()), TeamReservation::getTeamName, queryDTO.getTeamName())
               .like(StringUtils.hasText(queryDTO.getContactName()), TeamReservation::getContactName, queryDTO.getContactName())
               .eq(StringUtils.hasText(queryDTO.getContactPhone()), TeamReservation::getContactPhone, queryDTO.getContactPhone())
               .eq(StringUtils.hasText(queryDTO.getStatus()), TeamReservation::getStatus, queryDTO.getStatus())
               .ge(queryDTO.getCheckInDateStart() != null, TeamReservation::getCheckInDate, queryDTO.getCheckInDateStart())
               .le(queryDTO.getCheckInDateEnd() != null, TeamReservation::getCheckInDate, queryDTO.getCheckInDateEnd())
               .orderByDesc(TeamReservation::getCreatedAt);

        Page<TeamReservation> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<TeamReservation> result = teamReservationMapper.selectPage(page, wrapper);

        List<TeamReservationVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 根据ID查询团队预订
     */
    public TeamReservationVO getById(Long id) {
        TeamReservation reservation = teamReservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }
        return convertToVO(reservation);
    }

    /**
     * 创建团队预订
     */
    @Transactional(rollbackFor = Exception.class)

    /**
     * 计算房间总房费（根据每日房价）
     *
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @return 总房费
     */
    private BigDecimal calculateRoomTotalAmount(Long hotelId, Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, Long pricePlanId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDate currentDate = checkInDate;

        log.info("开始计算房间总房费：hotelId={}, roomTypeId={}, 入住={}, 离店={}", hotelId, roomTypeId, checkInDate, checkOutDate);

        while (currentDate.isBefore(checkOutDate)) {
            // 查询当日房价
            RoomPriceVO priceVO = roomPriceService.getPriceByDate(hotelId, roomTypeId, currentDate, pricePlanId);

            if (priceVO != null && priceVO.getPrice() != null) {
                // 使用每日房价
                totalAmount = totalAmount.add(priceVO.getPrice());
                log.info("日期{}: 使用每日房价={}, 累计金额={}", currentDate, priceVO.getPrice(), totalAmount);
            } else {
                // 如果没有房价信息，使用房型基础价格
                RoomType roomType = roomTypeMapper.selectById(roomTypeId);
                if (roomType != null) {
                    totalAmount = totalAmount.add(roomType.getBasePrice());
                    log.info("日期{}: 使用基础价格={}, 累计金额={}", currentDate, roomType.getBasePrice(), totalAmount);
                } else {
                    log.warn("日期{}: 未找到房型信息，roomTypeId={}", currentDate, roomTypeId);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        log.info("计算房间总房费完成：总金额={}", totalAmount);
        return totalAmount;
    }

public TeamReservationVO create(TeamReservationCreateDTO dto) {
        // 验证酒店存在
        Hotel hotel = hotelMapper.selectById(dto.getHotelId());
        if (hotel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "酒店不存在");
        }

        // 验证日期
        if (dto.getCheckInDate().isAfter(dto.getCheckOutDate())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "入住日期不能晚于离店日期");
        }

        // 验证房间明细
        if (dto.getRooms() == null || dto.getRooms().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "房间明细不能为空");
        }

        // 验证房型存在
        for (TeamReservationCreateDTO.RoomDetail room : dto.getRooms()) {
            RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
            if (roomType == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房型不存在: " + room.getRoomTypeId());
            }

            // 如果预分配房间，验证房间存在且可用
            if (room.getRoomId() != null) {
                Room roomEntity = roomMapper.selectById(room.getRoomId());
                if (roomEntity == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房间不存在: " + room.getRoomId());
                }
                if (!RoomConstants.STATUS_AVAILABLE.equals(roomEntity.getStatus())) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "房间不可用: " + roomEntity.getRoomNo());
                }
            }
        }

        // 创建团队预订
        TeamReservation reservation = new TeamReservation();
        reservation.setHotelId(dto.getHotelId());
        reservation.setTeamReservationNo(generateTeamReservationNo(dto.getHotelId()));
        reservation.setTeamName(dto.getTeamName());
        reservation.setContactName(dto.getContactName());
        reservation.setContactPhone(dto.getContactPhone());
        reservation.setContactIdNo(dto.getContactIdNo());
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        reservation.setNights((int) (dto.getCheckOutDate().toEpochDay() - dto.getCheckInDate().toEpochDay()));
        reservation.setTotalRooms(dto.getRooms().size());
        reservation.setSettlementType(dto.getSettlementType());
        reservation.setStatus(ReservationConstants.STATUS_PENDING);
        reservation.setSpecialRequests(dto.getSpecialRequests());

        teamReservationMapper.insert(reservation);

        // 创建房间明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (TeamReservationCreateDTO.RoomDetail roomDetail : dto.getRooms()) {
            TeamReservationRoom room = new TeamReservationRoom();
            room.setTeamReservationId(reservation.getId());
            room.setRoomTypeId(roomDetail.getRoomTypeId());
            room.setRoomId(roomDetail.getRoomId());
            room.setGuestName(roomDetail.getGuestName());
            room.setGuestPhone(roomDetail.getGuestPhone());
            room.setGuestIdNo(roomDetail.getGuestIdNo());
            room.setGuestGender(roomDetail.getGuestGender());
            room.setStatus(ReservationConstants.ROOM_STATUS_PENDING);

            // 计算金额（优先使用用户指定金额，否则使用每日房价计算）
            BigDecimal amount = roomDetail.getAmount();
            if (amount == null) {
                amount = calculateRoomTotalAmount(reservation.getHotelId(), roomDetail.getRoomTypeId(), reservation.getCheckInDate(), reservation.getCheckOutDate(), dto.getPricePlanId());
            }
            room.setAmount(amount);
            totalAmount = totalAmount.add(amount);

            teamReservationRoomMapper.insert(room);
        }

        // 更新总金额
        reservation.setTotalAmount(totalAmount);
        reservation.setPricePlanId(dto.getPricePlanId());
        teamReservationMapper.updateById(reservation);

        log.info("创建团队预订成功：id={}, teamReservationNo={}", reservation.getId(), reservation.getTeamReservationNo());

        return convertToVO(reservation);
    }

    /**
     * 更新团队预订
     */
    @Transactional(rollbackFor = Exception.class)
    public TeamReservationVO update(Long id, TeamReservationUpdateDTO dto) {
        TeamReservation reservation = teamReservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }

        // 只有待确认状态可以修改
        if (!ReservationConstants.STATUS_PENDING.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许修改");
        }

        // 更新基本信息
        if (StringUtils.hasText(dto.getTeamName())) {
            reservation.setTeamName(dto.getTeamName());
        }
        if (StringUtils.hasText(dto.getContactName())) {
            reservation.setContactName(dto.getContactName());
        }
        if (StringUtils.hasText(dto.getContactPhone())) {
            reservation.setContactPhone(dto.getContactPhone());
        }
        if (dto.getContactIdNo() != null) {
            reservation.setContactIdNo(dto.getContactIdNo());
        }
        if (StringUtils.hasText(dto.getSettlementType())) {
            reservation.setSettlementType(dto.getSettlementType());
        }
        if (dto.getSpecialRequests() != null) {
            reservation.setSpecialRequests(dto.getSpecialRequests());
        }

        teamReservationMapper.updateById(reservation);

        log.info("更新团队预订成功：id={}", id);

        return convertToVO(reservation);
    }

    /**
     * 取消团队预订
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        TeamReservation reservation = teamReservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }

        // 只有待确认和已确认状态可以取消
        if (!ReservationConstants.STATUS_PENDING.equals(reservation.getStatus()) &&
            !ReservationConstants.STATUS_CONFIRMED.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许取消");
        }

        reservation.setStatus(ReservationConstants.STATUS_CANCELLED);
        teamReservationMapper.updateById(reservation);

        log.info("取消团队预订成功：id={}, teamReservationNo={}", id, reservation.getTeamReservationNo());
    }

    /**
     * 确认团队预订
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        TeamReservation reservation = teamReservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }

        if (!ReservationConstants.STATUS_PENDING.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许确认");
        }

        reservation.setStatus(ReservationConstants.STATUS_CONFIRMED);
        teamReservationMapper.updateById(reservation);

        log.info("确认团队预订成功：id={}, teamReservationNo={}", id, reservation.getTeamReservationNo());
    }

    /**
     * 团队入住
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCheckIn(Long id, TeamCheckInDTO dto) {
        TeamReservation reservation = teamReservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }

        if (!ReservationConstants.STATUS_CONFIRMED.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许入住");
        }

        // 查询需要入住的房间明细
        List<TeamReservationRoom> rooms;
        if (dto.getRoomIds() != null && !dto.getRoomIds().isEmpty()) {
            rooms = dto.getRoomIds().stream()
                    .map(teamReservationRoomMapper::selectById)
                    .collect(Collectors.toList());
        } else {
            LambdaQueryWrapper<TeamReservationRoom> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TeamReservationRoom::getTeamReservationId, id)
                   .eq(TeamReservationRoom::getStatus, ReservationConstants.ROOM_STATUS_PENDING);
            rooms = teamReservationRoomMapper.selectList(wrapper);
        }

        // 处理房间分配
        if (dto.getAssignments() != null) {
            for (TeamCheckInDTO.RoomAssignment assignment : dto.getAssignments()) {
                TeamReservationRoom room = teamReservationRoomMapper.selectById(assignment.getRoomDetailId());
                if (room != null) {
                    room.setRoomId(assignment.getRoomId());
                    teamReservationRoomMapper.updateById(room);
                }
            }
        }

        // 执行入住
        for (TeamReservationRoom room : rooms) {
            if (room.getRoomId() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "房间未分配，请先分配房间");
            }

            // 创建或查找客人
            Guest guest = findOrCreateGuest(
                reservation.getHotelId(),
                room.getGuestName(),
                room.getGuestPhone(),
                room.getGuestIdNo(),
                room.getGuestGender()
            );

            // 创建入住单
            Stay stay = new Stay();
            stay.setHotelId(reservation.getHotelId());
            stay.setStayNo(generateStayNo(reservation.getHotelId()));
            stay.setTeamReservationId(reservation.getId());
            stay.setRoomId(room.getRoomId());
            stay.setGuestId(guest.getId());
            stay.setCheckInTime(LocalDateTime.now());
            stay.setCheckOutTime(reservation.getCheckOutDate().atTime(12, 0));
            stay.setTotalAmount(room.getAmount());
            stay.setStatus(StayConstants.STATUS_CHECKED_IN);
            stay.setCheckInType("TEAM");
            // 使用团队预订的房价码
            stay.setPricePlanId(reservation.getPricePlanId());
            stayMapper.insert(stay);

            // 更新房间状态
            Room roomEntity = roomMapper.selectById(room.getRoomId());
            roomEntity.setStatus(RoomConstants.STATUS_OCCUPIED);
            roomMapper.updateById(roomEntity);

            // 更新房间明细状态
            room.setStayId(stay.getId());
            room.setCheckInTime(LocalDateTime.now());
            room.setStatus(ReservationConstants.ROOM_STATUS_CHECKED_IN);
            teamReservationRoomMapper.updateById(room);
        }

        // 更新团队预订状态
        reservation.setStatus(ReservationConstants.STATUS_CHECKED_IN);
        teamReservationMapper.updateById(reservation);

        // 创建账务单（无论统一结算还是分开结算，都创建团队账务单便于统一管理）
        TeamFolio teamFolio = new TeamFolio();
        teamFolio.setHotelId(reservation.getHotelId());
        teamFolio.setTeamReservationId(reservation.getId());
        teamFolio.setFolioNo(generateFolioNo(reservation.getHotelId()));
        teamFolio.setTotalAmount(reservation.getTotalAmount());
        teamFolio.setPaidAmount(BigDecimal.ZERO);
        teamFolio.setBalance(reservation.getTotalAmount());
        teamFolio.setStatus(StayConstants.FOLIO_STATUS_OPEN);
        teamFolioMapper.insert(teamFolio);
        
        // 为每个入住单创建独立账务单（便于单间房收款）
        for (TeamReservationRoom room : rooms) {
            Stay stay = stayMapper.selectById(room.getStayId());
            if (stay != null) {
                Folio folio = new Folio();
                folio.setHotelId(reservation.getHotelId());
                folio.setStayId(stay.getId());
                folio.setGuestId(stay.getGuestId());
                folio.setFolioNo(generateStayFolioNo(reservation.getHotelId()));
                folio.setTotalAmount(room.getAmount());
                folio.setPaidAmount(BigDecimal.ZERO);
                folio.setBalance(room.getAmount());
                folio.setStatus(StayConstants.FOLIO_STATUS_OPEN);
                folioMapper.insert(folio);
            }
        }

        log.info("团队入住成功：id={}, 入住{}间房", id, rooms.size());
    }

    /**
     * 团队退房
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCheckOut(Long id, TeamCheckOutDTO dto) {
        TeamReservation reservation = teamReservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }

        if (!ReservationConstants.STATUS_CHECKED_IN.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许退房");
        }

        // 查询需要退房的房间明细
        List<TeamReservationRoom> rooms;
        if (dto.getRoomIds() != null && !dto.getRoomIds().isEmpty()) {
            rooms = dto.getRoomIds().stream()
                    .map(teamReservationRoomMapper::selectById)
                    .collect(Collectors.toList());
        } else {
            LambdaQueryWrapper<TeamReservationRoom> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TeamReservationRoom::getTeamReservationId, id)
                   .eq(TeamReservationRoom::getStatus, ReservationConstants.ROOM_STATUS_CHECKED_IN);
            rooms = teamReservationRoomMapper.selectList(wrapper);
        }

        // check settlement before checkout
        if (ReservationConstants.SETTLEMENT_SEPARATE.equals(reservation.getSettlementType())) {
            for (TeamReservationRoom room : rooms) {
                if (room.getStayId() != null) {
                    LambdaQueryWrapper<Folio> folioCheck = new LambdaQueryWrapper<>();
                    folioCheck.eq(Folio::getStayId, room.getStayId())
                             .eq(Folio::getStatus, StayConstants.FOLIO_STATUS_OPEN);
                    Folio stayFolio = folioMapper.selectOne(folioCheck);
                    if (stayFolio != null && stayFolio.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        throw new BusinessException(ResultCode.BAD_REQUEST, "SETTLEMENT_REQUIRED");
                    }
                }
            }
        }

        // 执行退房
        for (TeamReservationRoom room : rooms) {
            // 更新入住单状态
            Stay stay = stayMapper.selectById(room.getStayId());
            if (stay != null) {
                stay.setStatus(StayConstants.STATUS_CHECKED_OUT);
                stay.setActualCheckOutTime(LocalDateTime.now());
                stayMapper.updateById(stay);
            }

            // 更新房间状态为脏房
            Room roomEntity = roomMapper.selectById(room.getRoomId());
            if (roomEntity != null) {
                roomEntity.setStatus(RoomConstants.STATUS_DIRTY);
                roomMapper.updateById(roomEntity);
            }

            // 更新房间明细状态
            room.setCheckOutTime(LocalDateTime.now());
            room.setStatus(ReservationConstants.ROOM_STATUS_CHECKED_OUT);
            teamReservationRoomMapper.updateById(room);
        }

        // 检查是否所有房间都已退房
        LambdaQueryWrapper<TeamReservationRoom> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(TeamReservationRoom::getTeamReservationId, id)
                    .ne(TeamReservationRoom::getStatus, ReservationConstants.ROOM_STATUS_CHECKED_OUT);
        long remaining = teamReservationRoomMapper.selectCount(checkWrapper);

        if (remaining == 0) {
            // 所有房间都已退房
            reservation.setStatus(ReservationConstants.STATUS_CHECKED_OUT);
            teamReservationMapper.updateById(reservation);

            // 如果统一结算，关闭团队账务单
            if (ReservationConstants.SETTLEMENT_UNIFIED.equals(reservation.getSettlementType())) {
                LambdaQueryWrapper<TeamFolio> folioWrapper = new LambdaQueryWrapper<>();
                folioWrapper.eq(TeamFolio::getTeamReservationId, id)
                           .eq(TeamFolio::getStatus, "OPEN");
                TeamFolio folio = teamFolioMapper.selectOne(folioWrapper);
                if (folio != null) {
                    folio.setStatus("CLOSED");
                    teamFolioMapper.updateById(folio);
                }
            }
        }

        log.info("团队退房成功：id={}, 退房{}间房", id, rooms.size());
    }

    /**
     * 团队批量续住
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchExtend(Long id, TeamExtendDTO dto) {
        TeamReservation reservation = teamReservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }

        if (!ReservationConstants.STATUS_CHECKED_IN.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许续住");
        }

        LocalDate newCheckOutDate = dto.getNewCheckOutDate();
        if (!newCheckOutDate.isAfter(reservation.getCheckOutDate())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新离店日期必须晚于当前离店日期");
        }

        // 查询所有在住的房间明细
        LambdaQueryWrapper<TeamReservationRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamReservationRoom::getTeamReservationId, id)
               .eq(TeamReservationRoom::getStatus, ReservationConstants.ROOM_STATUS_CHECKED_IN);
        List<TeamReservationRoom> rooms = teamReservationRoomMapper.selectList(wrapper);

        if (rooms.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "没有在住的房间");
        }

        BigDecimal totalAdditionalAmount = BigDecimal.ZERO;

        for (TeamReservationRoom room : rooms) {
            // 更新入住单
            Stay stay = stayMapper.selectById(room.getStayId());
            if (stay != null) {
                LocalDateTime oldCheckOutTime = stay.getCheckOutTime();
                LocalDateTime newCheckOutTime = newCheckOutDate.atTime(12, 0);
                int extendedDays = (int) ChronoUnit.DAYS.between(oldCheckOutTime.toLocalDate(), newCheckOutDate);

                // 计算续住费用
                BigDecimal additionalAmount = calculateExtendAmount(stay, extendedDays);

                // 更新入住单
                stay.setCheckOutTime(newCheckOutTime);
                stay.setTotalAmount(stay.getTotalAmount().add(additionalAmount));
                stayMapper.updateById(stay);

                // 更新个人账务单
                updateFolioForExtend(stay.getId(), additionalAmount);

                // 更新房间明细金额
                room.setAmount(room.getAmount().add(additionalAmount));
                teamReservationRoomMapper.updateById(room);

                totalAdditionalAmount = totalAdditionalAmount.add(additionalAmount);
            }
        }

        // 更新团队预订
        reservation.setCheckOutDate(newCheckOutDate);
        reservation.setNights((int) (newCheckOutDate.toEpochDay() - reservation.getCheckInDate().toEpochDay()));
        reservation.setTotalAmount(reservation.getTotalAmount().add(totalAdditionalAmount));
        teamReservationMapper.updateById(reservation);

        // 如果是统一结算，更新团队账务单
        if (ReservationConstants.SETTLEMENT_UNIFIED.equals(reservation.getSettlementType())) {
            LambdaQueryWrapper<TeamFolio> folioWrapper = new LambdaQueryWrapper<>();
            folioWrapper.eq(TeamFolio::getTeamReservationId, id)
                       .eq(TeamFolio::getStatus, StayConstants.FOLIO_STATUS_OPEN);
            TeamFolio teamFolio = teamFolioMapper.selectOne(folioWrapper);
            if (teamFolio != null) {
                teamFolio.setTotalAmount(teamFolio.getTotalAmount().add(totalAdditionalAmount));
                teamFolio.setBalance(teamFolio.getBalance().add(totalAdditionalAmount));
                teamFolioMapper.updateById(teamFolio);
            }
        }

        log.info("团队续住成功：id={}, 续住{}间房, 新离店日期={}", id, rooms.size(), newCheckOutDate);
    }

    private BigDecimal calculateExtendAmount(Stay stay, int extendedDays) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDate currentDate = stay.getCheckOutTime().toLocalDate();
        LocalDate checkOutDate = currentDate.plusDays(extendedDays);

        // 通过roomId获取roomTypeId
        Room room = roomMapper.selectById(stay.getRoomId());
        Long roomTypeId = room != null ? room.getRoomTypeId() : null;

        while (currentDate.isBefore(checkOutDate)) {
            if (roomTypeId != null) {
                com.hotel.pms.common.dto.RoomPriceVO priceVO = roomPriceService.getPriceByDate(
                    stay.getHotelId(), roomTypeId, currentDate, stay.getPricePlanId());
                totalAmount = totalAmount.add(priceVO.getPrice());
            }
            currentDate = currentDate.plusDays(1);
        }

        return totalAmount;
    }

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
     * 分配房间
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRooms(Long id, List<TeamCheckInDTO.RoomAssignment> assignments) {
        TeamReservation reservation = teamReservationMapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }

        for (TeamCheckInDTO.RoomAssignment assignment : assignments) {
            TeamReservationRoom room = teamReservationRoomMapper.selectById(assignment.getRoomDetailId());
            if (room == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房间明细不存在: " + assignment.getRoomDetailId());
            }

            // 验证房间存在且可用
            Room roomEntity = roomMapper.selectById(assignment.getRoomId());
            if (roomEntity == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房间不存在: " + assignment.getRoomId());
            }
            if (!RoomConstants.STATUS_AVAILABLE.equals(roomEntity.getStatus())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "房间不可用: " + roomEntity.getRoomNo());
            }

            room.setRoomId(assignment.getRoomId());
            teamReservationRoomMapper.updateById(room);
        }

        log.info("分配房间成功：teamReservationId={}, 分配{}间房", id, assignments.size());
    }

    /**
     * 查找或创建客人
     */
    /**
     * 查找或创建客人（精确匹配+日志记录方式）
     */
    private Guest findOrCreateGuest(Long hotelId, String name, String phone, String idNo, String gender) {
        Guest existing = null;

        // 1. 精确匹配：证件号+姓名
        if (StringUtils.hasText(idNo) && StringUtils.hasText(name)) {
            LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Guest::getHotelId, hotelId)
                   .eq(Guest::getIdNo, idNo)
                   .eq(Guest::getName, name);
            existing = guestMapper.selectOne(wrapper);
            if (existing != null) {
                log.debug("客人匹配：证件号+姓名精确匹配成功，姓名={}, 证件号={}", name, idNo);
                return existing;
            }
        }

        // 2. 精确匹配：手机号+姓名
        if (StringUtils.hasText(phone) && StringUtils.hasText(name)) {
            LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Guest::getHotelId, hotelId)
                   .eq(Guest::getPhone, phone)
                   .eq(Guest::getName, name);
            existing = guestMapper.selectOne(wrapper);
            if (existing != null) {
                log.debug("客人匹配：手机号+姓名精确匹配成功，姓名={}, 手机号={}", name, phone);
                return existing;
            }
        }

        // 3. 宽松匹配：仅证件号（记录警告日志）
        if (StringUtils.hasText(idNo)) {
            LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Guest::getHotelId, hotelId)
                   .eq(Guest::getIdNo, idNo);
            existing = guestMapper.selectOne(wrapper);
            if (existing != null) {
                log.warn("客人匹配：证件号匹配但姓名不同，输入姓名={}, 匹配姓名={}, 证件号={}",
                        name, existing.getName(), idNo);
                // 可以选择返回existing或null，这里选择返回existing但记录警告
                return existing;
            }
        }

        // 4. 宽松匹配：仅手机号（记录警告日志）
        if (StringUtils.hasText(phone)) {
            LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Guest::getHotelId, hotelId)
                   .eq(Guest::getPhone, phone);
            existing = guestMapper.selectOne(wrapper);
            if (existing != null) {
                log.warn("客人匹配：手机号匹配但姓名不同，输入姓名={}, 匹配姓名={}, 手机号={}",
                        name, existing.getName(), phone);
                // 可以选择返回existing或null，这里选择返回existing但记录警告
                return existing;
            }
        }

        // 5. 创建新客人
        Guest guest = new Guest();
        guest.setHotelId(hotelId);
        guest.setName(name);
        guest.setPhone(phone);
        guest.setIdNo(idNo);
        guest.setGender(gender);
        guest.setIdType("ID_CARD");
        guest.setNationality("中国");
        guestMapper.insert(guest);
        log.info("客人匹配：创建新客人，姓名={}, 手机号={}, 证件号={}", name, phone, idNo);
        return guest;
    }

    /**
     * 生成团队预订号
     */
    private String generateTeamReservationNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = "T" + dateStr;

        LambdaQueryWrapper<TeamReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamReservation::getHotelId, hotelId)
               .likeRight(TeamReservation::getTeamReservationNo, prefix)
               .orderByDesc(TeamReservation::getTeamReservationNo)
               .last("LIMIT 1");

        TeamReservation latest = teamReservationMapper.selectOne(wrapper);

        int nextSeq = 1;
        if (latest != null && latest.getTeamReservationNo() != null) {
            String lastNo = latest.getTeamReservationNo();
            String seqStr = lastNo.substring(lastNo.length() - 4);
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        }

        return String.format("%s%04d", prefix, nextSeq);
    }

    /**
     * 生成入住单号
     */
    private String generateStayNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = "S" + dateStr;

        // 简化版，实际应查询数据库
        return prefix + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 生成账单号
     */
    private String generateFolioNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = "F" + dateStr;

        LambdaQueryWrapper<TeamFolio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamFolio::getHotelId, hotelId)
               .likeRight(TeamFolio::getFolioNo, prefix)
               .orderByDesc(TeamFolio::getFolioNo)
               .last("LIMIT 1");

        TeamFolio latest = teamFolioMapper.selectOne(wrapper);

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

    private String generateStayFolioNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = "SF" + dateStr;

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
     * 转换为VO
     */
    private TeamReservationVO convertToVO(TeamReservation reservation) {
        TeamReservationVO vo = new TeamReservationVO();
        vo.setId(reservation.getId());
        vo.setHotelId(reservation.getHotelId());
        vo.setTeamReservationNo(reservation.getTeamReservationNo());
        vo.setTeamName(reservation.getTeamName());
        vo.setContactName(reservation.getContactName());
        vo.setContactPhone(reservation.getContactPhone());
        vo.setContactIdNo(reservation.getContactIdNo());
        vo.setCheckInDate(reservation.getCheckInDate());
        vo.setCheckOutDate(reservation.getCheckOutDate());
        vo.setNights(reservation.getNights());
        vo.setTotalRooms(reservation.getTotalRooms());
        vo.setTotalAmount(reservation.getTotalAmount());
        vo.setSettlementType(reservation.getSettlementType());
        vo.setStatus(reservation.getStatus());
        vo.setSpecialRequests(reservation.getSpecialRequests());
        vo.setCreatedAt(reservation.getCreatedAt());

        // 查询酒店名称
        Hotel hotel = hotelMapper.selectById(reservation.getHotelId());
        if (hotel != null) {
            vo.setHotelName(hotel.getName());
        }

        // 查询房间明细
        LambdaQueryWrapper<TeamReservationRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamReservationRoom::getTeamReservationId, reservation.getId());
        List<TeamReservationRoom> rooms = teamReservationRoomMapper.selectList(wrapper);

        List<TeamReservationRoomVO> roomVOs = rooms.stream()
                .map(this::convertRoomToVO)
                .collect(Collectors.toList());
        vo.setRooms(roomVOs);

        return vo;
    }

    /**
     * 转换房间明细为VO
     */
    private TeamReservationRoomVO convertRoomToVO(TeamReservationRoom room) {
        TeamReservationRoomVO vo = new TeamReservationRoomVO();
        vo.setId(room.getId());
        vo.setTeamReservationId(room.getTeamReservationId());
        vo.setRoomTypeId(room.getRoomTypeId());
        vo.setRoomId(room.getRoomId());
        vo.setGuestName(room.getGuestName());
        vo.setGuestPhone(room.getGuestPhone());
        vo.setGuestIdNo(room.getGuestIdNo());
        vo.setGuestGender(room.getGuestGender());
        vo.setAmount(room.getAmount());
        vo.setStatus(room.getStatus());
        vo.setStayId(room.getStayId());
        vo.setCheckInTime(room.getCheckInTime());
        vo.setCheckOutTime(room.getCheckOutTime());

        // 查询房型名称
        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        if (roomType != null) {
            vo.setRoomTypeName(roomType.getName());
        }

        // 查询房间号
        if (room.getRoomId() != null) {
            Room roomEntity = roomMapper.selectById(room.getRoomId());
            if (roomEntity != null) {
                vo.setRoomNo(roomEntity.getRoomNo());
            }
        }

        return vo;
    }

    /**
     * 拆分房间（从团队中移除房间，转为散客）
     * <p>
     * 场景A（未入住）：删除房间明细，更新团队汇总
     * 场景B（已入住）：解除入住单关联，删除房间明细，更新团队汇总，处理账务拆分
     * </p>
     *
     * @param teamReservationId 团队预订ID
     * @param roomDetailId      团队预订房间明细ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void splitRoom(Long teamReservationId, Long roomDetailId) {
        // 1. 校验团队预订
        TeamReservation reservation = teamReservationMapper.selectById(teamReservationId);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队预订不存在");
        }
        if (ReservationConstants.STATUS_CANCELLED.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已取消的团队预订不能拆分房间");
        }
        if (ReservationConstants.STATUS_CHECKED_OUT.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已离店的团队预订不能拆分房间");
        }

        // 2. 校验房间明细
        TeamReservationRoom teamRoom = teamReservationRoomMapper.selectById(roomDetailId);
        if (teamRoom == null || !teamRoom.getTeamReservationId().equals(teamReservationId)) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房间明细不存在或不属于该团队预订");
        }

        // 3. 校验团队房间数 >= 2
        LambdaQueryWrapper<TeamReservationRoom> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(TeamReservationRoom::getTeamReservationId, teamReservationId);
        long roomCount = teamReservationRoomMapper.selectCount(countWrapper);
        if (roomCount < 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "团队只剩1间房，不允许拆分");
        }

        BigDecimal roomAmount = teamRoom.getAmount() != null ? teamRoom.getAmount() : BigDecimal.ZERO;

        // 4. 根据团队状态处理
        boolean isCheckedIn = ReservationConstants.STATUS_CHECKED_IN.equals(reservation.getStatus());

        if (isCheckedIn && teamRoom.getStayId() != null) {
            // 场景B：已入住 - 解除入住单与团队的关联
            Stay stay = stayMapper.selectById(teamRoom.getStayId());
            if (stay != null) {
                stay.setTeamReservationId(null);
                stay.setReservationId(null);
                stay.setCheckInType("INDIVIDUAL");
                stayMapper.updateById(stay);
                log.info("入住单已解除团队关联：stayId={}, teamReservationId={}", stay.getId(), teamReservationId);

                // 账务处理
                handleFolioSplitForRoom(stay, reservation, roomAmount);
            }
        }

        // 5. 删除房间明细
        teamReservationRoomMapper.deleteById(roomDetailId);
        log.info("团队房间明细已删除：roomDetailId={}, teamReservationId={}", roomDetailId, teamReservationId);

        // 6. 更新团队预订汇总
        reservation.setTotalRooms((int) (roomCount - 1));
        reservation.setTotalAmount(reservation.getTotalAmount().subtract(roomAmount));
        teamReservationMapper.updateById(reservation);

        // 7. 更新团队账务单金额
        updateTeamFolioAmount(teamReservationId, roomAmount.negate());

        log.info("房间拆分成功：teamReservationId={}, roomDetailId={}, roomAmount={}", teamReservationId, roomDetailId, roomAmount);
    }

    /**
     * 拆分房间时的账务处理
     * <p>
     * 统一结算：从团队账务单扣除金额，入住单的独立账务单保持不变
     * 分开结算：入住单的账务单已独立，无需额外处理
     * </p>
     */
    private void handleFolioSplitForRoom(Stay stay, TeamReservation reservation, BigDecimal roomAmount) {
        if (ReservationConstants.SETTLEMENT_UNIFIED.equals(reservation.getSettlementType())) {
            // 统一结算：入住单的独立账务单已存在（入住时创建），无需额外处理
            // 团队账务单金额在 updateTeamFolioAmount 中统一更新
            log.info("统一结算拆分：入住单账务单保持独立，stayId={}", stay.getId());
        } else {
            // 分开结算：账务单已独立，无需处理
            log.info("分开结算拆分：入住单账务单已独立，stayId={}", stay.getId());
        }
    }

    /**
     * 更新团队账务单金额
     */
    private void updateTeamFolioAmount(Long teamReservationId, BigDecimal amountChange) {
        LambdaQueryWrapper<TeamFolio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamFolio::getTeamReservationId, teamReservationId)
               .eq(TeamFolio::getStatus, StayConstants.FOLIO_STATUS_OPEN);
        TeamFolio teamFolio = teamFolioMapper.selectOne(wrapper);
        if (teamFolio != null) {
            teamFolio.setTotalAmount(teamFolio.getTotalAmount().add(amountChange));
            teamFolio.setBalance(teamFolio.getBalance().add(amountChange));
            teamFolioMapper.updateById(teamFolio);
            log.info("团队账务单金额已更新：teamFolioId={}, amountChange={}, newTotal={}",
                    teamFolio.getId(), amountChange, teamFolio.getTotalAmount());
        }
    }
}
