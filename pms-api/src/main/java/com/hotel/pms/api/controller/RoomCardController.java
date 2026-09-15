package com.hotel.pms.api.controller;

import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.doorlock.DoorLockConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 房卡控制器
 * <p>
 * 处理房卡相关的请求，包括发卡、读卡、注销等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/door-lock/cards")
@RequiredArgsConstructor
@Tag(name = "房卡管理", description = "房卡管理相关的接口")
public class RoomCardController {
    
    private final DoorLockConfigService doorLockConfigService;
    
    /**
     * 发卡
     * <p>
     * 为客人发放房卡
     * </p>
     * 
     * @param deviceId 设备ID
     * @param cardInfo 卡信息
     * @return 发卡结果
     */
    @PostMapping("/device/{deviceId}/issue")
    @Operation(summary = "发卡", description = "为客人发放房卡")
    public Result<Map<String, Object>> issueCard(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @RequestBody Map<String, Object> cardInfo) {
        // 1. 检查设备是否存在
        doorLockConfigService.getById(deviceId);
        
        // 2. 模拟发卡操作
        // 在实际项目中，这里需要调用门锁设备的SDK进行发卡
        Map<String, Object> result = Map.of(
            "success", true,
            "message", "发卡成功",
            "cardNo", "CARD_" + System.currentTimeMillis(),
            "roomId", cardInfo.get("roomId"),
            "guestName", cardInfo.get("guestName"),
            "validStart", cardInfo.get("validStart"),
            "validEnd", cardInfo.get("validEnd")
        );
        
        // 3. 记录日志
        log.info("发卡操作: 设备ID={}, 房间={}, 客人={}", 
                deviceId, cardInfo.get("roomId"), cardInfo.get("guestName"));
        
        return Result.success(result);
    }
    
    /**
     * 读卡
     * <p>
     * 读取房卡信息
     * </p>
     * 
     * @param deviceId 设备ID
     * @return 卡信息
     */
    @PostMapping("/device/{deviceId}/read")
    @Operation(summary = "读卡", description = "读取房卡信息")
    public Result<Map<String, Object>> readCard(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        // 1. 检查设备是否存在
        doorLockConfigService.getById(deviceId);
        
        // 2. 模拟读卡操作
        // 在实际项目中，这里需要调用门锁设备的SDK进行读卡
        Map<String, Object> result = Map.of(
            "success", true,
            "message", "读卡成功",
            "cardNo", "CARD_" + System.currentTimeMillis(),
            "cardType", "GUEST_CARD",
            "roomId", 101,
            "roomNo", "101",
            "validStart", "2026-08-22 14:00:00",
            "validEnd", "2026-08-23 12:00:00"
        );
        
        // 3. 记录日志
        log.info("读卡操作: 设备ID={}", deviceId);
        
        return Result.success(result);
    }
    
    /**
     * 注销卡
     * <p>
     * 注销房卡
     * </p>
     * 
     * @param deviceId 设备ID
     * @param cardNo 卡号
     * @return 注销结果
     */
    @PostMapping("/device/{deviceId}/cancel")
    @Operation(summary = "注销卡", description = "注销房卡")
    public Result<Map<String, Object>> cancelCard(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @RequestParam String cardNo) {
        // 1. 检查设备是否存在
        doorLockConfigService.getById(deviceId);
        
        // 2. 模拟注销卡操作
        // 在实际项目中，这里需要调用门锁设备的SDK进行注销
        Map<String, Object> result = Map.of(
            "success", true,
            "message", "注销成功",
            "cardNo", cardNo
        );
        
        // 3. 记录日志
        log.info("注销卡操作: 设备ID={}, 卡号={}", deviceId, cardNo);
        
        return Result.success(result);
    }
    
    /**
     * 检查设备状态
     * <p>
     * 检查门锁设备的连接状态
     * </p>
     * 
     * @param deviceId 设备ID
     * @return 设备状态
     */
    @GetMapping("/device/{deviceId}/status")
    @Operation(summary = "检查设备状态", description = "检查门锁设备的连接状态")
    public Result<Map<String, Object>> checkDeviceStatus(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        // 1. 检查设备是否存在
        doorLockConfigService.getById(deviceId);
        
        // 2. 模拟检查设备状态
        // 在实际项目中，这里需要调用门锁设备的SDK检查状态
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
