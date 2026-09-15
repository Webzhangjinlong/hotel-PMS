package com.hotel.pms.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.dto.MenuVO;
import com.hotel.pms.common.dto.SysPermissionDTO;
import com.hotel.pms.common.dto.SysPermissionVO;
import com.hotel.pms.dao.entity.SysPermission;
import com.hotel.pms.dao.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务类
 * <p>
 * 负责权限查询和菜单构建
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final SysPermissionMapper permissionMapper;
    /**
     * 查询权限详情
     * 
     * @param id 权限ID
     * @return 权限详情
     */
    public SysPermissionVO getPermission(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        return permission == null ? null : toVO(permission);
    }
    
    /**
     * 创建权限
     * 
     * @param dto 权限信息
     * @return 权限信息
     */
    public SysPermissionVO createPermission(SysPermissionDTO dto) {
        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(dto, permission);
        permissionMapper.insert(permission);
        log.info("创建权限成功：permissionId={}, permissionCode={}", permission.getId(), permission.getPermissionCode());
        return toVO(permission);
    }
    
    /**
     * 更新权限
     * 
     * @param id  权限ID
     * @param dto 权限信息
     * @return 权限信息
     */
    public SysPermissionVO updatePermission(Long id, SysPermissionDTO dto) {
        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(dto, permission);
        permission.setId(id);
        permissionMapper.updateById(permission);
        log.info("更新权限成功：permissionId={}", id);
        return toVO(permission);
    }
    
    /**
     * 删除权限
     * 
     * @param id 权限ID
     */
    public void deletePermission(Long id) {
        permissionMapper.deleteById(id);
        log.info("删除权限成功：permissionId={}", id);
    }
    
    /**
     * 查询所有权限列表
     * 
     * @return 权限列表
     */
    public List<SysPermissionVO> listAllPermissions() {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getDeleted, false)
               .eq(SysPermission::getStatus, "ACTIVE")
               .orderByAsc(SysPermission::getSortOrder);
        return permissionMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据用户ID查询权限编码集合
     * 
     * @param userId 用户ID
     * @return 权限编码集合
     */
    public Set<String> getPermissionCodesByUserId(Long userId) {
        List<SysPermission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        return permissions.stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toSet());
    }
    
    /**
     * 根据用户ID查询菜单列表（树形结构）
     * 
     * @param userId 用户ID
     * @return 菜单树
     */
    public List<MenuVO> getMenusByUserId(Long userId) {
        // 【查询用户的菜单权限】
        List<SysPermission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        
        // 【过滤菜单类型】
        List<SysPermission> menuPermissions = permissions.stream()
                .filter(p -> "MENU".equals(p.getResourceType()))
                .collect(Collectors.toList());
        
        // 【构建菜单树】
        return buildMenuTree(menuPermissions, null);
    }
    
    /**
     * 权限实体转 VO
     */
    private SysPermissionVO toVO(SysPermission permission) {
        SysPermissionVO vo = new SysPermissionVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }

    /**
     * 构建菜单树
     * 
     * @param permissions 权限列表
     * @param parentId 父ID
     * @return 菜单树
     */
    private List<MenuVO> buildMenuTree(List<SysPermission> permissions, Long parentId) {
        List<MenuVO> menus = new ArrayList<>();
        
        for (SysPermission permission : permissions) {
            // 【判断是否为当前层级】
            if ((parentId == null && permission.getParentId() == null) ||
                (parentId != null && parentId.equals(permission.getParentId()))) {
                
                // 【构建菜单VO】
                MenuVO menu = MenuVO.builder()
                        .id(permission.getId())
                        .name(permission.getPermissionName())
                        .code(permission.getPermissionCode())
                        .path(permission.getResourcePath())
                        .icon(permission.getIcon())
                        .sortOrder(permission.getSortOrder())
                        .build();
                
                // 【递归构建子菜单】
                List<MenuVO> children = buildMenuTree(permissions, permission.getId());
                if (!children.isEmpty()) {
                    menu.setChildren(children);
                }
                
                menus.add(menu);
            }
        }
        
        return menus;
    }
}
