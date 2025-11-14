package com.course.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    private String username;

    // 密码可以为空，表示不修改密码
    // 使用自定义验证器，只有在密码不为空时才验证长度
    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    
    // 班级名称，仅学生用户需要
    private String classname;
}
