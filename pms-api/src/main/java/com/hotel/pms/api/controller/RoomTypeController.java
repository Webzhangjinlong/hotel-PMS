package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.RoomTypeCreateDTO;
import com.hotel.pms.common.dto.RoomTypeQueryDTO;
import com.hotel.pms.common.dto.RoomTypeUpdateDTO;
import com.hotel.pms.common.dto.RoomTypeVO;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.master.RoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房型控制器
 * <p>
 * 处理房型相关的请求
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/room-types")
@RequiredArgsConstructor
@Tag(name = "房型管理", description = "房型的增删改查接口")
public class RoomTypeController {
    
    /** 房型服务 */
    private final RoomTypeService roomTypeService;
    
    /**
     * 分页查询房型列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询房型列表", description = "根据条件分页查询房型列表")
    public Result<PageResponse<RoomTypeVO>> pageList(RoomTypeQueryDTO queryDTO) {
        // 【调用服务查询】
        PageResponse<RoomTypeVO> result = roomTypeService.pageList(queryDTO);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 查询房型列表（不分页）
     * 
     * @param hotelId 酒店ID
     * @return 房型列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询房型列表", description = "根据酒店ID查询房型列表（不分页）")
    public Result<List<RoomTypeVO>> list(@Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        // 【调用服务查询】
        List<RoomTypeVO> result = roomTypeService.listByHotelId(hotelId);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 查询房型下拉选项
     * 
     * @param hotelId 酒店ID
     * @return 房型列表（只包含ID、名称、编码）
     */
    @GetMapping("/options")
    @Operation(summary = "查询房型下拉选项", description = "查询房型下拉选项（用于房间选择）")
    public Result<List<RoomTypeVO>> options(@Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        // 【调用服务查询】
        List<RoomTypeVO> result = roomTypeService.listOptions(hotelId);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 根据ID查询房型
     * 
     * @param id 房型ID
     * @return 房型信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询房型", description = "根据房型ID查询房型详细信息")
    public Result<RoomTypeVO> getById(@Parameter(description = "房型ID") @PathVariable Long id) {
        // 【调用服务查询】
        RoomTypeVO result = roomTypeService.getById(id);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 创建房型
     * 
     * @param dto 创建参数
     * @return 房型信息
     */
    @PostMapping
    @Operation(summary = "创建房型", description = "创建新的房型")
    public Result<RoomTypeVO> create(@Valid @RequestBody RoomTypeCreateDTO dto) {
        // 【调用服务创建】
        RoomTypeVO result = roomTypeService.create(dto);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 更新房型
     * 
     * @param id 房型ID
     * @param dto 更新参数
     * @return 房型信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新房型", description = "根据房型ID更新房型信息")
    public Result<RoomTypeVO> update(
            @Parameter(description = "房型ID") @PathVariable Long id,
            @Valid @RequestBody RoomTypeUpdateDTO dto) {
        // 【调用服务更新】
        RoomTypeVO result = roomTypeService.update(id, dto);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 删除房型
     * 
     * @param id 房型ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除房型", description = "根据房型ID删除房型（逻辑删除）")
    public Result<Void> delete(@Parameter(description = "房型ID") @PathVariable Long id) {
        // 【调用服务删除】
        roomTypeService.delete(id);
        
        // 【返回结果】
        return Result.success();
    }
    
    /**
     * 更新房型状态
     * 
     * @param id 房型ID
     * @param status 状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新房型状态", description = "根据房型ID更新房型状态")
    public Result<Void> updateStatus(
            @Parameter(description = "房型ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam String status) {
        // 【调用服务更新状态】
        roomTypeService.updateStatus(id, status);
        
        // 【返回结果】
        return Result.success();
    }
}
