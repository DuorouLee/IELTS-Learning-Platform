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

// onMounted 表示：
// 当这个 Vue 页面加载完成后，自动执行里面的代码。
onMounted(async () => {
  try {
    // 调用我们刚才写的 API 方法。
    // 这里先固定获取 id = 1 的 Reading Test。
    testData.value = await getFullReadingTest(1)
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

        <!-- 遍历当前 Passage 里的 Question -->
        <div v-for="question in passage.questions" :key="question.id">
          <h3>Question {{ question.questionNumber }}</h3>

          <p>{{ question.questionText }}</p>
        </div>
      </section>
    </div>
  </main>
</template>
