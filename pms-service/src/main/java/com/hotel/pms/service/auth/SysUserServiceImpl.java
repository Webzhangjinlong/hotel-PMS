package com.hotel.pms.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.dto.SysAccountDTO;
import com.hotel.pms.common.dto.SysAccountVO;
import com.hotel.pms.dao.entity.SysAccount;
import com.hotel.pms.dao.entity.SysUserRole;
import com.hotel.pms.dao.mapper.SysAccountMapper;
import com.hotel.pms.dao.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统账号管理服务实现（用户管理）
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysAccountMapper accountMapper;
    private final SysUserRoleMapper userRoleMapper;

    /** 密码编码器（BCrypt） */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Override
    public List<SysAccountVO> list(Long hotelId) {
        // 【步骤1】按酒店过滤，按创建时间倒序
        LambdaQueryWrapper<SysAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAccount::getHotelId, hotelId)
               .orderByDesc(SysAccount::getCreatedAt);
        // 【步骤2】查询并转换为 VO（去除密码）
        return accountMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public SysAccountVO get(Long id) {
        // 【步骤1】按ID查询
        SysAccount account = accountMapper.selectById(id);
        // 【步骤2】转换为 VO
        return account == null ? null : toVO(account);
    }

    @Override
    public SysAccountVO create(Long hotelId, SysAccountDTO dto) {
        // 【步骤1】组装实体：酒店ID、状态、密码加密
        SysAccount account = new SysAccount();
        BeanUtils.copyProperties(dto, account);
        account.setHotelId(hotelId);
        account.setStatus("ACTIVE");
        account.setLoginFailCount(0);
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            account.setPassword(ENCODER.encode(dto.getPassword()));
        }
        // 【步骤2】插入
        accountMapper.insert(account);
        // 【步骤3】返回 VO
        return toVO(account);
    }

    @Override
    public SysAccountVO update(Long id, SysAccountDTO dto) {
        // 【步骤1】组装实体（忽略密码，密码走重置接口）
        SysAccount account = new SysAccount();
        BeanUtils.copyProperties(dto, account);
        account.setId(id);
        account.setPassword(null);
        // 【步骤2】更新
        accountMapper.updateById(account);
        // 【步骤3】返回更新后的用户
        return get(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 【步骤1】删除用户角色关联
        userRoleMapper.deleteByUserId(id);
        // 【步骤2】删除账号
        accountMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        // 【步骤1】组装实体，BCrypt 加密新密码
        SysAccount account = new SysAccount();
        account.setId(id);
        account.setPassword(ENCODER.encode(newPassword));
        // 【步骤2】更新密码
        accountMapper.updateById(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 【步骤1】清空原有角色
        userRoleMapper.deleteByUserId(userId);
        // 【步骤2】插入新角色
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public List<Long> getUserRoles(Long userId) {
        // 【步骤1】按用户查询角色关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        // 【步骤2】提取角色ID列表
        return userRoleMapper.selectList(wrapper).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    /**
     * 实体转 VO（排除密码等敏感字段）
     */
    private SysAccountVO toVO(SysAccount account) {
        SysAccountVO vo = new SysAccountVO();
        BeanUtils.copyProperties(account, vo);
        return vo;
    }
}
