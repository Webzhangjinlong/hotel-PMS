package com.hotel.pms.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.dto.OperationLogQueryDTO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.dao.entity.OperationLogEntity;
import com.hotel.pms.service.system.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/operation-logs")
@RequiredArgsConstructor
@Tag(name = "操作日志", description = "操作日志查询")
public class OperationLogController {
    
    private final OperationLogService operationLogService;
    
    /**
     * 分页查询操作日志
     */
    @GetMapping
    @Operation(summary = "分页查询操作日志")
    public Result<IPage<OperationLogEntity>> getOperationLogPage(OperationLogQueryDTO queryDTO) {
        Long hotelId = UserContext.getHotelId();
        IPage<OperationLogEntity> page = operationLogService.getOperationLogPage(hotelId, queryDTO);
        return Result.success(page);
    }
    
    /**
     * 查询操作日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询操作日志详情")
    public Result<OperationLogEntity> getOperationLogById(@PathVariable Long id) {
        OperationLogEntity log = operationLogService.getOperationLogById(id);
        return Result.success(log);
    }
}
