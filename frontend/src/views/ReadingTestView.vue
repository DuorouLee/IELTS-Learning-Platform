<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getFullReadingTest, type FullReadingTestResponse } from '@/api/reading'

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
 * onMounted
 *
 * 当 ReadingTestView 页面加载完成以后，
 * Vue 会自动执行这里的代码。
 */
onMounted(async () => {
  try {
    /**
     * 调用后端完整 Reading Test API。
     *
     * 当前先固定读取：
     *
     * ReadingTest id = 3
     *
     * 因为我们刚刚重新导入成功的数据 id 是 3。
     *
     * 后续项目继续开发时，
     * 会改成从 Vue Router 的 URL 参数读取 testId。
     *
     * 例如：
     *
     * /reading/tests/3
     *
     * 现在先不提前做。
     */
    testData.value = await getFullReadingTest(3)
  } catch (error) {
    /**
     * 请求失败。
     *
     * 如果是标准 JavaScript Error，
     * 就读取里面的 message。
     */
    if (error instanceof Error) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = '发生未知错误'
    }
  } finally {
    /**
     * 不管请求成功还是失败，
     * 请求结束以后都停止 loading。
     */
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

        当前总题数先固定为 13。
        下一步我们再改成动态计算。
      -->
      <p>已答：{{ answeredCount }} / 13</p>

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
