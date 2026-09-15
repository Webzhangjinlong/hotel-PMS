package com.hotel.pms.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.dto.PoliceManualUploadDTO;
import com.hotel.pms.common.dto.PoliceUploadQueryDTO;
import com.hotel.pms.common.dto.PoliceUploadRecordVO;
import com.hotel.pms.common.dto.PoliceUploadStatsVO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.police.PoliceUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公安上传控制器
 * <p>
 * 处理客人信息上传公安系统的相关请求
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/police/uploads")
@RequiredArgsConstructor
@Tag(name = "公安上传", description = "客人信息上传公安系统")
public class PoliceUploadController {

    @Autowired
    private PoliceUploadService policeUploadService;

    /**
     * 查询上传记录列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "查询上传记录列表", description = "根据条件分页查询上传记录")
    public Result<IPage<PoliceUploadRecordVO>> getUploadList(PoliceUploadQueryDTO queryDTO) {
        IPage<PoliceUploadRecordVO> result = policeUploadService.getUploadList(queryDTO);
        return Result.success(result);
    }

    /**
     * 查询上传记录详情
     *
     * @param id 记录ID
     * @return 记录详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询上传记录详情", description = "根据ID查询上传记录详情")
    public Result<PoliceUploadRecordVO> getUploadById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        PoliceUploadRecordVO result = policeUploadService.getUploadById(id);
        return Result.success(result);
    }

    /**
     * 重试上传失败的记录
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/{id}/retry")
    @Operation(summary = "重试上传", description = "重试上传失败的记录")
    public Result<Void> retryUpload(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        policeUploadService.retryUpload(id);
        return Result.success();
    }

    /**
     * 批量重试上传失败的记录
     *
     * @param ids 记录ID列表
     * @return 操作结果
     */
    @PostMapping("/batch-retry")
    @Operation(summary = "批量重试上传", description = "批量重试上传失败的记录")
    public Result<Void> batchRetryUpload(@RequestBody List<Long> ids) {
        policeUploadService.batchRetryUpload(ids);
        return Result.success();
    }

    /**
     * 人工补传
     *
     * @param dto 补传数据
     * @return 操作结果
     */
    @PostMapping("/manual")
    @Operation(summary = "人工补传", description = "手动上传客人信息到公安系统")
    public Result<Void> manualUpload(@Valid @RequestBody PoliceManualUploadDTO dto) {
        policeUploadService.manualUpload(dto);
        return Result.success();
    }

    /**
     * 查询上传统计
     *
     * @param hotelId 酒店ID
     * @return 统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "查询上传统计", description = "查询公安上传统计数据")
    public Result<PoliceUploadStatsVO> getUploadStats(
            @Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        PoliceUploadStatsVO result = policeUploadService.getUploadStats(hotelId);
        return Result.success(result);
    }
}
