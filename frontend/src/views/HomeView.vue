<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import {
  getReadingTests,
  type ReadingTest,
} from '@/api/reading'

/**
 * 保存后端返回的 Reading Test 列表。
 */
const readingTests = ref<ReadingTest[]>([])

/**
 * 页面是否正在加载 Reading Test。
 */
const loading = ref(true)

/**
 * 如果请求失败，
 * 保存错误信息。
 */
const errorMessage = ref('')

/**
 * 页面加载完成以后，
 * 自动请求 Reading Test 列表。
 */
onMounted(async () => {
  try {
    readingTests.value = await getReadingTests()
  } catch (error) {
    if (error instanceof Error) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = '获取 Reading Test 列表失败'
    }
  } finally {
    loading.value = false
  }
})

/**
 * 开始一次全新的 Reading Practice。
 *
 * ReadingTestView 实际保存答案使用的 key 是：
 *
 * reading-test-{testId}-answers
 *
 * 所以这里必须删除完全相同的 key。
 */
function startNewReadingPractice(testId: number) {
  const localStorageKey = `reading-test-${testId}-answers`

  localStorage.removeItem(localStorageKey)
}
</script>

<template>
  <main class="home-page">
    <header class="home-header">
      <h1>IELTS Learning Platform</h1>

      <p>
        当前先完成 IELTS Reading 学习流程。
      </p>
    </header>

    <section class="reading-section">
      <div class="section-header">
        <div>
          <h2>Reading</h2>

          <p>
            选择一套 Reading Test 开始练习。
          </p>
        </div>

        <RouterLink to="/reading/history" class="history-link">
          View Reading Practice History
        </RouterLink>
      </div>

      <!-- 正在加载 -->
      <p v-if="loading">
        正在加载 Reading Tests...
      </p>

      <!-- 加载失败 -->
      <p v-else-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </p>

      <!-- 没有 Test -->
      <p v-else-if="readingTests.length === 0">
        暂无 Reading Test
      </p>

      <!-- Reading Test 列表 -->
      <div v-else class="test-list">
        <article v-for="test in readingTests" :key="test.id" class="test-card">
          <div>
            <h3>
              {{ test.title }}
            </h3>

            <p class="test-source">
              来源：{{ test.source }}
            </p>
          </div>

          <!--
            注意：

            这里不能写死：
            /reading/tests/3

            因为当前页面是 v-for，
            每一张 Test Card 都有自己的 test.id。

            例如：

            Test 7
            → /reading/tests/7

            Test 12
            → /reading/tests/12
          -->
          <RouterLink :to="`/reading/tests/${test.id}`" class="practice-link" @click="startNewReadingPractice(test.id)">
            开始练习
          </RouterLink>
        </article>
      </div>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  width: 100%;
  padding: 32px 36px;
  box-sizing: border-box;
}

.home-header {
  margin-bottom: 32px;
}

.home-header h1 {
  margin: 0 0 10px;
}

.home-header p {
  margin: 0;
}

.reading-section {
  width: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;

  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0 0 8px;
}

.section-header p {
  margin: 0;
}

.history-link,
.practice-link {
  display: inline-block;

  padding: 9px 14px;

  border: 1px solid #ccc;
  border-radius: 8px;

  text-decoration: none;
}

.test-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.test-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;

  padding: 20px;

  border: 1px solid #ddd;
  border-radius: 10px;
}

.test-card h3 {
  margin: 0 0 8px;
}

.test-source {
  margin: 0;
}

.error-message {
  margin-top: 16px;
}

@media (max-width: 700px) {

  .section-header,
  .test-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>