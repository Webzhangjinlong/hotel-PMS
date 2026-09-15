package com.hotel.pms.service.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.dao.entity.Reservation;
import com.hotel.pms.dao.entity.Room;
import com.hotel.pms.dao.entity.Stay;
import com.hotel.pms.dao.mapper.ReservationMapper;
import com.hotel.pms.dao.mapper.RoomMapper;
import com.hotel.pms.dao.mapper.StayMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表服务类
 * <p>
 * 负责酒店经营数据分析，包括基础指标、渠道收入占比、经营趋势等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final StayMapper stayMapper;
    private final RoomMapper roomMapper;
    private final ReservationMapper reservationMapper;
    
    /**
     * 获取基础经营指标
     * <p>
     * 计算入住率、ADR、RevPAR等核心指标
     * </p>
     * 
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 基础指标数据
     */
    public MetricsVO getMetrics(Long hotelId, LocalDate startDate, LocalDate endDate) {
        MetricsVO vo = new MetricsVO();
        
        // 1. 查询时间范围内的所有入住单
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<Stay> stayWrapper = new LambdaQueryWrapper<>();
        stayWrapper.eq(Stay::getHotelId, hotelId)
                .lt(Stay::getCheckInTime, end)
                .and(w -> w.gt(Stay::getActualCheckOutTime, start)
                        .or().isNull(Stay::getActualCheckOutTime)
                        .and(w2 -> w2.ge(Stay::getCheckOutTime, start)));
        
        List<Stay> stays = stayMapper.selectList(stayWrapper);
        
        // 2. 计算可售房间总数（总房间数 - 维修房）
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getHotelId, hotelId).ne(Room::getStatus, "MAINTENANCE");
        long totalRooms = roomMapper.selectCount(roomWrapper);
        vo.setTotalAvailableRooms((int) totalRooms);
        
        // 3. 计算入住夜数和总收入
        long nightsSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Stay stay : stays) {
            LocalDateTime checkIn = stay.getCheckInTime();
            LocalDateTime checkOut = stay.getActualCheckOutTime() != null ?
                    stay.getActualCheckOutTime() : endDate.plusDays(1).atStartOfDay();
            
            // 计算重叠天数
            LocalDateTime effStart = checkIn.isAfter(start) ? checkIn : start;
            LocalDateTime effEnd = checkOut.isBefore(end) ? checkOut : end;
            
            if (effStart.isBefore(effEnd)) {
                long days = ChronoUnit.DAYS.between(effStart, effEnd);
                nightsSold += days;
                totalRevenue = totalRevenue.add(stay.getTotalAmount() != null ? stay.getTotalAmount() : BigDecimal.ZERO);
            }
        }
        
        vo.setRoomNightsSold((int) nightsSold);
        vo.setTotalRevenue(totalRevenue);
        
        // 4. 计算核心指标
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (daysBetween == 0) daysBetween = 1;
        
        if (totalRooms > 0) {
            vo.setOccupancyRate(BigDecimal.valueOf(nightsSold)
                    .divide(BigDecimal.valueOf(totalRooms * daysBetween), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
        }
        
        if (nightsSold > 0) {
            vo.setAdr(totalRevenue.divide(BigDecimal.valueOf(nightsSold), 2, RoundingMode.HALF_UP));
        }
        
        // RevPAR = Total Revenue / (Total Rooms * Days)
        long totalAvailableNights = totalRooms * daysBetween;
        if (totalAvailableNights > 0) {
            vo.setRevpar(totalRevenue.divide(BigDecimal.valueOf(totalAvailableNights), 2, RoundingMode.HALF_UP));
        }
        
        return vo;
    }
    
    /**
     * 获取渠道收入占比数据
     * <p>
     * 按预订渠道统计收入占比，用于分析各渠道贡献
     * </p>
     * 
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 渠道收入占比列表
     */
    public List<ChannelRevenueVO> getChannelRevenues(Long hotelId, LocalDate startDate, LocalDate endDate) {
        // 1. 查询时间范围内的所有入住单
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<Stay> stayWrapper = new LambdaQueryWrapper<>();
        stayWrapper.eq(Stay::getHotelId, hotelId)
                .lt(Stay::getCheckInTime, end)
                .and(w -> w.gt(Stay::getActualCheckOutTime, start)
                        .or().isNull(Stay::getActualCheckOutTime)
                        .and(w2 -> w2.ge(Stay::getCheckOutTime, start)));
        
        List<Stay> stays = stayMapper.selectList(stayWrapper);
        
        // 2. 获取预订信息，用于确定渠道
        Map<Long, Reservation> reservationMap = new HashMap<>();
        for (Stay stay : stays) {
            if (stay.getReservationId() != null) {
                Reservation reservation = reservationMapper.selectById(stay.getReservationId());
                if (reservation != null) {
                    reservationMap.put(stay.getReservationId(), reservation);
                }
            }
        }
        
        // 3. 按渠道统计收入
        Map<String, ChannelRevenueVO> channelMap = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Stay stay : stays) {
            // 获取预订渠道
            String channel = "WALK_IN";
            if (stay.getReservationId() != null && reservationMap.containsKey(stay.getReservationId())) {
                channel = reservationMap.get(stay.getReservationId()).getSource();
            }
            if (channel == null || channel.isEmpty()) {
                channel = "WALK_IN";
            }
            
            BigDecimal revenue = stay.getTotalAmount() != null ? stay.getTotalAmount() : BigDecimal.ZERO;
            
            // 累加渠道收入
            ChannelRevenueVO channelVO = channelMap.getOrDefault(channel,
                    ChannelRevenueVO.builder()
                            .channelName(getChannelName(channel))
                            .channelCode(channel)
                            .revenue(BigDecimal.ZERO)
                            .orderCount(0)
                            .build());
            
            channelVO.setRevenue(channelVO.getRevenue().add(revenue));
            channelVO.setOrderCount(channelVO.getOrderCount() + 1);
            channelMap.put(channel, channelVO);
            
            totalRevenue = totalRevenue.add(revenue);
        }
        
        // 4. 计算占比和平均房价
        List<ChannelRevenueVO> result = new ArrayList<>();
        for (ChannelRevenueVO channelVO : channelMap.values()) {
            // 计算占比
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = channelVO.getRevenue()
                        .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                channelVO.setPercentage(percentage);
            }
            
            // 计算平均房价
            if (channelVO.getOrderCount() > 0) {
                BigDecimal avgRate = channelVO.getRevenue()
                        .divide(BigDecimal.valueOf(channelVO.getOrderCount()), 2, RoundingMode.HALF_UP);
                channelVO.setAvgRate(avgRate);
            }
            
            result.add(channelVO);
        }
        
        // 5. 按收入降序排序
        result.sort((a, b) -> b.getRevenue().compareTo(a.getRevenue()));
        
        return result;
    }
    
    /**
     * 获取经营趋势数据
     * <p>
     * 按日期统计经营指标趋势，用于分析经营状况变化
     * </p>
     * 
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据列表
     */
    public List<TrendDataVO> getTrendData(Long hotelId, LocalDate startDate, LocalDate endDate) {
        List<TrendDataVO> trendDataList = new ArrayList<>();
        
        // 1. 获取可售房间数
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getHotelId, hotelId).ne(Room::getStatus, "MAINTENANCE");
        long totalRooms = roomMapper.selectCount(roomWrapper);
        
        // 2. 遍历每一天
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            
            // 3. 查询当天在住的入住单
            LambdaQueryWrapper<Stay> stayWrapper = new LambdaQueryWrapper<>();
            stayWrapper.eq(Stay::getHotelId, hotelId)
                    .lt(Stay::getCheckInTime, dayEnd)
                    .and(w -> w.gt(Stay::getActualCheckOutTime, dayStart)
                            .or().isNull(Stay::getActualCheckOutTime)
                            .and(w2 -> w2.ge(Stay::getCheckOutTime, dayStart)));
            
            List<Stay> dayStays = stayMapper.selectList(stayWrapper);
            
            // 4. 计算当天指标
            BigDecimal dayRevenue = BigDecimal.ZERO;
            int roomNights = 0;
            
            for (Stay stay : dayStays) {
                BigDecimal revenue = stay.getTotalAmount() != null ? stay.getTotalAmount() : BigDecimal.ZERO;
                dayRevenue = dayRevenue.add(revenue);
                roomNights++;
            }
            
            // 5. 计算入住率
            BigDecimal occupancyRate = BigDecimal.ZERO;
            if (totalRooms > 0) {
                occupancyRate = BigDecimal.valueOf(roomNights)
                        .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            
            // 6. 计算ADR
            BigDecimal adr = BigDecimal.ZERO;
            if (roomNights > 0) {
                adr = dayRevenue.divide(BigDecimal.valueOf(roomNights), 2, RoundingMode.HALF_UP);
            }
            
            // 7. 计算RevPAR
            BigDecimal revpar = BigDecimal.ZERO;
            if (totalRooms > 0) {
                revpar = dayRevenue.divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP);
            }
            
            // 8. 构建趋势数据
            TrendDataVO trendData = TrendDataVO.builder()
                    .date(date)
                    .revenue(dayRevenue)
                    .occupancyRate(occupancyRate)
                    .adr(adr)
                    .revpar(revpar)
                    .roomNights(roomNights)
                    .availableRooms((int) totalRooms)
                    .build();
            
            trendDataList.add(trendData);
        }
        
        return trendDataList;
    }
    
    /**
     * 获取完整报表数据
     * <p>
     * 整合基础指标、渠道收入占比、趋势数据
     * </p>
     * 
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 完整报表数据
     */
    public ReportVO getFullReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        // 1. 获取基础指标
        MetricsVO metrics = getMetrics(hotelId, startDate, endDate);
        
        // 2. 获取渠道收入占比
        List<ChannelRevenueVO> channelRevenues = getChannelRevenues(hotelId, startDate, endDate);
        
        // 3. 获取趋势数据
        List<TrendDataVO> trendData = getTrendData(hotelId, startDate, endDate);
        
        // 4. 计算汇总数据
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Integer totalOrders = 0;
        BigDecimal totalOccupancy = BigDecimal.ZERO;
        BigDecimal totalAdr = BigDecimal.ZERO;
        int days = 0;
        
        for (TrendDataVO trend : trendData) {
            totalRevenue = totalRevenue.add(trend.getRevenue());
            totalOrders += trend.getRoomNights();
            totalOccupancy = totalOccupancy.add(trend.getOccupancyRate());
            totalAdr = totalAdr.add(trend.getAdr());
            days++;
        }
        
        // 5. 构建报表数据
        ReportVO report = ReportVO.builder()
                .metrics(metrics)
                .channelRevenues(channelRevenues)
                .trendData(trendData)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .avgOccupancyRate(days > 0 ? totalOccupancy.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .avgAdr(days > 0 ? totalAdr.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build();
        
        return report;
    }
    
    /**
     * 获取渠道名称
     * 
     * @param channelCode 渠道代码
     * @return 渠道名称
     */
    private String getChannelName(String channelCode) {
        switch (channelCode) {
            case "WALK_IN":
                return "直接入住";
            case "PHONE":
                return "电话预订";
            case "OTA":
                return "OTA渠道";
            case "TRAVEL_AGENCY":
                return "旅行社";
            case "CORPORATE":
                return "协议单位";
            case "TEAM":
                return "团队";
            default:
                return channelCode;
        }
    }
}
