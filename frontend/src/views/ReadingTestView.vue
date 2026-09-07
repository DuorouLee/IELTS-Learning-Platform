<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
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
 * route.params.testId 默认是字符串。
 *
 * 例如 URL：
 *
 * /reading/tests/3
 *
 * route.params.testId 得到：
 *
 * "3"
 *
 * Number(...) 把它转换成数字：
 *
 * 3
 */
const currentTestId = Number(route.params.testId)

console.log('route.params.testId =', route.params.testId)
console.log('currentTestId =', currentTestId)

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
 * 根据当前 Test id 自动生成 localStorage key。
 *
 * 当前：
 * reading-answers-3
 *
 * 以后如果切换到 Test 4：
 * reading-answers-4
 */
const localStorageKey = `reading-answers-${currentTestId}`

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
    localStorage.setItem(localStorageKey, JSON.stringify(newAnswers))
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
 * onMounted
 *
 * 当 ReadingTestView 页面加载完成以后，
 * Vue 会自动执行这里的代码。
 */
onMounted(async () => {
  try {
    /**
     * 页面加载时，
     * 先尝试读取之前保存在 localStorage 的答案。
     */
    const savedAnswers = localStorage.getItem(localStorageKey)

    if (savedAnswers) {
      /**
       * localStorage 保存的是字符串，
       * JSON.parse 把它重新变回对象。
       */
      answers.value = JSON.parse(savedAnswers)
    }

    /**
     * 再读取 Reading Test。
     */
    testData.value = await getFullReadingTest(currentTestId)
  } catch (error) {
    if (error instanceof Error) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = '发生未知错误'
    }
  } finally {
    loading.value = false
  }
})
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
        <!-- Passage 编号 -->
        <h2>Passage {{ passage.passageNumber }}</h2>

        <!-- Passage 正文 -->
        <p>
          {{ passage.content }}
        </p>

        <!--
          遍历当前 Passage 下面的 QuestionGroup。

          当前后端结构：

          Passage
          └── questionGroups
              ├── questionType
              ├── instruction
              ├── options
              └── questions
        -->
        <div v-for="group in passage.questionGroups" :key="group.id">
          <!--
            当前题组的题型。

            例如：

            MATCHING_HEADINGS

            MATCHING_FEATURES
          -->
          <h3>
            {{ group.questionType }}
          </h3>

          <!--
            IELTS 当前题组的答题说明。
          -->
          <p>
            {{ group.instruction }}
          </p>

          <!--
            显示当前题组所有可选答案。
          -->
          <ul>
            <li v-for="option in group.options" :key="option.id">
              <!--
                optionValue：

                MATCHING_HEADINGS：
                i / ii / iii ...

                MATCHING_FEATURES：
                A / B / C ...
              -->
              <strong>
                {{ option.optionValue }}
              </strong>

              {{ option.optionText }}
            </li>
          </ul>

          <!--
            遍历当前 QuestionGroup 下的所有题目。
          -->
          <div v-for="question in group.questions" :key="question.id">
            <!-- 显示 IELTS 题号 -->
            <h4>Question {{ question.questionNumber }}</h4>

            <!-- 显示题目正文 -->
            <p>
              {{ question.questionText }}
            </p>

            <!--
              用户选择答案。

              v-model 的作用：

              当用户选择一个 option 后，

              Vue 会自动保存：

              answers[question.id] = option.optionValue

              例如：

              answers[14] = "viii"

              或：

              answers[22] = "D"
            -->
            <select v-model="answers[question.id]">
              <!--
                用户还没有作答时显示。
              -->
              <option value="" disabled>请选择答案</option>

              <!--
                当前题组所有可选答案。
              -->
              <option v-for="option in group.options" :key="option.id" :value="option.optionValue">
                {{ option.optionValue }}
                -
                {{ option.optionText }}
              </option>
            </select>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
