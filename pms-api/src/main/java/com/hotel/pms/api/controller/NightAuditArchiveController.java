package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.NightAuditArchiveVO;
import com.hotel.pms.common.dto.NightAuditQueryDTO;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.nightaudit.NightAuditArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 夜审历史数据管理Controller
 */
@RestController
@RequestMapping("/api/v1/night-audit/archive")
@Tag(name = "夜审历史数据", description = "夜审历史数据管理接口")
public class NightAuditArchiveController {
    
    @Autowired
    private NightAuditArchiveService archiveService;
    
    /**
     * 归档历史数据
     */
    @PostMapping("/archive")
    @Operation(summary = "归档历史数据", description = "将指定日期之前的夜审数据归档")
    public Result<Integer> archiveData(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beforeDate,
            @RequestParam(defaultValue = "system") String archivedBy) {
        int count = archiveService.archiveData(hotelId, beforeDate, archivedBy);
        return Result.success(count);
    }
    
    /**
     * 查询归档数据列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询归档数据", description = "分页查询归档的夜审数据")
    public Result<PageResponse<NightAuditArchiveVO>> getArchiveList(NightAuditQueryDTO queryDTO) {
        PageResponse<NightAuditArchiveVO> result = archiveService.getArchiveList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 查询归档统计
     */
    @GetMapping("/stats")
    @Operation(summary = "查询归档统计", description = "查询可归档和已归档的数据统计")
    public Result<NightAuditArchiveService.ArchiveStatsVO> getArchiveStats(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beforeDate) {
        NightAuditArchiveService.ArchiveStatsVO stats = archiveService.getArchiveStats(hotelId, beforeDate);
        return Result.success(stats);
    }
}