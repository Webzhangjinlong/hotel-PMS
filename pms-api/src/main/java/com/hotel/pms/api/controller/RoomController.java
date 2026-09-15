package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.master.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房间控制器
 * <p>
 * 处理房间相关的请求
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "房间管理", description = "房间的增删改查接口")
public class RoomController {

    /** 房间服务 */
    private final RoomService roomService;

    /**
     * 分页查询房间列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询房间列表", description = "根据条件分页查询房间列表")
    public Result<PageResponse<RoomVO>> pageList(RoomQueryDTO queryDTO) {
        // 【调用服务查询】
        PageResponse<RoomVO> result = roomService.pageList(queryDTO);

        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 查询房间列表（不分页）
     *
     * @param hotelId 酒店ID
     * @return 房间列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询房间列表", description = "根据酒店ID查询房间列表（不分页）")
    public Result<List<RoomVO>> list(@Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        // 【调用服务查询】
        List<RoomVO> result = roomService.listByHotelId(hotelId);

        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 根据ID查询房间
     *
     * @param id 房间ID
     * @return 房间信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询房间", description = "根据房间ID查询房间详细信息")
    public Result<RoomVO> getById(@Parameter(description = "房间ID") @PathVariable Long id) {
        // 【调用服务查询】
        RoomVO result = roomService.getById(id);
        log.info("试试git好用吗？");

        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 创建房间
     *
     * @param dto 创建参数
     * @return 房间信息
     */
    @OperationLog(module = "房态管理", action = "创建房间", targetType = "房间")

