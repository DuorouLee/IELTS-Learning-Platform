<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import {
    getReadingPracticeHistoryDetail,
    type ReadingPracticeHistoryDetail,
} from '@/api/reading'

/**
 * 当前路由对象。
 *
 * 用于读取：
 *
 * /reading/history/:id
 *
 * 中的 id。
 */
const route = useRoute()

/**
 * 保存后端返回的某一次 Reading Practice 详情。
 */
const historyDetail =
    ref<ReadingPracticeHistoryDetail | null>(null)

/**
 * 是否正在加载。
 */
const loading = ref(true)

/**
 * 如果请求失败，
 * 保存错误信息。
 */
const errorMessage = ref('')

/**
 * 页面加载完成以后，
 * 自动请求 History Detail API。
 */
onMounted(async () => {
    try {
        /**
         * route.params.id 默认是 string，
         * 所以这里转换成 number。
         */
        const historyId = Number(route.params.id)

        historyDetail.value =
            await getReadingPracticeHistoryDetail(historyId)
    } catch (error) {
        errorMessage.value =
            error instanceof Error
                ? error.message
                : 'Failed to load history detail'
    } finally {
        loading.value = false
    }
})
</script>

<template>
    <main class="history-detail-page">

        <!-- 加载状态 -->
        <p v-if="loading">
            Loading history detail...
        </p>

        <!-- 错误状态 -->
        <p v-else-if="errorMessage">
            {{ errorMessage }}
        </p>

        <!-- 正常内容 -->
        <section v-else-if="historyDetail">

            <h1>
                {{ historyDetail.testTitle }}
            </h1>

            <p>
                Score:
                {{ historyDetail.correctCount }}
                /
                {{ historyDetail.totalQuestions }}
            </p>

            <p>
                Accuracy:
                {{ historyDetail.percentage.toFixed(1) }}%
            </p>

            <hr />

            <h2>Question Review</h2>

            <div v-for="answer in historyDetail.answers" :key="answer.questionId" class="answer-card">
                <h3>
                    Question {{ answer.questionNumber }}
                </h3>

                <p>
                    Your answer:
                    {{ answer.userAnswer || 'Not answered' }}
                </p>

                <p>
                    Correct answer:
                    {{ answer.correctAnswer }}
                </p>

                <p>
                    Result:
                    <strong>
                        {{ answer.correct ? 'Correct' : 'Incorrect' }}
                    </strong>
                </p>
            </div>

        </section>

    </main>
</template>

<style scoped>
.history-detail-page {
    max-width: 900px;
    margin: 0 auto;
    padding: 32px;
}

.answer-card {
    margin-top: 16px;
    padding: 16px;
    border: 1px solid #ddd;
    border-radius: 8px;
}
</style>