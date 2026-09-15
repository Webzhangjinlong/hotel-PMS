package com.hotel.pms.service.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.constant.RoomConstants;
import com.hotel.pms.common.constant.ReservationConstants;
import com.hotel.pms.common.constant.StayConstants;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.dao.entity.Reservation;
import com.hotel.pms.dao.entity.Room;
import com.hotel.pms.dao.entity.RoomType;
import com.hotel.pms.dao.entity.Stay;
import com.hotel.pms.dao.mapper.ReservationMapper;
import com.hotel.pms.dao.mapper.RoomMapper;
import com.hotel.pms.dao.mapper.RoomTypeMapper;
import com.hotel.pms.dao.mapper.StayMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作台统计服务
 * <p>
 * 提供工作台所需的统计数据和今日概览列表查询功能
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class DashboardService {

    /** 房间Mapper */
    @Autowired
    private RoomMapper roomMapper;

    /** 房型Mapper */
    @Autowired
    private RoomTypeMapper roomTypeMapper;

    /** 预订Mapper */
    @Autowired
    private ReservationMapper reservationMapper;

    /** 入住Mapper */
    @Autowired
    private StayMapper stayMapper;

    /**
     * 获取工作台统计数据
     * <p>
     * 查询酒店的房间状态统计、今日预订和入住情况
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 统计数据
     */
    public DashboardStatsVO getStats(Long hotelId) {
        DashboardStatsVO stats = new DashboardStatsVO();

        // 1. 统计房间状态
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getHotelId, hotelId);
        Long totalRooms = roomMapper.selectCount(roomWrapper);
        stats.setTotalRooms(totalRooms.intValue());

        // 空闲房间数
        LambdaQueryWrapper<Room> availableWrapper = new LambdaQueryWrapper<>();
        availableWrapper.eq(Room::getHotelId, hotelId)
                       .eq(Room::getStatus, RoomConstants.STATUS_AVAILABLE);
        stats.setAvailableRooms(roomMapper.selectCount(availableWrapper).intValue());

        // 在住房间数
        LambdaQueryWrapper<Room> occupiedWrapper = new LambdaQueryWrapper<>();
        occupiedWrapper.eq(Room::getHotelId, hotelId)
                      .eq(Room::getStatus, RoomConstants.STATUS_OCCUPIED);
        stats.setOccupiedRooms(roomMapper.selectCount(occupiedWrapper).intValue());

        // 脏房数量
        LambdaQueryWrapper<Room> dirtyWrapper = new LambdaQueryWrapper<>();
        dirtyWrapper.eq(Room::getHotelId, hotelId)
                   .eq(Room::getStatus, RoomConstants.STATUS_DIRTY);
        stats.setDirtyRooms(roomMapper.selectCount(dirtyWrapper).intValue());

        // 维修房间数
        LambdaQueryWrapper<Room> maintenanceWrapper = new LambdaQueryWrapper<>();
        maintenanceWrapper.eq(Room::getHotelId, hotelId)
                         .eq(Room::getStatus, RoomConstants.STATUS_MAINTENANCE);
        stats.setMaintenanceRooms(roomMapper.selectCount(maintenanceWrapper).intValue());

        // 预留房间数
        LambdaQueryWrapper<Room> reservedWrapper = new LambdaQueryWrapper<>();
        reservedWrapper.eq(Room::getHotelId, hotelId)
                      .eq(Room::getStatus, RoomConstants.STATUS_RESERVED);
        stats.setReservedRooms(roomMapper.selectCount(reservedWrapper).intValue());

        // 2. 统计今日预订
        LocalDate today = LocalDate.now();
        stats.setTodayArrivals(getTodayArrivalsCount(hotelId, today));
        stats.setTodayDepartures(getTodayDeparturesCount(hotelId, today));
        stats.setPendingReservations(getPendingReservationsCount(hotelId));
        stats.setTodayCheckIns(getTodayCheckInsCount(hotelId, today));

        log.info("获取工作台统计成功：hotelId={}, totalRooms={}, available={}, occupied={}", 
                hotelId, stats.getTotalRooms(), stats.getAvailableRooms(), stats.getOccupiedRooms());

        return stats;
    }

    /**
     * 获取今日离店列表
     * <p>
     * 查询今日预计离店的在住客人列表
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 今日离店列表
     */
    public List<StayVO> getTodayDepartures(Long hotelId) {
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getHotelId, hotelId)
               .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
               .ge(Stay::getCheckOutTime, LocalDate.now().atStartOfDay())
               .lt(Stay::getCheckOutTime, LocalDate.now().plusDays(1).atStartOfDay());
        
        List<Stay> stays = stayMapper.selectList(wrapper);
        return stays.stream()
                .map(this::convertStayToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取待处理预订列表
     * <p>
     * 查询状态为待确认的预订列表
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 待处理预订列表
     */
    public List<ReservationVO> getPendingReservations(Long hotelId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getHotelId, hotelId)
               .eq(Reservation::getStatus, ReservationConstants.STATUS_PENDING)
               .orderByDesc(Reservation::getCreatedAt);
        
        List<Reservation> reservations = reservationMapper.selectList(wrapper);
        return reservations.stream()
                .map(this::convertReservationToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取今日入住列表
     * <p>
     * 查询今日办理入住的客人列表
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 今日入住列表
     */
    public List<StayVO> getTodayCheckIns(Long hotelId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getHotelId, hotelId)
               .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
               .ge(Stay::getCheckInTime, today.atStartOfDay())
               .le(Stay::getCheckInTime, today.atTime(LocalTime.MAX));
        
        List<Stay> stays = stayMapper.selectList(wrapper);
        return stays.stream()
                .map(this::convertStayToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取脏房列表
     * <p>
     * 查询状态为脏房的房间列表
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 脏房列表
     */
    public List<RoomVO> getDirtyRooms(Long hotelId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getHotelId, hotelId)
               .eq(Room::getStatus, RoomConstants.STATUS_DIRTY)
               .orderByAsc(Room::getRoomNo);
        
        List<Room> rooms = roomMapper.selectList(wrapper);
        return rooms.stream()
                .map(this::convertRoomToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取维修房间列表
     * <p>
     * 查询状态为维修的房间列表
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 维修房间列表
     */
    public List<RoomVO> getMaintenanceRooms(Long hotelId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getHotelId, hotelId)
               .eq(Room::getStatus, RoomConstants.STATUS_MAINTENANCE)
               .orderByAsc(Room::getRoomNo);
        
        List<Room> rooms = roomMapper.selectList(wrapper);
        return rooms.stream()
                .map(this::convertRoomToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取今日抵店数
     */
    private int getTodayArrivalsCount(Long hotelId, LocalDate date) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getHotelId, hotelId)
               .eq(Reservation::getStatus, ReservationConstants.STATUS_CONFIRMED)
               .eq(Reservation::getCheckInDate, date);
        return reservationMapper.selectCount(wrapper).intValue();
    }

    /**
     * 获取今日离店数
     */
    private int getTodayDeparturesCount(Long hotelId, LocalDate date) {
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getHotelId, hotelId)
               .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
               .ge(Stay::getCheckOutTime, date.atStartOfDay())
               .lt(Stay::getCheckOutTime, date.plusDays(1).atStartOfDay());
        return stayMapper.selectCount(wrapper).intValue();
    }

    /**
     * 获取待确认预订数
     */
    private int getPendingReservationsCount(Long hotelId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getHotelId, hotelId)
               .eq(Reservation::getStatus, ReservationConstants.STATUS_PENDING);
        return reservationMapper.selectCount(wrapper).intValue();
    }

    /**
     * 获取今日入住数
     */
    private int getTodayCheckInsCount(Long hotelId, LocalDate date) {
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getHotelId, hotelId)
               .eq(Stay::getStatus, StayConstants.STATUS_CHECKED_IN)
               .ge(Stay::getCheckInTime, date.atStartOfDay())
               .le(Stay::getCheckInTime, date.atTime(LocalTime.MAX));
        return stayMapper.selectCount(wrapper).intValue();
    }

    /**
     * 入住单转换为VO
     */
    private StayVO convertStayToVO(Stay stay) {
        StayVO vo = new StayVO();
        BeanUtils.copyProperties(stay, vo);
        
        // 查询房间信息
        Room room = roomMapper.selectById(stay.getRoomId());
        if (room != null) {
            vo.setRoomNo(room.getRoomNo());
            // 查询房型
            RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
            if (roomType != null) {
                vo.setRoomTypeName(roomType.getName());
            }
        }
        
        return vo;
    }

    /**
     * 预订转换为VO
     */
    private ReservationVO convertReservationToVO(Reservation reservation) {
        ReservationVO vo = new ReservationVO();
        BeanUtils.copyProperties(reservation, vo);
        
        // 查询房型名称
        RoomType roomType = roomTypeMapper.selectById(reservation.getRoomTypeId());
        if (roomType != null) {
            vo.setRoomTypeName(roomType.getName());
        }
        
        return vo;
    }

    /**
     * 房间转换为VO
     */
    private RoomVO convertRoomToVO(Room room) {
        RoomVO vo = new RoomVO();
        BeanUtils.copyProperties(room, vo);
        
        // 查询房型名称
        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        if (roomType != null) {
            vo.setRoomTypeName(roomType.getName());
        }
        
        return vo;
    }


    /**
     * 获取今日营收
     */
    public java.math.BigDecimal getTodayRevenue(Long hotelId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getHotelId, hotelId)
               .ge(Stay::getCheckInTime, today.atStartOfDay())
               .le(Stay::getCheckInTime, today.atTime(java.time.LocalTime.MAX));
        List<Stay> stays = stayMapper.selectList(wrapper);
        return stays.stream()
                .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * 获取近N天营收趋势
     */
    public java.util.List<java.util.Map<String, Object>> getRevenueTrend(Long hotelId, int days) {
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Stay::getHotelId, hotelId)
                   .ge(Stay::getCheckInTime, date.atStartOfDay())
                   .le(Stay::getCheckInTime, date.atTime(java.time.LocalTime.MAX));
            List<Stay> stays = stayMapper.selectList(wrapper);
            java.math.BigDecimal total = stays.stream()
                    .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : java.math.BigDecimal.ZERO)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", date.toString());
            item.put("revenue", total);
            result.add(item);
        }
        return result;
    }
}
