package com.duorou.ieltsbackend.reading.config;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ReadingDataInitializer
 *
 * 作用：
 * 在开发阶段，为数据库插入一套最小的 Reading 示例数据。
 *
 * 注意：
 * 这里只用于当前开发和前后端联调。
 * 以后接入真实 IELTS 题库后，
 * 会替换成更正式的 JSON 题库导入机制。
 */
@Configuration
public class ReadingDataInitializer {

    /**
     * CommandLineRunner：
     *
     * Spring Boot 启动完成后，
     * 会自动执行这里面的代码。
     */
    @Bean
    CommandLineRunner initReadingData(
            ReadingTestRepository readingTestRepository,
            ReadingPassageRepository readingPassageRepository,
            ReadingQuestionRepository readingQuestionRepository
    ) {
        return args -> {

            /**
             * 关键保护：
             *
             * 如果数据库里已经有 ReadingTest，
             * 就直接结束，不重复插入。
             *
             * 这样可以避免：
             * 每启动一次 Spring Boot
             * 就多出一套相同数据。
             */
            if (readingTestRepository.count() > 0) {
                return;
            }

            // =========================
            // 1. 创建 Reading Test
            // =========================

            ReadingTest test = new ReadingTest();

            test.setTitle("Cambridge IELTS 18 Test 1");
            test.setSource("Cambridge IELTS 18");

            // 保存后，test 会获得数据库生成的 id。
            test = readingTestRepository.save(test);


            // =========================
            // 2. 创建 Passage
            // =========================

            ReadingPassage passage = new ReadingPassage();

            passage.setPassageNumber(1);
            passage.setTitle("Sample Reading Passage");

            passage.setContent(
                    "IELTS is an international English language testing system."
            );

            // 建立关系：
            //
            // ReadingTest
            //     ↓
            // ReadingPassage
            passage.setReadingTest(test);

            passage = readingPassageRepository.save(passage);


            // =========================
            // 3. 创建 Question 1
            // =========================

            ReadingQuestion question1 = new ReadingQuestion();

            question1.setQuestionNumber(1);
            question1.setQuestionType("TRUE_FALSE_NOT_GIVEN");

            question1.setQuestionText(
                    "IELTS is only used for university admission."
            );

            question1.setCorrectAnswer("FALSE");

            question1.setExplanation(
                    "IELTS is also used for immigration, employment and other purposes."
            );

            // 建立关系：
            //
            // ReadingPassage
            //     ↓
            // ReadingQuestion
            question1.setReadingPassage(passage);

            readingQuestionRepository.save(question1);


            // =========================
            // 4. 创建 Question 2
            // =========================

            ReadingQuestion question2 = new ReadingQuestion();

            question2.setQuestionNumber(2);
            question2.setQuestionType("TRUE_FALSE_NOT_GIVEN");

            question2.setQuestionText(
                    "IELTS is an English language test."
            );

            question2.setCorrectAnswer("TRUE");

            question2.setExplanation(
                    "The passage states that IELTS is an international English language testing system."
            );

            question2.setReadingPassage(passage);

            readingQuestionRepository.save(question2);
        };
    }
}
