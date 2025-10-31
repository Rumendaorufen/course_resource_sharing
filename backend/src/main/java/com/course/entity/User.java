package com.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    private String username;
    
    private String password;
    
    private String role = "USER";
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    private String avatar;
    
    private Boolean enabled = true;
    
    private LocalDateTime lastLoginTime;
}
