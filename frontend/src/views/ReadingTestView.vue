<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getFullReadingTest, type FullReadingTestResponse } from '@/api/reading'

// testData 用来保存后端返回的数据。
// 一开始还没请求完成，所以是 null。
const testData = ref<FullReadingTestResponse | null>(null)

// loading 表示页面是否正在加载。
const loading = ref(true)

// errorMessage 用来保存错误信息。
const errorMessage = ref('')

/**
 * answers 用来保存用户当前选择的答案。
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
 * 用户选择的 optionKey
 *
 * 这样每一道题都有自己独立的答案状态。
 */
const answers = ref<Record<number, string>>({})

// onMounted 表示：
// 当这个 Vue 页面加载完成后，自动执行里面的代码。
onMounted(async () => {
  try {
    // 调用我们刚才写的 API 方法。
    // 目前先固定读取刚刚重新导入成功的 Reading Test。
    //
    // 当前数据库中这份完整新结构数据的 id = 3。
    //
    // 后续我们会再把这里改成从路由参数读取，
    // 现在先不提前做，保持当前阶段最简单。
    testData.value = await getFullReadingTest(3)
  } catch (error) {
    // 如果请求失败，就显示错误信息。
    if (error instanceof Error) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = '发生未知错误'
    }
  } finally {
    // 不管成功还是失败，请求结束后都停止 loading。
    loading.value = false
  }
})
</script>

<template>
  <main>
    <!-- 请求还没完成时显示 -->
    <p v-if="loading">正在加载 Reading Test...</p>

    <!-- 请求失败时显示 -->
    <p v-else-if="errorMessage">
      {{ errorMessage }}
    </p>

    <!-- 请求成功后显示 -->
    <div v-else-if="testData">
      <h1>{{ testData.test.title }}</h1>

      <p>来源：{{ testData.test.source }}</p>

      <!-- 遍历所有 Passage -->
      <section v-for="passage in testData.passages" :key="passage.id">
        <h2>Passage {{ passage.passageNumber }}</h2>

        <p>{{ passage.content }}</p>

        <!--
          遍历当前 Passage 里的 QuestionGroup。

          后端现在返回的结构是：

          Passage
          └── questionGroups
              ├── questionType
              ├── instruction
              ├── options
              └── questions
        -->
        <div v-for="group in passage.questionGroups" :key="group.id">
          <!--
            显示当前题组的题型。

            例如：
            MATCHING_HEADINGS
            MATCHING_FEATURES
          -->
          <h3>{{ group.questionType }}</h3>

          <!--
            IELTS 原始答题说明。

            例如：
            Choose the correct heading for each paragraph...
          -->
          <p>{{ group.instruction }}</p>

          <!--
            显示当前题组可以选择的所有答案。

            MATCHING_HEADINGS:
            i   xxx
            ii  xxx
            iii xxx

            MATCHING_FEATURES:
            A   xxx
            B   xxx
            C   xxx
          -->
          <ul>
            <li v-for="option in group.options" :key="option.id">
              <strong>{{ option.optionValue }}</strong>
              {{ option.optionText }}
            </li>
          </ul>

          <!--
            遍历这个 QuestionGroup 下面真正的题目。
          -->
          <div v-for="question in group.questions" :key="question.id">
            <!-- 显示题号 -->
            <h4>Question {{ question.questionNumber }}</h4>

            <!-- 显示题目正文 -->
            <p>{{ question.questionText }}</p>

            <!--
              当前先只做最基础的答案选择框。

              这里的 options 来自当前 QuestionGroup。

              MATCHING_HEADINGS 会显示：
              i / ii / iii ...

              MATCHING_FEATURES 会显示：
              A / B / C ...
            -->
            <select v-model="answers[question.id]">
              <!--
                value="" 表示目前还没有选择答案。

                因为现在使用了 v-model，
                Vue 会自动把用户选中的 value
                保存到 answers[question.id]。
              -->
              <option value="" disabled>请选择答案</option>

              <option v-for="option in group.options" :key="option.id" :value="option.optionValue">
                {{ option.optionValue }} - {{ option.optionText }}
              </option>
            </select>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
