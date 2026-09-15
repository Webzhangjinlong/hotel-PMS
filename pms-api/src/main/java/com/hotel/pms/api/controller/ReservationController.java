package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.reservation.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预订控制器
 * <p>
 * 处理预订相关的请求
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "预订管理", description = "预订的增删改查接口")
public class ReservationController {
    
    /** 预订服务 */
    private final ReservationService reservationService;
    
    /**
     * 分页查询预订列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询预订列表", description = "根据条件分页查询预订列表")
    public Result<PageResponse<ReservationVO>> pageList(ReservationQueryDTO queryDTO) {
        PageResponse<ReservationVO> result = reservationService.pageList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询预订
     * 
     * @param id 预订ID
     * @return 预订信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询预订", description = "根据预订ID查询预订详细信息")
    public Result<ReservationVO> getById(@Parameter(description = "预订ID") @PathVariable Long id) {
        ReservationVO result = reservationService.getById(id);
        return Result.success(result);
    }
    
    /**
     * 创建预订
     * 
     * @param dto 创建参数
     * @return 预订信息
     */
    @OperationLog(module = "预订管理", action = "创建预订", targetType = "预订")

    @PostMapping
    @Operation(summary = "创建预订", description = "创建新的预订")
    public Result<ReservationVO> create(@Valid @RequestBody ReservationCreateDTO dto) {
        ReservationVO result = reservationService.create(dto);
        return Result.success(result);
    }
    
    /**
     * 更新预订
     * 
     * @param id 预订ID
     * @param dto 更新参数
     * @return 预订信息
     */
    @OperationLog(module = "预订管理", action = "修改预订", targetType = "预订", targetIdParam = "id")

    @PutMapping("/{id}")
    @Operation(summary = "更新预订", description = "根据预订ID更新预订信息")
    public Result<ReservationVO> update(
            @Parameter(description = "预订ID") @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateDTO dto) {
        ReservationVO result = reservationService.update(id, dto);
        return Result.success(result);
    }
    
    /**
     * 取消预订
     * 
     * @param id 预订ID
     * @return 操作结果
     */
    @OperationLog(module = "预订管理", action = "取消预订", targetType = "预订", targetIdParam = "id")

    @DeleteMapping("/{id}")
    @Operation(summary = "取消预订", description = "根据预订ID取消预订")
    public Result<Void> cancel(@Parameter(description = "预订ID") @PathVariable Long id) {
        reservationService.cancel(id);
        return Result.success();
    }
    
    /**
     * 确认预订
     * 
     * @param id 预订ID
     * @return 操作结果
     */
    @OperationLog(module = "预订管理", action = "确认预订", targetType = "预订", targetIdParam = "id")

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认预订", description = "确认预订")
    public Result<Void> confirm(@Parameter(description = "预订ID") @PathVariable Long id) {
        reservationService.confirm(id);
        return Result.success();
    }
    
    /**
     * 标记未到店
     * 
     * @param id 预订ID
     * @return 操作结果
     */
    @OperationLog(module = "预订管理", action = "标记未到店", targetType = "预订", targetIdParam = "id")

    @PutMapping("/{id}/no-show")
    @Operation(summary = "标记未到店", description = "标记预订为未到店状态")
    public Result<Void> markNoShow(@Parameter(description = "预订ID") @PathVariable Long id) {
        reservationService.markNoShow(id);
        return Result.success();
    }
    
    /**
     * 查询今日抵店预订
     * 
     * @param hotelId 酒店ID
     * @return 今日抵店列表
     */
    @GetMapping("/today-arrivals")
    @Operation(summary = "查询今日抵店预订", description = "查询今日需要入住的预订列表")
    public Result<List<ReservationVO>> getTodayArrivals(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        List<ReservationVO> result = reservationService.getTodayArrivals(hotelId);
        return Result.success(result);
    }
}

