package com.course.security;

import com.course.common.exception.ServiceException;
import com.course.entity.User;
import com.course.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    @Cacheable(value = "userDetails", key = "#username", unless = "#result == null")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        try {
            User user = userMapper.findByUsername(username);
            if (user == null) {
                log.warn("User not found with username: {}", username);
                throw new UsernameNotFoundException("用户不存在: " + username);
            }
            
            if (!user.getEnabled()) {
                log.warn("User is disabled: {}", username);
                throw new ServiceException("用户已被禁用");
            }
            
            log.debug("User found: {}", username);
            return new UserDetailsImpl(user);
            
        } catch (Exception e) {
            if (e instanceof UsernameNotFoundException || e instanceof ServiceException) {
                throw e;
            }
            log.error("Error loading user by username: {}", username, e);
            throw new ServiceException("加载用户信息时发生错误");
        }
    }
}
