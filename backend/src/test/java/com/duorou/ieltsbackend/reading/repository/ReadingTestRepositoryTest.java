package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ReadingTestRepository 的集成测试。
 *
 * 这个测试会验证：
 *
 * ReadingTest
 *      ↓
 * ReadingTestRepository
 *      ↓
 * Spring Data JPA
 *      ↓
 * Hibernate
 *      ↓
 * SQLite
 *
 * 是否能够正常保存和查询数据。
 */
@SpringBootTest
@Transactional
class ReadingTestRepositoryTest {

    /**
     * Spring 会自动注入 ReadingTestRepository。
     *
     * @Autowired 的意思是：
     * 不需要我们自己 new ReadingTestRepository，
     * Spring 会帮我们提供这个对象。
     */
    @Autowired
    private ReadingTestRepository readingTestRepository;

    @Test
    void shouldSaveAndFindReadingTest() {

        // 1. 创建一个 ReadingTest Java 对象。
        //
        // 注意：
        // 此时它还只是 Java 内存中的对象，
        // 还没有进入数据库。
        ReadingTest readingTest = new ReadingTest();

        readingTest.setTitle("Cambridge IELTS 18 Test 1");
        readingTest.setSource("Cambridge IELTS 18");


        // 2. 保存到数据库。
        //
        // save() 是 JpaRepository 已经帮我们提供的方法，
        // 所以这里不需要自己写 INSERT SQL。
        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // 3. 保存成功后，数据库应该自动生成 id。
        //
        // 如果 id 不是 null，
        // 说明数据已经成功持久化。
        assertNotNull(savedTest.getId());


        // 4. 根据刚刚生成的 id 再查询一次。
        //
        // findById() 也是 JpaRepository 自带的方法。
        ReadingTest foundTest =
                readingTestRepository
                        .findById(savedTest.getId())
                        .orElseThrow();


        // 5. 验证查询出来的数据是否正确。
        assertEquals(
                "Cambridge IELTS 18 Test 1",
                foundTest.getTitle()
        );

        assertEquals(
                "Cambridge IELTS 18",
                foundTest.getSource()
        );
    }
}
