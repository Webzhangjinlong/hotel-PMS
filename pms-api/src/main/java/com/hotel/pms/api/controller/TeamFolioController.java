package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.folio.TeamFolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/team-folios")
@RequiredArgsConstructor
@Tag(name = "团队账务管理", description = "团队账务的收款和查询接口")
public class TeamFolioController {
    
    private final TeamFolioService teamFolioService;
    
    @GetMapping
    @Operation(summary = "分页查询团队账务列表")
    public Result<PageResponse<TeamFolioVO>> pageList(TeamFolioQueryDTO queryDTO) {
        PageResponse<TeamFolioVO> result = teamFolioService.pageList(queryDTO);
        return Result.success(result);
    }
    
    @GetMapping("/stats")
    @Operation(summary = "统计团队账务数据")
    public Result<FolioStatsVO> getStats(TeamFolioQueryDTO queryDTO) {
        FolioStatsVO result = teamFolioService.getStats(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询团队账务详情")
    public Result<TeamFolioVO> getById(@Parameter(description = "账务单ID") @PathVariable Long id) {
        TeamFolioVO result = teamFolioService.getById(id);
        return Result.success(result);
    }
    
    @PostMapping("/{id}/payments")
    @Operation(summary = "团队收款")
    public Result<TeamFolioPaymentVO> addPayment(
            @Parameter(description = "账务单ID") @PathVariable Long id,
            @Valid @RequestBody TeamFolioPaymentDTO dto) {
        TeamFolioPaymentVO result = teamFolioService.addPayment(id, dto);
        return Result.success(result);
    }
    
    @GetMapping("/{id}/payments")
    @Operation(summary = "查询收款记录")
    public Result<List<TeamFolioPaymentVO>> getPayments(@Parameter(description = "账务单ID") @PathVariable Long id) {
        List<TeamFolioPaymentVO> result = teamFolioService.getPayments(id);
        return Result.success(result);
    }
    
    @PutMapping("/{id}/close")
    @Operation(summary = "关闭账务单")
    public Result<Void> closeFolio(@Parameter(description = "账务单ID") @PathVariable Long id) {
        teamFolioService.closeFolio(id);
        return Result.success();
    }
}
