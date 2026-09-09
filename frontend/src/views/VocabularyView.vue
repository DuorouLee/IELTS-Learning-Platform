<script setup lang="ts">
import { onMounted, ref } from 'vue'

import {
    getVocabularyWords,
    type VocabularyWord,
} from '@/api/vocabulary'

/**
 * words
 *
 * 保存后端返回的 Vocabulary Word 列表。
 */
const words = ref<VocabularyWord[]>([])

/**
 * loading
 *
 * true：
 * 正在从 Spring Boot 获取数据。
 *
 * false：
 * 请求已经完成。
 */
const loading = ref(true)

/**
 * errorMessage
 *
 * 如果请求失败，
 * 就把错误信息显示在页面上。
 */
const errorMessage = ref('')

/**
 * 页面加载完成以后，
 * 自动调用：
 *
 * GET /api/vocabulary/words
 */
onMounted(async () => {
    try {
        words.value = await getVocabularyWords()
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '获取 Vocabulary Word 列表失败'
        }
    } finally {
        loading.value = false
    }
})
</script>

<template>
    <main>
        <h1>Vocabulary</h1>

        <!-- 后端请求还没有完成 -->
        <p v-if="loading">
            Loading vocabulary...
        </p>

        <!-- 请求失败 -->
        <p v-else-if="errorMessage">
            {{ errorMessage }}
        </p>

        <!-- 数据库目前没有单词 -->
        <p v-else-if="words.length === 0">
            暂无单词
        </p>

        <!-- 显示 Vocabulary Word 列表 -->
        <div v-else>
            <article v-for="word in words" :key="word.id" class="word-item">
                <h2>{{ word.word }}</h2>

                <p>
                    {{ word.meaning }}
                </p>

                <p v-if="word.exampleSentence">
                    <strong>Example:</strong>
                    {{ word.exampleSentence }}
                </p>
            </article>
        </div>
    </main>
</template>

<style scoped>
main {
    max-width: 900px;
    margin: 0 auto;
    padding: 32px;
}

.word-item {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #ddd;
}
</style>