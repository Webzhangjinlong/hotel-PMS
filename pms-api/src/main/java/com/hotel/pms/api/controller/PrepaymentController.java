package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.payment.PrepaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预付控制器
 * <p>
 * 处理预订时的预付款操作
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/prepayments")
@RequiredArgsConstructor
@Tag(name = "预订预付管理", description = "预订时的预付款处理")
public class PrepaymentController {
    
    private final PrepaymentService prepaymentService;
    
    /**
     * 预订时预付
     */
    @OperationLog(module = "预付款管理", action = "预订预付", targetType = "预付款")

    @PostMapping
    @Operation(summary = "预订预付", description = "预订时进行预付款")
    public Result<PrepaymentVO> prepay(@Valid @RequestBody PrepaymentDTO dto) {
        PrepaymentVO result = prepaymentService.prepay(dto);
        return Result.success(result);
    }
    
    /**
     * 预付款转入住账务
     */
    @OperationLog(module = "预付款管理", action = "预付转入住", targetType = "预付款", targetIdParam = "id")

    @PostMapping("/{id}/transfer/{stayId}")
    @Operation(summary = "预付转入住账务", description = "将预付款转入入住账务单")
    public Result<Void> transferToStayFolio(
            @Parameter(description = "预付ID") @PathVariable Long id,
            @Parameter(description = "入住单ID") @PathVariable Long stayId) {
        prepaymentService.transferToStayFolio(id, stayId);
        return Result.success();
    }
    
    /**
     * 预订取消退款
     */
    @OperationLog(module = "预付款管理", action = "预付退款", targetType = "预付款", targetIdParam = "id")

    @PostMapping("/{id}/refund")
    @Operation(summary = "预付退款", description = "预订取消时退款")
    public Result<TransactionVO> refundPrepayment(
            @Parameter(description = "预付ID") @PathVariable Long id,
            @Parameter(description = "退款方式") @RequestParam String refundMethod) {
        TransactionVO result = prepaymentService.refundPrepayment(id, refundMethod);
        return Result.success(result);
    }
    
    /**
     * 分页查询预付款列表
     */
    @GetMapping
    @Operation(summary = "查询预付款列表", description = "分页查询预付款列表")
    public Result<Page<PrepaymentVO>> list(PrepaymentQueryDTO queryDTO) {
        Page<PrepaymentVO> result = prepaymentService.list(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 预付统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "预付统计", description = "查询预付统计数据")
    public Result<PrepaymentStatisticsVO> statistics(PrepaymentQueryDTO queryDTO) {
        PrepaymentStatisticsVO result = prepaymentService.statistics(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据散客预订ID查询预付款
     */
    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "查询散客预订预付款", description = "根据散客预订ID查询预付款")
    public Result<List<PrepaymentVO>> getByReservationId(
            @Parameter(description = "散客预订ID") @PathVariable Long reservationId) {
        List<PrepaymentVO> result = prepaymentService.getByReservationId(reservationId);
        return Result.success(result);
    }
    
    /**
     * 根据团队预订ID查询预付款
     */
    @GetMapping("/team-reservation/{teamReservationId}")
    @Operation(summary = "查询团队预订预付款", description = "根据团队预订ID查询预付款")
    public Result<List<PrepaymentVO>> getByTeamReservationId(
            @Parameter(description = "团队预订ID") @PathVariable Long teamReservationId) {
        List<PrepaymentVO> result = prepaymentService.getByTeamReservationId(teamReservationId);
        return Result.success(result);
    }
}

