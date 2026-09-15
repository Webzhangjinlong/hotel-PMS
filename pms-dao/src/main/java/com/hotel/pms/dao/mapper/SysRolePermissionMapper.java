package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联Mapper接口
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    
    /**
     * 删除角色的所有权限关联
     * 
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}