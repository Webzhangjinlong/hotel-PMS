package com.hotel.pms.api.controller;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.common.dto.SysAccountDTO;
import com.hotel.pms.common.dto.SysAccountVO;
import com.hotel.pms.common.dto.UserRoleDTO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.auth.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理")
public class UserController {

    private final SysUserService sysUserService;

    @GetMapping
    @Operation(summary = "查询用户列表", description = "查询当前酒店的所有用户")
    public Result<List<SysAccountVO>> list() {
        Long hotelId = UserContext.getHotelId();
        return Result.success(sysUserService.list(hotelId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情", description = "根据ID查询用户详情")
    public Result<SysAccountVO> get(@PathVariable Long id) {
        return Result.success(sysUserService.get(id));
    }

    @OperationLog(module = "用户管理", action = "创建用户", targetType = "用户")

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新员工账号")
    public Result<SysAccountVO> create(@Valid @RequestBody SysAccountDTO dto) {
        Long hotelId = UserContext.getHotelId();
        return Result.success(sysUserService.create(hotelId, dto));
    }

    @OperationLog(module = "用户管理", action = "修改用户", targetType = "用户", targetIdParam = "id")

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Result<SysAccountVO> update(@PathVariable Long id, @Valid @RequestBody SysAccountDTO dto) {
        return Result.success(sysUserService.update(id, dto));
    }

    @OperationLog(module = "用户管理", action = "删除用户", targetType = "用户", targetIdParam = "id")

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除员工账号")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.success();
    }

    @OperationLog(module = "用户管理", action = "重置密码", targetType = "用户", targetIdParam = "id")

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置密码", description = "重置用户密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        sysUserService.resetPassword(id, newPassword);
        return Result.success();
    }

    @OperationLog(module = "用户管理", action = "分配角色", targetType = "用户")

    @PostMapping("/roles")
    @Operation(summary = "分配用户角色", description = "为用户分配角色")
    public Result<Void> assignRoles(@Valid @RequestBody UserRoleDTO dto) {
        sysUserService.assignRoles(dto.getUserId(), dto.getRoleIds());
        return Result.success();
    }

    @GetMapping("/{userId}/roles")
    @Operation(summary = "查询用户角色", description = "查询用户的角色ID列表")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        return Result.success(sysUserService.getUserRoles(userId));
    }
}
