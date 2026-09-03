package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ReadingQuestionRepository 集成测试。
 *
 * 这次主要验证：
 *
 * ReadingTest
 *      ↓
 * ReadingPassage
 *      ↓
 * ReadingQuestion
 *
 * 三层数据关系是否能够正常保存和查询。
 */
@SpringBootTest
@Transactional
class ReadingQuestionRepositoryTest {

    @Autowired
    private ReadingTestRepository readingTestRepository;

    @Autowired
    private ReadingPassageRepository readingPassageRepository;

    @Autowired
    private ReadingQuestionRepository readingQuestionRepository;

    @Test
    void shouldSaveAndFindReadingQuestion() {

        // 1. 先创建 ReadingTest。
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Cambridge IELTS 18 Test 1");
        readingTest.setSource("Cambridge IELTS 18");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // 2. 创建 Passage，并关联到刚刚的 ReadingTest。
        ReadingPassage passage = new ReadingPassage();
        passage.setReadingTest(savedTest);
        passage.setPassageNumber(1);
        passage.setTitle("Sample Passage");
        passage.setContent(
                "This is a sample IELTS reading passage."
        );

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        // 3. 创建一道 Reading Question。
        ReadingQuestion question = new ReadingQuestion();

        // 这里建立：
        //
        // Question -> Passage
        //
        // Hibernate 会自动把 savedPassage.id
        // 写入 reading_question.passage_id。
        question.setReadingPassage(savedPassage);

        question.setQuestionNumber(1);

        question.setQuestionType(
                "TRUE_FALSE_NOT_GIVEN"
        );

        question.setQuestionText(
                "The passage states that this is a sample IELTS text."
        );

        question.setCorrectAnswer(
                "TRUE"
        );

        question.setExplanation(
                "The statement matches the information in the passage."
        );


        // 4. 保存 Question。
        ReadingQuestion savedQuestion =
                readingQuestionRepository.save(question);


        // 5. 验证 Question 已经成功生成主键。
        assertNotNull(savedQuestion.getId());


        // 6. 再根据 id 查询一次。
        ReadingQuestion foundQuestion =
                readingQuestionRepository
                        .findById(savedQuestion.getId())
                        .orElseThrow();


        // 7. 验证 Question 自己的数据。
        assertEquals(
                1,
                foundQuestion.getQuestionNumber()
        );

        assertEquals(
                "TRUE_FALSE_NOT_GIVEN",
                foundQuestion.getQuestionType()
        );

        assertEquals(
                "TRUE",
                foundQuestion.getCorrectAnswer()
        );


        // 8. 验证 Question -> Passage 的关系。
        assertEquals(
                savedPassage.getId(),
                foundQuestion.getReadingPassage().getId()
        );


        // 9. 再继续向上验证：
        //
        // Question
        //    ↓
        // Passage
        //    ↓
        // ReadingTest
        assertEquals(
                savedTest.getId(),
                foundQuestion
                        .getReadingPassage()
                        .getReadingTest()
                        .getId()
        );
    }
}
