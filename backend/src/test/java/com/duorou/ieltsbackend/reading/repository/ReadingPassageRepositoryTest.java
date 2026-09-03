package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ReadingPassageRepository 集成测试。
 *
 * 这次主要验证：
 *
 * ReadingTest
 *     ↓
 * ReadingPassage
 *
 * 这两个实体之间的 ManyToOne 外键关系是否正常。
 */
@SpringBootTest
@Transactional
class ReadingPassageRepositoryTest {

    @Autowired
    private ReadingTestRepository readingTestRepository;

    @Autowired
    private ReadingPassageRepository readingPassageRepository;

    @Test
    void shouldSaveAndFindReadingPassage() {

        // 1. 先创建并保存一个 ReadingTest。
        //
        // 因为 ReadingPassage.test_id 是 NOT NULL，
        // 所以 Passage 不能脱离 Test 单独存在。
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Cambridge IELTS 18 Test 1");
        readingTest.setSource("Cambridge IELTS 18");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // 2. 创建 ReadingPassage。
        ReadingPassage passage = new ReadingPassage();

        // 这里非常重要：
        //
        // 我们不是自己写 test_id = 1，
        // 而是直接把 ReadingTest Java 对象关联进去。
        //
        // Hibernate 最后会自动把：
        //
        // savedTest.getId()
        //
        // 写到数据库的 test_id 字段。
        passage.setReadingTest(savedTest);

        passage.setPassageNumber(1);
        passage.setTitle("Sample Reading Passage");
        passage.setContent(
                "This is a sample IELTS reading passage."
        );


        // 3. 保存 Passage。
        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        // 4. 验证 Passage 的主键已经生成。
        assertNotNull(savedPassage.getId());


        // 5. 再从数据库中查询。
        ReadingPassage foundPassage =
                readingPassageRepository
                        .findById(savedPassage.getId())
                        .orElseThrow();


        // 6. 验证 Passage 自身的数据。
        assertEquals(
                1,
                foundPassage.getPassageNumber()
        );

        assertEquals(
                "Sample Reading Passage",
                foundPassage.getTitle()
        );


        // 7. 验证 Passage 和 ReadingTest 的关系。
        //
        // 这里是本次测试最关键的地方。
        assertEquals(
                savedTest.getId(),
                foundPassage.getReadingTest().getId()
        );

        assertEquals(
                "Cambridge IELTS 18 Test 1",
                foundPassage.getReadingTest().getTitle()
        );
    }
}
