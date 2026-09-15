package com.hotel.pms.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.dto.RoleCreateDTO;
import com.hotel.pms.common.dto.RoleUpdateDTO;
import com.hotel.pms.common.dto.RoleVO;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.SysRole;
import com.hotel.pms.dao.entity.SysRolePermission;
import com.hotel.pms.dao.entity.SysUserRole;
import com.hotel.pms.dao.mapper.SysRoleMapper;
import com.hotel.pms.dao.mapper.SysRolePermissionMapper;
import com.hotel.pms.dao.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务类
 * <p>
 * 负责角色的增删改查和权限分配
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    
    /**
     * 查询角色列表
     * 
     * @param hotelId 酒店ID
     * @return 角色列表
     */
    public List<RoleVO> listRoles(Long hotelId) {
        // 【查询角色列表】
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getHotelId, hotelId)
               .eq(SysRole::getDeleted, false)
               .orderByAsc(SysRole::getSortOrder);
        
        List<SysRole> roles = roleMapper.selectList(wrapper);
        
        // 【转换为VO】
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询角色详情
     * 
     * @param roleId 角色ID
     * @return 角色详情
     */
    public RoleVO getRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return convertToVO(role);
    }
    
    /**
     * 创建角色
     * 
     * @param hotelId 酒店ID
     * @param dto 创建参数
     * @return 角色信息
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(Long hotelId, RoleCreateDTO dto) {
        // 【检查角色编码是否重复】
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getHotelId, hotelId)
               .eq(SysRole::getRoleCode, dto.getRoleCode())
               .eq(SysRole::getDeleted, false);
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色编码已存在");
        }
        
        // 【创建角色】
        SysRole role = new SysRole();
        role.setHotelId(hotelId);
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        role.setStatus("ACTIVE");
        roleMapper.insert(role);
        
        // 【分配权限】
        if (!CollectionUtils.isEmpty(dto.getPermissionIds())) {
            assignPermissions(role.getId(), dto.getPermissionIds());
        }
        
        log.info("创建角色成功：roleId={}, roleCode={}", role.getId(), role.getRoleCode());
        
        return convertToVO(role);
    }
    
    /**
     * 更新角色
     * 
     * @param roleId 角色ID
     * @param dto 更新参数
     * @return 角色信息
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(Long roleId, RoleUpdateDTO dto) {
        // 【查询角色】
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        
        // 【更新角色信息】
        if (dto.getRoleName() != null) {
            role.setRoleName(dto.getRoleName());
        }
        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }
        if (dto.getSortOrder() != null) {
            role.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        roleMapper.updateById(role);
        
        // 【更新权限】
        if (dto.getPermissionIds() != null) {
            assignPermissions(roleId, dto.getPermissionIds());
        }
        
        log.info("更新角色成功：roleId={}", roleId);
        
        return convertToVO(role);
    }
    
    /**
     * 删除角色
     * 
     * @param roleId 角色ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        // 【检查是否有用户使用此角色】
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, roleId);
        if (userRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该角色下还有用户，无法删除");
        }
        
        // 【删除角色权限关联】
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 【删除角色】
        roleMapper.deleteById(roleId);
        
        log.info("删除角色成功：roleId={}", roleId);
    }
    
    /**
     * 分配权限
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 【删除原有权限关联】
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 【添加新的权限关联】
        if (!CollectionUtils.isEmpty(permissionIds)) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }
        
        log.info("分配权限成功：roleId={}, permissionCount={}", roleId, permissionIds.size());
    }
    
    /**
     * 查询角色的权限ID列表
     * 
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    public List<Long> getRolePermissionIds(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        return rolePermissionMapper.selectList(wrapper)
                .stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为VO
     * 
     * @param role 角色实体
     * @return 角色VO
     */
    private RoleVO convertToVO(SysRole role) {
        // 【查询权限数量】
        LambdaQueryWrapper<SysRolePermission> permissionWrapper = new LambdaQueryWrapper<>();
        permissionWrapper.eq(SysRolePermission::getRoleId, role.getId());
        int permissionCount = Math.toIntExact(rolePermissionMapper.selectCount(permissionWrapper));
        
        // 【查询用户数量】
        LambdaQueryWrapper<SysUserRole> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUserRole::getRoleId, role.getId());
        int userCount = Math.toIntExact(userRoleMapper.selectCount(userWrapper));
        
        return RoleVO.builder()
                .id(role.getId())
                .hotelId(role.getHotelId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .description(role.getDescription())
                .sortOrder(role.getSortOrder())
                .status(role.getStatus())
                .permissionCount(permissionCount)
                .userCount(userCount)
                .createdAt(role.getCreatedAt())
                .build();
    }
}