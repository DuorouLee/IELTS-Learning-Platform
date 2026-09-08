package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.dto.ReadingSubmitRequest;
import com.duorou.ieltsbackend.reading.dto.ReadingSubmitResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.springframework.stereotype.Service;
import com.duorou.ieltsbackend.reading.dto.ReadingQuestionReviewResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPracticeRecord;
import com.duorou.ieltsbackend.reading.repository.ReadingPracticeRecordRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ReadingSubmissionService
 * <p>
 * 专门负责 Reading Test 的：
 * <p>
 * 提交答案
 * ↓
 * 判分
 * ↓
 * 返回成绩
 * <p>
 * 这个 Service 和 ReadingTestService 分开的原因是：
 * <p>
 * ReadingTestService：
 * 负责读取 Test 数据。
 * <p>
 * ReadingSubmissionService：
 * 负责“用户提交答案以后”的业务逻辑。
 * <p>
 * 这样每个 Service 的职责更加清楚。
 */
@Service
public class ReadingSubmissionService {

    /**
     * 用来读取 Reading Question。
     * <p>
     * 我们需要通过：
     * <p>
     * testId
     * ↓
     * 查询当前 Test 的所有 Question
     * <p>
     * 然后拿 correctAnswer 和用户答案进行比较。
     */
    private final ReadingQuestionRepository readingQuestionRepository;

    /**
     * 用来确认用户提交的 testId 是否真的存在。
     */
    private final ReadingTestRepository readingTestRepository;

    /**
     * 用来保存每次 Reading 提交记录。
     */
    private final ReadingPracticeRecordRepository readingPracticeRecordRepository;

    /**
     * 构造器注入。
     * <p>
     * Spring 会自动把 Repository 注入进来。
     */
    public ReadingSubmissionService(
            ReadingQuestionRepository readingQuestionRepository,
            ReadingTestRepository readingTestRepository,
            ReadingPracticeRecordRepository readingPracticeRecordRepository
    ) {
        this.readingQuestionRepository = readingQuestionRepository;
        this.readingTestRepository = readingTestRepository;
        this.readingPracticeRecordRepository = readingPracticeRecordRepository;
    }

    /**
     * submitTest
     * <p>
     * 对一整套 Reading Test 进行判分。
     *
     * @param testId  当前 Reading Test id
     * @param request 前端提交的答案
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

        int correctCount = 0;

        /**
         * 保存每一道题的 Review 结果。
         */
        List<ReadingQuestionReviewResponse> reviewQuestions = new ArrayList<>();

        for (ReadingQuestion question : questions) {

            String userAnswer = answers.get(question.getId());

            String correctAnswer = question.getCorrectAnswer();

            /**
             * 默认认为当前题错误。
             */
            boolean correct = false;

            /**
             * 用户有作答，并且正确答案不为 null 时，
             * 才进行答案比较。
             */
            if (userAnswer != null && correctAnswer != null) {
                correct = correctAnswer
                        .trim()
                        .equalsIgnoreCase(userAnswer.trim());
            }

            /**
             * 如果当前题答对，
             * correctCount 加 1。
             */
            if (correct) {
                correctCount++;
            }

            /**
             * 无论答对、答错还是未作答，
             * 都生成一条 Review 数据。
             */
            reviewQuestions.add(
                    new ReadingQuestionReviewResponse(
                            question.getId(),
                            question.getQuestionNumber(),
                            userAnswer,
                            correctAnswer,
                            correct
                    )
            );
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
         * 保存本次 Reading Practice History。
         */
        ReadingPracticeRecord practiceRecord =
                new ReadingPracticeRecord();

        /**
         * 当前提交的是哪一个 Reading Test。
         */
        practiceRecord.setReadingTest(
                readingTestRepository.findById(testId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Reading test not found: " + testId
                                )
                        )
        );

        /**
         * 保存成绩。
         */
        practiceRecord.setCorrectCount(correctCount);
        practiceRecord.setTotalQuestions(totalQuestions);
        practiceRecord.setPercentage(percentage);

        /**
         * 保存提交时间。
         */
        practiceRecord.setSubmittedAt(System.currentTimeMillis());

        /**
         * 写入 reading_practice_record 表。
         */
        readingPracticeRecordRepository.save(practiceRecord);

        /**
         * 返回最终判分结果。
         */
        return new ReadingSubmitResponse(
                totalQuestions,
                correctCount,
                incorrectCount,
                percentage,
                reviewQuestions
        );
    }
}
