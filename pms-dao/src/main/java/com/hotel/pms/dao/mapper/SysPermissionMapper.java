package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    
    /**
     * 根据用户ID查询权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = FALSE AND p.status = 'ACTIVE'")
    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = FALSE AND p.status = 'ACTIVE'")
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);
}