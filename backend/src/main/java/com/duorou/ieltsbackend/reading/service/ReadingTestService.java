package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.dto.ReadingPassageResponse;
import com.duorou.ieltsbackend.reading.dto.ReadingQuestionResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.duorou.ieltsbackend.reading.dto.ReadingTestDetailResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.QuestionGroup;
import com.duorou.ieltsbackend.reading.repository.QuestionGroupRepository;
import com.duorou.ieltsbackend.reading.dto.QuestionGroupResponse;
import com.duorou.ieltsbackend.reading.dto.QuestionOptionResponse;
import com.duorou.ieltsbackend.reading.entity.QuestionGroup;
import com.duorou.ieltsbackend.reading.entity.QuestionOption;

import java.util.List;

/**
 * ReadingTestService
 *
 * 作用：
 * 负责 ReadingTest 相关的业务逻辑。
 *
 * 分层关系：
 *
 * Controller
 *     ↓
 * Service
 *     ↓
 * Repository
 *     ↓
 * Database
 *
 * Repository 负责“怎么访问数据库”。
 * Service 负责“业务应该怎么执行”。
 */
@Service
public class ReadingTestService {

    /**
     * ReadingTestRepository
     *
     * Service 不直接操作数据库，
     * 而是通过 Repository 访问数据库。
     */
    private final ReadingTestRepository readingTestRepository;

    private final ReadingPassageRepository readingPassageRepository;

    private final ReadingQuestionRepository readingQuestionRepository;

    private final QuestionGroupRepository questionGroupRepository;

    /**
     * 构造器注入。
     *
     * Spring 会自动找到 ReadingTestRepository，
     * 然后传进这个构造方法。
     *
     * 这里使用构造器注入，而不是字段上写 @Autowired，
     * 因为这种方式更适合正式项目，也更容易测试。
     */
    public ReadingTestService(
            ReadingTestRepository readingTestRepository,
            ReadingPassageRepository readingPassageRepository,
            ReadingQuestionRepository readingQuestionRepository,
            QuestionGroupRepository questionGroupRepository
    ) {
        this.readingTestRepository = readingTestRepository;
        this.readingPassageRepository = readingPassageRepository;
        this.readingQuestionRepository = readingQuestionRepository;
        this.questionGroupRepository = questionGroupRepository;
    }

    /**
     * 查询所有 Reading Test。
     *
     * JpaRepository 已经提供 findAll()，
     * 所以 Service 这里只负责调用它。
     */
    public List<ReadingTest> findAll() {
        return readingTestRepository.findAll();
    }

