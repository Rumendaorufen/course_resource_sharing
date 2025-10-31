package com.course.controller;

import com.course.common.api.ApiResult;
import com.course.dto.LoginDTO;
import com.course.dto.UserDTO;
import com.course.entity.User;
import com.course.service.UserService;
import com.course.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户登录、注册等认证相关接口")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并获取认证token")
    public ApiResult<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        log.info("用户登录请求: {}", loginDTO.getUsername());
        try {
            String token = userService.login(loginDTO);
            User user = userService.findByUsername(loginDTO.getUsername());
            
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", userVO);
            result.put("token", token);
            
            log.info("用户登录成功: {}", loginDTO.getUsername());
            return ApiResult.success(result);
        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", loginDTO.getUsername(), e.getMessage());
            return ApiResult.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public ApiResult<Void> register(@Validated @RequestBody UserDTO userDTO) {
        log.info("用户注册请求: {}", userDTO.getUsername());
        try {
            userService.register(userDTO);
            log.info("用户注册成功: {}", userDTO.getUsername());
            return ApiResult.success();
        } catch (Exception e) {
            log.error("用户注册失败: {}, 错误: {}", userDTO.getUsername(), e.getMessage());
            return ApiResult.error(e.getMessage());
        }
    }

    @GetMapping("/current-user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ApiResult<UserVO> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ApiResult.error("用户未登录");
        }
        
        log.info("获取当前用户信息: {}", user.getUsername());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ApiResult.success(userVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "清除用户登录状态")
    public ApiResult<Void> logout() {
        SecurityContextHolder.clearContext();
        return ApiResult.success();
    }
}
