package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ReadingPassageService
 *
 * 作用：
 * 负责 ReadingPassage 的业务逻辑。
 *
 * 调用关系：
 *
 * ReadingPassageController
 *          ↓
 * ReadingPassageService
 *          ↓
 * ReadingPassageRepository
 *          ↓
 * SQLite
 */
@Service
public class ReadingPassageService {

    /**
     * Repository 负责真正访问数据库。
     */
    private final ReadingPassageRepository readingPassageRepository;

    /**
     * 构造器注入。
     *
     * Spring 启动时，会自动找到
     * ReadingPassageRepository，
     * 然后传给这个 Service。
     */
    public ReadingPassageService(
            ReadingPassageRepository readingPassageRepository
    ) {
        this.readingPassageRepository = readingPassageRepository;
    }

    /**
     * 查询全部 Passage。
     */
    public List<ReadingPassage> findAll() {
        return readingPassageRepository.findAll();
    }

    /**
     * 根据 id 查询 Passage。
     */
    public ReadingPassage findById(Long id) {
        return readingPassageRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "ReadingPassage not found: " + id
                        )
                );
    }
}
