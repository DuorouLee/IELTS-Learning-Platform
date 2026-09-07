<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getFullReadingTest, type FullReadingTestResponse } from '@/api/reading'
import { useRoute } from 'vue-router'

/**
 * testData
 *
 * 用来保存后端：
 *
 * GET /api/reading/tests/{id}/full
 *
 * 返回的完整 Reading Test 数据。
 *
 * 一开始请求还没有完成，所以是 null。
 */
const testData = ref<FullReadingTestResponse | null>(null)

/**
 * loading
 *
 * 表示页面当前是否正在请求后端数据。
 *
 * true：
 * 正在加载
 *
 * false：
 * 请求已经结束
 */
const loading = ref(true)

/**
 * errorMessage
 *
 * 如果调用后端 API 失败，
 * 就把错误信息保存在这里。
 */
const errorMessage = ref('')

/**
 * route
 *
 * 表示当前浏览器访问的 Vue Router 路由。
 *
 * 例如：
 *
 * /reading/tests/3
 */
const route = useRoute()

/**
 * currentTestId
 *
 * 从当前 URL 动态读取 Reading Test id。
 *
 * 例如：
 *
 * /reading/tests/3
 *        ↓
 * route.params.testId = "3"
 *        ↓
 * currentTestId.value = 3
 *
 * 使用 computed 的原因是：
 * 当 URL 中的 testId 变化时，
 * currentTestId 也会自动变化。
 */
const currentTestId = computed(() => {
  return Number(route.params.testId)
})

/**
 * answers
 *
 * 用来保存用户当前选择的答案。
 *
 * 数据结构类似：
 *
 * {
 *   14: "viii",
 *   15: "iv",
 *   22: "D"
 * }
 *
 * key：
 * ReadingQuestion 的 id
 *
 * value：
 * 用户选择的 optionValue
 *
 * 例如：
 *
 * Question id = 14
 * 用户选择 heading "viii"
 *
 * 最终：
 *
 * answers[14] = "viii"
 *
 *
 * Question id = 22
 * 用户选择国家 "D"
 *
 * 最终：
 *
 * answers[22] = "D"
 */
const answers = ref<Record<number, string>>({})

/**
 * 每一套 Reading Test 都有自己独立的答案存储空间。
 *
 * Test 3：
 * reading-answers-3
 *
 * Test 4：
 * reading-answers-4
 */
const localStorageKey = computed(() => {
  return `reading-answers-${currentTestId.value}`
})
/**
 * 监听 answers 的变化。
 *
 * 每次用户选择答案后，
 * 都把最新 answers 保存到浏览器 localStorage。
 *
 * localStorage 只能保存字符串，
 * 所以要用 JSON.stringify 转换。
 */
watch(
  answers,
  (newAnswers) => {
    localStorage.setItem(localStorageKey.value, JSON.stringify(newAnswers))
  },
  {
    /**
     * answers 是对象，
     * deep: true 表示对象内部字段变化时也能监听到。
     */
    deep: true,
  },
)

/**
 * answeredCount
 *
 * computed 是 Vue 的“计算属性”。
 *
 * 它会根据 answers 自动重新计算。
 *
 * 比如：
 *
 * answers:
 *
 * {
 *   14: "viii",
 *   15: "iv",
 *   16: "ix"
 * }
 *
 * Object.values(answers.value)
 *
 * 得到：
 *
 * ["viii", "iv", "ix"]
 *
 * length = 3
 *
 * 所以页面会显示：
 *
 * 已答：3 / 13
 *
 * 当用户继续选择答案时，
 * answeredCount 会自动更新。
 */
const answeredCount = computed(() => {
  return Object.values(answers.value).filter((answer) => answer).length
})

/**
 * totalQuestionCount
 *
 * 自动计算当前 Reading Test 一共有多少道题。
 *
 * 数据结构是：
 *
 * testData
 * └── passages
 *     └── questionGroups
 *         └── questions
 *
 * 所以这里需要：
 *
 * 1. 遍历所有 Passage
 * 2. 遍历每个 Passage 的 QuestionGroup
 * 3. 把每个 group.questions.length 加起来
 */
const totalQuestionCount = computed(() => {
  /**
   * 如果后端数据还没加载完成，
   * testData.value 还是 null，
   * 那么总题数就是 0。
   */
  if (!testData.value) {
    return 0
  }

  /**
   * total 用来累计所有题目的数量。
   */
  let total = 0

  /**
   * 遍历所有 Passage。
   */
  for (const passage of testData.value.passages) {
    /**
     * 遍历当前 Passage 下的所有 QuestionGroup。
     */
    for (const group of passage.questionGroups) {
      /**
       * 当前 group.questions.length
       * 就是这一组有多少道题。
       *
       * 例如：
       *
       * MATCHING_HEADINGS = 8
       * MATCHING_FEATURES = 5
       *
       * 最终：
       *
       * total = 8 + 5 = 13
       */
      total += group.questions.length
    }
  }

  return total
})

