package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.QuestionGroup;
import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 QuestionGroupRepository 是否可以正常操作数据库。
 *
 * 这次测试的目标是验证：
 *
 * ReadingTest
 *      ↓
 * ReadingPassage
 *      ↓
 * QuestionGroup
 *
 * 是否能够正常保存到 SQLite。
 */
@SpringBootTest
class QuestionGroupRepositoryTest {

    @Autowired
    private ReadingTestRepository readingTestRepository;

    @Autowired
    private ReadingPassageRepository readingPassageRepository;

    @Autowired
    private QuestionGroupRepository questionGroupRepository;

    @Test
    void shouldSaveQuestionGroup() {

        // --------------------------------------------------
        // 1. 先创建一个 ReadingTest
        // --------------------------------------------------
        ReadingTest readingTest = new ReadingTest();

        readingTest.setTitle("Question Group Test");
        readingTest.setSource("Test");

        // 保存后，readingTest 会获得数据库生成的 id。
        readingTest = readingTestRepository.save(readingTest);


        // --------------------------------------------------
        // 2. 创建 Passage
        //
        // 因为 QuestionGroup 必须属于一个 Passage，
        // 所以不能直接创建 QuestionGroup。
        // --------------------------------------------------
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(readingTest);
        passage.setPassageNumber(1);
        passage.setContent("This is a test passage.");

        passage = readingPassageRepository.save(passage);


        // --------------------------------------------------
        // 3. 创建 QuestionGroup
        // --------------------------------------------------
        QuestionGroup group = new QuestionGroup();

        // 当前题组属于刚才创建的 Passage。
        group.setReadingPassage(passage);

        // 测试 Matching Headings 类型。
        group.setQuestionType("MATCHING_HEADINGS");

        // 整组题共享的 instruction。
        group.setInstruction(
                "Choose the correct heading for each paragraph."
        );

        // Matching Headings 当前测试不允许重复使用选项。
        group.setAllowOptionReuse(false);


        // --------------------------------------------------
        // 4. 保存 QuestionGroup
        // --------------------------------------------------
        QuestionGroup savedGroup =
                questionGroupRepository.save(group);


        // --------------------------------------------------
        // 5. 验证保存结果
        // --------------------------------------------------

        // 保存成功之后，数据库应该生成 id。
        assertNotNull(savedGroup.getId());

        assertEquals(
                "MATCHING_HEADINGS",
                savedGroup.getQuestionType()
        );

        assertEquals(
                passage.getId(),
                savedGroup.getReadingPassage().getId()
        );
    }
}
