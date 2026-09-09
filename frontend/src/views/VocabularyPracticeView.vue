<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import {
    getVocabularyWords,
    updateVocabularyLearningStatus,
    type VocabularyWord,
} from '@/api/vocabulary'

/**
 * 当前需要练习的单词。
 *
 * 第一版 Practice 只练：
 *
 * LEARNING
 *
 * 已经 MASTERED 的单词暂时不会进入练习列表。
 */
const words = ref<VocabularyWord[]>([])

/**
 * 当前正在练习第几个单词。
 */
const currentIndex = ref(0)

/**
 * 是否已经显示答案。
 *
 * false：
 * 只看到英文单词。
 *
 * true：
 * 显示释义和例句。
 */
const showAnswer = ref(false)

const loading = ref(true)
const errorMessage = ref('')

/**
 * 当前正在学习的单词。
 *
 * 如果列表为空，
 * 返回 null。
 */
const currentWord = computed(() => {
    return words.value[currentIndex.value] ?? null
})

/**
 * 进入 Practice 时，
 * 一共有多少个 LEARNING 单词。
 *
 * 这个数字不会因为点击 I Know 而减少，
 * 用来表示本轮 Practice 的初始任务量。
 */
const initialWordCount = ref(0)

/**
 * 本轮已经标记为 MASTERED 的数量。
 */
const masteredThisSession = ref(0)

/**
 * 页面加载时获取所有 Vocabulary Word。
 *
 * 然后只保留：
 *
 * learningStatus === 'LEARNING'
 *
 * 的单词。
 */
onMounted(async () => {
    try {
        const allWords = await getVocabularyWords()

        words.value = allWords.filter(
            word => word.learningStatus === 'LEARNING',
        )

        /**
         * 保存本轮开始时的单词数量。
         */
        initialWordCount.value = words.value.length
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '加载 Vocabulary Practice 失败'
        }
    } finally {
        loading.value = false
    }
})

/**
 * 显示当前单词的答案。
 */
function revealAnswer() {
    showAnswer.value = true
}

/**
 * 进入下一个单词。
 */
function goToNextWord() {
    if (words.value.length === 0) {
        return
    }

    currentIndex.value =
        (currentIndex.value + 1) % words.value.length

    /**
     * 切换到新单词以后，
     * 再次隐藏答案。
     */
    showAnswer.value = false
}

/**
 * I Know
 *
 * 用户认为自己已经掌握这个单词。
 *
 * 后端：
 *
 * LEARNING → MASTERED
 *
 * 更新成功以后，
 * 当前单词会从 Practice 列表中移除。
 */
async function handleKnowWord() {
    const word = currentWord.value

    if (!word) {
        return
    }

    try {
        await updateVocabularyLearningStatus(
            word.id,
            'MASTERED',
        )

        masteredThisSession.value += 1

        /**
         * MASTERED 单词不再属于当前 Practice。
         */
        words.value = words.value.filter(
            item => item.id !== word.id,
        )

        /**
         * 删除当前单词以后，
         * currentIndex 有可能超过新的数组长度。
         *
         * 例如：
         *
         * 原来有 3 个单词，当前 index = 2
         * 删除后只剩 2 个，
         * 最大 index 就只能是 1。
         */
        if (
            currentIndex.value >= words.value.length &&
            words.value.length > 0
        ) {
            currentIndex.value = 0
        }

        showAnswer.value = false
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '修改学习状态失败'
        }
    }
}

/**
 * Still Learning
 *
 * 用户认为这个单词还没有掌握。
 *
 * 保持：
 *
 * LEARNING
 *
 * 然后继续下一个单词。
 */
async function handleStillLearning() {
    const word = currentWord.value

    if (!word) {
        return
    }

    try {
        await updateVocabularyLearningStatus(
            word.id,
            'LEARNING',
        )

        goToNextWord()
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '修改学习状态失败'
        }
    }
}
</script>

<template>
    <main>
        <h1>Vocabulary Practice</h1>

        <p v-if="loading">
            Loading...
        </p>

        <p v-else-if="errorMessage">
            {{ errorMessage }}
        </p>

        <!--
            所有 LEARNING 单词都被标记为 MASTERED 后，
            Practice 就完成了。
        -->
        <section v-else-if="words.length === 0">
            <h2>Practice Complete</h2>

            <p>
                本轮单词：
                <strong>{{ initialWordCount }}</strong>
            </p>

            <p>
                本轮掌握：
                <strong>{{ masteredThisSession }}</strong>
            </p>

            <p>
                Remaining Learning:
                <strong>0</strong>
            </p>
        </section>

        <section v-else-if="currentWord" class="practice-card">
            <p>
                {{ currentIndex + 1 }} / {{ words.length }}
            </p>

            <h2>
                {{ currentWord.word }}
            </h2>

            <!-- 第一步：先让用户自己回忆 -->
            <button v-if="!showAnswer" type="button" @click="revealAnswer">
                Show Answer
            </button>

            <!-- 第二步：显示答案 -->
            <div v-else>
                <p>
                    <strong>Meaning:</strong>
                    {{ currentWord.meaning }}
                </p>

                <p v-if="currentWord.exampleSentence">
                    <strong>Example:</strong>
                    {{ currentWord.exampleSentence }}
                </p>

                <!--
          用户根据自己的掌握情况选择。
        -->
                <button type="button" @click="handleStillLearning">
                    Still Learning
                </button>

                <button type="button" @click="handleKnowWord">
                    I Know
                </button>
            </div>
        </section>
    </main>
</template>

<style scoped>
main {
    max-width: 700px;
    margin: 0 auto;
    padding: 32px;
}

.practice-card {
    padding: 24px;
    border: 1px solid #ddd;
}

button {
    margin-right: 12px;
}
</style>