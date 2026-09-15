package com.hotel.pms.api.controller;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.common.dto.SysShiftDTO;
import com.hotel.pms.common.dto.SysShiftMessageVO;
import com.hotel.pms.common.dto.SysShiftNotifyConfigVO;
import com.hotel.pms.common.dto.SysShiftVO;
import com.hotel.pms.service.shift.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Tag(name = "交班管理", description = "交班管理")
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @Operation(summary = "创建交班草稿")
    public Result<SysShiftVO> create(@RequestBody SysShiftDTO dto) {
        Long hotelId = UserContext.getHotelId();
        Long userId = UserContext.getUserId();
        return Result.success(shiftService.createShift(dto, hotelId, userId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新交班草稿")
    public Result<SysShiftVO> update(@PathVariable Long id, @RequestBody SysShiftDTO dto) {
        return Result.success(shiftService.updateShift(id, dto));
    }

    @OperationLog(module = "交班管理", action = "提交交班", targetType = "交班单")
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交交班")
    public Result<SysShiftVO> submit(@PathVariable Long id) {
        return Result.success(shiftService.submitShift(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除交班草稿")
    public Result<Void> delete(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return Result.success();
    }

    @OperationLog(module = "交班管理", action = "接收交班", targetType = "交班单")
    @PostMapping("/{id}/accept")
    @Operation(summary = "接收交班")
    public Result<SysShiftVO> accept(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        return Result.success(shiftService.acceptShift(id, userId));
    }

    @OperationLog(module = "交班管理", action = "确认交班", targetType = "交班单")
    @PostMapping("/{id}/confirm")
    @Operation(summary = "确认交班")
    public Result<SysShiftVO> confirm(@PathVariable Long id) {
        return Result.success(shiftService.confirmShift(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回交班")
    public Result<SysShiftVO> reject(@PathVariable Long id, @RequestParam String reason) {
        return Result.success(shiftService.rejectShift(id, reason));
    }

    @GetMapping
    @Operation(summary = "查询交班列表")
    public Result<List<SysShiftVO>> list(@RequestParam(required = false) String status) {
        Long hotelId = UserContext.getHotelId();
        return Result.success(shiftService.listShifts(hotelId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询交班详情")
    public Result<SysShiftVO> get(@PathVariable Long id) {
        return Result.success(shiftService.getShift(id));
    }

    @GetMapping("/pending")
    @Operation(summary = "查询待接收交班")
    public Result<List<SysShiftVO>> listPending() {
        Long hotelId = UserContext.getHotelId();
        return Result.success(shiftService.listPendingShifts(hotelId));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取当班统计")
    public Result<SysShiftVO> statistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Long hotelId = UserContext.getHotelId();
        Long userId = UserContext.getUserId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime, formatter) : null;
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime, formatter) : null;
        return Result.success(shiftService.getStatistics(hotelId, userId, start, end));
    }

    @GetMapping("/messages")
    @Operation(summary = "查询消息列表")
    public Result<List<SysShiftMessageVO>> listMessages() {
        Long userId = UserContext.getUserId();
        return Result.success(shiftService.listMessages(userId));
    }

    @PostMapping("/messages/{id}/read")
    @Operation(summary = "标记消息已读")
    public Result<Void> markMessageRead(@PathVariable Long id) {
        shiftService.markMessageRead(id);
        return Result.success();
    }

    @GetMapping("/notify-config")
    @Operation(summary = "获取通知配置")
    public Result<List<SysShiftNotifyConfigVO>> getNotifyConfig() {
        Long hotelId = UserContext.getHotelId();
        return Result.success(shiftService.getNotifyConfig(hotelId));
    }

    @PostMapping("/notify-config")
    @Operation(summary = "保存通知配置")
    public Result<Void> saveNotifyConfig(@RequestBody List<Long> userIds) {
        Long hotelId = UserContext.getHotelId();
        shiftService.saveNotifyConfig(hotelId, userIds);
        return Result.success();
    }
    /**
     * 交班核对
     * <p>
     * 核对系统统计数据与实际交接金额
     * </p>
     *
     * @param shiftId 交班记录ID
     * @param actualCash 实际现金金额
     * @param actualPos 实际POS金额
     * @param actualWechat 实际微信金额
     * @param actualAlipay 实际支付宝金额
     * @return 核对结果
     */
    @OperationLog(module = "交班管理", action = "交班核对", targetType = "交班单")
    @PostMapping("/{shiftId}/verify")
    @Operation(summary = "交班核对", description = "核对系统统计数据与实际交接金额")
    public Result<SysShiftVO> verifyShift(
            @PathVariable Long shiftId,
            @RequestParam BigDecimal actualCash,
            @RequestParam BigDecimal actualPos,
            @RequestParam BigDecimal actualWechat,
            @RequestParam BigDecimal actualAlipay) {
        return Result.success(shiftService.verifyShift(shiftId, actualCash, actualPos, actualWechat, actualAlipay));
    }

    /**
     * 保存实际交接金额
     * <p>
     * 保存实际交接金额，用于交班核对
     * </p>
     *
     * @param shiftId 交班记录ID
     * @param actualCash 实际现金金额
     * @param actualPos 实际POS金额
     * @param actualWechat 实际微信金额
     * @param actualAlipay 实际支付宝金额
     * @return 更新后的交班记录
     */
    @PostMapping("/{shiftId}/actual-amounts")
    @Operation(summary = "保存实际交接金额", description = "保存实际交接金额，用于交班核对")
    public Result<SysShiftVO> saveActualAmounts(
            @PathVariable Long shiftId,
            @RequestParam BigDecimal actualCash,
            @RequestParam BigDecimal actualPos,
            @RequestParam BigDecimal actualWechat,
            @RequestParam BigDecimal actualAlipay) {
        return Result.success(shiftService.saveActualAmounts(shiftId, actualCash, actualPos, actualWechat, actualAlipay));
    }

    /**
     * 获取交班报表
     * <p>
     * 生成交班报表，包含详细统计信息
     * </p>
     *
     * @param shiftId 交班记录ID
     * @return 交班报表数据
     */
    @GetMapping("/{shiftId}/report")
    @Operation(summary = "获取交班报表", description = "生成交班报表，包含详细统计信息")
    public Result<SysShiftVO> getShiftReport(@PathVariable Long shiftId) {
        return Result.success(shiftService.getShiftReport(shiftId));
    }
}
