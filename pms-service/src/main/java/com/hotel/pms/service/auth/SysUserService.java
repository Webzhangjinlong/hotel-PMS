package com.hotel.pms.service.auth;

import com.hotel.pms.common.dto.SysAccountDTO;
import com.hotel.pms.common.dto.SysAccountVO;

import java.util.List;

/**
 * 系统账号管理服务（用户管理）
 */
public interface SysUserService {

    /**
     * 查询当前酒店的用户列表
     *
     * @param hotelId 酒店ID
     * @return 用户列表（不含密码）
     */
    List<SysAccountVO> list(Long hotelId);

    /**
     * 查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情（不含密码）
     */
    SysAccountVO get(Long id);

    /**
     * 创建用户
     *
     * @param hotelId 酒店ID
     * @param dto     用户信息
     * @return 创建后的用户
     */
    SysAccountVO create(Long hotelId, SysAccountDTO dto);

    /**
     * 更新用户（不更新密码）
     *
     * @param id  用户ID
     * @param dto 用户信息
     * @return 更新后的用户
     */
    SysAccountVO update(Long id, SysAccountDTO dto);

    /**
     * 删除用户（同时删除其角色关联）
     *
     * @param id 用户ID
     */
    void delete(Long id);

    /**
     * 重置用户密码
     *
     * @param id          用户ID
     * @param newPassword 新密码（BCrypt 加密存储）
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 为用户分配角色（先清后插，事务）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 查询用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoles(Long userId);
}