    /**
     * 根据 id 查询一个 Reading Test。
     *
     * findById() 返回 Optional。
     *
     * 如果数据库中存在对应 id：
     * 直接返回 ReadingTest。
     *
     * 如果不存在：
     * 返回 404 Not Found，
     * 而不是让系统抛出 500 Internal Server Error。
     */
    public ReadingTest findById(Long id) {

        return readingTestRepository
                .findById(id)

                // 如果没有找到数据，
                // 抛出一个带有 HTTP 404 状态码的异常。
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reading test not found: " + id
                        )
                );
    }

    /**
     * 创建一个新的 Reading Test。
     *
     * 当前第一版先直接保存。
     *
     * 后面业务变复杂以后，
     * 比如：
     * - title 不能为空
     * - source 格式校验
     * - 防止重复 Test
     *
     * 这些规则都应该放在 Service 层。
     */
    public ReadingTest create(ReadingTest readingTest) {
        return readingTestRepository.save(readingTest);
    }

    /**
     * 查询一个 ReadingTest，
     * 并把它下面的 Passage 一起返回。
     */
    public ReadingTestDetailResponse findDetailById(Long testId) {

        ReadingTest test = findById(testId);

        List<ReadingPassage> passages =
                readingPassageRepository.findByReadingTestId(testId);

        List<ReadingQuestion> questions =
                readingQuestionRepository.findByReadingPassageReadingTestId(testId);

        /**
         * 查询当前 Reading Test 下每一个 Passage 对应的 QuestionGroup。
         *
         * 目前先把所有题组查出来，
         * 下一步再把它们放进 DTO 返回给前端。
         */
        List<QuestionGroup> questionGroups =
                passages.stream()
                        .flatMap(passage ->
                                questionGroupRepository
                                        .findByReadingPassage_Id(passage.getId())
                                        .stream()
                        )
                        .toList();

        List<ReadingPassageResponse> passageResponses =
                passages.stream()
                        .map(passage -> {

                            /**
                             * 先找出属于当前 passage 的所有 Question。
                             */
                            List<ReadingQuestionResponse> passageQuestions =
                                    questions.stream()
                                            .filter(question ->
                                                    question.getReadingPassage()
                                                            .getId()
                                                            .equals(passage.getId())
                                            )
                                            .map(question ->
                                                    new ReadingQuestionResponse(
                                                            question.getId(),
                                                            question.getQuestionNumber(),
                                                            question.getQuestionType(),
                                                            question.getQuestionText(),
                                                            question.getExplanation()
                                                    )
                                            )
                                            .toList();

                            /**
                             * 找出属于当前 Passage 的所有 QuestionGroup，
                             * 并转换成 API 返回用的 DTO。
                             */
                            List<QuestionGroupResponse> passageQuestionGroups =
                                    questionGroups.stream()

                                            /**
                                             * 只保留当前 Passage 的题组。
                                             */
                                            .filter(group ->
                                                    group.getReadingPassage()
                                                            .getId()
                                                            .equals(passage.getId())
                                            )

                                            /**
                                             * Entity -> DTO
                                             */
                                            .map(group -> {

                                                /**
                                                 * 当前 QuestionGroup 下的所有选项。
                                                 */
                                                List<QuestionOptionResponse> optionResponses =
                                                        group.getOptions()
                                                                .stream()
                                                                .map(option ->
                                                                        new QuestionOptionResponse(
                                                                                option.getId(),
                                                                                option.getOptionValue(),
                                                                                option.getOptionText(),
                                                                                option.getDisplayOrder()
                                                                        )
                                                                )
                                                                .toList();


                                                /**
                                                 * 当前 QuestionGroup 下的所有 Questions。
                                                 *
                                                 * ReadingQuestion 目前通过 groupId
                                                 * 关联 QuestionGroup。
                                                 */
                                                List<ReadingQuestionResponse> groupedQuestions =
                                                        questions.stream()

                                                                .filter(question ->
                                                                        question.getGroupId() != null
                                                                                &&
                                                                                question.getGroupId()
                                                                                        .equals(group.getId())
                                                                )

                                                                .map(question ->
                                                                        new ReadingQuestionResponse(
                                                                                question.getId(),
                                                                                question.getQuestionNumber(),
                                                                                question.getQuestionType(),
                                                                                question.getQuestionText(),
                                                                                question.getExplanation()
                                                                        )
                                                                )

                                                                .toList();


                                                return new QuestionGroupResponse(
                                                        group.getId(),
                                                        group.getQuestionType(),
                                                        group.getInstruction(),
                                                        group.getAllowOptionReuse(),
                                                        optionResponses,
                                                        groupedQuestions
                                                );
                                            })

                                            .toList();

                            /**
                             * 再创建当前 Passage 的 DTO，
                             * 并把它自己的 Questions 放进去。
                             */
                            return new ReadingPassageResponse(
                                    passage.getId(),
                                    passage.getPassageNumber(),
                                    passage.getTitle(),
                                    passage.getInstruction(),
                                    passage.getContent(),
                                    passageQuestions,
                                    passageQuestionGroups
                            );
                        })
                        .toList();

        return new ReadingTestDetailResponse(
                test,
                passageResponses
        );
    }
}
