package com.course.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path}")
    private String baseUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源处理
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
                
        // 配置文件上传目录的访问
        // 将相对路径转换为绝对路径，避免依赖启动目录
        Path uploadPath = Paths.get(baseUploadPath).toAbsolutePath().normalize();
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath + "/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
                
        // Knife4j 4.0.0 官方推荐配置
        // 配置doc.html入口文件
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        // 配置webjars资源
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
                
        // 配置Knife4j静态资源
        registry.addResourceHandler("/knife4j/**")
                .addResourceLocations("classpath:/META-INF/resources/knife4j/");
                
        // 配置API文档相关资源，确保返回正确的JSON格式
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
                
        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/");
                
        // 配置swagger-config.json资源，这是Knife4j 4.0.0必需的配置
        registry.addResourceHandler("/swagger-config")
                .addResourceLocations("classpath:/META-INF/resources/");
    }
}
