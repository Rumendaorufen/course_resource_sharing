package com.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.course.common.exception.ServiceException;
import com.course.dto.LoginDTO;
import com.course.dto.UserDTO;
import com.course.entity.StudentCourse;
import com.course.entity.User;
import com.course.mapper.StudentCourseMapper;
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
    private final StudentCourseMapper studentCourseMapper;
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
        log.info("更新用户信息: ID = {}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查用户名是否已被其他用户使用
        User existUser = userMapper.findByUsername(userDTO.getUsername());
        if (existUser != null && !existUser.getId().equals(id)) {
            throw new ServiceException("用户名已存在");
        }

        // 不能修改管理员用户的角色
        if ("ADMIN".equals(user.getRole())) {
            throw new ServiceException("不能修改管理员用户的信息");
        }

        // 更新基本信息
        user.setUsername(userDTO.getUsername());
        user.setRealName(userDTO.getRealName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setClassname(userDTO.getClassname());
        
        // 如果提供了角色信息且与当前角色不同，则更新角色
        if (userDTO.getRole() != null && !userDTO.getRole().equals(user.getRole())) {
            user.setRole(userDTO.getRole());
            // 如果切换到非学生角色，清空班级信息
            if (!"STUDENT".equals(userDTO.getRole())) {
                user.setClassname(null);
            }
        }
        
        // 如果提供了密码且不为空，则更新密码
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            // 手动验证密码长度
            if (userDTO.getPassword().length() < 6 || userDTO.getPassword().length() > 20) {
                throw new ServiceException("密码长度必须在6-20个字符之间");
            }
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        // 学生用户需要验证班级信息
        if ("STUDENT".equals(user.getRole()) && (user.getClassname() == null || user.getClassname().trim().isEmpty())) {
            throw new ServiceException("学生用户必须填写班级信息");
        }

        userMapper.updateById(user);
        log.info("用户信息更新成功: {} (ID = {})", user.getUsername(), id);
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
    
    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }
    
    @Override
    @Transactional
    public void addUser(UserDTO userDTO) {
        log.info("管理员添加新用户: {}", userDTO.getUsername());
        User existUser = userMapper.findByUsername(userDTO.getUsername());
        if (existUser != null) {
            throw new ServiceException("用户名已存在");
        }

        // 学生用户需要验证班级信息
        if ("STUDENT".equals(userDTO.getRole()) && (userDTO.getClassname() == null || userDTO.getClassname().trim().isEmpty())) {
            throw new ServiceException("学生用户必须填写班级信息");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(userDTO.getRole());
        user.setRealName(userDTO.getRealName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setClassname(userDTO.getClassname());
        user.setEnabled(true);

        userMapper.insert(user);
        log.info("用户添加成功: {}", userDTO.getUsername());
    }
    

    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("管理员删除用户: ID = {}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 不能删除管理员用户
        if ("ADMIN".equals(user.getRole())) {
            throw new ServiceException("不能删除管理员用户");
        }
        
        try {
            // 处理学生用户的课程关联
            if ("STUDENT".equals(user.getRole())) {
                // 删除学生课程关联记录
                studentCourseMapper.delete(
                    new QueryWrapper<StudentCourse>().eq("student_id", id)
                );
                log.info("已删除学生 {} (ID = {}) 的课程关联记录", user.getUsername(), id);
            }
            
            // 最后删除用户
            userMapper.deleteById(id);
            log.info("用户删除成功: {} (ID = {})", user.getUsername(), id);
        } catch (Exception e) {
            log.error("删除用户失败: ID = {}, 错误信息: {}", id, e.getMessage());
            throw new ServiceException("该用户存在其他关联数据，无法直接删除。请先清除相关关联后重试。");
        }
    }
}
