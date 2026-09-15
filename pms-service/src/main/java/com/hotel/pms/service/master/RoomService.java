package com.hotel.pms.service.master;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.constant.RoomConstants;
import com.hotel.pms.common.constant.StayConstants;
import com.hotel.pms.common.dto.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.HotelFloor;
import com.hotel.pms.dao.entity.Reservation;
import com.hotel.pms.dao.entity.Stay;
import com.hotel.pms.dao.entity.Guest;
import com.hotel.pms.dao.entity.Room;
import com.hotel.pms.dao.entity.RoomPrice;
import com.hotel.pms.dao.entity.RoomPrice;
import com.hotel.pms.dao.entity.RoomType;
import com.hotel.pms.dao.mapper.HotelFloorMapper;
import com.hotel.pms.dao.mapper.ReservationMapper;
import com.hotel.pms.dao.mapper.StayMapper;
import com.hotel.pms.dao.mapper.GuestMapper;




import com.hotel.pms.dao.mapper.RoomMapper;
import com.hotel.pms.dao.mapper.RoomPriceMapper;
import com.hotel.pms.dao.mapper.RoomTypeMapper;
import com.hotel.pms.service.config.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间服务类
 * <p>
 * 负责房间的业务逻辑处理
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class RoomService extends BaseService<Room, RoomMapper> {

    /** 房型Mapper */
    @Autowired
    private RoomTypeMapper roomTypeMapper;

    /** 楼层Mapper */
    @Autowired
    private HotelFloorMapper floorMapper;

    /** 入住Mapper */
    @Autowired
    private GuestMapper guestMapper;

    /** 入住Mapper */
    @Autowired
    private StayMapper stayMapper;

    /** 预订Mapper */
    @Autowired
    private ReservationMapper reservationMapper;

    /** 房价Mapper */
    @Autowired
    private RoomPriceMapper roomPriceMapper;

    /** 房价码Mapper */
//    @Autowired
//
//    /** 房价码明细Mapper */
//    @Autowired

    /**
     * 分页查询房间列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResponse<RoomVO> pageList(RoomQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, Room::getHotelId, queryDTO.getHotelId())
               .eq(queryDTO.getRoomTypeId() != null, Room::getRoomTypeId, queryDTO.getRoomTypeId())
               .eq(queryDTO.getFloorId() != null, Room::getFloorId, queryDTO.getFloorId())
               .like(StringUtils.hasText(queryDTO.getRoomNo()), Room::getRoomNo, queryDTO.getRoomNo())
               .eq(StringUtils.hasText(queryDTO.getStatus()), Room::getStatus, queryDTO.getStatus())
               .orderByAsc(Room::getRoomNo);

        // 执行分页查询
        Page<Room> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Room> result = mapper.selectPage(page, wrapper);

        // 转换为VO
        List<RoomVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 查询房间列表（不分页）
     *
     * @param hotelId 酒店ID
     * @return 房间列表
     */
    public List<RoomVO> listByHotelId(Long hotelId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getHotelId, hotelId)
               .orderByAsc(Room::getRoomNo);

        List<Room> list = mapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询房间
     *
     * @param id 房间ID
     * @return 房间信息
     */
    public RoomVO getById(Long id) {
        Room room = mapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return convertToVO(room);
    }

    /**
     * 创建房间
     *
     * @param dto 创建参数
     * @return 房间信息
     */
    @Transactional(rollbackFor = Exception.class)
    public RoomVO create(RoomCreateDTO dto) {
        // 检查房间号是否重复
        checkRoomNoDuplicate(dto.getHotelId(), dto.getRoomNo(), null);

        // 检查房型是否存在
        RoomType roomType = roomTypeMapper.selectById(dto.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房型不存在");
        }

        // 检查楼层是否存在
        HotelFloor floor = floorMapper.selectById(dto.getFloorId());
        if (floor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "楼层不存在");
        }

        // 创建房间实体
        Room room = new Room();
        BeanUtils.copyProperties(dto, room);
        room.setStatus(RoomConstants.STATUS_AVAILABLE);

        // 保存到数据库
        mapper.insert(room);

        log.info("创建房间成功：hotelId={}, roomNo={}", dto.getHotelId(), dto.getRoomNo());

        return convertToVO(room);
    }

    /**
     * 更新房间
     *
     * @param id 房间ID
     * @param dto 更新参数
     * @return 房间信息
     */
    @Transactional(rollbackFor = Exception.class)
    public RoomVO update(Long id, RoomUpdateDTO dto) {
        // 查询房间
        Room room = mapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 如果修改了房间号，检查是否重复
        if (StringUtils.hasText(dto.getRoomNo()) && !dto.getRoomNo().equals(room.getRoomNo())) {
            checkRoomNoDuplicate(room.getHotelId(), dto.getRoomNo(), id);
        }

        // 更新字段
        if (dto.getRoomTypeId() != null) {
            room.setRoomTypeId(dto.getRoomTypeId());
        }
        if (dto.getFloorId() != null) {
            room.setFloorId(dto.getFloorId());
        }
        if (StringUtils.hasText(dto.getRoomNo())) {
            room.setRoomNo(dto.getRoomNo());
        }
        if (dto.getDescription() != null) {
            room.setDescription(dto.getDescription());
        }

        // 保存到数据库
        mapper.updateById(room);

        log.info("更新房间成功：id={}, roomNo={}", id, room.getRoomNo());

        return convertToVO(room);
    }

    /**
     * 删除房间（逻辑删除）
     *
     * @param id 房间ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 查询房间
        Room room = mapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // TODO: 检查房间是否有未完成的预订或入住

        // 逻辑删除
        mapper.deleteById(id);

        log.info("删除房间成功：id={}", id);
    }

    /**
     * 更新房间状态
     *
     * @param id 房间ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        // 查询房间
        Room room = mapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 更新状态
        room.setStatus(status);
        mapper.updateById(room);

        log.info("更新房间状态成功：id={}, status={}", id, status);
    }

    /**
     * 查询房间统计
     *
     * @param hotelId 酒店ID
     * @return 统计信息
     */
    public RoomSummaryVO getSummary(Long hotelId) {
        // 查询酒店所有房间（MyBatis-Plus自动处理逻辑删除）
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getHotelId, hotelId);
        List<Room> rooms = mapper.selectList(wrapper);

        // 按状态分组统计
        Map<String, Long> counts = rooms.stream()
                .collect(Collectors.groupingBy(Room::getStatus, Collectors.counting()));

        return RoomSummaryVO.builder()
                .totalRooms((long) rooms.size())
                .availableRooms(counts.getOrDefault(RoomConstants.STATUS_AVAILABLE, 0L))
                .occupiedRooms(counts.getOrDefault(RoomConstants.STATUS_OCCUPIED, 0L))
                .dirtyRooms(counts.getOrDefault(RoomConstants.STATUS_DIRTY, 0L))
                .maintenanceRooms(counts.getOrDefault(RoomConstants.STATUS_MAINTENANCE, 0L))
                .oooRooms(counts.getOrDefault(RoomConstants.STATUS_OOO, 0L))
                .reservedRooms(counts.getOrDefault(RoomConstants.STATUS_RESERVED, 0L))
                .statusCounts(counts)
                .build();
    }

    /**
     * 获取房态看板数据
     *
     * @param hotelId 酒店ID
     * @param floorId 楼层ID（可选）
     * @param status 状态筛选（可选）
     * @return 房态看板数据
     */
    public RoomDashboardVO getDashboard(Long hotelId, Long floorId, String status) {
        // 1. 查询房间列表
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getHotelId, hotelId)
                   .eq(floorId != null, Room::getFloorId, floorId)
                   .eq(StringUtils.hasText(status), Room::getStatus, status)
                   .orderByAsc(Room::getFloorId)
                   .orderByAsc(Room::getRoomNo);
        List<Room> rooms = mapper.selectList(roomWrapper);

        // 2. 查询房型信息
        Map<Long, RoomType> roomTypeMap = new HashMap<>();
        LambdaQueryWrapper<RoomType> roomTypeWrapper = new LambdaQueryWrapper<>();
        roomTypeWrapper.eq(RoomType::getHotelId, hotelId);
        List<RoomType> roomTypes = roomTypeMapper.selectList(roomTypeWrapper);
        roomTypes.forEach(rt -> roomTypeMap.put(rt.getId(), rt));

        // 3. 查询楼层信息
        Map<Long, HotelFloor> floorMap = new HashMap<>();
        LambdaQueryWrapper<HotelFloor> floorWrapper = new LambdaQueryWrapper<>();
        floorWrapper.eq(HotelFloor::getHotelId, hotelId);
        List<HotelFloor> floors = floorMapper.selectList(floorWrapper);
        floors.forEach(f -> floorMap.put(f.getId(), f));

        // 4. 查询今日房价
        Map<Long, BigDecimal> priceMap = new HashMap<>();
        LambdaQueryWrapper<RoomPrice> priceWrapper = new LambdaQueryWrapper<>();
        priceWrapper.eq(RoomPrice::getHotelId, hotelId)
                    .eq(RoomPrice::getPriceDate, LocalDate.now());
        List<RoomPrice> prices = roomPriceMapper.selectList(priceWrapper);
        prices.forEach(p -> priceMap.put(p.getRoomTypeId(), p.getPrice()));

        // 5. 查询当前在住的预订
        Map<Long, Reservation> occupiedMap = new HashMap<>();
        LambdaQueryWrapper<Reservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.eq(Reservation::getHotelId, hotelId)
                          .eq(Reservation::getStatus, RoomConstants.RESERVATION_CHECKED_IN)
                          .isNotNull(Reservation::getRoomId);
        List<Reservation> occupiedReservations = reservationMapper.selectList(reservationWrapper);
        occupiedReservations.forEach(r -> occupiedMap.put(r.getRoomId(), r));

        // 5.1 查询在住的散客入住单（补充散客入住的情况）
        Map<Long, Stay> stayMap = new HashMap<>();
        LambdaQueryWrapper<Stay> stayWrapper = new LambdaQueryWrapper<>();
        stayWrapper.eq(Stay::getHotelId, hotelId)
                  .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
                  .isNotNull(Stay::getRoomId);
        List<Stay> occupiedStays = stayMapper.selectList(stayWrapper);
        occupiedStays.forEach(s -> {
            if (!occupiedMap.containsKey(s.getRoomId())) {
                stayMap.put(s.getRoomId(), s);
            }
        });

        // 6. 查询未来预订（7天内）
        Map<Long, List<Reservation>> futureReservationMap = new HashMap<>();
        LambdaQueryWrapper<Reservation> futureWrapper = new LambdaQueryWrapper<>();
        futureWrapper.eq(Reservation::getHotelId, hotelId)
                     .in(Reservation::getStatus,
                         RoomConstants.RESERVATION_PENDING,
                         RoomConstants.RESERVATION_CONFIRMED)
                     .isNotNull(Reservation::getRoomId)
                     .ge(Reservation::getCheckInDate, LocalDate.now())
                     .le(Reservation::getCheckInDate, LocalDate.now().plusDays(7))
                     .orderByAsc(Reservation::getCheckInDate);
        List<Reservation> futureReservations = reservationMapper.selectList(futureWrapper);
        futureReservations.forEach(r -> {
            futureReservationMap.computeIfAbsent(r.getRoomId(), k -> new ArrayList<>()).add(r);
        });

        // 7. 按状态分组
        Map<String, List<Room>> statusGroupMap = rooms.stream()
                .collect(Collectors.groupingBy(Room::getStatus));

        // 8. 构建状态分组数据
        List<RoomDashboardVO.StatusGroupVO> statusGroups = new ArrayList<>();
        String[] allStatuses = RoomConstants.getAllRoomStatuses();

        for (String statusCode : allStatuses) {
            List<Room> statusRooms = statusGroupMap.getOrDefault(statusCode, new ArrayList<>());
            if (statusRooms.isEmpty() && StringUtils.hasText(status)) {
                continue; // 如果指定了状态筛选，跳过空状态
            }

            List<RoomDetailVO> roomDetails = statusRooms.stream()
                    .map(room -> convertToRoomDetailVO(room, roomTypeMap, floorMap, priceMap, occupiedMap, futureReservationMap))
                    .collect(Collectors.toList());

            statusGroups.add(RoomDashboardVO.StatusGroupVO.builder()
                    .statusCode(statusCode)
                    .statusName(RoomConstants.getStatusName(statusCode))
                    .roomCount((long) statusRooms.size())
                    .rooms(roomDetails)
                    .build());
        }

        // 9. 计算各状态统计
        Map<String, Long> statusCounts = rooms.stream()
                .collect(Collectors.groupingBy(Room::getStatus, Collectors.counting()));

        return RoomDashboardVO.builder()
                .totalRooms((long) rooms.size())
                .statusCounts(statusCounts)
                .statusGroups(statusGroups)
                .build();
    }

    /**
     * 获取房间详情（含房价和预订）
     *
     * @param roomId 房间ID
     * @return 房间详情
     */
    public RoomDetailVO getRoomDetail(Long roomId) {
        // 查询房间
        Room room = mapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 查询房型信息
        Map<Long, RoomType> roomTypeMap = new HashMap<>();
        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        if (roomType != null) {
            roomTypeMap.put(room.getRoomTypeId(), roomType);
        }

        // 查询楼层信息
        Map<Long, HotelFloor> floorMap = new HashMap<>();
        HotelFloor floor = floorMapper.selectById(room.getFloorId());
        if (floor != null) {
            floorMap.put(room.getFloorId(), floor);
        }

        // 查询今日房价
        Map<Long, BigDecimal> priceMap = new HashMap<>();
        LambdaQueryWrapper<RoomPrice> priceWrapper = new LambdaQueryWrapper<>();
        priceWrapper.eq(RoomPrice::getHotelId, room.getHotelId())
                    .eq(RoomPrice::getRoomTypeId, room.getRoomTypeId())
                    .eq(RoomPrice::getPriceDate, LocalDate.now());
        RoomPrice roomPrice = roomPriceMapper.selectOne(priceWrapper);
        if (roomPrice != null) {
            priceMap.put(room.getRoomTypeId(), roomPrice.getPrice());
        }

        // 查询当前在住的预订
        Map<Long, Reservation> occupiedMap = new HashMap<>();
        LambdaQueryWrapper<Reservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.eq(Reservation::getHotelId, room.getHotelId())
                          .eq(Reservation::getStatus, RoomConstants.RESERVATION_CHECKED_IN)
                          .eq(Reservation::getRoomId, roomId);
        Reservation occupiedReservation = reservationMapper.selectOne(reservationWrapper);
        if (occupiedReservation != null) {
            occupiedMap.put(roomId, occupiedReservation);
        }

        // 查询未来预订
        Map<Long, List<Reservation>> futureReservationMap = new HashMap<>();
        LambdaQueryWrapper<Reservation> futureWrapper = new LambdaQueryWrapper<>();
        futureWrapper.eq(Reservation::getHotelId, room.getHotelId())
                     .in(Reservation::getStatus,
                         RoomConstants.RESERVATION_PENDING,
                         RoomConstants.RESERVATION_CONFIRMED)
                     .eq(Reservation::getRoomId, roomId)
                     .ge(Reservation::getCheckInDate, LocalDate.now())
                     .le(Reservation::getCheckInDate, LocalDate.now().plusDays(7))
                     .orderByAsc(Reservation::getCheckInDate);
        List<Reservation> futureReservations = reservationMapper.selectList(futureWrapper);
        if (!futureReservations.isEmpty()) {
            futureReservationMap.put(roomId, futureReservations);
        }

        return convertToRoomDetailVO(room, roomTypeMap, floorMap, priceMap, occupiedMap, futureReservationMap);
    }

    /**
     * 转换为房间详情VO
     */
    /**
     * 转换为房间详情VO（兼容旧调用）
     */
    /**
     * 转换为房间详情VO（兼容旧调用）
     */
    private RoomDetailVO convertToRoomDetailVO(
            Room room,
            Map<Long, RoomType> roomTypeMap,
            Map<Long, HotelFloor> floorMap,
            Map<Long, BigDecimal> priceMap,
            Map<Long, Reservation> occupiedMap,
            Map<Long, List<Reservation>> futureReservationMap) {
          RoomDetailVO vo = RoomDetailVO.builder()
                  .roomId(room.getId())
                  .roomNo(room.getRoomNo())
                  .floorId(room.getFloorId())
                  .roomTypeId(room.getRoomTypeId())
                  .status(room.getStatus())
                  .statusName(RoomConstants.getStatusName(room.getStatus()))
                  .build();

          // 设置楼层信息
          HotelFloor floor = floorMap.get(room.getFloorId());
          if (floor != null) {
              vo.setFloorName(floor.getName());
          }

          // 设置房型信息
          RoomType roomType = roomTypeMap.get(room.getRoomTypeId());
          if (roomType != null) {
              vo.setRoomTypeName(roomType.getName());
              vo.setRoomTypeCode(roomType.getCode());
              vo.setBasePrice(roomType.getBasePrice());
          }

          // 设置今日房价
          BigDecimal todayPrice = priceMap.get(room.getRoomTypeId());
          vo.setTodayPrice(todayPrice != null ? todayPrice : vo.getBasePrice());

          // 设置当前入住信息（预订入住 或 散客入住）
          Reservation occupied = occupiedMap.get(room.getId());
          Stay stay = null;
          if (occupied != null) {
              // 预订入住
              long stayDays = ChronoUnit.DAYS.between(occupied.getCheckInDate(), LocalDate.now()) + 1;
              vo.setCurrentStay(RoomDetailVO.CurrentStayInfo.builder()
                      .reservationId(occupied.getId())
                      .guestName(occupied.getGuestName())
                      .checkInTime(occupied.getCheckInDate().atStartOfDay())
                      .checkOutTime(occupied.getCheckOutDate().atStartOfDay())
                      .stayDays((int) stayDays)
                      .build());
              // 查询入住单获取付款状态
              LambdaQueryWrapper<Stay> stayQuery = new LambdaQueryWrapper<>();
              stayQuery.eq(Stay::getReservationId, occupied.getId());
              stay = stayMapper.selectOne(stayQuery);
          } else {
              // 散客入住 - 直接查询入住单
              LambdaQueryWrapper<Stay> stayQuery2 = new LambdaQueryWrapper<>();
              stayQuery2.eq(Stay::getRoomId, room.getId())
                       .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN);
              stay = stayMapper.selectOne(stayQuery2);
              if (stay != null) {
                  Guest guest = guestMapper.selectById(stay.getGuestId());
                  long stayDays = ChronoUnit.DAYS.between(stay.getCheckInTime().toLocalDate(), LocalDate.now()) + 1;
                  vo.setCurrentStay(RoomDetailVO.CurrentStayInfo.builder()
                          .guestName(guest != null ? guest.getName() : "")
                          .checkInTime(stay.getCheckInTime())
                          .checkOutTime(stay.getCheckOutTime())
                          .stayDays((int) stayDays)
                          .build());
              }
          }
          // 设置付款状态
          if (stay != null && vo.getCurrentStay() != null) {
              vo.getCurrentStay().setPaidAmount(stay.getPaidAmount());
              vo.getCurrentStay().setTotalAmount(stay.getTotalAmount());
              if (stay.getPaidAmount() == null || stay.getPaidAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
                  vo.getCurrentStay().setPaymentStatus("UNPAID");
              } else if (stay.getPaidAmount().compareTo(stay.getTotalAmount()) >= 0) {
                  vo.getCurrentStay().setPaymentStatus("PAID");
              } else {
                  vo.getCurrentStay().setPaymentStatus("PARTIAL");
              }
          }

          // 设置未来预订
          List<Reservation> futures = futureReservationMap.get(room.getId());
          if (futures != null && !futures.isEmpty()) {
              List<RoomDetailVO.FutureReservation> futureList = futures.stream()
                      .map(r -> RoomDetailVO.FutureReservation.builder()
                              .reservationId(r.getId())
                              .guestName(r.getGuestName())
                              .checkInDate(r.getCheckInDate())
                              .checkOutDate(r.getCheckOutDate())
                              .status(r.getStatus())
                              .build())
                      .collect(Collectors.toList());
              vo.setFutureReservations(futureList);
          }

          return vo;
    }
    private void checkRoomNoDuplicate(Long hotelId, String roomNo, Long excludeId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getHotelId, hotelId)
               .eq(Room::getRoomNo, roomNo)
               .ne(excludeId != null, Room::getId, excludeId);

        Long count = mapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "房间号已存在");
        }
    }

    /**
     * 转换为VO
     *
     * @param room 房间实体
     * @return 房间VO
     */
    private RoomVO convertToVO(Room room) {
        RoomVO vo = new RoomVO();
        BeanUtils.copyProperties(room, vo);

        // 查询房型信息
        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        if (roomType != null) {
            vo.setRoomTypeName(roomType.getName());
            vo.setRoomTypeCode(roomType.getCode());
            vo.setBasePrice(roomType.getBasePrice());
        }

        // 查询楼层信息
        HotelFloor floor = floorMapper.selectById(room.getFloorId());
        if (floor != null) {
            vo.setFloorName(floor.getName());
        }

        // TODO: 查询酒店名称
        vo.setHotelName("默认酒店");

        return vo;
    }

    /**
     * 检查房间在指定日期范围内是否可用
     * 检查是否有冲突的预订
     *
     * @param roomId 房间ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @return 是否可用
     */
    public boolean checkAvailability(Long roomId, java.time.LocalDate checkInDate, java.time.LocalDate checkOutDate) {
        // 检查房间当前状态
        Room room = mapper.selectById(roomId);
        if (room == null) {
            return false;
        }
        
        // 如果房间状态不是空闲，不可用
        if (!"AVAILABLE".equals(room.getStatus())) {
            return false;
        }

        // 检查是否有冲突的预订（状态为 PENDING 或 CONFIRMED）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hotel.pms.dao.entity.Reservation> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.hotel.pms.dao.entity.Reservation::getRoomId, roomId)
                .in(com.hotel.pms.dao.entity.Reservation::getStatus, "PENDING", "CONFIRMED")
                .lt(com.hotel.pms.dao.entity.Reservation::getCheckInDate, checkOutDate)
                .gt(com.hotel.pms.dao.entity.Reservation::getCheckOutDate, checkInDate);
        
        Long count = reservationMapper.selectCount(wrapper);
        return count == 0;
    }

    /**
     * 查询房间预订日历数据
     * 返回指定日期范围内每个房间的每日预订状态
     *
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param days 天数
     * @param floorId 楼层ID（可选筛选）
     * @param roomTypeId 房型ID（可选筛选）
     * @return 房间日历数据
     */
    public RoomCalendarVO getRoomCalendar(Long hotelId, LocalDate startDate, int days, Long floorId, Long roomTypeId) {
        // 日期格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate = startDate.plusDays(days);
        
        // 生成日期列表
        java.util.List<String> dateList = new java.util.ArrayList<>();
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            dateList.add(date.format(formatter));
        }
        
        // 查询房间列表
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getHotelId, hotelId)
                   .ne(Room::getStatus, "OOO")  // 排除停用房
                   .eq(floorId != null, Room::getFloorId, floorId)
                   .eq(roomTypeId != null, Room::getRoomTypeId, roomTypeId)
                   .orderByAsc(Room::getRoomNo);
        List<Room> rooms = mapper.selectList(roomWrapper);
        
        // 查询日期范围内的所有预订
        LambdaQueryWrapper<Reservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.eq(Reservation::getHotelId, hotelId)
                         .in(Reservation::getStatus, "PENDING", "CONFIRMED")
                         .lt(Reservation::getCheckInDate, endDate)
                         .gt(Reservation::getCheckOutDate, startDate);
        List<Reservation> reservations = reservationMapper.selectList(reservationWrapper);
        
        // 构建房间ID到预订的映射
        Map<Long, List<Reservation>> roomReservationMap = new java.util.HashMap<>();
        for (Reservation res : reservations) {
            if (res.getRoomId() != null) {
                roomReservationMap.computeIfAbsent(res.getRoomId(), k -> new java.util.ArrayList<>()).add(res);
            }
        }
        
        // 构建房间日历数据
        java.util.List<RoomCalendarVO.RoomCalendarItem> roomItems = new java.util.ArrayList<>();
        for (Room room : rooms) {
            // 查询房型信息
            RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
            
            // 查询楼层信息
            HotelFloor floor = floorMapper.selectById(room.getFloorId());
            
            // 构建每日状态映射
            java.util.Map<String, RoomCalendarVO.CalendarCell> calendarMap = new LinkedHashMap<>();
            // 判断房间是否有特殊状态（维修、停用）
            String defaultStatus = "AVAILABLE";
            if ("MAINTENANCE".equals(room.getStatus()) || "OOO".equals(room.getStatus())) {
                defaultStatus = room.getStatus();
            }
            for (String dateStr : dateList) {
                LocalDate cellDate = LocalDate.parse(dateStr, formatter);
                RoomCalendarVO.CalendarCell cell = RoomCalendarVO.CalendarCell.builder()
                        .date(dateStr)
                        .status(defaultStatus)  // 维修/停用房间全部标记，其他房间默认空闲
                        .build();
                calendarMap.put(dateStr, cell);
            }
            
            // 获取该房间的预订列表
            List<Reservation> roomReservations = roomReservationMap.getOrDefault(room.getId(), new java.util.ArrayList<>());
            
            // 标记预订占用的日期
            for (Reservation res : roomReservations) {
                LocalDate resCheckIn = res.getCheckInDate();
                LocalDate resCheckOut = res.getCheckOutDate();
                
                // 遍历每个日期，检查是否在预订范围内
                for (String dateStr : dateList) {
                    LocalDate cellDate = LocalDate.parse(dateStr, formatter);
                    if (!cellDate.isBefore(resCheckIn) && cellDate.isBefore(resCheckOut)) {
                        // 该日期在预订范围内，更新单元格状态
                        RoomCalendarVO.CalendarCell cell = calendarMap.get(dateStr);
                        if (cell != null) {
                            cell.setStatus("RESERVED");
                            cell.setReservationId(res.getId());
                            cell.setGuestName(res.getGuestName());
                            cell.setReservationStatus(res.getStatus());
                        }
                    }
                }
            }
            
            // 构建简化预订列表
            java.util.List<RoomCalendarVO.CalendarReservation> calReservations = new java.util.ArrayList<>();
            for (Reservation res : roomReservations) {
                calReservations.add(RoomCalendarVO.CalendarReservation.builder()
                        .reservationId(res.getId())
                        .reservationNo(res.getReservationNo())
                        .guestName(res.getGuestName())
                        .checkInDate(res.getCheckInDate() != null ? res.getCheckInDate().format(formatter) : null)
                        .checkOutDate(res.getCheckOutDate() != null ? res.getCheckOutDate().format(formatter) : null)
                        .status(res.getStatus())
                        .totalAmount(res.getTotalAmount())
                        .build());
            }
            
            // 构建房间日历项
            RoomCalendarVO.RoomCalendarItem item = RoomCalendarVO.RoomCalendarItem.builder()
                    .roomId(room.getId())
                    .roomNo(room.getRoomNo())
                    .roomTypeName(roomType != null ? roomType.getName() : null)
                    .roomTypeId(room.getRoomTypeId())
                    .floorName(floor != null ? floor.getName() : null)
                    .floorId(room.getFloorId())
                    .basePrice(roomType != null ? roomType.getBasePrice() : null)
                    .currentStatus(room.getStatus())
                    .calendar(calendarMap)
                    .reservations(calReservations)
                    .build();
            
            roomItems.add(item);
        }
        
        return RoomCalendarVO.builder()
                .rooms(roomItems)
                .dates(dateList)
                .build();
    }

    /**
     * 检查房间日期冲突
     * 验证指定房间在入住期间是否已有其他预订
     *
     * @param roomId 房间ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @param excludeReservationId 排除的预订ID（编辑时使用，可选）
     * @return 是否有冲突
     */
    public boolean checkDateConflict(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, Long excludeReservationId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getRoomId, roomId)
               .in(Reservation::getStatus, "PENDING", "CONFIRMED")
               .lt(Reservation::getCheckInDate, checkOutDate)
               .gt(Reservation::getCheckOutDate, checkInDate)
               .ne(excludeReservationId != null, Reservation::getId, excludeReservationId);
        
        Long count = reservationMapper.selectCount(wrapper);
        return count > 0;
    }

    /**
     * 获取房间日期冲突详情
     * 返回与指定日期范围冲突的预订列表
     *
     * @param roomId 房间ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @param excludeReservationId 排除的预订ID（编辑时使用，可选）
     * @return 冲突的预订列表
     */
    public java.util.List<Reservation> getConflictReservations(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, Long excludeReservationId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getRoomId, roomId)
               .in(Reservation::getStatus, "PENDING", "CONFIRMED")
               .lt(Reservation::getCheckInDate, checkOutDate)
               .gt(Reservation::getCheckOutDate, checkInDate)
               .ne(excludeReservationId != null, Reservation::getId, excludeReservationId);
        
        return reservationMapper.selectList(wrapper);
    }
}









