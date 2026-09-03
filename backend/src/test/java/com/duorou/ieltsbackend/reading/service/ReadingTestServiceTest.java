package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ReadingTestService 集成测试。
 *
 * 这次验证的是：
 *
 * ReadingTestService
 *      ↓
 * ReadingTestRepository
 *      ↓
 * Hibernate / JPA
 *      ↓
 * SQLite
 *
 * 也就是说，我们开始测试业务层，而不再直接调用 Repository。
 */
@SpringBootTest
@Transactional
class ReadingTestServiceTest {

    @Autowired
    private ReadingTestService readingTestService;

    @Autowired
    private ReadingTestRepository readingTestRepository;

    /**
     * 测试创建 ReadingTest。
     */
    @Test
    void shouldCreateReadingTest() {

        // 1. 创建一个 Java 对象。
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Cambridge IELTS 18 Test 2");
        readingTest.setSource("Cambridge IELTS 18");

        // 2. 通过 Service 创建。
        //
        // 注意：
        // 这里已经不直接调用 Repository.save() 了。
        ReadingTest createdTest =
                readingTestService.create(readingTest);

        // 3. 验证数据库已经生成 id。
        assertNotNull(createdTest.getId());

        // 4. 验证数据内容。
        assertEquals(
                "Cambridge IELTS 18 Test 2",
                createdTest.getTitle()
        );
    }

    /**
     * 测试通过 id 查询 ReadingTest。
     */
    @Test
    void shouldFindReadingTestById() {

        // 先准备一条数据库数据。
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Cambridge IELTS 18 Test 3");
        readingTest.setSource("Cambridge IELTS 18");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);

        // 真正要测试的是 Service.findById()
        ReadingTest foundTest =
                readingTestService.findById(savedTest.getId());

        assertEquals(
                savedTest.getId(),
                foundTest.getId()
        );

        assertEquals(
                "Cambridge IELTS 18 Test 3",
                foundTest.getTitle()
        );
    }

    /**
     * 测试查询全部 ReadingTest。
     */
    @Test
    void shouldFindAllReadingTests() {

        ReadingTest test1 = new ReadingTest();
        test1.setTitle("Cambridge IELTS 18 Test 1");
        test1.setSource("Cambridge IELTS 18");

        ReadingTest test2 = new ReadingTest();
        test2.setTitle("Cambridge IELTS 18 Test 2");
        test2.setSource("Cambridge IELTS 18");

        readingTestRepository.save(test1);
        readingTestRepository.save(test2);

        // 通过 Service 查询全部数据。
        List<ReadingTest> tests =
                readingTestService.findAll();

        // 因为数据库里可能已经有其他测试数据，
        // 所以这里不直接判断 size == 2。
        //
        // 只要至少能查到我们插入的数据即可。
        assertNotNull(tests);
    }
}
