package com.hotel.pms.api.controller;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.dto.MemberLevelDTO;
import com.hotel.pms.common.dto.MemberLevelVO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.member.MemberLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员等级管理控制器
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/member-levels")
@RequiredArgsConstructor
@Tag(name = "会员等级管理", description = "会员等级配置的增删改查")
public class MemberLevelController {

    private final MemberLevelService memberLevelService;

    @GetMapping
    @Operation(summary = "查询等级列表")
    public Result<List<MemberLevelVO>> list() {
        Long hotelId = UserContext.getHotelId();
        return Result.success(memberLevelService.getAll(hotelId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新等级配置")
    public Result<MemberLevelVO> update(@PathVariable Long id, @Valid @RequestBody MemberLevelDTO dto) {
        return Result.success(memberLevelService.update(id, dto));
    }

    @PostMapping("/init")
    @Operation(summary = "初始化默认等级")
    public Result<Void> init() {
        Long hotelId = UserContext.getHotelId();
        memberLevelService.initDefaultLevels(hotelId);
        return Result.success();
    }
}
