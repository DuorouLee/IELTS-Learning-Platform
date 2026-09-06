package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.QuestionGroup;
import com.duorou.ieltsbackend.reading.entity.QuestionOption;
import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 QuestionOption 是否可以正常保存到数据库。
 *
 * 这次验证的完整关系是：
 *
 * ReadingTest
 *      ↓
 * ReadingPassage
 *      ↓
 * QuestionGroup
 *      ↓
 * QuestionOption
 */
@SpringBootTest
class QuestionOptionRepositoryTest {

    @Autowired
    private ReadingTestRepository readingTestRepository;

    @Autowired
    private ReadingPassageRepository readingPassageRepository;

    @Autowired
    private QuestionGroupRepository questionGroupRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Test
    void shouldSaveQuestionOption() {

        // 1. 创建 ReadingTest
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Question Option Test");
        readingTest.setSource("Test");

        readingTest = readingTestRepository.save(readingTest);


        // 2. 创建 ReadingPassage
        ReadingPassage passage = new ReadingPassage();
        passage.setReadingTest(readingTest);
        passage.setPassageNumber(1);
        passage.setContent("This is a test passage.");

        passage = readingPassageRepository.save(passage);


        // 3. 创建 QuestionGroup
        QuestionGroup group = new QuestionGroup();
        group.setReadingPassage(passage);
        group.setQuestionType("MATCHING_HEADINGS");
        group.setInstruction(
                "Choose the correct heading for each paragraph."
        );
        group.setAllowOptionReuse(false);

        group = questionGroupRepository.save(group);


        // 4. 创建一个 QuestionOption
        QuestionOption option = new QuestionOption();

        // 指定这个选项属于刚才创建的题组
        option.setQuestionGroup(group);

        // 真正用于答案匹配的值
        option.setOptionValue("i");

        // 页面上展示给用户的文字
        option.setOptionText("The beginning of the project");

        // 显示顺序
        option.setDisplayOrder(1);


        // 5. 保存
        QuestionOption savedOption =
                questionOptionRepository.save(option);


        // 6. 验证
        assertNotNull(savedOption.getId());

        assertEquals(
                "i",
                savedOption.getOptionValue()
        );

        assertEquals(
                "The beginning of the project",
                savedOption.getOptionText()
        );

        assertEquals(
                group.getId(),
                savedOption.getQuestionGroup().getId()
        );
    }
}
