<script setup lang="ts">
import { onMounted, ref } from 'vue'

import {
    createVocabularyWord,
    deleteVocabularyWord,
    getVocabularyWords,
    updateVocabularyLearningStatus,
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
 * 新增单词表单数据。
 */
const newWord = ref('')
const newMeaning = ref('')
const newExampleSentence = ref('')

/**
 * submitting
 *
 * 防止用户连续点击提交按钮。
 */
const submitting = ref(false)

/**
 * 新增单词时的错误信息。
 */
const createErrorMessage = ref('')

/**
 * 创建一个新的 Vocabulary Word。
 *
 * 成功后：
 * 1. 把新单词加入当前列表
 * 2. 清空表单
 *
 * 这样不需要重新刷新整个页面。
 */
async function handleCreateWord() {
    createErrorMessage.value = ''

    /**
     * 最基础的前端校验。
     *
     * word 和 meaning 是数据库 NOT NULL，
     * 所以这里要求用户填写。
     */
    if (!newWord.value.trim() || !newMeaning.value.trim()) {
        createErrorMessage.value = 'Word 和 Meaning 不能为空'
        return
    }

    submitting.value = true

    try {
        const createdWord = await createVocabularyWord({
            word: newWord.value.trim(),
            meaning: newMeaning.value.trim(),
            exampleSentence: newExampleSentence.value.trim(),
        })

        /**
         * 把后端刚返回的新单词直接加入页面列表。
         */
        words.value.push(createdWord)

        /**
         * 创建成功后清空输入框。
         */
        newWord.value = ''
        newMeaning.value = ''
        newExampleSentence.value = ''
    } catch (error) {
        if (error instanceof Error) {
            createErrorMessage.value = error.message
        } else {
            createErrorMessage.value = '创建 Vocabulary Word 失败'
        }
    } finally {
        submitting.value = false
    }
}

/**
 * 删除指定单词。
 *
 * 删除成功以后，
 * 同时从当前页面的 words 数组中移除它，
 * 这样不需要刷新整个页面。
 */
async function handleDeleteWord(id: number) {
    try {
        await deleteVocabularyWord(id)

        words.value = words.value.filter(
            word => word.id !== id,
        )
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '删除 Vocabulary Word 失败'
        }
    }
}

/**
 * 切换单词学习状态。
 *
 * LEARNING → MASTERED
 *
 * MASTERED → LEARNING
 */
async function handleToggleLearningStatus(
    word: VocabularyWord,
) {
    /**
     * 根据当前状态决定下一个状态。
     */
    const nextStatus =
        word.learningStatus === 'MASTERED'
            ? 'LEARNING'
            : 'MASTERED'

    try {
        /**
         * 调用后端修改数据库。
         */
        const updatedWord =
            await updateVocabularyLearningStatus(
                word.id,
                nextStatus,
            )

        /**
         * 找到页面数组中的这个单词。
         */
        const index = words.value.findIndex(
            item => item.id === word.id,
        )

        /**
         * 使用后端返回的新数据
         * 替换页面里的旧数据。
         */
        if (index !== -1) {
            words.value[index] = updatedWord
        }
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '修改学习状态失败'
        }
    }
}

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

        <section class="create-word-form">
            <h2>Add Word</h2>

            <div>
                <label for="word">
                    Word
                </label>

                <input id="word" v-model="newWord" type="text" placeholder="例如：abandon" />
            </div>

            <div>
                <label for="meaning">
                    Meaning
                </label>

                <input id="meaning" v-model="newMeaning" type="text" placeholder="例如：放弃；抛弃" />
            </div>

            <div>
                <label for="exampleSentence">
                    Example Sentence
                </label>

                <textarea id="exampleSentence" v-model="newExampleSentence"
                    placeholder="例如：They had to abandon the plan." />

            </div>

            <p v-if="createErrorMessage">
                {{ createErrorMessage }}
            </p>

            <button type="button" :disabled="submitting" @click="handleCreateWord">
                {{ submitting ? 'Adding...' : 'Add Word' }}
            </button>
        </section>

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

                <p>
                    <strong>Status:</strong>
                    {{ word.learningStatus }}
                </p>

                <button type="button" @click="handleToggleLearningStatus(word)">
                    {{
                        word.learningStatus === 'MASTERED'
                            ? 'Mark as Learning'
                    : 'Mark as Mastered'
                    }}
                </button>

                <button type="button" @click="handleDeleteWord(word.id)">
                    Delete
                </button>
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

.create-word-form {
    margin-bottom: 32px;
}

.create-word-form div {
    margin-bottom: 12px;
}

.create-word-form label {
    display: block;
    margin-bottom: 4px;
}

.create-word-form input,
.create-word-form textarea {
    width: 100%;
    padding: 8px;
}
</style>