package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.price.RoomPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 房价控制器
 * <p>
 * 处理房价相关的请求
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
@Tag(name = "房价管理", description = "房价的增删改查接口")
public class RoomPriceController {
    
    /** 房价服务 */
    private final RoomPriceService roomPriceService;
    
    /**
     * 获取月历价格
     * 
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param year 年份
     * @param month 月份
     * @return 月历价格
     */
    @GetMapping("/calendar")
    @Operation(summary = "获取月历价格", description = "获取指定房型指定月份的月历价格")
    public Result<RoomPriceCalendarVO> getCalendarPrices(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "房型ID") @RequestParam Long roomTypeId,
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        RoomPriceCalendarVO result = roomPriceService.getCalendarPrices(hotelId, roomTypeId, year, month);
        return Result.success(result);
    }
    
    /**
     * 查询房价列表（分页）
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "查询房价列表", description = "根据条件分页查询房价列表")
    public Result<PageResponse<RoomPriceVO>> getPriceList(RoomPriceQueryDTO queryDTO) {
        PageResponse<RoomPriceVO> result = roomPriceService.getPriceList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 查询指定日期房价
     * 
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param date 日期
     * @return 房价信息
     */
    @GetMapping("/query")
    @Operation(summary = "查询指定日期房价", description = "查询指定房型指定日期的房价")
    public Result<RoomPriceVO> getPriceByDate(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "房型ID") @RequestParam Long roomTypeId,
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        RoomPriceVO result = roomPriceService.getPriceByDate(hotelId, roomTypeId, date);
        return Result.success(result);
    }
    
    /**
     * 更新单个日期价格
     * 
     * @param dto 更新参数
     * @return 房价信息
     */
    @OperationLog(module = "价格管理", action = "修改房价", targetType = "房价")

    @PutMapping
    @Operation(summary = "更新单个日期价格", description = "更新指定房型指定日期的价格")
    public Result<RoomPriceVO> updatePrice(@Valid @RequestBody RoomPriceUpdateDTO dto) {
        RoomPriceVO result = roomPriceService.updatePrice(dto);
        return Result.success(result);
    }
    
    /**
     * 批量调价
     * 
     * @param dto 批量调价参数
     * @return 影响行数
     */
    @OperationLog(module = "价格管理", action = "批量调价", targetType = "房价")

    @PostMapping("/batch")
    @Operation(summary = "批量调价", description = "按房型/日期范围批量调整价格")
    public Result<Integer> batchUpdatePrice(@Valid @RequestBody RoomPriceBatchDTO dto) {
        int result = roomPriceService.batchUpdatePrice(dto);
        return Result.success(result);
    }
    
    /**
     * 初始化房价
     * 
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 影响行数
     */
    @OperationLog(module = "价格管理", action = "初始化房价", targetType = "房价")

    @PostMapping("/init")
    @Operation(summary = "初始化房价", description = "根据房型基础价初始化指定日期范围的房价")
    public Result<Integer> initPrices(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "房型ID") @RequestParam Long roomTypeId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        int result = roomPriceService.initPrices(hotelId, roomTypeId, startDate, endDate);
        return Result.success(result);
    }
}

