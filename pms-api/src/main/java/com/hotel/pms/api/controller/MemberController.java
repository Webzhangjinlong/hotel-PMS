package com.hotel.pms.api.controller;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员管理控制器
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "会员管理", description = "会员的增删改查、积分管理")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @Operation(summary = "分页查询会员列表")
    public Result<PageResponse<MemberVO>> pageList(MemberQueryDTO queryDTO) {
        return Result.success(memberService.pageList(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询会员详情")
    public Result<MemberVO> getById(@PathVariable Long id) {
        return Result.success(memberService.getById(id));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "根据手机号查询会员")
    public Result<MemberVO> getByPhone(@PathVariable String phone) {
        Long hotelId = UserContext.getHotelId();
        var member = memberService.getByPhone(hotelId, phone);
        if (member == null) {
            return Result.success(null);
        }
        return Result.success(memberService.getById(member.getId()));
    }

    @OperationLog(module = "会员管理", action = "注册会员", targetType = "会员")
    @PostMapping
    @Operation(summary = "手动注册会员")
    public Result<MemberVO> register(@Valid @RequestBody MemberCreateDTO dto) {
        Long hotelId = UserContext.getHotelId();
        return Result.success(memberService.register(dto, hotelId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新会员信息")
    public Result<MemberVO> update(@PathVariable Long id, @RequestBody MemberCreateDTO dto) {
        return Result.success(memberService.update(id, dto));
    }

    @OperationLog(module = "会员管理", action = "积分兑换", targetType = "会员")
    @PostMapping("/{id}/exchange-points")
    @Operation(summary = "积分抵扣")
    public Result<BigDecimal> exchangePoints(@PathVariable Long id, @Valid @RequestBody MemberPointsExchangeDTO dto) {
        BigDecimal amount = memberService.exchangePoints(id, dto.getPoints(), dto.getStayId());
        return Result.success(amount);
    }

    @GetMapping("/{id}/points-logs")
    @Operation(summary = "查询积分流水")
    public Result<List<MemberPointsLogVO>> getPointsLogs(@PathVariable Long id) {
        return Result.success(memberService.getPointsLogs(id));
    }

    @GetMapping("/statistics")
    @Operation(summary = "会员统计")
    public Result<MemberStatisticsVO> getStatistics() {
        Long hotelId = UserContext.getHotelId();
        return Result.success(memberService.getStatistics(hotelId));
    }

    @OperationLog(module = "会员管理", action = "手动调整积分", targetType = "会员")
    @PostMapping("/{id}/adjust-points")
    @Operation(summary = "手动调整积分")
    public Result<Void> adjustPoints(
            @PathVariable Long id,
            @RequestParam Integer points,
            @RequestParam(required = false) String description) {
        Long operatorId = UserContext.getUserId();
        memberService.adjustPoints(id, points, description, operatorId);
        return Result.success();
    }
}
