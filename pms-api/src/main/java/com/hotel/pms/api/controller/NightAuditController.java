package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.nightaudit.NightAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

/**
 * 夜审管理Controller
 */
@RestController
@RequestMapping("/api/v1/night-audit")
@Tag(name = "夜审管理", description = "夜审相关接口")
public class NightAuditController {
    
    @Autowired
    private NightAuditService nightAuditService;
    
    /**
     * 执行夜审
     */
    @OperationLog(module = "夜审管理", action = "执行夜审", targetType = "夜审")

    @PostMapping("/execute")
    @Operation(summary = "执行夜审", description = "手动执行夜审")
    public Result<NightAuditVO> executeNightAudit(@RequestParam Long hotelId) {
        NightAuditVO result = nightAuditService.manualExecuteNightAudit(hotelId);
        return Result.success(result);
    }
    
    /**
     * 查询夜审记录列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询夜审记录", description = "分页查询夜审记录")
    public Result<PageResponse<NightAuditVO>> getNightAuditList(NightAuditQueryDTO queryDTO) {
        PageResponse<NightAuditVO> result = nightAuditService.getNightAuditList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 查询夜审详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询夜审详情", description = "查询夜审详情")
    public Result<NightAuditDetailVO> getNightAuditDetail(@PathVariable Long id) {
        NightAuditDetailVO result = nightAuditService.getNightAuditDetail(id);
        return Result.success(result);
    }
    
    /**
     * 重试失败的夜审步骤
     */
    @OperationLog(module = "夜审管理", action = "重试夜审步骤", targetType = "夜审", targetIdParam = "auditId")

    @PostMapping("/{auditId}/steps/{stepId}/retry")
    @Operation(summary = "重试失败步骤", description = "重试失败的夜审步骤")
    public Result<Void> retryNightAuditStep(@PathVariable Long auditId, @PathVariable Long stepId) {
        nightAuditService.retryNightAuditStep(auditId, stepId);
        return Result.success();
    }
    
    /**
     * 重置夜审状态
     */
    @OperationLog(module = "夜审管理", action = "重置夜审", targetType = "夜审", targetIdParam = "auditId")

    @PostMapping("/{auditId}/reset")
    @Operation(summary = "重置夜审状态", description = "重置卡在执行中的夜审状态")
    public Result<Void> resetNightAuditStatus(@PathVariable Long auditId) {
        nightAuditService.resetNightAuditStatus(auditId);
        return Result.success();
    }
}

