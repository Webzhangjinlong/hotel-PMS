package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.reservation.TeamReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团队预订控制器
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/team-reservations")
@RequiredArgsConstructor
@Tag(name = "团队预订管理", description = "团队预订的增删改查接口")
public class TeamReservationController {
    
    private final TeamReservationService teamReservationService;
    
    /**
     * 分页查询团队预订列表
     */
    @GetMapping
    @Operation(summary = "分页查询团队预订列表", description = "根据条件分页查询团队预订列表")
    public Result<PageResponse<TeamReservationVO>> pageList(TeamReservationQueryDTO queryDTO) {
        PageResponse<TeamReservationVO> result = teamReservationService.pageList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询团队预订
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询团队预订", description = "根据团队预订ID查询详细信息")
    public Result<TeamReservationVO> getById(@Parameter(description = "团队预订ID") @PathVariable Long id) {
        TeamReservationVO result = teamReservationService.getById(id);
        return Result.success(result);
    }
    
    /**
     * 创建团队预订
     */
    @OperationLog(module = "预订管理", action = "创建团队预订", targetType = "团队预订")

    @PostMapping
    @Operation(summary = "创建团队预订", description = "创建新的团队预订")
    public Result<TeamReservationVO> create(@Valid @RequestBody TeamReservationCreateDTO dto) {
        TeamReservationVO result = teamReservationService.create(dto);
        return Result.success(result);
    }
    
    /**
     * 更新团队预订
     */
    @OperationLog(module = "预订管理", action = "修改团队预订", targetType = "团队预订", targetIdParam = "id")

    @PutMapping("/{id}")
    @Operation(summary = "更新团队预订", description = "根据团队预订ID更新信息")
    public Result<TeamReservationVO> update(
            @Parameter(description = "团队预订ID") @PathVariable Long id,
            @Valid @RequestBody TeamReservationUpdateDTO dto) {
        TeamReservationVO result = teamReservationService.update(id, dto);
        return Result.success(result);
    }
    
    /**
     * 取消团队预订
     */
    @OperationLog(module = "预订管理", action = "取消团队预订", targetType = "团队预订", targetIdParam = "id")

    @DeleteMapping("/{id}")
    @Operation(summary = "取消团队预订", description = "根据团队预订ID取消预订")
    public Result<Void> cancel(@Parameter(description = "团队预订ID") @PathVariable Long id) {
        teamReservationService.cancel(id);
        return Result.success();
    }
    
    /**
     * 确认团队预订
     */
    @OperationLog(module = "预订管理", action = "确认团队预订", targetType = "团队预订", targetIdParam = "id")

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认团队预订", description = "确认团队预订")
    public Result<Void> confirm(@Parameter(description = "团队预订ID") @PathVariable Long id) {
        teamReservationService.confirm(id);
        return Result.success();
    }
    
    /**
     * 团队入住
     */
    @OperationLog(module = "入住管理", action = "团队入住", targetType = "团队预订", targetIdParam = "id")

    @PostMapping("/{id}/check-in")
    @Operation(summary = "团队入住", description = "团队批量入住")
    public Result<Void> batchCheckIn(
            @Parameter(description = "团队预订ID") @PathVariable Long id,
            @Valid @RequestBody TeamCheckInDTO dto) {
        teamReservationService.batchCheckIn(id, dto);
        return Result.success();
    }
    
    /**
     * 团队退房
     */
    @OperationLog(module = "入住管理", action = "团队退房", targetType = "团队预订", targetIdParam = "id")

    @PostMapping("/{id}/check-out")
    @Operation(summary = "团队退房", description = "团队批量退房")
    public Result<Void> batchCheckOut(
            @Parameter(description = "团队预订ID") @PathVariable Long id,
            @Valid @RequestBody TeamCheckOutDTO dto) {
        teamReservationService.batchCheckOut(id, dto);
        return Result.success();
    }
    
    /**
     * 分配房间
     */
    @OperationLog(module = "预订管理", action = "分配房间", targetType = "团队预订", targetIdParam = "id")

    @PostMapping("/{id}/assign-rooms")
    @Operation(summary = "分配房间", description = "为团队预订分配房间")
    public Result<Void> assignRooms(
            @Parameter(description = "团队预订ID") @PathVariable Long id,
            @Valid @RequestBody List<TeamCheckInDTO.RoomAssignment> assignments) {
        teamReservationService.assignRooms(id, assignments);
        return Result.success();
    }

    /**
     * 拆分房间（从团队中移除房间，转为散客）
     */
    @OperationLog(module = "预订管理", action = "拆分房间", targetType = "团队预订", targetIdParam = "id")

    @PostMapping("/{id}/rooms/{roomId}/split")
    @Operation(summary = "拆分房间", description = "将团队预订中的指定房间拆出，转为散客")
    public Result<Void> splitRoom(
            @Parameter(description = "团队预订ID") @PathVariable Long id,
            @Parameter(description = "团队预订房间明细ID") @PathVariable Long roomId) {
        teamReservationService.splitRoom(id, roomId);
        return Result.success();
    }
}
