package com.hotel.pms.api.controller;

import com.hotel.pms.common.result.Result;
import com.hotel.pms.dao.entity.SysPermission;
import com.hotel.pms.service.auth.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 * <p>
 * 处理权限的增删改查
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限的增删改查")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * 查询所有权限列表
     */
    @GetMapping
    @Operation(summary = "查询所有权限", description = "查询所有权限列表，用于角色权限分配")
    public Result<List<SysPermission>> list() {
        List<SysPermission> permissions = permissionService.listAllPermissions();
        return Result.success(permissions);
    }
    
    /**
     * 查询权限详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询权限详情", description = "根据ID查询权限详情")
    public Result<SysPermission> get(@PathVariable Long id) {
        SysPermission permission = permissionService.getPermission(id);
        return Result.success(permission);
    }
    
    /**
     * 创建权限
     */
    @PostMapping
    @Operation(summary = "创建权限", description = "创建新权限")
    public Result<SysPermission> create(@RequestBody SysPermission permission) {
        SysPermission result = permissionService.createPermission(permission);
        return Result.success(result);
    }
    
    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新权限", description = "更新权限信息")
    public Result<SysPermission> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permission.setId(id);
        SysPermission result = permissionService.updatePermission(permission);
        return Result.success(result);
    }
    
    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "删除权限")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success();
    }
}
