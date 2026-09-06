package com.duorou.ieltsbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JacksonConfig
 *
 * 作用：
 * 告诉 Spring 如何创建 ObjectMapper。
 *
 * ObjectMapper 是 Jackson 提供的 JSON 工具，
 * 主要负责：
 *
 * JSON
 *   ↓
 * Java Object
 *
 * 以及：
 *
 * Java Object
 *   ↓
 * JSON
 */
@Configuration
public class JacksonConfig {

    /**
     * @Bean 的意思：
     *
     * 把这个方法返回的 ObjectMapper
     * 注册到 Spring 容器中。
     *
     * 以后其他 Service 只要需要 ObjectMapper，
     * Spring 就可以自动注入这个对象。
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
