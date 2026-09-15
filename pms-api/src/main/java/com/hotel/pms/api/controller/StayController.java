package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.stay.StayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入住控制器
 * <p>
 * 处理入住相关的请求
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stays")
@RequiredArgsConstructor
@Tag(name = "入住管理", description = "入住的增删改查接口")
public class StayController {
    
    /** 入住服务 */
    private final StayService stayService;
    
    /**
     * 分页查询入住列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询入住列表", description = "根据条件分页查询入住列表")
    public Result<PageResponse<StayVO>> pageList(StayQueryDTO queryDTO) {
        PageResponse<StayVO> result = stayService.pageList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 分页查询团队入住汇总
     * 
     * @param queryDTO 查询条件
     * @return 团队汇总分页结果
     */
    @GetMapping("/team-summary")
    @Operation(summary = "分页查询团队入住汇总", description = "按团队维度分页查询入住汇总")
    public Result<PageResponse<TeamStaySummaryVO>> pageTeamSummary(StayQueryDTO queryDTO) {
        PageResponse<TeamStaySummaryVO> result = stayService.pageTeamSummary(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询入住详情
     * 
     * @param id 入住单ID
     * @return 入住信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询入住详情", description = "根据入住单ID查询入住详细信息")
    public Result<StayVO> getById(@Parameter(description = "入住单ID") @PathVariable Long id) {
        StayVO result = stayService.getById(id);
        return Result.success(result);
    }
    
    /**
     * 散客入住
     * 
     * @param dto 散客入住参数
     * @return 入住信息
     */
    @OperationLog(module = "入住管理", action = "办理入住", targetType = "入住单")
    @PostMapping("/walk-in")
    @Operation(summary = "散客入住", description = "散客直接到店入住")
    public Result<StayVO> walkInCheckIn(@Valid @RequestBody StayCreateDTO dto) {
        StayVO result = stayService.walkInCheckIn(dto);
        return Result.success(result);
    }
    
    /**
     * 预订入住
     * 
     * @param dto 预订入住参数
     * @return 入住信息
     */
    @OperationLog(module = "入住管理", action = "办理入住", targetType = "入住单")
    @PostMapping("/check-in")
    @Operation(summary = "预订入住", description = "从预订单入住")
    public Result<StayVO> reservationCheckIn(@Valid @RequestBody CheckInDTO dto) {
        StayVO result = stayService.reservationCheckIn(dto);
        return Result.success(result);
    }
    
    /**
     * 查询今日入住列表
     * 
     * @param hotelId 酒店ID
     * @return 今日入住列表
     */
    @GetMapping("/today-checkins")
    @Operation(summary = "查询今日入住列表", description = "查询今日入住的列表")
    public Result<List<StayVO>> getTodayCheckIns(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        List<StayVO> result = stayService.getTodayCheckIns(hotelId);
        return Result.success(result);
    }
    
    /**
     * 续住
     * 
     * @param dto 续住参数
     * @return 续住信息
     */
    @PutMapping("/extend")
    @Operation(summary = "续住", description = "延长客人住宿时间")
    public Result<StayExtendVO> extendStay(@Valid @RequestBody StayExtendDTO dto) {
        StayExtendVO result = stayService.extendStay(dto);
        return Result.success(result);
    }
    
    /**
     * 退房
     * 
     * @param dto 退房参数
     * @return 退房信息
     */
    @PutMapping("/check-out")
    @Operation(summary = "退房", description = "客人退房结算")
    public Result<StayCheckOutVO> checkOut(@Valid @RequestBody StayCheckOutDTO dto) {
        StayCheckOutVO result = stayService.checkOut(dto);
        return Result.success(result);
    }
    
    /**
     * 换房
     * 
     * @param dto 换房参数
     * @return 换房结果
     */
    @OperationLog(module = "入住管理", action = "换房", targetType = "入住单")
    @PutMapping("/change-room")
    @Operation(summary = "换房", description = "客人换房操作")
    public Result<ChangeRoomVO> changeRoom(@Valid @RequestBody ChangeRoomDTO dto) {
        ChangeRoomVO result = stayService.changeRoom(dto);
        return Result.success(result);
    }
    
    /**
     * 散客入住单转入团队预订
     * 
     * @param stayId 入住单ID
     * @param dto 转入参数
     * @return 更新后的入住信息
     */
    @PostMapping("/{stayId}/transfer-to-team")
    @Operation(summary = "散客转入团队", description = "将已入住的散客入住单转入团队预订")
    public Result<StayVO> transferToTeam(
            @Parameter(description = "入住单ID") @PathVariable Long stayId,
            @Valid @RequestBody StayTransferDTO dto) {
        StayVO result = stayService.transferToTeam(stayId, dto);
        return Result.success(result);
    }
}
