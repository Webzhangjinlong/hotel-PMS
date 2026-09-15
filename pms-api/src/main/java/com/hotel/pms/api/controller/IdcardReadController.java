package com.hotel.pms.api.controller;

import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.idcard.IdcardReaderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 身份证读取控制器
 * <p>
 * 处理身份证读取相关的请求，包括读取身份证信息、模拟读取等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/idcard/read")
@RequiredArgsConstructor
@Tag(name = "身份证读取", description = "身份证读取相关的接口")
public class IdcardReadController {
    
    private final IdcardReaderConfigService idcardReaderConfigService;
    
    /**
     * 读取身份证信息
     * <p>
     * 调用身份证阅读器读取身份证信息
     * </p>
     * 
     * @param deviceId 设备ID
     * @return 身份证信息
     */
    @PostMapping("/device/{deviceId}")
    @Operation(summary = "读取身份证", description = "调用身份证阅读器读取身份证信息")
    public Result<Map<String, Object>> readIdcard(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        // 1. 检查设备是否存在
        idcardReaderConfigService.getById(deviceId);
        
        // 2. 模拟读取身份证信息
        // 在实际项目中，这里需要调用身份证阅读器的SDK
        Map<String, Object> idcardInfo = Map.of(
            "success", true,
            "message", "读取成功",
            "data", Map.of(
                "name", "张三",
                "gender", "男",
                "nation", "汉",
                "birthDate", "1990-01-01",
                "address", "北京市朝阳区xxx街道xxx号",
                "cardNo", "110105199001011234",
                "issueOrg", "北京市公安局朝阳分局",
                "validStart", "2020-01-01",
                "validEnd", "2030-01-01"
            )
        );
        
        // 3. 记录日志
        log.info("读取身份证信息: 设备ID={}", deviceId);
        
        return Result.success(idcardInfo);
    }
    
    /**
     * 模拟读取身份证信息
     * <p>
     * 模拟读取身份证信息，用于测试
     * </p>
     * 
     * @return 模拟的身份证信息
     */
    @PostMapping("/simulate")
    @Operation(summary = "模拟读取", description = "模拟读取身份证信息，用于测试")
    public Result<Map<String, Object>> simulateRead() {
        // 模拟读取身份证信息
        Map<String, Object> idcardInfo = Map.of(
            "success", true,
            "message", "模拟读取成功",
            "data", Map.of(
                "name", "李四",
                "gender", "女",
                "nation", "汉",
                "birthDate", "1995-05-15",
                "address", "上海市浦东新区xxx路xxx号",
                "cardNo", "310115199505151234",
                "issueOrg", "上海市公安局浦东分局",
                "validStart", "2021-05-15",
                "validEnd", "2031-05-15"
            )
        );
        
        // 记录日志
        log.info("模拟读取身份证信息");
        
        return Result.success(idcardInfo);
    }
    
    /**
     * 检查设备状态
     * <p>
     * 检查身份证阅读器设备的连接状态
     * </p>
     * 
     * @param deviceId 设备ID
     * @return 设备状态
     */
    @GetMapping("/device/{deviceId}/status")
    @Operation(summary = "检查设备状态", description = "检查身份证阅读器设备的连接状态")
    public Result<Map<String, Object>> checkDeviceStatus(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        // 1. 检查设备是否存在
        idcardReaderConfigService.getById(deviceId);
        
        // 2. 模拟检查设备状态
        // 在实际项目中，这里需要调用身份证阅读器的SDK检查状态
        Map<String, Object> statusInfo = Map.of(
            "deviceId", deviceId,
            "status", "ONLINE",
            "statusName", "在线",
            "message", "设备连接正常"
        );
        
        // 3. 记录日志
        log.info("检查设备状态: 设备ID={}", deviceId);
        
        return Result.success(statusInfo);
    }
}
