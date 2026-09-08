<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import {
  getReadingTests,
  type ReadingTest,
} from '@/api/reading'

const readingTests = ref<ReadingTest[]>([])
const loading = ref(true)
const errorMessage = ref('')

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

      <p v-if="loading">
        正在加载 Reading Tests...
      </p>

      <p v-else-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </p>

      <p v-else-if="readingTests.length === 0">
        暂无 Reading Test
      </p>

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

          <RouterLink :to="`/reading/tests/${test.id}`" class="practice-link">
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