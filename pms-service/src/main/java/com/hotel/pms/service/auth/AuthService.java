package com.hotel.pms.service.auth;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.hotel.pms.common.utils.JwtUtil;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.SysAccount;
import com.hotel.pms.dao.entity.SysRole;
import com.hotel.pms.dao.mapper.SysAccountMapper;
import com.hotel.pms.dao.mapper.HotelMapper;
import com.hotel.pms.dao.entity.Hotel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.dao.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务类
 * <p>
 * 负责用户登录、退出、验证码等认证相关业务逻辑
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    /** 系统账号Mapper */
    private final SysAccountMapper sysAccountMapper;
    private final HotelMapper hotelMapper;
    
    /** 角色Mapper */
    private final SysRoleMapper roleMapper;
    
    /** JWT工具类 */
    private final JwtUtil jwtUtil;
    
    /** 权限服务 */
    private final PermissionService permissionService;
    
    /** Redis模板 */
    private final RedisTemplate<String, Object> redisTemplate;
    
    /** BCrypt密码编码器 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /** JWT过期时间（秒） */
    @Value("${pms.auth.token-expire:86400}")
    private long tokenExpire;
    
    /** 验证码Redis前缀 */
    private static final String CAPTCHA_PREFIX = "pms:captcha:";
    
    /** 最大登录失败次数 */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    
    /** 账号锁定时间（分钟） */
    private static final int LOCK_DURATION_MINUTES = 30;
    
    /**
     * 用户登录
     * 
     * @param loginDTO 登录请求参数
     * @return 登录响应结果
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO loginDTO) {
        // ========== 参数校验 ==========
        validateCaptcha(loginDTO.getCaptcha(), loginDTO.getCaptchaKey());
        
        // ========== 查询酒店 ==========
        Hotel hotel = hotelMapper.selectOne(
            new LambdaQueryWrapper<Hotel>()
                .eq(Hotel::getHotelCode, loginDTO.getHotelCode())
        );
        if (hotel == null) {
            log.warn("登录失败：酒店标识码不存在，hotelCode={}", loginDTO.getHotelCode());
            throw new BusinessException(ResultCode.BAD_REQUEST, "酒店标识码不存在");
        }
        if (!"ACTIVE".equals(hotel.getStatus())) {
            log.warn("登录失败：酒店已停用，hotelCode={}", loginDTO.getHotelCode());
            throw new BusinessException(ResultCode.BAD_REQUEST, "酒店已停用");
        }
        
        // ========== 查询账号 ==========
        SysAccount account = sysAccountMapper.selectByHotelIdAndUsername(hotel.getId(), loginDTO.getUsername());
        if (account == null) {
            log.warn("登录失败：用户名不存在，hotelCode={}, username={}", loginDTO.getHotelCode(), loginDTO.getUsername());
            throw new BusinessException(ResultCode.AUTH_USERNAME_PASSWORD_ERROR);
        }
        
        // ========== 检查账号状态 ==========
        if ("INACTIVE".equals(account.getStatus())) {
            log.warn("登录失败：账号已停用，username={}", loginDTO.getUsername());
            throw new BusinessException(ResultCode.AUTH_ACCOUNT_DISABLED);
        }
        
        if (account.getLockTime() != null && account.getLockTime().isAfter(LocalDateTime.now())) {
            log.warn("登录失败：账号已锁定，username={}, lockTime={}", loginDTO.getUsername(), account.getLockTime());
            throw new BusinessException(ResultCode.AUTH_ACCOUNT_LOCKED);
        }
        
        // ========== 验证密码 ==========
        if (!passwordEncoder.matches(loginDTO.getPassword(), account.getPassword())) {
            handleLoginFail(account);
            log.warn("登录失败：密码错误，username={}, failCount={}", loginDTO.getUsername(), account.getLoginFailCount());
            throw new BusinessException(ResultCode.AUTH_USERNAME_PASSWORD_ERROR);
        }
        
        // ========== 登录成功处理 ==========
        account.setLoginFailCount(0);
        account.setLockTime(null);
        account.setLastLoginTime(LocalDateTime.now());
        sysAccountMapper.updateById(account);
        
        // 【查询用户角色】
        List<SysRole> roles = roleMapper.selectRolesByUserId(account.getId());
        Set<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());
        
        // 【查询用户权限】
        Set<String> permissionCodes = permissionService.getPermissionCodesByUserId(account.getId());
        
        // 【查询菜单权限】
        List<MenuVO> menus = permissionService.getMenusByUserId(account.getId());
        
        // 【生成JWT令牌】
        String token = jwtUtil.generateToken(
                account.getId(),
                account.getUsername(),
                String.join(",", roleCodes),  // 多角色用逗号分隔
                account.getHotelId()
        );
        
        // 【构建角色VO列表】
        List<RoleVO> roleVOs = roles.stream()
                .map(role -> RoleVO.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .roleCode(role.getRoleCode())
                        .build())
                .collect(Collectors.toList());
        
        // 【构建用户信息】
        UserInfoVO userInfo = UserInfoVO.builder()
                .id(account.getId())
                .username(account.getUsername())
                .realName(account.getRealName())
                .phone(account.getPhone())
                .email(account.getEmail())
                .hotelId(account.getHotelId())
                .hotelName(getHotelName(account.getHotelId()))
                .roles(roleVOs)
                .roleCodes(roleCodes)
                .permissionCodes(permissionCodes)
                .menus(menus)
                .build();
        
        // 【构建登录响应】
        LoginVO loginVO = LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(tokenExpire)
                .userInfo(userInfo)
                .build();
        
        log.info("登录成功：username={}, roles={}", account.getUsername(), roleCodes);
        
        return loginVO;
    }
    
    /**
     * 获取当前用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserInfoVO getUserInfo(Long userId) {
        SysAccount account = sysAccountMapper.selectById(userId);
        if (account == null) {
            throw new BusinessException(ResultCode.AUTH_TOKEN_INVALID);
        }
        
        // 【查询用户角色】
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        Set<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());
        
        // 【查询用户权限】
        Set<String> permissionCodes = permissionService.getPermissionCodesByUserId(userId);
        
        // 【查询菜单权限】
        List<MenuVO> menus = permissionService.getMenusByUserId(userId);
        
        // 【构建角色VO列表】
        List<RoleVO> roleVOs = roles.stream()
                .map(role -> RoleVO.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .roleCode(role.getRoleCode())
                        .build())
                .collect(Collectors.toList());
        
        return UserInfoVO.builder()
                .id(account.getId())
                .username(account.getUsername())
                .realName(account.getRealName())
                .phone(account.getPhone())
                .email(account.getEmail())
                .hotelId(account.getHotelId())
                .hotelName(getHotelName(account.getHotelId()))
                .roles(roleVOs)
                .roleCodes(roleCodes)
                .permissionCodes(permissionCodes)
                .menus(menus)
                .build();
    }
    
    /**
     * 退出登录
     */
    public void logout(Long userId) {
        log.info("用户退出登录：userId={}", userId);
    }
    
    /**
     * 获取验证码
     */
    public CaptchaVO getCaptcha() {
        // 【创建验证码】宽度120，高度40，4位数，干扰线80条
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 80);
        
        // 【生成验证码】调用无参方法自动生成
        captcha.createCode();
        
        // 【获取验证码文本】
        String code = captcha.getCode();
        
        // 【生成验证码Key】
        String key = RandomUtil.randomString(32);
        
        // 【保存到Redis】5分钟过期
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, code, 5, TimeUnit.MINUTES);
        
        // 【返回验证码信息】
        return CaptchaVO.builder()
                .key(key)
                .image(captcha.getImageBase64())
                .build();
    }
    
    private void validateCaptcha(String captcha, String captchaKey) {
        String redisCaptcha = (String) redisTemplate.opsForValue().get(CAPTCHA_PREFIX + captchaKey);
        if (redisCaptcha == null) {
            throw new BusinessException(ResultCode.AUTH_CAPTCHA_ERROR);
        }
        redisTemplate.delete(CAPTCHA_PREFIX + captchaKey);
        if (!redisCaptcha.equalsIgnoreCase(captcha)) {
            throw new BusinessException(ResultCode.AUTH_CAPTCHA_ERROR);
        }
    }
    
    private void handleLoginFail(SysAccount account) {
        int failCount = (account.getLoginFailCount() == null ? 0 : account.getLoginFailCount()) + 1;
        account.setLoginFailCount(failCount);
        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            account.setLockTime(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        sysAccountMapper.updateById(account);
    }
    
    private String getHotelName(Long hotelId) {
        // TODO: 从酒店表查询
        return "默认酒店";
    }
}