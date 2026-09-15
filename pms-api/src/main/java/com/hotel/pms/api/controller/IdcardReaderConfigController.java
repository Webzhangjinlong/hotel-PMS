package com.hotel.pms.api.controller;

import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.common.dto.IdcardReaderConfigDTO;
import com.hotel.pms.common.dto.IdcardReaderConfigQueryDTO;
import com.hotel.pms.common.dto.IdcardReaderConfigVO;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.idcard.IdcardReaderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 身份证阅读器设备配置控制器
 * <p>
 * 处理身份证阅读器设备配置的管理请求
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/idcard/readers")
@RequiredArgsConstructor
@Tag(name = "身份证阅读器管理", description = "身份证阅读器设备配置的增删改查")
public class IdcardReaderConfigController {
    
    private final IdcardReaderConfigService idcardReaderConfigService;
    
    /**
     * 分页查询设备配置列表
     * 
     * @param queryDTO 查询条件
     * @return 设备配置列表
     */
    @GetMapping
    @Operation(summary = "查询设备配置列表", description = "分页查询身份证阅读器设备配置列表")
    public Result<PageResponse<IdcardReaderConfigVO>> pageList(IdcardReaderConfigQueryDTO queryDTO) {
        PageResponse<IdcardReaderConfigVO> result = idcardReaderConfigService.pageList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询设备配置详情
     * 
     * @param id 设备配置ID
     * @return 设备配置详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询设备配置详情", description = "根据ID查询设备配置详细信息")
    public Result<IdcardReaderConfigVO> getById(@Parameter(description = "设备配置ID") @PathVariable Long id) {
        IdcardReaderConfigVO result = idcardReaderConfigService.getById(id);
        return Result.success(result);
    }
    
    /**
     * 创建设备配置
     * <p>
     * 创建新的身份证阅读器设备配置
     * </p>
     * 
     * @param dto 设备配置信息
     * @return 创建的设备配置信息
     */
    @OperationLog(module = "身份证阅读器管理", action = "创建设备配置", targetType = "设备配置")
    @PostMapping
    @Operation(summary = "创建设备配置", description = "创建新的身份证阅读器设备配置")
    public Result<IdcardReaderConfigVO> create(@Valid @RequestBody IdcardReaderConfigDTO dto) {
        IdcardReaderConfigVO result = idcardReaderConfigService.create(dto);
        return Result.success(result);
    }
    
    /**
     * 更新设备配置
     * <p>
     * 更新身份证阅读器设备配置
     * </p>
     * 
     * @param id 设备配置ID
     * @param dto 设备配置信息
     * @return 更新后的设备配置信息
     */
    @OperationLog(module = "身份证阅读器管理", action = "更新设备配置", targetType = "设备配置")
    @PutMapping("/{id}")
    @Operation(summary = "更新设备配置", description = "更新身份证阅读器设备配置")
    public Result<IdcardReaderConfigVO> update(
            @Parameter(description = "设备配置ID") @PathVariable Long id,
            @Valid @RequestBody IdcardReaderConfigDTO dto) {
        IdcardReaderConfigVO result = idcardReaderConfigService.update(id, dto);
        return Result.success(result);
    }
    
    /**
     * 删除设备配置
     * <p>
     * 逻辑删除身份证阅读器设备配置
     * </p>
     * 
     * @param id 设备配置ID
     * @return 操作结果
     */
    @OperationLog(module = "身份证阅读器管理", action = "删除设备配置", targetType = "设备配置")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除设备配置", description = "删除身份证阅读器设备配置")
    public Result<Void> delete(@Parameter(description = "设备配置ID") @PathVariable Long id) {
        idcardReaderConfigService.delete(id);
        return Result.success();
    }
    
    /**
     * 更新设备状态
     * <p>
     * 更新身份证阅读器的在线状态
     * </p>
     * 
     * @param id 设备配置ID
     * @param deviceStatus 设备状态
     * @return 操作结果
     */
    @OperationLog(module = "身份证阅读器管理", action = "更新设备状态", targetType = "设备配置")
    @PutMapping("/{id}/status")
    @Operation(summary = "更新设备状态", description = "更新身份证阅读器的在线状态")
    public Result<Void> updateDeviceStatus(
            @Parameter(description = "设备配置ID") @PathVariable Long id,
            @Parameter(description = "设备状态") @RequestParam String deviceStatus) {
        idcardReaderConfigService.updateDeviceStatus(id, deviceStatus);
        return Result.success();
    }
    
    /**
     * 获取所有启用的设备
     * <p>
     * 获取酒店所有启用的身份证阅读器设备
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 设备列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取启用设备", description = "获取酒店所有启用的身份证阅读器设备")
    public Result<List<IdcardReaderConfigVO>> listActiveDevices(@RequestParam Long hotelId) {
        List<IdcardReaderConfigVO> result = idcardReaderConfigService.listActiveDevices(hotelId);
        return Result.success(result);
    }
}
