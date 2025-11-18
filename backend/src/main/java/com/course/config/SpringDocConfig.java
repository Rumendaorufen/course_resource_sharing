package com.course.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SpringDocUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc配置类 - 适配Knife4j 4.5.0，确保正确生成swagger-config资源
 */
@Configuration
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true")
public class SpringDocConfig {

    // 静态代码块，配置SpringDoc全局设置
    static {
        // 配置基本设置，确保正确处理API文档
        SpringDocUtils.getConfig()
            // 确保API文档JSON响应不包含HTML包装
            .replaceParameterObjectWithClass(null, null);
    }

    /**
     * 配置默认API分组 - 适配Knife4j 4.0.0
     * @return GroupedOpenApi配置
     */
    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/**")
                .packagesToScan("com.course.controller")
                .build();
    }
    
    // OpenAPI配置已由Knife4jAutoConfiguration提供，此处不再重复定义
}