package com.duorou.ieltsbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查控制器。
 *
 * Controller 的职责：
 * 接收 HTTP 请求，并返回响应。
 *
 * @RestController 表示：
 * 1. 这个类由 Spring 管理；
 * 2. 这个类中的方法可以处理 HTTP 请求；
 * 3. 方法返回的数据会直接写入 HTTP Response Body。
 */
@RestController

/**
 * 给当前 Controller 中的所有接口统一添加 /api 前缀。
 *
 * 所以下面的 /health 最终地址是：
 * GET /api/health
 */
@RequestMapping("/api")
public class HealthController {

    /**
     * @GetMapping 表示这个方法处理 HTTP GET 请求。
     *
     * 请求地址：
     * GET http://localhost:8080/api/health
     */
    @GetMapping("/health")
    public Map<String, String> health() {

        /**
         * Map<String, String> 可以理解成：
         *
         * key   -> value
         * status -> ok
         *
         * Spring Boot 会自动把这个 Java Map 转换成 JSON：
         *
         * {
         *   "status": "ok"
         * }
         */
        return Map.of("status", "ok");
    }
}
