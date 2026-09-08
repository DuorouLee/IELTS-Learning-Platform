package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.dto.ReadingSubmitRequest;
import com.duorou.ieltsbackend.reading.dto.ReadingSubmitResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ReadingSubmissionService
 *
 * 专门负责 Reading Test 的：
 *
 * 提交答案
 * ↓
 * 判分
 * ↓
 * 返回成绩
 *
 * 这个 Service 和 ReadingTestService 分开的原因是：
 *
 * ReadingTestService：
 * 负责读取 Test 数据。
 *
 * ReadingSubmissionService：
 * 负责“用户提交答案以后”的业务逻辑。
 *
 * 这样每个 Service 的职责更加清楚。
 */
@Service
public class ReadingSubmissionService {

    /**
     * 用来读取 Reading Question。
     *
     * 我们需要通过：
     *
     * testId
     * ↓
     * 查询当前 Test 的所有 Question
     *
     * 然后拿 correctAnswer 和用户答案进行比较。
     */
    private final ReadingQuestionRepository readingQuestionRepository;

    /**
     * 用来确认用户提交的 testId 是否真的存在。
     */
    private final ReadingTestRepository readingTestRepository;

    /**
     * 构造器注入。
     *
     * Spring 会自动把 Repository 注入进来。
     */
    public ReadingSubmissionService(
            ReadingQuestionRepository readingQuestionRepository,
            ReadingTestRepository readingTestRepository
    ) {
        this.readingQuestionRepository = readingQuestionRepository;
        this.readingTestRepository = readingTestRepository;
    }

    /**
     * submitTest
     *
     * 对一整套 Reading Test 进行判分。
     *
     * @param testId  当前 Reading Test id
     * @param request 前端提交的答案
     *
     * @return ReadingSubmitResponse
     */
    public ReadingSubmitResponse submitTest(
            Long testId,
            ReadingSubmitRequest request
    ) {

        /**
         * 第一步：
         * 确认 Test 存在。
         *
         * 如果用户请求：
         *
         * POST /api/reading/tests/999/submit
         *
         * 但 Test 999 不存在，
         * 就不应该继续判分。
         */
        if (!readingTestRepository.existsById(testId)) {
            throw new IllegalArgumentException(
                    "Reading test not found: " + testId
            );
        }

        /**
         * 第二步：
         * 查询当前 Test 下所有 Question。
         *
         * Repository 已经有：
         *
         * findByReadingPassageReadingTestId(testId)
         */
        List<ReadingQuestion> questions =
                readingQuestionRepository.findByReadingPassageReadingTestId(testId);

        /**
         * 第三步：
         * 读取用户提交的 answers。
         *
         * 数据类似：
         *
         * {
         *   27: "viii",
         *   28: "iv",
         *   35: "D"
         * }
         */
        Map<Long, String> answers = request.getAnswers();

        /**
         * correctCount
         *
         * 用来记录答对多少题。
         */
        int correctCount = 0;

        /**
         * 第四步：
         * 遍历所有 Question，
         * 对比用户答案和正确答案。
         */
        for (ReadingQuestion question : questions) {

            /**
             * 当前题目的用户答案。
             *
             * question.getId()
             * 就是 Map 的 key。
             */
            String userAnswer = answers.get(question.getId());

            /**
             * 如果用户没答这一题，
             * userAnswer 会是 null。
             *
             * null 直接算错，
             * 所以不增加 correctCount。
             */
            if (userAnswer == null) {
                continue;
            }

            /**
             * 获取数据库中的正确答案。
             */
            String correctAnswer = question.getCorrectAnswer();

            /**
             * trim()
             * 去掉用户答案前后的多余空格。
             *
             * equalsIgnoreCase()
             * 让：
             *
             * D
             * d
             *
             * 暂时都可以判断为相同。
             *
             * 对 MATCHING_HEADINGS 的：
             *
             * viii
             *
             * 也不会造成问题。
             */
            if (
                    correctAnswer != null
                            && correctAnswer.trim()
                            .equalsIgnoreCase(userAnswer.trim())
            ) {
                correctCount++;
            }
        }

        /**
         * 总题数。
         */
        int totalQuestions = questions.size();

        /**
         * 错误题数。
         *
         * 当前规则：
         *
         * 未答题也算 incorrect。
         */
        int incorrectCount = totalQuestions - correctCount;

        /**
         * 正确率。
         *
         * 防止 totalQuestions = 0 时除以 0。
         */
        double percentage = 0;

        if (totalQuestions > 0) {
            percentage =
                    (double) correctCount
                            / totalQuestions
                            * 100;
        }

        /**
         * 返回最终判分结果。
         */
        return new ReadingSubmitResponse(
                totalQuestions,
                correctCount,
                incorrectCount,
                percentage
        );
    }
}
