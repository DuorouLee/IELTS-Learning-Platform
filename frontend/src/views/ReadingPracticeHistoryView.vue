<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
    deleteReadingPracticeRecord,
    getReadingPracticeHistory,
    type ReadingPracticeRecord,
} from '@/api/reading'

const records = ref<ReadingPracticeRecord[]>([])
/**
 * 总练习次数。
 */
const practiceCount = computed(() => records.value.length)

/**
 * 平均正确率。
 */
const averageAccuracy = computed(() => {
    if (records.value.length === 0) {
        return 0
    }

    const total = records.value.reduce(
        (sum, record) => sum + record.percentage,
        0,
    )

    return total / records.value.length
})

/**
 * 历史最佳正确率。
 */
const bestAccuracy = computed(() => {
    if (records.value.length === 0) {
        return 0
    }

    return Math.max(
        ...records.value.map((record) => record.percentage),
    )
})
const loading = ref(true)
const errorMessage = ref('')

function formatSubmittedAt(timestamp: number) {
    return new Date(timestamp).toLocaleString()
}

async function loadHistory() {
    loading.value = true
    errorMessage.value = ''

    try {
        records.value = await getReadingPracticeHistory()
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '加载 Reading Practice History 失败'
        }
    } finally {
        loading.value = false
    }
}

async function deleteRecord(recordId: number) {
    try {
        await deleteReadingPracticeRecord(recordId)

        /**
         * 删除成功后，
         * 直接把前端数组中的这条记录移除。
         *
         * 这样不用重新刷新整个页面。
         */
        records.value = records.value.filter(
            (record) => record.id !== recordId,
        )
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '删除 Reading Practice History 失败'
        }
    }
}

onMounted(() => {
    loadHistory()
})
</script>

<template>
    <main class="history-page">
        <RouterLink to="/" class="back-link">
            ← Back to Home
        </RouterLink>
        <h1>Reading Practice History</h1>

        <section v-if="!loading && !errorMessage && records.length > 0" class="history-summary">
            <div class="summary-item">
                <strong>{{ practiceCount }}</strong>
                <span>Practice Count</span>
            </div>

            <div class="summary-item">
                <strong>{{ averageAccuracy.toFixed(2) }}%</strong>
                <span>Average Accuracy</span>
            </div>

            <div class="summary-item">
                <strong>{{ bestAccuracy.toFixed(2) }}%</strong>
                <span>Best Accuracy</span>
            </div>
        </section>

        <p v-if="loading">
            Loading...
        </p>

        <p v-else-if="errorMessage" class="error-message">
            {{ errorMessage }}
        </p>

        <p v-else-if="records.length === 0">
            暂无 Reading 练习记录。
        </p>

        <div v-else class="history-list">
            <article v-for="record in records" :key="record.id" class="history-card">
                <div>
                    <h2>
                        <RouterLink :to="`/reading/tests/${record.testId}`" class="test-link">
                            {{ record.testTitle }}
                        </RouterLink>
                    </h2>

                    <p>
                        {{ formatSubmittedAt(record.submittedAt) }}
                    </p>
                </div>

                <div class="history-result">
                    <strong>
                        {{ record.correctCount }} / {{ record.totalQuestions }}
                    </strong>

                    <span>
                        {{ record.percentage.toFixed(2) }}%
                    </span>

                    <button class="delete-button" @click="deleteRecord(record.id)">
                        Delete
                    </button>
                </div>
            </article>
        </div>
    </main>
</template>

<style scoped>
.history-page {
    width: 100%;
    padding: 32px 36px;
    box-sizing: border-box;
}

.history-page h1 {
    margin-top: 0;
    margin-bottom: 24px;
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.history-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 24px;

    padding: 20px;

    border: 1px solid #ddd;
    border-radius: 10px;
}

.history-card h2 {
    margin: 0 0 8px;
    font-size: 18px;
}

.history-card p {
    margin: 0;
}

.history-result {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
}

.history-result strong {
    font-size: 22px;
}

.error-message {
    margin-top: 16px;
}

.test-link {
    text-decoration: none;
}

.test-link:hover {
    text-decoration: underline;
}

.back-link {
    display: inline-block;
    margin-bottom: 16px;
    text-decoration: none;
}

.back-link:hover {
    text-decoration: underline;
}

.history-summary {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;

    margin-bottom: 24px;
}

.summary-item {
    display: flex;
    flex-direction: column;
    gap: 6px;

    padding: 18px;

    border: 1px solid #ddd;
    border-radius: 10px;
}

.summary-item strong {
    font-size: 24px;
}

.summary-item span {
    font-size: 14px;
}

.delete-button {
    margin-top: 8px;
    padding: 6px 10px;

    border: 1px solid #ccc;
    border-radius: 6px;

    cursor: pointer;
}
</style>