    @PostMapping
    @Operation(summary = "创建房间", description = "创建新的房间")
    public Result<RoomVO> create(@Valid @RequestBody RoomCreateDTO dto) {
        // 【调用服务创建】
        RoomVO result = roomService.create(dto);

        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 更新房间
     *
     * @param id 房间ID
     * @param dto 更新参数
     * @return 房间信息
     */
    @OperationLog(module = "房态管理", action = "修改房间", targetType = "房间", targetIdParam = "id")

    @PutMapping("/{id}")
    @Operation(summary = "更新房间", description = "根据房间ID更新房间信息")
    public Result<RoomVO> update(
            @Parameter(description = "房间ID") @PathVariable Long id,
            @Valid @RequestBody RoomUpdateDTO dto) {
        // 【调用服务更新】
        RoomVO result = roomService.update(id, dto);

        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 删除房间
     *
     * @param id 房间ID
     * @return 操作结果
     */
    @OperationLog(module = "房态管理", action = "删除房间", targetType = "房间", targetIdParam = "id")

    @DeleteMapping("/{id}")
    @Operation(summary = "删除房间", description = "根据房间ID删除房间（逻辑删除）")
    public Result<Void> delete(@Parameter(description = "房间ID") @PathVariable Long id) {
        // 【调用服务删除】
        roomService.delete(id);

        // 【返回结果】
        return Result.success();
    }

    /**
     * 更新房间状态
     *
     * @param id 房间ID
     * @param status 状态
     * @return 操作结果
     */
    @OperationLog(module = "房态管理", action = "修改房态", targetType = "房间", targetIdParam = "id")

    @PutMapping("/{id}/status")
    @Operation(summary = "更新房间状态", description = "根据房间ID更新房间状态")
    public Result<Void> updateStatus(
            @Parameter(description = "房间ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam String status) {
        // 【调用服务更新状态】
        roomService.updateStatus(id, status);

        // 【返回结果】
        return Result.success();
    }

    /**
     * 查询房间统计
     *
     * @param hotelId 酒店ID
     * @return 统计信息
     */
    @GetMapping("/summary")
    @Operation(summary = "查询房间统计", description = "查询酒店房间各状态统计")
    public Result<RoomSummaryVO> getSummary(@Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        // 【调用服务查询】
        RoomSummaryVO result = roomService.getSummary(hotelId);

        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 房态看板
     *
     * @param hotelId 酒店ID
     * @param floorId 楼层ID（可选）
     * @param status 状态筛选（可选）
     * @return 房态看板数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "房态看板", description = "获取房态看板数据，包含房价和预订信息")
    public Result<RoomDashboardVO> getDashboard(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "楼层ID") @RequestParam(required = false) Long floorId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status) {
        // 【调用服务查询】
        RoomDashboardVO result = roomService.getDashboard(hotelId, floorId, status);

        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 房间详情
     *
     * @param id 房间ID
     * @return 房间详情（含房价和预订）
     */
    @GetMapping("/{id}/detail")
    @Operation(summary = "房间详情", description = "获取房间详情，包含房价和预订信息")
    public Result<RoomDetailVO> getRoomDetail(@Parameter(description = "房间ID") @PathVariable Long id) {
        // 【调用服务查询】
        RoomDetailVO result = roomService.getRoomDetail(id);

        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 检查房间可用性
     * 验证指定房间在入住期间是否可以预订
     *
     * @param roomId 房间ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @return 是否可用
     */
    @GetMapping("/availability")
    @Operation(summary = "检查房间可用性", description = "验证房间在指定日期范围内是否可用")
    public Result<Boolean> checkAvailability(
            @Parameter(description = "房间ID") @RequestParam Long roomId,
            @Parameter(description = "入住日期") @RequestParam String checkInDate,
            @Parameter(description = "离店日期") @RequestParam String checkOutDate) {
        boolean available = roomService.checkAvailability(roomId,
                java.time.LocalDate.parse(checkInDate),
                java.time.LocalDate.parse(checkOutDate));
        return Result.success(available);
    }

    /**
     * 查询房间预订日历
     * 返回指定日期范围内每个房间的每日预订状态
     *
     * @param hotelId 酒店ID
     * @param startDate 开始日期（可选，默认今天）
     * @param days 天数（可选，默认7天）
     * @param floorId 楼层ID（可选筛选）
     * @param roomTypeId 房型ID（可选筛选）
     * @return 房间日历数据
     */
    @GetMapping("/calendar")
    @Operation(summary = "查询房间预订日历", description = "获取指定日期范围内房间的预订和排房情况")
    public Result<RoomCalendarVO> getRoomCalendar(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "楼层ID") @RequestParam(required = false) Long floorId,
            @Parameter(description = "房型ID") @RequestParam(required = false) Long roomTypeId) {
        // 解析开始日期，默认为今天
        java.time.LocalDate start = (startDate != null && !startDate.isEmpty())
                ? java.time.LocalDate.parse(startDate)
                : java.time.LocalDate.now();

        // 限制最大天数为30天
        if (days > 30) days = 30;
        if (days < 1) days = 7;

        // 调用服务查询
        RoomCalendarVO result = roomService.getRoomCalendar(hotelId, start, days, floorId, roomTypeId);
        return Result.success(result);
    }

    /**
     * 检查房间日期冲突
     * 验证指定房间在入住期间是否已有其他预订
     *
     * @param roomId 房间ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @param excludeReservationId 排除的预订ID（编辑时使用，可选）
     * @return 冲突检查结果
     */
    @GetMapping("/conflict-check")
    @Operation(summary = "检查房间日期冲突", description = "验证房间在指定日期范围内是否可用")
    public Result<java.util.Map<String, Object>> checkConflict(
            @Parameter(description = "房间ID") @RequestParam Long roomId,
            @Parameter(description = "入住日期") @RequestParam String checkInDate,
            @Parameter(description = "离店日期") @RequestParam String checkOutDate,
            @Parameter(description = "排除的预订ID") @RequestParam(required = false) Long excludeReservationId) {

        java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInDate);
        java.time.LocalDate checkOut = java.time.LocalDate.parse(checkOutDate);

        // 检查是否有冲突
        boolean hasConflict = roomService.checkDateConflict(roomId, checkIn, checkOut, excludeReservationId);

        // 构建返回结果
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("hasConflict", hasConflict);
        result.put("roomId", roomId);
        result.put("checkInDate", checkInDate);
        result.put("checkOutDate", checkOutDate);

        // 如果有冲突，返回冲突的预订信息
        if (hasConflict) {
            result.put("conflictingReservations",
                    roomService.getConflictReservationsVO(roomId, checkIn, checkOut, excludeReservationId));
        }

        return Result.success(result);
    }
}
