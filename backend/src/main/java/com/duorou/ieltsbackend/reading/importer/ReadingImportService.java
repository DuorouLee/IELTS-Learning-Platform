package com.duorou.ieltsbackend.reading.importer;

import com.duorou.ieltsbackend.reading.importer.dto.ReadingImportDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ReadingImportService
 *
 * 作用：
 * 负责读取 resources 目录里的 Reading JSON 文件，
 * 并把 JSON 转换成 Java 对象。
 *
 * 当前阶段只做：
 *
 * JSON
 *   ↓
 * ReadingImportDto
 *
 * 还不会写入数据库。
 */
@Service
public class ReadingImportService {

    /**
     * ObjectMapper 是 Jackson 提供的核心类。
     *
     * 它负责：
     *
     * JSON
     *   ↓
     * Java Object
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造器注入。
     *
     * Spring Boot 已经自动配置好了 ObjectMapper，
     * 所以这里直接让 Spring 注入即可。
     */
    public ReadingImportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 读取一份 Reading JSON 文件。
     *
     * @param fileName 文件名，例如：
     *                 reading-p1-high-01.json
     *
     * @return 转换后的 ReadingImportDto
     */
    public ReadingImportDto loadReadingFile(String fileName) {

        try {

            /**
             * JSON 文件位于：
             *
             * src/main/resources/data/reading/
             *
             * ClassPathResource 可以读取 resources 目录里的文件。
             */
            ClassPathResource resource =
                    new ClassPathResource(
                            "data/reading/" + fileName
                    );

            /**
             * ObjectMapper.readValue(...)
             *
             * 会把 JSON 内容转换成：
             *
             * ReadingImportDto
             */
            return objectMapper.readValue(
                    resource.getInputStream(),
                    ReadingImportDto.class
            );

        } catch (IOException e) {

            /**
             * 如果文件不存在，
             * 或 JSON 格式错误，
             * 就抛出一个更容易理解的异常。
             */
            throw new IllegalStateException(
                    "Failed to load Reading file: " + fileName,
                    e
            );
        }
    }
}
