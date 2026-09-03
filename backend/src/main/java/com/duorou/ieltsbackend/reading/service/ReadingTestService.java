package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.springframework.stereotype.Service;
import com.duorou.ieltsbackend.reading.dto.ReadingTestDetailResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPassage;



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
            ReadingQuestionRepository readingQuestionRepository
    ) {
        this.readingTestRepository = readingTestRepository;
        this.readingPassageRepository = readingPassageRepository;
        this.readingQuestionRepository = readingQuestionRepository;
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
     * findById() 返回 Optional，
     * 如果没有找到数据，
     * 这里暂时直接抛出异常。
     *
     * 后面做 REST API 时，
     * 我们会再把这种异常改成更规范的业务异常。
     */
    public ReadingTest findById(Long id) {
        return readingTestRepository
                .findById(id)
                .orElseThrow();
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
                readingQuestionRepository.findAll();

        return new ReadingTestDetailResponse(
                test,
                passages,
                questions
        );
    }
}
