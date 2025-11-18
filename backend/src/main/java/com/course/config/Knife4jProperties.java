package com.course.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Knife4j配置属性类
 * 用于读取knife4j相关的配置信息
 */
@Data
@Configuration
@ConfigurationProperties("knife4j")
public class Knife4jProperties {

    /**
     * 标题
     **/
    private String title = "课程资源共享平台 API文档";

    /**
     * 网关
     */
    private String gateway;

    /**
     * 获取token的URL
     */
    private String tokenUrl = "/api/auth/login";

    /**
     * 作用域
     */
    private String scope = "read,write";

    /**
     * OAuth2.0客户端ID
     */
    private String clientId = "web-client";
    
    /**
     * OAuth2.0客户端密钥
     */
    private String clientSecret = "web-client-secret";

    private Map<String, String> services;

    /**
     * 作者
     **/
    private String author = "开发团队";

    /**
     * 描述
     **/
    private String description = "提供课程资源共享平台的所有API接口说明";

    /**
     * 版本
     **/
    private String version = "1.0.0";
}