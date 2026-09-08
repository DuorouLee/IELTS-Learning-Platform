<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  getFullReadingTest,
  submitReadingTest,
  type FullReadingTestResponse,
  type ReadingSubmitResponse,
} from '@/api/reading'
import { useRoute } from 'vue-router'

/**
 * testData
 *
 * 保存后端：
 *
 * GET /api/reading/tests/{id}/full
 *
 * 返回的完整 Reading Test 数据。
 */
const testData = ref<FullReadingTestResponse | null>(null)

/**
 * loading
 *
 * true：正在加载
 * false：请求结束
 */
const loading = ref(true)

/**
 * errorMessage
 *
 * API 请求失败时保存错误信息。
 */
const errorMessage = ref('')

/**
 * route
 *
 * 当前 Vue Router 路由。
 */
const route = useRoute()

/**
 * currentTestId
 *
 * 从 URL 中动态读取 testId。
 *
 * 例如：
 *
 * /reading/tests/3
 * ↓
 * currentTestId.value = 3
 */
const currentTestId = computed(() => {
  return Number(route.params.testId)
})

/**
 * answers
 *
 * 保存用户当前答案。
 *
 * 结构：
 *
 * {
 *   14: "viii",
 *   15: "iv",
 *   22: "D"
 * }
 *
 * key：
 * ReadingQuestion id
 *
 * value：
 * 用户选择的 optionValue
 */
const answers = ref<Record<number, string>>({})

/**
 * submitResult
 *
 * 保存后端判分结果。
 *
 * 用户还没有点击提交之前：
 * null
 *
 * 提交成功以后，例如：
 *
 * {
 *   totalQuestions: 13,
 *   correctCount: 10,
 *   incorrectCount: 3,
 *   percentage: 76.92
 * }
 */
const submitResult = ref<ReadingSubmitResponse | null>(null)

/**
 * submitting
 *
 * 防止用户连续点击提交按钮。
 */
const submitting = ref(false)

/**
 * submitErrorMessage
 *
 * 保存提交答案失败时的错误信息。
 */
const submitErrorMessage = ref('')

/**
 * 每一套 Reading Test 使用独立的 localStorage key。
 *
 * 例如：
 *
 * Test 3
 * ↓
 * reading-answers-3
 */
const localStorageKey = computed(() => {
  return `reading-answers-${currentTestId.value}`
})

/**
 * 监听 answers。
 *
 * 用户每次改变答案后，
 * 自动保存到 localStorage。
 */
watch(
  answers,
  (newAnswers) => {
    localStorage.setItem(localStorageKey.value, JSON.stringify(newAnswers))
  },
  {
    /**
     * answers 是对象，
     * 所以需要 deep: true
     * 才能监听对象内部字段变化。
     */
    deep: true,
  },
)

/**
 * answeredCount
 *
 * 计算当前已经回答多少道题。
 */
const answeredCount = computed(() => {
  return Object.values(answers.value).filter((answer) => answer).length
})

/**
 * totalQuestionCount
 *
 * 自动计算整套 Reading Test 的题目总数。
 *
 * 数据结构：
 *
 * testData
 * └── passages
 *     └── questionGroups
 *         └── questions
 */
const totalQuestionCount = computed(() => {
  if (!testData.value) {
    return 0
  }

  let total = 0

  for (const passage of testData.value.passages) {
    for (const group of passage.questionGroups) {
      total += group.questions.length
    }
  }

  return total
})

/**
 * loadReadingTest
 *
 * 根据当前 URL 中的 testId：
 *
 * 1. 清理上一套题状态
 * 2. 恢复 localStorage 答案
 * 3. 请求后端完整 Reading Test
 */
