<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import {
  getReadingTests,
  type ReadingTest
} from '@/api/reading'

/**
 * readingTests
 *
 * 保存后端返回的 Reading Test 列表。
 *
 * 例如：
 *
 * [
 *   {
 *     id: 3,
 *     title: "A Brief History of Tea",
 *     source: "..."
 *   }
 * ]
 */
const readingTests = ref<ReadingTest[]>([])

/**
 * loading
 *
 * true：
 * 正在从 Spring Boot 获取 Reading Test 列表。
 *
 * false：
 * 请求已经结束。
 */
const loading = ref(true)

/**
 * errorMessage
 *
 * 如果后端请求失败，
 * 就把错误信息保存在这里。
 */
const errorMessage = ref('')

/**
 * onMounted
 *
 * HomeView 页面加载完成以后，
 * 自动请求：
 *
 * GET /api/reading/tests
 */
onMounted(async () => {
  try {
    /**
     * 调用 reading.ts 中刚刚完成的 API 方法。
     *
     * 后端返回的列表会保存到：
     *
     * readingTests.value
     */
    readingTests.value = await getReadingTests()
  } catch (error) {

    /**
     * 如果请求失败，
     * 页面显示错误信息。
     */
    if (error instanceof Error) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = '获取 Reading Test 列表失败'
    }

  } finally {

    /**
     * 不管成功还是失败，
     * 请求结束以后都停止 loading。
     */
    loading.value = false
  }
})
</script>

<template>
  <main>
    <h1>IELTS Learning Platform</h1>

    <p>
      当前先完成 IELTS Reading 学习流程。
    </p>

    <section>
      <h2>Reading</h2>

      <!--
        后端请求还没有完成时显示。
      -->
      <p v-if="loading">
        正在加载 Reading Tests...
      </p>

      <!--
        API 请求失败时显示。
      -->
      <p v-else-if="errorMessage">
        {{ errorMessage }}
      </p>

      <!--
        请求成功，但是数据库没有 Reading Test。
      -->
      <p v-else-if="readingTests.length === 0">
        暂无 Reading Test
      </p>

      <!--
        遍历后端返回的 Reading Test。

        以后数据库里有：
        Test 3
        Test 4
        Test 5

        页面就会自动显示三份题目。
      -->
      <div v-else v-for="test in readingTests" :key="test.id">
        <!-- Reading Test 标题 -->
        <h3>
          {{ test.title }}
        </h3>

        <!-- 题目来源 -->
        <p>
          来源：{{ test.source }}
        </p>

        <!--
          动态生成路由。

          如果：
          test.id = 3

          最终地址就是：
          /reading/tests/3

          如果以后：
          test.id = 4

          地址就自动变成：
          /reading/tests/4
        -->
        <RouterLink :to="`/reading/tests/${test.id}`">
          开始练习
        </RouterLink>
      </div>
    </section>
  </main>
</template>