package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作台控制器
 * <p>
 * 处理工作台相关的请求，包括统计数据和今日概览列表
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "工作台", description = "工作台统计数据和今日概览接口")
public class DashboardController {

    /** 工作台服务 */
    private final DashboardService dashboardService;

    /**
     * 获取工作台统计数据
     * <p>
     * 查询酒店的房间状态统计、今日预订和入住情况
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取工作台统计数据", description = "查询房间统计、今日抵店、今日离店等数据")
    public Result<DashboardStatsVO> getStats(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        DashboardStatsVO result = dashboardService.getStats(hotelId);
        return Result.success(result);
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
    @GetMapping("/today-departures")
    @Operation(summary = "获取今日离店列表", description = "查询今日预计离店的在住客人")
    public Result<List<StayVO>> getTodayDepartures(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        List<StayVO> result = dashboardService.getTodayDepartures(hotelId);
        return Result.success(result);
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
    @GetMapping("/pending-reservations")
    @Operation(summary = "获取待处理预订列表", description = "查询待确认的预订列表")
    public Result<List<ReservationVO>> getPendingReservations(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        List<ReservationVO> result = dashboardService.getPendingReservations(hotelId);
        return Result.success(result);
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
    @GetMapping("/today-checkins")
    @Operation(summary = "获取今日入住列表", description = "查询今日入住的客人列表")
    public Result<List<StayVO>> getTodayCheckIns(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        List<StayVO> result = dashboardService.getTodayCheckIns(hotelId);
        return Result.success(result);
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
    @GetMapping("/dirty-rooms")
    @Operation(summary = "获取脏房列表", description = "查询状态为脏房的房间")
    public Result<List<RoomVO>> getDirtyRooms(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        List<RoomVO> result = dashboardService.getDirtyRooms(hotelId);
        return Result.success(result);
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
    @GetMapping("/maintenance-rooms")
    @Operation(summary = "获取维修房间列表", description = "查询状态为维修的房间")
    public Result<List<RoomVO>> getMaintenanceRooms(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        List<RoomVO> result = dashboardService.getMaintenanceRooms(hotelId);
        return Result.success(result);
    }


    /**
     * 获取今日营收
     */
    @GetMapping("/today-revenue")
    @Operation(summary = "获取今日营收", description = "查询今日的总营收金额")
    public Result<java.math.BigDecimal> getTodayRevenue(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        java.math.BigDecimal result = dashboardService.getTodayRevenue(hotelId);
        return Result.success(result);
    }

    /**
     * 获取营收趋势
     */
    @GetMapping("/revenue-trend")
    @Operation(summary = "获取营收趋势", description = "查询近N天的每日营收")
    public Result<java.util.List<java.util.Map<String, Object>>> getRevenueTrend(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days) {
        java.util.List<java.util.Map<String, Object>> result = dashboardService.getRevenueTrend(hotelId, days);
        return Result.success(result);
    }
}