async function loadReadingTest() {
  loading.value = true
  errorMessage.value = ''
  testData.value = null

  /**
   * 恢复当前 Test 的本地答案。
   */
  const savedAnswers = localStorage.getItem(localStorageKey.value)

  if (savedAnswers) {
    answers.value = JSON.parse(savedAnswers)
  } else {
    answers.value = {}
  }

  try {
    /**
     * 例如：
     *
     * /reading/tests/3
     * ↓
     * GET /api/reading/tests/3/full
     */
    testData.value = await getFullReadingTest(currentTestId.value)
  } catch (error) {
    if (error instanceof Error) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = '发生未知错误'
    }
  } finally {
    loading.value = false
  }
}

/**
 * submitAnswers
 *
 * 把当前 answers 提交给 Spring Boot 判分。
 */
async function submitAnswers() {
  /**
   * 开始提交。
   */
  submitting.value = true

  /**
   * 清掉上一次提交错误。
   */
  submitErrorMessage.value = ''

  try {
    /**
     * 前端当前 answers：
     *
     * {
     *   79: "viii",
     *   80: "iv",
     *   87: "D"
     * }
     *
     * 直接包装成后端需要的：
     *
     * {
     *   answers: {...}
     * }
     */
    submitResult.value = await submitReadingTest(
      currentTestId.value,
      {
        answers: answers.value,
      },
    )
  } catch (error) {
    if (error instanceof Error) {
      submitErrorMessage.value = error.message
    } else {
      submitErrorMessage.value = '提交 Reading Test 时发生未知错误'
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 监听 URL 中 testId 的变化。
 *
 * immediate: true
 * 表示页面第一次打开时也立即加载。
 */
watch(
  () => route.params.testId,
  () => {
    loadReadingTest()
  },
  {
    immediate: true,
  },
)

/**
 * splitPassageContent
 *
 * 现在 instruction 和 title 已经由后端独立提供，
 * 所以前端不再写死：
 *
 * A Brief History of Tea
 *
 *这里只负责按：
 *
 * Paragraph A
 * Paragraph B
 * Paragraph C
 * ...
 *
 * 拆分正文。
 *
 * 这样以后加载其他 Reading Passage 时，
 * 不需要修改前端代码。
 */
function splitPassageContent(content: string): string[] {
  return content
    .split(/(?=Paragraph [A-Z])/)
    .map((part) => part.trim())
    .filter((part) => part.length > 0)
}
</script>

<template>
  <main>
    <!--
      后端请求还没有完成。
    -->
    <p v-if="loading">正在加载 Reading Test...</p>

    <!--
      API 请求失败。
    -->
    <p v-else-if="errorMessage">
      {{ errorMessage }}
    </p>

    <!--
      请求成功。
    -->
    <div v-else-if="testData">
      <!--
        Reading Test 页头。

        左边：
        Test 标题 + 来源

        右边：
        当前答题进度
      -->
      <div class="reading-header">
        <div>
          <h1 class="reading-title">
            {{ testData.test.title }}
          </h1>

          <p class="reading-source">
            来源：{{ testData.test.source }}
          </p>
        </div>

        <!--
          当前答题进度。

          answeredCount：
          已答题数量

          totalQuestionCount：
          总题数
        -->
        <div class="reading-actions">
          <div class="reading-progress">
            已答：{{ answeredCount }} / {{ totalQuestionCount }}
          </div>

          <!--
            提交整套 Reading Test。
          -->
          <button class="submit-button" :disabled="submitting" @click="submitAnswers">
            {{ submitting ? '提交中...' : '提交答案' }}
          </button>
        </div>
      </div>

      <!--
        遍历所有 Passage。
      -->
      <section v-for="passage in testData.passages" :key="passage.id">
        <!--
          当前 Reading Passage 的编号。
        -->
        <h2 class="passage-heading">
          <!--
            当前阶段先直接显示判分结果。

            下一阶段我们会再做正式 Result / Review 页面。
          -->
          <div v-if="submitResult" class="submit-result">
            <strong>
              得分：{{ submitResult.correctCount }} / {{ submitResult.totalQuestions }}
            </strong>

            <span>
              正确率：{{ submitResult.percentage.toFixed(2) }}%
            </span>
          </div>

          <p v-if="submitErrorMessage" class="submit-error">
            {{ submitErrorMessage }}
          </p>

          Reading Passage {{ passage.passageNumber }}
        </h2>

        <!--
          Passage 公共说明。

          这个字段现在正式来自后端：

          reading_passage.instruction
          ↓
          /full API
          ↓
          ReadingPassage TypeScript
          ↓
          Vue

          因为 instruction 属于整个 Passage，
          所以放在 Article / Questions 双栏上方。
        -->
        <p v-if="passage.instruction" class="passage-instruction">
          {{ passage.instruction }}
        </p>

        <div class="reading-layout">

          <!-- =========================
               左侧 Passage
          ========================== -->
          <div class="reading-passage">
            <h3 class="panel-title">
              Article
            </h3>

            <!--
              Passage 文章标题。

              例如：
              A Brief History of Tea

              现在直接读取后端 title，
              不再依赖 content 中的文字。
            -->
            <h4 v-if="passage.title" class="article-title">
              {{ passage.title }}
            </h4>

            <!--
              Passage 正文拆成多个段落。
            -->
            <div class="passage-content">
              <p v-for="(contentPart, index) in splitPassageContent(passage.content)" :key="index"
                class="passage-paragraph">
                {{ contentPart }}
              </p>
            </div>
          </div>

          <!-- =========================
               右侧 Questions
          ========================== -->
          <div class="reading-questions">
            <h3 class="panel-title">
              Questions
            </h3>

            <!--
              遍历当前 Passage 的 QuestionGroup。
            -->
            <div v-for="group in passage.questionGroups" :key="group.id" class="question-group">
              <!-- ==================================
                   MATCHING_HEADINGS
              =================================== -->
              <div v-if="group.questionType === 'MATCHING_HEADINGS'">
                <h3>
                  Matching Headings
                </h3>

                <!-- IELTS 原始题目说明 -->
                <p>
                  {{ group.instruction }}
                </p>

                <!--
                  Headings 参考选项区域。

                  这里和下面的答题区域单独分开，
                  让页面结构更加清楚。
                -->
                <div class="heading-options">
                  <h4>
                    List of Headings
                  </h4>

                  <ul>
                    <li v-for="option in group.options" :key="option.id">
                      <strong>
                        {{ option.optionValue }}
                      </strong>

                      {{ option.optionText }}
                    </li>
                  </ul>
                </div>

                <!--
                  真正的 Questions 答题区域。
                -->
                <div class="question-list">
                  <div v-for="question in group.questions" :key="question.id" class="question-item">
                    <p>
                      <strong>
                        {{ question.questionNumber }}.
                      </strong>

                      {{ question.questionText }}
                    </p>

                    <!--
                      v-model 会把用户答案保存到：

                      answers[question.id]
                    -->
                    <select v-model="answers[question.id]">
                      <option value="" disabled>
                        请选择 Heading
                      </option>

                      <option v-for="option in group.options" :key="option.id" :value="option.optionValue">
                        {{ option.optionValue }}
                        -
                        {{ option.optionText }}
                      </option>
                    </select>
                  </div>
                </div>
              </div>

              <!-- ==================================
                  MATCHING_FEATURES
              =================================== -->
              <div v-else-if="group.questionType === 'MATCHING_FEATURES'">
                <h3>
                  Matching Features
                </h3>

                <!--
                  IELTS 原始题目说明。
                -->
                <p>
                  {{ group.instruction }}
                </p>

                <!--
                  Features 参考选项区域。

                  例如：

                  A China
                  B Japan
                  C Portugal

                  这一块只是“参考选项”，
                  所以单独放进 feature-options。
                -->
                <div class="feature-options">
                  <h4>
                    Options
                  </h4>

                  <ul>
                    <li v-for="option in group.options" :key="option.id">
                      <strong>
                        {{ option.optionValue }}
                      </strong>

                      {{ option.optionText }}
                    </li>
                  </ul>
                </div>

                <!--
                  真正的答题区域。

                  每一道题都有自己的 select。
                -->
                <div class="question-list">
                  <div v-for="question in group.questions" :key="question.id" class="question-item">
                    <p>
                      <strong>
                        {{ question.questionNumber }}.
                      </strong>

                      {{ question.questionText }}
                    </p>

                    <!--
                      v-model 会继续保存：

                      answers[question.id]
                    -->
                    <select v-model="answers[question.id]">
                      <option value="" disabled>
                        请选择选项
                      </option>

                      <option v-for="option in group.options" :key="option.id" :value="option.optionValue">
                        {{ option.optionValue }}
                        -
                        {{ option.optionText }}
                      </option>
                    </select>
                  </div>
                </div>
              </div>

              <!-- ==================================
                   通用 fallback
              =================================== -->
              <div v-else>
                <h3>
                  {{ group.questionType }}
                </h3>

                <p>
                  {{ group.instruction }}
                </p>

                <div v-for="question in group.questions" :key="question.id">
                  <p>
                    <strong>
                      {{ question.questionNumber }}.
                    </strong>

                    {{ question.questionText }}
                  </p>

                  <select v-model="answers[question.id]">
                    <option value="" disabled>
                      请选择答案
                    </option>

                    <option v-for="option in group.options" :key="option.id" :value="option.optionValue">
                      {{ option.optionValue }}
                      -
                      {{ option.optionText }}
                    </option>
                  </select>
                </div>
              </div>

            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
/*
  Reading 页面整体区域。

  width: 100%
  使用父容器全部可用宽度。

  box-sizing: border-box
  让 padding 计算在 width 内部。
*/
main {
  width: 100%;
  max-width: none;
  box-sizing: border-box;

  padding: 32px 48px;
}

/*
  Reading 双栏主体。

  左侧：
  Passage

  右侧：
  Questions
*/
.reading-layout {
  display: grid;

  /*
    两栏平均分配空间。

    minmax(0, 1fr)
    可以避免里面的长文本或 select
    把 Grid 列强行撑宽。
  */
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);

  gap: 32px;
  width: 100%;

  /*
    给顶部标题区域留出空间。

    超出区域后，
    左右栏自己滚动。
  */
  height: calc(100vh - 230px);

  overflow: hidden;
}

/*
  左侧 Passage 区域。
*/
.reading-passage {
  min-width: 0;
  overflow-y: auto;

  padding: 24px;

  border: 1px solid #ddd;
  border-radius: 8px;

  line-height: 1.8;
}

/*
  右侧答题区域。
*/
.reading-questions {
  min-width: 0;
  overflow-y: auto;

  padding: 24px;

  border: 1px solid #ddd;
  border-radius: 8px;
}

/*
  右侧 select。

  width: 100%
  尽量占据当前题目区域。

  max-width:
  防止宽屏时 select 太长。
*/
.reading-questions select {
  width: 100%;
  max-width: 520px;

  padding: 6px 8px;
  margin-bottom: 10px;
}

/*
  QuestionGroup 之间保留距离。
*/
.reading-questions>div {
  margin-bottom: 32px;
}

/*
  Passage 正文整体。
*/
.passage-content {
  line-height: 1.8;
}

/*
  Passage 每个独立段落。
*/
.passage-paragraph {
  margin: 0 0 20px 0;
}

/*
  ==============================
  Matching Headings 参考选项区域
  ==============================

  这里把 List of Headings
  独立成一个视觉区域。

  目的：
  用户可以明显区分：

  上面 = 可以参考的 headings

  下面 = 真正需要作答的问题
*/
.heading-options {
  padding: 16px 20px;
  margin-bottom: 24px;

  border: 1px solid #ddd;
  border-radius: 8px;

  background: #f8f8f8;
}

/*
  List of Headings 标题。

  去掉顶部默认 margin，
  避免卡片顶部留白太大。
*/
.heading-options h4 {
  margin-top: 0;
}

/*
  Headings 列表底部不再额外留白。
*/
.heading-options ul {
  margin-bottom: 0;
}

/*
  ==============================
  Matching Headings 答题区域
  ==============================

  和上面的参考选项保持一定距离。
*/
.question-list {
  padding-top: 8px;
}

/*
  每一道题之间稍微分开。

  这样 Question 1、2、3
  不会全部挤在一起。
*/
.question-item {
  margin-bottom: 16px;
}

/*
  Question 文本和 select
  之间不要产生过大的默认间距。
*/
.question-item p {
  margin-bottom: 6px;
}

/*
  ==============================
  Matching Features 参考选项区域
  ==============================

  和 Matching Headings 使用同样的视觉结构。

  上面：
  参考选项

  下面：
  Questions
*/
.feature-options {
  padding: 16px 20px;
  margin-bottom: 24px;

  border: 1px solid #ddd;
  border-radius: 8px;

  background: #f8f8f8;
}

/*
  去掉 Options 标题顶部多余空间。
*/
.feature-options h4 {
  margin-top: 0;
}

/*
  去掉列表底部多余空间。
*/
.feature-options ul {
  margin-bottom: 0;
}

/*
  每一个 QuestionGroup 都作为一个独立区域。

  例如：
  - Matching Headings
  - Matching Features

  这样不同题型之间不会视觉上粘在一起。
*/
.question-group {
  padding-bottom: 32px;
  margin-bottom: 32px;

  border-bottom: 1px solid #e5e5e5;
}

/*
  最后一组题不需要底部分隔线。
*/
.question-group:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

/*
  ==============================
  Reading Test 顶部页头
  ==============================

  display: flex
  让标题信息和答题进度左右排列。
*/
.reading-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;

  gap: 24px;

  margin-bottom: 24px;
}

/*
  Reading Test 主标题。
*/
.reading-title {
  margin: 0 0 8px 0;
}

/*
  来源信息。

  用稍微弱一点的文字表现，
  避免和主标题抢视觉重点。
*/
.reading-source {
  margin: 0;

  font-size: 14px;
  color: #666;
}

/*
  答题进度。

  单独做成一个小区域，
  用户进入页面后可以很快看到当前进度。
*/
.reading-progress {
  padding: 8px 14px;

  border: 1px solid #ddd;
  border-radius: 8px;

  white-space: nowrap;

  font-weight: 600;
}

/*
  Passage 标题和下面双栏主体保持更紧凑的间距。
*/
.passage-heading {
  margin: 0 0 12px 0;

  font-size: 20px;
  font-weight: 600;
}

/*
  左右栏区域标题。

  作用：
  明确告诉用户当前区域是 Passage 还是 Questions。
*/
.panel-title {
  margin: 0 0 16px 0;

  padding-bottom: 10px;

  border-bottom: 1px solid #e5e5e5;

  font-size: 18px;
  font-weight: 600;
}

/*
  Passage 公共说明。

  它属于整个 Reading Passage，
  所以显示在双栏主体上方。

  使用稍弱的文字颜色，
  和文章正文区分开。
*/
.passage-instruction {
  margin: 0 0 16px 0;

  color: #555;
  line-height: 1.6;
}

/*
  Article 正式标题。

  例如：
  A Brief History of Tea
*/
.article-title {
  margin: 0 0 20px 0;

  font-size: 18px;
  font-weight: 600;
}

/*
  顶部答题操作区。
*/
.reading-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/*
  提交按钮。
*/
.submit-button {
  padding: 8px 16px;

  border: 1px solid #ccc;
  border-radius: 8px;

  cursor: pointer;
}

/*
  提交过程中禁止重复点击。
*/
.submit-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/*
  临时成绩显示区域。
*/
.submit-result {
  display: flex;
  gap: 20px;

  margin-bottom: 16px;
  padding: 12px 16px;

  border: 1px solid #ddd;
  border-radius: 8px;
}

/*
  提交失败提示。
*/
.submit-error {
  margin-bottom: 16px;
}
</style>