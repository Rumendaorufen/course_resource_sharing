package com.course.service.impl;

import com.course.common.exception.ServiceException;
import com.course.dto.LoginDTO;
import com.course.dto.UserDTO;
import com.course.entity.User;
import com.course.mapper.UserMapper;
import com.course.security.JwtTokenUtil;
import com.course.security.UserDetailsImpl;
import com.course.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public String login(LoginDTO loginDTO) {
        log.info("用户尝试登录: {}", loginDTO.getUsername());
        try {
            // 使用 Spring Security 的认证管理器进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
                )
            );

            // 认证成功后，获取用户信息
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // 生成 JWT token
            String token = jwtTokenUtil.generateToken(userDetails);
            log.info("用户登录成功: {}", loginDTO.getUsername());
            return token;
        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", loginDTO.getUsername(), e.getMessage());
            throw new ServiceException("用户名或密码错误");
        }
    }

    @Override
    @Transactional
    public void register(UserDTO userDTO) {
        log.info("新用户注册: {}", userDTO.getUsername());
        User existUser = userMapper.findByUsername(userDTO.getUsername());
        if (existUser != null) {
            throw new ServiceException("用户名已存在");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword())); // 使用 PasswordEncoder 加密密码
        user.setRole(userDTO.getRole());
        user.setRealName(userDTO.getRealName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);

        userMapper.insert(user);
        log.info("用户注册成功: {}", userDTO.getUsername());
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserDTO userDTO) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        User existUser = userMapper.findByUsername(userDTO.getUsername());
        if (existUser != null && !existUser.getId().equals(id)) {
            throw new ServiceException("用户名已存在");
        }

        user.setUsername(userDTO.getUsername());
        user.setRealName(userDTO.getRealName());
        user.setEmail(userDTO.getEmail());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ServiceException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword)); 
        userMapper.updateById(user);
    }

    @Override
    public List<User> getAllTeachers() {
        return userMapper.findByRole("TEACHER");
    }
}
