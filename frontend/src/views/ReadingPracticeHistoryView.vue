<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
    getReadingPracticeHistory,
    type ReadingPracticeRecord,
} from '@/api/reading'

const records = ref<ReadingPracticeRecord[]>([])
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

onMounted(() => {
    loadHistory()
})
</script>

<template>
    <main class="history-page">
        <h1>Reading Practice History</h1>

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
</style>