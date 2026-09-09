<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

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
 * Vocabulary 总单词数。
 */
const totalWords = computed(() => {
    return words.value.length
})

/**
 * 还在学习的单词数量。
 */
const learningWords = computed(() => {
    return words.value.filter(
        word => word.learningStatus === 'LEARNING',
    ).length
})

/**
 * 已经掌握的单词数量。
 */
const masteredWords = computed(() => {
    return words.value.filter(
        word => word.learningStatus === 'MASTERED',
    ).length
})

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
    <main class="vocabulary-page">

        <!-- =====================================================
         顶部导航
         ===================================================== -->
        <nav class="top-navigation">

            <RouterLink to="/" class="brand">
                <span class="brand-mark">
                    I
                </span>

                <span class="brand-text">
                    IELTS Learning
                </span>
            </RouterLink>


            <div class="navigation-links">

                <RouterLink to="/" class="navigation-link">
                    Home
                </RouterLink>

                <RouterLink to="/reading" class="navigation-link">
                    Reading
                </RouterLink>

                <RouterLink to="/vocabulary" class="navigation-link active">
                    Vocabulary
                </RouterLink>

            </div>

        </nav>


        <!-- =====================================================
         Vocabulary Header
         ===================================================== -->
        <header class="vocabulary-header">

            <p class="section-label">
                IELTS VOCABULARY
            </p>

            <h1>
                Vocabulary
            </h1>

            <p class="header-description">
                流水不争先，争的是滔滔不绝。
            </p>

        </header>


        <!-- =====================================================
         Today Review

         不再使用之前那个圆圈。
         整体改成一块轻液态玻璃。
         ===================================================== -->
        <section class="today-card">

            <div class="today-glow"></div>


            <div class="today-main">

                <p class="today-label">
                    TODAY'S REVIEW
                </p>


                <h2>
                    日积月累。
                </h2>


                <p class="today-description">
                    当前有
                    <strong>
                        {{ learningWords }}
                    </strong>
                    个单词正在学习中。
                </p>


                <RouterLink to="/vocabulary/practice" class="start-button">
                    Start Review

                    <span>
                        →
                    </span>
                </RouterLink>

            </div>


            <!--
        右边不再放圆形统计。

        用更简单的排版呈现数字。
      -->
            <div class="today-number">

                <span class="big-number">
                    {{ learningWords }}
                </span>

                <span class="number-label">
                    words waiting
                </span>

            </div>

        </section>


        <!-- =====================================================
         小型统计

         不再做三个很重的大 Dashboard Card。
         ===================================================== -->
        <section class="statistics-row">

            <div class="stat-item">

                <span class="stat-number">
                    {{ totalWords }}
                </span>

                <span class="stat-label">
                    Total
                </span>

            </div>


            <div class="stat-divider"></div>


            <div class="stat-item">

                <span class="stat-number">
                    {{ learningWords }}
                </span>

                <span class="stat-label">
                    Learning
                </span>

            </div>


            <div class="stat-divider"></div>


            <div class="stat-item">

                <span class="stat-number">
                    {{ masteredWords }}
                </span>

                <span class="stat-label">
                    Mastered
                </span>

            </div>

        </section>


        <!-- =====================================================
         Vocabulary Library
         ===================================================== -->
        <section class="library-section">

            <div class="section-heading">

                <div>

                    <p class="section-label">
                        VOCABULARY LIBRARY
                    </p>

                    <h2>
                        我的词库
                    </h2>

                </div>


                <span class="word-count">
                    {{ totalWords }} words
                </span>

            </div>


            <!-- Loading -->
            <div v-if="loading" class="state-card">
                Loading vocabulary...
            </div>


            <!-- Error -->
            <div v-else-if="errorMessage" class="state-card error-message">
                {{ errorMessage }}
            </div>


            <!-- Empty -->
            <div v-else-if="words.length === 0" class="empty-card">

                <span class="empty-symbol">
                    +
                </span>

                <h3>
                    暂时还没有单词
                </h3>

                <p>
                    后面我们会加入批量词库导入。
                </p>

            </div>


            <!-- Word List -->
            <div v-else class="word-list">

                <article v-for="word in words" :key="word.id" class="word-card">

                    <div class="word-main">

                        <div class="word-title-row">

                            <h3>
                                {{ word.word }}
                            </h3>


                            <span class="status-badge" :class="{
                                mastered:
                                    word.learningStatus === 'MASTERED',
                            }">
                                {{
                                    word.learningStatus === 'MASTERED'
                                        ? 'Mastered'
                                : 'Learning'
                                }}
                            </span>

                        </div>


                        <p class="word-meaning">
                            {{ word.meaning }}
                        </p>


                        <p v-if="word.exampleSentence" class="word-example">
                            {{ word.exampleSentence }}
                        </p>

                    </div>


                    <div class="word-actions">

                        <button type="button" class="status-button" @click="handleToggleLearningStatus(word)">
                            {{
                                word.learningStatus === 'MASTERED'
                                    ? '重新学习'
                            : '标记掌握'
                            }}
                        </button>


                        <button type="button" class="delete-button" @click="handleDeleteWord(word.id)">
                            删除
                        </button>

                    </div>

                </article>

            </div>

        </section>


        <!-- =====================================================
         Add Word

         目前仍然保留已有功能，
         但视觉优先级明显降低。
         ===================================================== -->
        <section class="add-section">

            <div class="section-heading">

                <div>

                    <p class="section-label">
                        PERSONAL VOCABULARY
                    </p>

                    <h2>
                        添加单词
                    </h2>

                </div>

            </div>


            <p class="add-description">
                阅读中遇到想留下来的词，可以暂时加入这里。
            </p>


            <div class="form-grid">

                <div class="form-field">

                    <label for="word">
                        Word
                    </label>

                    <input id="word" v-model="newWord" type="text" placeholder="significant" />

                </div>


                <div class="form-field">

                    <label for="meaning">
                        Meaning
                    </label>

                    <input id="meaning" v-model="newMeaning" type="text" placeholder="显著的；重要的" />

                </div>


                <div class="form-field full-width">

                    <label for="exampleSentence">
                        Example
                    </label>

                    <textarea id="exampleSentence" v-model="newExampleSentence" rows="4"
                        placeholder="The study showed a significant improvement." />

                </div>

            </div>


            <p v-if="createErrorMessage" class="form-error">
                {{ createErrorMessage }}
            </p>


            <div class="form-footer">

                <button type="button" class="add-button" :disabled="submitting" @click="handleCreateWord">
                    {{
                        submitting
                            ? 'Adding...'
                            : 'Add Word'
                    }}
                </button>

            </div>

        </section>

    </main>
