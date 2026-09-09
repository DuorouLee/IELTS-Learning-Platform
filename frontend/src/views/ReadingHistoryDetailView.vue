<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

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
 * 把 submittedAt 时间戳
 * 转换成用户可读的日期时间。
 *
 * 例如：
 *
 * 1788876101847
 *
 * 会转换成类似：
 *
 * 2026/9/8 21:21:41
 */
function formatSubmittedAt(timestamp: number) {
    return new Date(timestamp).toLocaleString()
}

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
        <p v-else-if="errorMessage" class="error-message">
            {{ errorMessage }}
        </p>

        <!-- 正常内容 -->
        <section v-else-if="historyDetail">

            <!-- 返回 History 列表 -->
            <RouterLink to="/reading/history" class="back-link">
                ← Back to Reading History
            </RouterLink>

            <!-- Test 标题 -->
            <h1>
                {{ historyDetail.testTitle }}
            </h1>

            <!-- 本次成绩 -->
            <div class="history-summary">

                <div class="summary-item">
                    <span>Score</span>

                    <strong>
                        {{ historyDetail.correctCount }}
                        /
                        {{ historyDetail.totalQuestions }}
                    </strong>
                </div>

                <div class="summary-item">
                    <span>Accuracy</span>

                    <strong>
                        {{ historyDetail.percentage.toFixed(1) }}%
                    </strong>
                </div>

            </div>

            <!-- 提交时间 -->
            <p class="submitted-time">
                Submitted:
                {{ formatSubmittedAt(historyDetail.submittedAt) }}
            </p>

            <hr />

            <!-- 逐题 Review -->
            <h2>
                Question Review
            </h2>

            <div v-for="answer in historyDetail.answers" :key="answer.questionId" class="answer-card" :class="{
                correct: answer.correct,
                incorrect: !answer.correct,
            }">
                <h3>
                    Question {{ answer.questionNumber }}
                </h3>

                <p>
                    Your answer:
                    <strong>
                        {{ answer.userAnswer || 'Not answered' }}
                    </strong>
                </p>

                <p>
                    Correct answer:
                    <strong>
                        {{ answer.correctAnswer }}
                    </strong>
                </p>

                <p>
                    Result:

                    <strong>
                        {{ answer.correct ? '✓ Correct' : '✗ Incorrect' }}
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

/**
 * 返回 History 列表。
 */
.back-link {
    display: inline-block;
    margin-bottom: 16px;
    text-decoration: none;
}

.back-link:hover {
    text-decoration: underline;
}

/**
 * 成绩概要区域。
 */
.history-summary {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    margin-top: 24px;
    margin-bottom: 16px;
}

.summary-item {
    display: flex;
    flex-direction: column;
    gap: 6px;

    padding: 18px;

    border: 1px solid #ddd;
    border-radius: 10px;
}

.summary-item span {
    font-size: 14px;
}

.summary-item strong {
    font-size: 24px;
}

/**
 * 提交时间。
 */
.submitted-time {
    margin-bottom: 24px;
}

/**
 * 每一道题的历史 Review。
 */
.answer-card {
    margin-top: 16px;
    padding: 16px;

    border: 1px solid #ddd;
    border-radius: 8px;
}

/**
 * 答对的题。
 */
.answer-card.correct {
    border-left: 4px solid #2e7d32;
}

/**
 * 答错 / 未作答的题。
 */
.answer-card.incorrect {
    border-left: 4px solid #c62828;
}

.answer-card h3 {
    margin-top: 0;
}

.error-message {
    margin-top: 16px;
}
</style>