package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.HotelConfigDTO;
import com.hotel.pms.common.dto.HotelConfigVO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.config.HotelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 酒店配置控制器
 */
@RestController
@RequestMapping("/api/v1/hotel-config")
@RequiredArgsConstructor
@Tag(name = "酒店配置", description = "酒店配置管理接口")
public class HotelConfigController {
    
    private final HotelConfigService hotelConfigService;
    
    /**
     * 获取酒店所有配置
     */
    @GetMapping("/{hotelId}")
    @Operation(summary = "获取酒店配置")
    public Result<List<HotelConfigVO>> getConfigs(@PathVariable Long hotelId) {
        List<HotelConfigVO> configs = hotelConfigService.getConfigsByHotelId(hotelId);
        return Result.success(configs);
    }
    
    /**
     * 更新酒店配置
     */
    @PutMapping("/{hotelId}")
    @Operation(summary = "更新酒店配置")
    public Result<Void> updateConfigs(@PathVariable Long hotelId, @RequestBody HotelConfigDTO configDTO) {
        hotelConfigService.updateConfigs(hotelId, configDTO.getConfigs());
        return Result.success();
    }
}
