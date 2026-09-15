package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.FloorCreateDTO;
import com.hotel.pms.common.dto.FloorQueryDTO;
import com.hotel.pms.common.dto.FloorUpdateDTO;
import com.hotel.pms.common.dto.FloorVO;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.master.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 楼层控制器
 * <p>
 * 处理楼层相关的请求
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/floors")
@RequiredArgsConstructor
@Tag(name = "楼层管理", description = "楼层的增删改查接口")
public class FloorController {
    
    /** 楼层服务 */
    private final FloorService floorService;
    
    /**
     * 分页查询楼层列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询楼层列表", description = "根据条件分页查询楼层列表")
    public Result<PageResponse<FloorVO>> pageList(FloorQueryDTO queryDTO) {
        // 【调用服务查询】
        PageResponse<FloorVO> result = floorService.pageList(queryDTO);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 查询楼层列表（不分页）
     * 
     * @param hotelId 酒店ID
     * @return 楼层列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询楼层列表", description = "根据酒店ID查询楼层列表（不分页）")
    public Result<List<FloorVO>> list(@Parameter(description = "酒店ID") @RequestParam Long hotelId) {
        // 【调用服务查询】
        List<FloorVO> result = floorService.listByHotelId(hotelId);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 根据ID查询楼层
     * 
     * @param id 楼层ID
     * @return 楼层信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询楼层", description = "根据楼层ID查询楼层详细信息")
    public Result<FloorVO> getById(@Parameter(description = "楼层ID") @PathVariable Long id) {
        // 【调用服务查询】
        FloorVO result = floorService.getById(id);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 创建楼层
     * 
     * @param dto 创建参数
     * @return 楼层信息
     */
    @PostMapping
    @Operation(summary = "创建楼层", description = "创建新的楼层")
    public Result<FloorVO> create(@Valid @RequestBody FloorCreateDTO dto) {
        // 【调用服务创建】
        FloorVO result = floorService.create(dto);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 更新楼层
     * 
     * @param id 楼层ID
     * @param dto 更新参数
     * @return 楼层信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新楼层", description = "根据楼层ID更新楼层信息")
    public Result<FloorVO> update(
            @Parameter(description = "楼层ID") @PathVariable Long id,
            @Valid @RequestBody FloorUpdateDTO dto) {
        // 【调用服务更新】
        FloorVO result = floorService.update(id, dto);
        
        // 【返回结果】
        return Result.success(result);
    }
    
    /**
     * 删除楼层
     * 
     * @param id 楼层ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除楼层", description = "根据楼层ID删除楼层（逻辑删除）")
    public Result<Void> delete(@Parameter(description = "楼层ID") @PathVariable Long id) {
        // 【调用服务删除】
        floorService.delete(id);
        
        // 【返回结果】
        return Result.success();
    }
    
    /**
     * 更新楼层状态
     * 
     * @param id 楼层ID
     * @param status 状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新楼层状态", description = "根据楼层ID更新楼层状态")
    public Result<Void> updateStatus(
            @Parameter(description = "楼层ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam String status) {
        // 【调用服务更新状态】
        floorService.updateStatus(id, status);
        
        // 【返回结果】
        return Result.success();
    }
}
