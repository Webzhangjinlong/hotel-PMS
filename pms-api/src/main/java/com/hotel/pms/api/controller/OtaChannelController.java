package com.hotel.pms.api.controller;

import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.common.dto.OtaChannelDTO;
import com.hotel.pms.common.dto.OtaChannelQueryDTO;
import com.hotel.pms.common.dto.OtaChannelVO;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.ota.OtaChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OTA渠道配置控制器
 * <p>
 * 处理OTA渠道配置的管理请求
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ota/channels")
@RequiredArgsConstructor
@Tag(name = "OTA渠道管理", description = "OTA渠道配置的增删改查")
public class OtaChannelController {
    
    private final OtaChannelService otaChannelService;
    
    /**
     * 分页查询渠道列表
     * 
     * @param queryDTO 查询条件
     * @return 渠道列表
     */
    @GetMapping
    @Operation(summary = "查询渠道列表", description = "分页查询OTA渠道列表")
    public Result<PageResponse<OtaChannelVO>> pageList(OtaChannelQueryDTO queryDTO) {
        PageResponse<OtaChannelVO> result = otaChannelService.pageList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询渠道详情
     * 
     * @param id 渠道ID
     * @return 渠道详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询渠道详情", description = "根据ID查询渠道详细信息")
    public Result<OtaChannelVO> getById(@Parameter(description = "渠道ID") @PathVariable Long id) {
        OtaChannelVO result = otaChannelService.getById(id);
        return Result.success(result);
    }
    
    /**
     * 创建渠道
     * <p>
     * 创建新的OTA渠道配置
     * </p>
     * 
     * @param dto 渠道信息
     * @return 创建的渠道信息
     */
    @OperationLog(module = "OTA渠道管理", action = "创建渠道", targetType = "OTA渠道")
    @PostMapping
    @Operation(summary = "创建渠道", description = "创建新的OTA渠道配置")
    public Result<OtaChannelVO> create(@Valid @RequestBody OtaChannelDTO dto) {
        OtaChannelVO result = otaChannelService.create(dto);
        return Result.success(result);
    }
    
    /**
     * 更新渠道
     * <p>
     * 更新OTA渠道配置
     * </p>
     * 
     * @param id 渠道ID
     * @param dto 渠道信息
     * @return 更新后的渠道信息
     */
    @OperationLog(module = "OTA渠道管理", action = "更新渠道", targetType = "OTA渠道")
    @PutMapping("/{id}")
    @Operation(summary = "更新渠道", description = "更新OTA渠道配置")
    public Result<OtaChannelVO> update(
            @Parameter(description = "渠道ID") @PathVariable Long id,
            @Valid @RequestBody OtaChannelDTO dto) {
        OtaChannelVO result = otaChannelService.update(id, dto);
        return Result.success(result);
    }
    
    /**
     * 删除渠道
     * <p>
     * 逻辑删除OTA渠道
     * </p>
     * 
     * @param id 渠道ID
     * @return 操作结果
     */
    @OperationLog(module = "OTA渠道管理", action = "删除渠道", targetType = "OTA渠道")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除渠道", description = "删除OTA渠道")
    public Result<Void> delete(@Parameter(description = "渠道ID") @PathVariable Long id) {
        otaChannelService.delete(id);
        return Result.success();
    }
    
    /**
     * 获取所有启用的渠道
     * <p>
     * 获取酒店所有启用的OTA渠道
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 渠道列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取启用渠道", description = "获取酒店所有启用的OTA渠道")
    public Result<List<OtaChannelVO>> listActiveChannels(@RequestParam Long hotelId) {
        List<OtaChannelVO> result = otaChannelService.listActiveChannels(hotelId);
        return Result.success(result);
    }
}
