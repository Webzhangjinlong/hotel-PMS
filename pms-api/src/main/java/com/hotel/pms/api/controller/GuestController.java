package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.GuestQueryDTO;
import com.hotel.pms.common.dto.GuestVO;
import com.hotel.pms.common.dto.StayVO;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.guest.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客人档案管理控制器
 * <p>
 * 处理客人档案管理相关的请求，包括客人信息查询、VIP设置、黑名单管理等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
@Tag(name = "客人档案管理", description = "客人档案的增删改查接口")
public class GuestController {

    /** 客人服务 */
    private final GuestService guestService;

    /**
     * 分页查询客人列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询客人列表", description = "根据条件分页查询客人列表")
    public Result<PageResponse<GuestVO>> pageList(GuestQueryDTO queryDTO) {
        // 【调用服务查询】
        PageResponse<GuestVO> result = guestService.getGuestPage(queryDTO);
        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 根据ID查询客人详情
     * 
     * @param id 客人ID
     * @return 客人信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询客人详情", description = "根据客人ID查询客人详细信息")
    public Result<GuestVO> getById(@Parameter(description = "客人ID") @PathVariable Long id) {
        // 【调用服务查询】
        GuestVO result = guestService.getGuestById(id);
        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 设置客人VIP状态
     * 
     * @param id 客人ID
     * @param isVip VIP状态
     * @return 更新后的客人信息
     */
    @PutMapping("/{id}/vip")
    @Operation(summary = "设置客人VIP状态", description = "设置客人是否为VIP")
    public Result<GuestVO> setVipStatus(
            @Parameter(description = "客人ID") @PathVariable Long id,
            @Parameter(description = "VIP状态") @RequestParam Boolean isVip) {
        // 【调用服务设置VIP状态】
        GuestVO result = guestService.setVipStatus(id, isVip);
        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 将客人加入黑名单
     * 
     * @param id 客人ID
     * @param reason 黑名单原因
     * @return 更新后的客人信息
     */
    @PutMapping("/{id}/blacklist/add")
    @Operation(summary = "将客人加入黑名单", description = "将客人加入黑名单并设置原因")
    public Result<GuestVO> addToBlacklist(
            @Parameter(description = "客人ID") @PathVariable Long id,
            @Parameter(description = "黑名单原因") @RequestParam String reason) {
        // 【调用服务加入黑名单】
        GuestVO result = guestService.addToBlacklist(id, reason);
        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 将客人从黑名单移出
     * 
     * @param id 客人ID
     * @return 更新后的客人信息
     */
    @PutMapping("/{id}/blacklist/remove")
    @Operation(summary = "将客人从黑名单移出", description = "将客人从黑名单移出")
    public Result<GuestVO> removeFromBlacklist(
            @Parameter(description = "客人ID") @PathVariable Long id) {
        // 【调用服务移出黑名单】
        GuestVO result = guestService.removeFromBlacklist(id);
        // 【返回结果】
        return Result.success(result);
    }

    /**
     * 获取客人入住历史
     * 
     * @param guestId 客人ID
     * @return 入住历史列表
     */
    @GetMapping("/{guestId}/history")
    @Operation(summary = "获取客人入住历史", description = "获取客人的入住历史记录")
    public Result<List<StayVO>> getStayHistory(
            @Parameter(description = "客人ID") @PathVariable Long guestId) {
        // 【调用服务查询入住历史】
        List<StayVO> result = guestService.getStayHistory(guestId);
        // 【返回结果】
        return Result.success(result);
    }
}
