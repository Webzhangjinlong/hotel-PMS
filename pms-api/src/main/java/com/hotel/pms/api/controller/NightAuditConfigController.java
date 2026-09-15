package com.hotel.pms.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hotel.pms.common.dto.NightAuditConfigDTO;
import com.hotel.pms.common.dto.NightAuditConfigVO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.config.NightAuditConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 夜审配置控制器
 */
@RestController
@RequestMapping("/api/v1/night-audit-config")
@RequiredArgsConstructor
@Tag(name = "夜审配置", description = "夜审配置管理接口")
public class NightAuditConfigController {

    private final NightAuditConfigService nightAuditConfigService;

    /**
     * 获取夜审配置
     */
    @GetMapping("/{hotelId}")
    @Operation(summary = "获取夜审配置")
    public Result<NightAuditConfigVO> getConfig(@PathVariable Long hotelId) {
        NightAuditConfigVO config = nightAuditConfigService.getConfig(hotelId);
        return Result.success(config);
    }

    /**
     * 更新夜审配置
     */
    @PutMapping("/{hotelId}")
    @Operation(summary = "更新夜审配置")
    public Result<Void> updateConfig(@PathVariable Long hotelId, @RequestBody NightAuditConfigDTO configDTO) throws JsonProcessingException {
        nightAuditConfigService.updateConfig(hotelId, configDTO);
        return Result.success();
    }
}