</template>


<style scoped>
/* ============================================================
   Vocabulary

   与 Home 使用同一套设计系统：

   字体：
   Maple Mono NF CN

   颜色：
   淡蓝 + 冷白

   风格：
   轻液态玻璃
   ============================================================ */

.vocabulary-page {
    min-height: 100vh;

    box-sizing: border-box;

    padding:
        24px 34px 80px;

    color: #293f54;

    background:
        radial-gradient(circle at 12% 10%,
            rgba(184, 218, 245, 0.55),
            transparent 26%),
        radial-gradient(circle at 91% 6%,
            rgba(211, 227, 249, 0.74),
            transparent 29%),
        linear-gradient(180deg,
            #edf6fc 0%,
            #f8fbfd 50%,
            #eef5fa 100%);

    /*
    这次整个 Vocabulary，
    中文英文全部使用 Maple Mono NF CN。
  */
    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;
}


/* ============================================================
   Navigation
   ============================================================ */

.top-navigation {
    width: min(1240px, 100%);

    min-height: 68px;

    margin:
        0 auto;

    box-sizing: border-box;

    display: flex;

    align-items: center;

    justify-content: space-between;

    padding:
        10px 12px 10px 16px;

    background:
        rgba(255,
            255,
            255,
            0.32);

    border:
        1px solid rgba(255,
            255,
            255,
            0.70);

    border-radius: 24px;

    backdrop-filter:
        blur(22px) saturate(145%);

    -webkit-backdrop-filter:
        blur(22px) saturate(145%);

    box-shadow:
        0 18px 50px rgba(80,
            123,
            161,
            0.075);
}


.brand {
    display: flex;

    align-items: center;

    gap: 11px;

    color: #26394d;

    text-decoration: none;
}


.brand-mark {
    width: 38px;
    height: 38px;

    display: grid;

    place-items: center;

    color: #ffffff;

    background:
        linear-gradient(145deg,
            #8ab2d5,
            #6d97bd);

    border-radius: 13px;

    box-shadow:
        0 8px 20px rgba(79,
            125,
            167,
            0.20);
}


.brand-text {
    font-size: 0.9rem;

    font-weight: 600;
}


.navigation-links {
    display: flex;

    gap: 5px;
}


.navigation-link {
    padding:
        10px 16px;

    color: #667b90;

    text-decoration: none;

    border-radius: 999px;

    font-size: 0.8rem;

    font-weight: 600;
}


.navigation-link:hover,
.navigation-link.active {
    color: #344f69;

    background:
        rgba(255,
            255,
            255,
            0.60);
}


/* ============================================================
   Header
   ============================================================ */

.vocabulary-header {
    width: min(1240px, 100%);

    margin:
        90px auto 42px;
}


.section-label {
    margin:
        0 0 10px;

    color: #89a3ba;

    font-size: 0.7rem;

    font-weight: 600;

    letter-spacing: 0.12em;
}


.vocabulary-header h1 {
    margin: 0;

    color: #304d66;

    font-size:
        clamp(3.2rem,
            6vw,
            5.8rem);

    font-weight: 600;

    letter-spacing: -0.08em;
}


.header-description {
    margin:
        18px 0 0;

    color: #718ba2;

    font-size:
        clamp(1rem,
            1.6vw,
            1.35rem);

    line-height: 1.8;
}


/* ============================================================
   Today Review
   ============================================================ */

.today-card {
    position: relative;

    width: min(1240px, 100%);

    min-height: 300px;

    margin:
        0 auto 22px;

    box-sizing: border-box;

    display: flex;

    align-items: center;

    justify-content: space-between;

    gap: 50px;

    padding:
        46px 52px;

    overflow: hidden;

    background:
        linear-gradient(135deg,
            rgba(255, 255, 255, 0.52),
            rgba(207, 230, 247, 0.33));

    border:
        1px solid rgba(255,
            255,
            255,
            0.76);

    border-radius: 36px;

    backdrop-filter:
        blur(25px) saturate(140%);

    -webkit-backdrop-filter:
        blur(25px) saturate(140%);

    box-shadow:
        0 28px 75px rgba(70,
            111,
            151,
            0.10),
        inset 0 1px 0 rgba(255,
            255,
            255,
            0.92);
}


.today-glow {
    position: absolute;

    width: 380px;
    height: 380px;

    right: -100px;
    top: -180px;

    border-radius: 50%;

    background:
        radial-gradient(circle,
            rgba(178, 213, 241, 0.48),
            rgba(178, 213, 241, 0));

    pointer-events: none;
}


.today-main {
    position: relative;

    z-index: 1;
}


.today-label {
    margin:
        0 0 16px;

    color: #88a3ba;

    font-size: 0.7rem;

    font-weight: 600;

    letter-spacing: 0.11em;
}


.today-main h2 {
    margin: 0;

    color: #35526b;

    font-size:
        clamp(2rem,
            3.5vw,
            3.2rem);

    font-weight: 600;

    letter-spacing: -0.06em;
}


.today-description {
    margin:
        16px 0 30px;

    color: #7d94a8;

    font-size: 0.9rem;
}


.today-description strong {
    color: #53738e;
}


/* ============================================================
   Start Button
   ============================================================ */

.start-button {
    display: inline-flex;

    align-items: center;

    gap: 12px;

    padding:
        14px 21px;

    color: #ffffff;

    text-decoration: none;

    background:
        linear-gradient(135deg,
            #7ca8cd,
            #668fb7);

    border-radius: 999px;

    font-size: 0.8rem;

    font-weight: 600;

    box-shadow:
        0 12px 28px rgba(82,
            133,
            178,
            0.22);

    transition:
        transform 0.2s ease,
        box-shadow 0.2s ease;
}


.start-button:hover {
    color: #ffffff;

    transform:
        translateY(-2px);

    box-shadow:
        0 17px 34px rgba(82,
            133,
            178,
            0.27);
}


/* ============================================================
   Today Number

   不再做圆圈。
   ============================================================ */

.today-number {
    position: relative;

    z-index: 1;

    min-width: 200px;

    display: flex;

    flex-direction: column;

    align-items: flex-end;
}


.big-number {
    color: #4b6b87;

    font-size:
        clamp(4.5rem,
            8vw,
            7.5rem);

    font-weight: 500;

    letter-spacing: -0.1em;

    line-height: 0.95;
}


.number-label {
    margin-top: 12px;

    color: #91a7b9;

    font-size: 0.7rem;
}


/* ============================================================
   Statistics

   改成一整条，
   而不是三个 Dashboard Card。
   ============================================================ */

.statistics-row {
    width: min(1240px, 100%);

    min-height: 105px;

    margin:
        0 auto 80px;

    box-sizing: border-box;

    display: flex;

    align-items: center;

    justify-content: center;

    gap: 44px;

    padding:
        20px 32px;

    background:
        rgba(255,
            255,
            255,
            0.33);

    border:
        1px solid rgba(255,
            255,
            255,
            0.66);

    border-radius: 25px;

    backdrop-filter:
        blur(18px);

    -webkit-backdrop-filter:
        blur(18px);
}


.stat-item {
    min-width: 100px;

    display: flex;

    flex-direction: column;

    align-items: center;

    gap: 4px;
}


.stat-number {
    color: #3f5e78;

    font-size: 1.45rem;

    font-weight: 600;
}


.stat-label {
    color: #8da2b4;

    font-size: 0.7rem;
}


.stat-divider {
    width: 1px;
    height: 34px;

    background:
        rgba(118,
            152,
            181,
            0.18);
}


/* ============================================================
   Sections
   ============================================================ */

.library-section,
.add-section {
    width: min(1240px, 100%);

    margin:
        0 auto 80px;
}


.section-heading {
    display: flex;

    align-items: flex-end;

    justify-content: space-between;

    gap: 30px;

    margin-bottom: 28px;
}


.section-heading h2 {
    margin: 0;

    color: #38546c;

    font-size:
        clamp(1.7rem,
            3vw,
            2.5rem);

    font-weight: 600;

    letter-spacing: -0.055em;
}


.word-count {
    padding:
        9px 13px;

    color: #7690a7;

    background:
        rgba(255,
            255,
            255,
            0.42);

    border:
        1px solid rgba(255,
            255,
            255,
            0.70);

    border-radius: 999px;

    font-size: 0.7rem;
}


/* ============================================================
   Empty / State
   ============================================================ */

.state-card,
.empty-card {
    box-sizing: border-box;

    padding:
        42px 30px;

    text-align: center;

    color: #8399ab;

    background:
        rgba(255,
            255,
            255,
            0.40);

    border:
        1px solid rgba(255,
            255,
            255,
            0.70);

    border-radius: 28px;

    backdrop-filter:
        blur(18px);

    -webkit-backdrop-filter:
        blur(18px);
}


.empty-symbol {
    display: block;

    margin-bottom: 14px;

    color: #87a7c1;

    font-size: 2rem;
}


.empty-card h3 {
    margin:
        0 0 8px;

    color: #54728c;
}


.empty-card p {
    margin: 0;

    font-size: 0.8rem;
}


/* ============================================================
   Word List
   ============================================================ */

.word-list {
    display: grid;

    gap: 12px;
}


.word-card {
    display: flex;

    align-items: center;

    justify-content: space-between;

    gap: 30px;

    padding:
        23px 25px;

    background:
        rgba(255,
            255,
            255,
            0.43);

    border:
        1px solid rgba(255,
            255,
            255,
            0.72);

    border-radius: 23px;

    backdrop-filter:
        blur(16px);

    -webkit-backdrop-filter:
        blur(16px);

    transition:
        transform 0.2s ease,
        background 0.2s ease,
        box-shadow 0.2s ease;
}


.word-card:hover {
    transform:
        translateY(-2px);

    background:
        rgba(255,
            255,
            255,
            0.60);

    box-shadow:
        0 18px 40px rgba(74,
            112,
            146,
            0.075);
}


.word-main {
    flex: 1;

    min-width: 0;
}


.word-title-row {
    display: flex;

    align-items: center;

    flex-wrap: wrap;

    gap: 12px;
}


.word-title-row h3 {
    margin: 0;

    color: #38556d;

    font-size: 1.15rem;

    font-weight: 600;
}


.status-badge {
    padding:
        5px 9px;

    color: #64839d;

    background:
        rgba(210,
            231,
            247,
            0.66);

    border-radius: 999px;

    font-size: 0.63rem;
}


.status-badge.mastered {
    color: #5f7d7d;

    background:
        rgba(214,
            234,
            232,
            0.68);
}


.word-meaning {
    margin:
        9px 0 0;

    color: #627d93;

    font-size: 0.85rem;
}


.word-example {
    margin:
        8px 0 0;

    color: #91a5b5;

    font-size: 0.76rem;

    line-height: 1.6;
}


/* ============================================================
   Word Actions
   ============================================================ */

.word-actions {
    display: flex;

    gap: 8px;

    flex-shrink: 0;
}


button {
    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;
}


.status-button,
.delete-button {
    padding:
        10px 13px;

    border: 0;

    border-radius: 12px;

    cursor: pointer;
}


.status-button {
    color: #587994;

    background:
        rgba(214,
            231,
            245,
            0.62);
}


.delete-button {
    color: #9d747c;

    background: transparent;
}


/* ============================================================
   Add Word
   ============================================================ */

.add-description {
    margin:
        -12px 0 28px;

    color: #8197aa;

    font-size: 0.82rem;
}


.form-grid {
    display: grid;

    grid-template-columns:
        repeat(2,
            1fr);

    gap: 18px;
}


.form-field {
    display: flex;

    flex-direction: column;

    gap: 8px;
}


.form-field.full-width {
    grid-column:
        1 / -1;
}


.form-field label {
    color: #607d95;

    font-size: 0.72rem;
}


.form-field input,
.form-field textarea {
    box-sizing: border-box;

    width: 100%;

    padding:
        14px 15px;

    color: #38536a;

    background:
        rgba(255,
            255,
            255,
            0.46);

    border:
        1px solid rgba(255,
            255,
            255,
            0.72);

    border-radius: 15px;

    outline: none;

    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;

    backdrop-filter:
        blur(15px);

    -webkit-backdrop-filter:
        blur(15px);
}


.form-field input:focus,
.form-field textarea:focus {
    border-color:
        rgba(115,
            161,
            200,
            0.56);

    box-shadow:
        0 0 0 4px rgba(112,
            162,
            204,
            0.09);
}


.form-field textarea {
    resize: vertical;
}


.form-footer {
    display: flex;

    justify-content: flex-end;

    margin-top: 20px;
}


.add-button {
    padding:
        12px 20px;

    color: #ffffff;

    background:
        linear-gradient(135deg,
            #7ca8cd,
            #668fb7);

    border: 0;

    border-radius: 14px;

    cursor: pointer;

    box-shadow:
        0 10px 24px rgba(82,
            133,
            178,
            0.20);
}


.add-button:disabled {
    opacity: 0.55;

    cursor: not-allowed;
}


.error-message,
.form-error {
    color: #a66c79;
}


/* ============================================================
   Mobile
   ============================================================ */

@media (max-width: 700px) {

    .vocabulary-page {
        padding:
            14px 14px 55px;
    }


    .brand-text {
        display: none;
    }


    .navigation-link {
        padding:
            9px 10px;

        font-size: 0.68rem;
    }


    .vocabulary-header {
        margin-top: 65px;
    }


    .vocabulary-header h1 {
        font-size: 2.7rem;
    }


    .today-card {
        align-items: flex-start;

        flex-direction: column;

        padding:
            34px 25px;
    }


    .today-number {
        min-width: 0;

        align-items: flex-start;
    }


    .statistics-row {
        gap: 16px;

        margin-bottom: 60px;

        padding:
            18px 12px;
    }


    .stat-divider {
        height: 28px;
    }


    .word-card {
        align-items: flex-start;

        flex-direction: column;
    }


    .word-actions {
        width: 100%;
    }


    .word-actions button {
        flex: 1;
    }


    .form-grid {
        grid-template-columns: 1fr;
    }


    .form-field.full-width {
        grid-column: auto;
    }


    .form-footer {
        justify-content: stretch;
    }


    .add-button {
        width: 100%;
    }

}
</style>