/**
 * loadReadingTest
 *
 * 根据当前 URL 里的 testId：
 *
 * 1. 清理上一套题的页面状态
 * 2. 恢复当前 Test 的本地答案
 * 3. 请求后端完整 Reading Test
 */
async function loadReadingTest() {
  // 开始新的请求。
  loading.value = true

  // 清掉上一套题留下的错误信息。
  errorMessage.value = ''

  // 清掉上一套题的数据。
  testData.value = null

  /**
   * 先恢复当前 Test 保存过的答案。
   */
  const savedAnswers = localStorage.getItem(localStorageKey.value)

  if (savedAnswers) {
    answers.value = JSON.parse(savedAnswers)
  } else {
    /**
     * 如果当前 Test 从来没有保存过答案，
     * 就从空答案开始。
     */
    answers.value = {}
  }

  try {
    /**
     * 根据当前 URL 的 testId 请求后端。
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
 * 监听 URL 中 testId 的变化。
 *
 * immediate: true 表示：
 * 页面第一次打开时也立即执行一次。
 *
 * 所以它同时替代了原来的 onMounted。
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
</script>

<template>
  <main>
    <!--
      后端请求还没有完成时显示。
    -->
    <p v-if="loading">正在加载 Reading Test...</p>

    <!--
      如果 API 请求失败，
      显示错误信息。
    -->
    <p v-else-if="errorMessage">
      {{ errorMessage }}
    </p>

    <!--
      请求成功以后，
      testData 不再是 null，
      开始显示 Reading Test。
    -->
    <div v-else-if="testData">
      <!-- Reading Test 标题 -->
      <h1>
        {{ testData.test.title }}
      </h1>

      <!-- 数据来源 -->
      <p>来源：{{ testData.test.source }}</p>

      <!--
        当前答题进度。
        answeredCount 会随着 answers 的变化自动更新。
      -->
      <p>已答：{{ answeredCount }} / {{ totalQuestionCount }}</p>

      <!--
        遍历当前 Reading Test 的所有 Passage。
      -->
      <section v-for="passage in testData.passages" :key="passage.id">
        <!--
          当前 Passage 的标题。
          标题暂时放在双栏区域上方。
        -->
        <h2>Passage {{ passage.passageNumber }}</h2>

        <!--
          reading-layout

          这是 Reading 页面双栏布局的最外层容器。

          左边：
          Passage 正文

          右边：
          QuestionGroups / Questions

          目前我们只是建立布局骨架，
          不修改任何答题逻辑。
        -->
        <div class="reading-layout">

          <!--
            左侧区域：
            专门显示 Passage 正文。
          -->
          <div class="reading-passage">
            <p>
              {{ passage.content }}
            </p>
          </div>

          <!--
            右侧区域：
            放当前 Passage 下的所有题组。
          -->
          <div class="reading-questions">

            <!--
              这里继续保留你原来的 QuestionGroup 循环。
            -->
            <div v-for="group in passage.questionGroups" :key="group.id">

              <!--
                MATCHING_HEADINGS
              -->
              <div v-if="group.questionType === 'MATCHING_HEADINGS'">
                <h3>Matching Headings</h3>

                <p>
                  {{ group.instruction }}
                </p>

                <div>
                  <h4>List of Headings</h4>

                  <ul>
                    <li v-for="option in group.options" :key="option.id">
                      <strong>
                        {{ option.optionValue }}
                      </strong>

                      {{ option.optionText }}
                    </li>
                  </ul>
                </div>

                <div v-for="question in group.questions" :key="question.id">
                  <p>
                    <strong>
                      {{ question.questionNumber }}.
                    </strong>

                    {{ question.questionText }}
                  </p>

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

              <!--
                MATCHING_FEATURES
              -->
              <div v-else-if="group.questionType === 'MATCHING_FEATURES'">
                <h3>Matching Features</h3>

                <p>
                  {{ group.instruction }}
                </p>

                <div>
                  <h4>Options</h4>

                  <ul>
                    <li v-for="option in group.options" :key="option.id">
                      <strong>
                        {{ option.optionValue }}
                      </strong>

                      {{ option.optionText }}
                    </li>
                  </ul>
                </div>

                <div v-for="question in group.questions" :key="question.id">
                  <p>
                    <strong>
                      {{ question.questionNumber }}.
                    </strong>

                    {{ question.questionText }}
                  </p>

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

              <!--
                其他题型 fallback
              -->
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
  Reading 页面双栏布局。

  display: grid
  表示这个容器使用 CSS Grid。

  grid-template-columns: 1fr 1fr
  表示左右两栏各占一半宽度。
*/
.reading-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

/*
  左侧 Passage 区域。
  目前先不做复杂视觉样式，
  只确保正文区域可以独立显示。
*/
.reading-passage {
  min-width: 0;
}

/*
  右侧 Questions 区域。
*/
.reading-questions {
  min-width: 0;
}
</style>
