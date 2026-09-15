package com.hotel.pms.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.master.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 酒店控制器
 */
@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Tag(name = "酒店管理", description = "酒店增删改查接口")
public class HotelController {
    
    private final HotelService hotelService;
    
    /**
     * 分页查询酒店列表
     */
    @GetMapping
    @Operation(summary = "分页查询酒店列表")
    public Result<Page<HotelVO>> getHotelPage(HotelQueryDTO queryDTO) {
        Page<HotelVO> page = hotelService.getHotelPage(queryDTO);
        return Result.success(page);
    }
    
    /**
     * 根据ID查询酒店
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询酒店")
    public Result<HotelVO> getHotelById(@PathVariable Long id) {
        HotelVO hotelVO = hotelService.getHotelById(id);
        return Result.success(hotelVO);
    }
    
    /**
     * 创建酒店
     */
    @PostMapping
    @Operation(summary = "创建酒店")
    public Result<Long> createHotel(@Valid @RequestBody HotelCreateDTO createDTO) {
        Long id = hotelService.createHotel(createDTO);
        return Result.success(id);
    }
    
    /**
     * 更新酒店
     */
    @PutMapping
    @Operation(summary = "更新酒店")
    public Result<Void> updateHotel(@Valid @RequestBody HotelUpdateDTO updateDTO) {
        hotelService.updateHotel(updateDTO);
        return Result.success();
    }
    
    /**
     * 更新夜审配置
     */
    @PutMapping("/{id}/night-audit-config")
    @Operation(summary = "更新夜审配置")
    public Result<Void> updateNightAuditConfig(@PathVariable Long id, 
                                               @RequestBody NightAuditConfigDTO configDTO) {
        hotelService.updateNightAuditConfig(id, configDTO);
        return Result.success();
    }
    
    /**
     * 删除酒店
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除酒店")
    public Result<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return Result.success();
    }
}
