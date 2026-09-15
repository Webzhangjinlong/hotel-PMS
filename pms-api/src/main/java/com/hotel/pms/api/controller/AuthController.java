package com.hotel.pms.api.controller;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.dto.CaptchaVO;
import com.hotel.pms.common.dto.LoginDTO;
import com.hotel.pms.common.dto.LoginVO;
import com.hotel.pms.common.dto.UserInfoVO;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 处理用户登录、退出、获取用户信息等认证相关请求
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、退出、获取用户信息等认证相关接口")
public class AuthController {
    
    /** 认证服务 */
    private final AuthService authService;
    
    /**
     * 用户登录
     * <p>
     * 验证用户名密码，返回JWT令牌和用户信息
     * </p>
     * 
     * @param loginDTO 登录请求参数
     * @return 登录结果，包含JWT令牌和用户信息
     */
    @OperationLog(module = "登录登出", action = "登录", targetType = "用户")

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "验证用户名密码，返回JWT令牌和用户信息")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // 【调用登录服务】
        LoginVO loginVO = authService.login(loginDTO);
        
        // 【返回登录结果】
        return Result.success(loginVO);
    }
    
    /**
     * 获取当前用户信息
     * <p>
     * 根据JWT令牌获取当前登录用户的信息
     * </p>
     * 
     * @return 用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "根据JWT令牌获取当前登录用户的信息")
    public Result<UserInfoVO> getCurrentUser() {
        // 【获取当前用户ID】从UserContext中获取
        Long userId = UserContext.getUserId();
        
        // 【查询用户信息】
        UserInfoVO userInfo = authService.getUserInfo(userId);
        
        // 【返回用户信息】
        return Result.success(userInfo);
    }
    
    /**
     * 退出登录
     * <p>
     * 退出当前登录用户
     * </p>
     * 
     * @return 操作结果
     */
    @OperationLog(module = "登录登出", action = "登出", targetType = "用户")

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "退出当前登录用户")
    public Result<Void> logout() {
        // 【获取当前用户ID】
        Long userId = UserContext.getUserId();
        
        // 【调用退出服务】
        authService.logout(userId);
        
        // 【返回成功】
        return Result.success();
    }
    
    /**
     * 获取验证码
     * <p>
     * 生成验证码图片和验证码Key
     * </p>
     * 
     * @return 验证码信息，包含验证码图片（Base64）和验证码Key
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "生成验证码图片和验证码Key")
    public Result<CaptchaVO> getCaptcha() {
        // 【获取验证码】
        CaptchaVO captchaVO = authService.getCaptcha();
        
        // 【返回验证码信息】
        return Result.success(captchaVO);
    }
}
