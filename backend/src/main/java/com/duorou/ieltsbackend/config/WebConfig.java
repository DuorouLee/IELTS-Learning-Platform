package com.duorou.ieltsbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 全局配置。
 *
 * 这里主要解决前后端分离开发时的跨域问题。
 *
 * 前端：
 * http://localhost:5173
 *
 * 后端：
 * http://localhost:8080
 *
 * 因为端口不同，浏览器会认为它们是两个不同的来源（Origin），
 * 所以需要由后端明确允许前端访问。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域访问规则。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry
                // 所有 /api/** 接口都应用这个 CORS 配置
                .addMapping("/api/**")

                // 只允许我们的 Vue 开发服务器访问
                .allowedOrigins("http://localhost:5173")

                // 当前项目常用的 HTTP 方法
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )

                // 允许前端发送请求头
                .allowedHeaders("*");
    }
}
