package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.UserRoleDTO;
import com.hotel.pms.dao.entity.SysAccount;
import com.hotel.pms.dao.mapper.SysAccountMapper;
import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.dao.entity.SysUserRole;
import com.hotel.pms.dao.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理")
public class UserController {
    
    private final SysUserRoleMapper userRoleMapper;
    private final SysAccountMapper accountMapper;
    
    @GetMapping
    @Operation(summary = "查询用户列表", description = "查询当前酒店的所有用户")
    public Result<List<SysAccount>> list() {
        Long hotelId = UserContext.getHotelId();
        LambdaQueryWrapper<SysAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAccount::getHotelId, hotelId)
               .orderByDesc(SysAccount::getCreatedAt);
        List<SysAccount> users = accountMapper.selectList(wrapper);
        return Result.success(users);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情", description = "根据ID查询用户详情")
    public Result<SysAccount> get(@PathVariable Long id) {
        SysAccount user = accountMapper.selectById(id);
        return Result.success(user);
    }
    
    @OperationLog(module = "用户管理", action = "创建用户", targetType = "用户")

    
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新员工账号")
    public Result<SysAccount> create(@RequestBody SysAccount account) {
        Long hotelId = UserContext.getHotelId();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        account.setHotelId(hotelId);
        account.setStatus("ACTIVE");
        account.setLoginFailCount(0);
        if (account.getPassword() != null && !account.getPassword().isEmpty()) {
            account.setPassword(encoder.encode(account.getPassword()));
        }
        accountMapper.insert(account);
        return Result.success(account);
    }
    
    @OperationLog(module = "用户管理", action = "修改用户", targetType = "用户", targetIdParam = "id")

    
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Result<SysAccount> update(@PathVariable Long id, @RequestBody SysAccount account) {
        account.setId(id);
        account.setPassword(null);
        accountMapper.updateById(account);
        return Result.success(account);
    }
    
    @OperationLog(module = "用户管理", action = "删除用户", targetType = "用户", targetIdParam = "id")

    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除员工账号")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        userRoleMapper.deleteByUserId(id);
        accountMapper.deleteById(id);
        return Result.success();
    }
    
    @OperationLog(module = "用户管理", action = "重置密码", targetType = "用户", targetIdParam = "id")

    
    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置密码", description = "重置用户密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        SysAccount account = new SysAccount();
        account.setId(id);
        account.setPassword(encoder.encode(newPassword));
        accountMapper.updateById(account);
        return Result.success();
    }
    
    @OperationLog(module = "用户管理", action = "分配角色", targetType = "用户")

    
    @PostMapping("/roles")
    @Operation(summary = "分配用户角色", description = "为用户分配角色")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignRoles(@Valid @RequestBody UserRoleDTO dto) {
        userRoleMapper.deleteByUserId(dto.getUserId());
        for (Long roleId : dto.getRoleIds()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(dto.getUserId());
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        return Result.success();
    }
    
    @GetMapping("/{userId}/roles")
    @Operation(summary = "查询用户角色", description = "查询用户的角色ID列表")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<Long> roleIds = userRoleMapper.selectList(wrapper)
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        return Result.success(roleIds);
    }
}


