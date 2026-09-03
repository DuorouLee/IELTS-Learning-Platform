package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * ReadingQuestionService
 * <p>
 * 作用：
 * 负责 ReadingQuestion 的业务逻辑。
 * <p>
 * 调用链路：
 * <p>
 * ReadingQuestionController
 * ↓
 * ReadingQuestionService
 * ↓
 * ReadingQuestionRepository
 * ↓
 * SQLite
 */
@Service
public class ReadingQuestionService {

    /**
     * Repository 负责访问数据库。
     */
    private final ReadingQuestionRepository readingQuestionRepository;

    /**
     * 构造器注入。
     * <p>
     * Spring 启动时，
     * 会自动把 ReadingQuestionRepository
     * 注入进来。
     */
    public ReadingQuestionService(
            ReadingQuestionRepository readingQuestionRepository
    ) {
        this.readingQuestionRepository = readingQuestionRepository;
    }

    /**
     * 查询所有 Question。
     */
    public List<ReadingQuestion> findAll() {
        return readingQuestionRepository.findAll();
    }

    /**
     * 根据 id 查询 Question。
     * <p>
     * 如果找不到，
     * 返回 HTTP 404。
     */
    public ReadingQuestion findById(Long id) {
        return readingQuestionRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "ReadingQuestion not found: " + id
                        )
                );
    }

    /**
     * 创建一个新的 Question。
     */
    public ReadingQuestion create(
            ReadingQuestion readingQuestion
    ) {
        return readingQuestionRepository.save(readingQuestion);
    }

    /**
     * 查询某一个 Passage 下的全部题目。
     */
    public List<ReadingQuestion> findByPassageId(Long passageId) {
        return readingQuestionRepository.findByReadingPassageId(passageId);
    }
}
