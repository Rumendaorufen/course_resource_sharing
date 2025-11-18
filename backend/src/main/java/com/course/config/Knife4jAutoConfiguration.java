package com.course.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j自动配置类
 * 配置基本的OpenAPI信息，不包含认证设置
 */
@Configuration
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true")
public class Knife4jAutoConfiguration {

    @Autowired
    private Knife4jProperties knife4jProperties;

    /**
     * 配置OpenAPI，包含Bearer Token认证设置
     * @return OpenAPI配置
     */
    @Bean
    public OpenAPI springOpenAPI() {
        // 基本配置，不包含认证设置
        return new OpenAPI()
                .info(new Info()
                        .title(knife4jProperties.getTitle())
                        .description(knife4jProperties.getDescription())
                        .version(knife4jProperties.getVersion())
                        .contact(new Contact().name(knife4jProperties.getAuthor())));
    }
}