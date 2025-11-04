package com.course.service;

import com.course.dto.LoginDTO;
import com.course.dto.UserDTO;
import com.course.entity.User;

import java.util.List;

public interface UserService {
    /**
     * 用户登录
     */
    String login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    void register(UserDTO userDTO);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据ID查找用户
     */
    User getById(Long id);

    /**
     * 更新用户信息
     */
    void updateUser(Long id, UserDTO userDTO);

    /**
     * 修改密码
     */
    void updatePassword(Long id, String oldPassword, String newPassword);

    /**
     * 获取所有教师
     */
    List<User> getAllTeachers();

    
    /**
     * 获取所有用户
     */
    List<User> getAllUsers();
    
    /**
     * 新增用户（管理员使用）
     */
    void addUser(UserDTO userDTO);
    
    /**
     * 删除用户（管理员使用）
     */
    void deleteUser(Long id);
}
