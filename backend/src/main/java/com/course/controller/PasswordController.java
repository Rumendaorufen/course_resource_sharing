package com.course.controller;

import com.course.common.api.ApiResult;
import com.course.entity.User;
import com.course.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/encrypt-all")
    public ApiResult<String> encryptAllPasswords() {
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            // 假设当前密码是明文
            String rawPassword = user.getPassword();
            // 加密密码
            String encodedPassword = passwordEncoder.encode(rawPassword);
            // 更新用户密码
            user.setPassword(encodedPassword);
            userMapper.updateById(user);
        }
        return ApiResult.success("所有用户密码已加密");
    }
}
