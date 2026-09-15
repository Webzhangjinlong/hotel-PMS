package com.hotel.pms.api.controller;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.dto.RoleCreateDTO;
import com.hotel.pms.common.dto.RoleUpdateDTO;
import com.hotel.pms.common.dto.RoleVO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.auth.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * <p>
 * 处理角色的增删改查和权限分配
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色的增删改查和权限分配")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 查询角色列表
     */
    @GetMapping
    @Operation(summary = "查询角色列表", description = "查询当前酒店的所有角色")
    public Result<List<RoleVO>> list() {
        Long hotelId = UserContext.getHotelId();
        List<RoleVO> roles = roleService.listRoles(hotelId);
        return Result.success(roles);
    }
    
    /**
     * 查询角色详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询角色详情", description = "根据ID查询角色详情")
    public Result<RoleVO> get(@PathVariable Long id) {
        RoleVO role = roleService.getRole(id);
        return Result.success(role);
    }
    
    /**
     * 创建角色
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新角色并分配权限")
    public Result<RoleVO> create(@Valid @RequestBody RoleCreateDTO dto) {
        Long hotelId = UserContext.getHotelId();
        RoleVO role = roleService.createRole(hotelId, dto);
        return Result.success(role);
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "更新角色信息和权限")
    public Result<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        RoleVO role = roleService.updateRole(id, dto);
        return Result.success(role);
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "删除角色（需确保角色下无用户）")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
    
    /**
     * 查询角色的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary = "查询角色权限", description = "查询角色的权限ID列表")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getRolePermissionIds(id);
        return Result.success(permissionIds);
    }
    
    /**
     * 分配角色权限
     */
    @PostMapping("/{id}/permissions")
    @Operation(summary = "分配角色权限", description = "为角色分配权限")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
}
