<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppNavigation from '@/components/AppNavigation.vue'

import {
    getVocabularyWords,
    updateVocabularyLearningStatus,
    type VocabularyWord,
} from '@/api/vocabulary'

/**
 * 当前需要练习的单词。
 *
 * 当前第一版 Practice 只练：
 *
 * LEARNING
 *
 * 已经 MASTERED 的单词暂时不会进入本轮练习。
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
 * 只显示单词，让用户先主动回忆。
 *
 * true：
 * 显示释义、例句以及学习结果按钮。
 */
const showAnswer = ref(false)

/**
 * 页面加载状态。
 */
const loading = ref(true)

/**
 * 页面错误信息。
 */
const errorMessage = ref('')

/**
 * 当前正在练习的单词。
 *
 * 如果当前列表为空，
 * 返回 null。
 */
const currentWord = computed(() => {
    return words.value[currentIndex.value] ?? null
})

/**
 * 进入 Practice 时，
 * 一共有多少个 LEARNING 单词。
 *
 * 这个数字不会因为点击“掌握了”而减少，
 * 用于表示本轮最开始的任务量。
 */
const initialWordCount = ref(0)

/**
 * 本轮已经被标记为 MASTERED 的数量。
 */
const masteredThisSession = ref(0)

/**
 * 当前已经完成多少个单词。
 *
 * 这里暂时使用：
 *
 * initialWordCount - words.length
 *
 * 计算本轮已经被移出 LEARNING 队列的数量。
 */
const completedCount = computed(() => {
    return initialWordCount.value - words.value.length
})

/**
 * 本轮进度百分比。
 *
 * 例如：
 *
 * 初始 10 个单词
 * 已掌握 3 个
 *
 * progress = 30
 */
const progressPercentage = computed(() => {
    if (initialWordCount.value === 0) {
        return 0
    }

    return Math.round(
        (completedCount.value / initialWordCount.value) * 100,
    )
})

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
 * 显示当前单词答案。
 */
function revealAnswer() {
    showAnswer.value = true
}

/**
 * 进入下一个单词。
 *
 * 当前词仍然保留在 LEARNING，
 * 所以后面还可能再次遇到它。
 */
function goToNextWord() {
    if (words.value.length === 0) {
        return
    }

    currentIndex.value =
        (currentIndex.value + 1) % words.value.length

    /**
     * 进入新单词后重新隐藏答案，
     * 让用户再次主动回忆。
     */
    showAnswer.value = false
}

/**
 * I Know
 *
 * 用户认为自己已经掌握当前单词。
 *
 * 后端状态：
 *
 * LEARNING → MASTERED
 *
 * 更新成功后，
 * 当前单词从本轮 Practice 中移除。
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
         * 删除当前单词之后，
         * currentIndex 有可能超过新的数组长度。
         *
         * 例如：
         *
         * 原来有 3 个单词
         * 当前 index = 2
         *
         * 删除以后只剩 2 个，
         * 最大 index 只能是 1。
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
 * 用户认为当前单词还没有真正掌握。
 *
 * 后端继续保持：
 *
 * LEARNING
 *
 * 然后切换到下一个单词。
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
    <main class="practice-page">

        <!-- =====================================================
         顶部导航
         ===================================================== -->
        <AppNavigation />


        <!-- =====================================================
         页面顶部
         ===================================================== -->
        <header class="practice-header">

            <div>

                <p class="section-label">
                    VOCABULARY PRACTICE
                </p>

                <h1>
                    Practice
                </h1>

                <p class="practice-description">
                    先回忆，再看答案。
                </p>

            </div>


            <RouterLink to="/vocabulary" class="back-button">
                ← Back to Vocabulary
            </RouterLink>

        </header>


        <!-- =====================================================
         Loading
         ===================================================== -->
        <section v-if="loading" class="state-card">
            Loading vocabulary...
        </section>


        <!-- =====================================================
         Error
         ===================================================== -->
        <section v-else-if="errorMessage" class="state-card error-message">
            {{ errorMessage }}
        </section>


        <!-- =====================================================
         Practice Complete
         ===================================================== -->
        <section v-else-if="words.length === 0" class="complete-card">

            <p class="complete-label">
                SESSION COMPLETE
            </p>

            <h2>
                今日这一轮完成了。
            </h2>

            <p class="complete-description">
                一次不需要记住很多，
                重要的是持续回来。
            </p>


            <div class="complete-statistics">

                <div class="complete-stat">

                    <span class="complete-number">
                        {{ initialWordCount }}
                    </span>

                    <span class="complete-stat-label">
                        Total
                    </span>

                </div>


                <div class="complete-divider"></div>


                <div class="complete-stat">

                    <span class="complete-number">
                        {{ masteredThisSession }}
                    </span>

                    <span class="complete-stat-label">
                        Mastered
                    </span>

                </div>


                <div class="complete-divider"></div>


                <div class="complete-stat">

                    <span class="complete-number">
                        0
                    </span>

                    <span class="complete-stat-label">
                        Remaining
                    </span>

                </div>

            </div>


            <RouterLink to="/vocabulary" class="complete-button">
                Back to Vocabulary
                <span>→</span>
            </RouterLink>

        </section>


        <!-- =====================================================
         当前正在学习的单词
         ===================================================== -->
        <section v-else-if="currentWord" class="practice-shell">

            <!-- =================================================
           Practice Progress
           ================================================= -->
            <div class="practice-progress">

                <div class="progress-info">

                    <span>
                        {{ currentIndex + 1 }}
                        /
                        {{ words.length }}
                    </span>

                    <span>
                        {{ progressPercentage }}%
                    </span>

                </div>


                <div class="progress-track">

                    <div class="progress-bar" :style="{
                        width: `${progressPercentage}%`,
                    }"></div>

                </div>

            </div>


            <!-- =================================================
           Flashcard
           ================================================= -->
            <article class="flashcard" :class="{
                revealed: showAnswer,
            }">

                <!-- 装饰光晕 -->
                <div class="card-glow card-glow-one"></div>
                <div class="card-glow card-glow-two"></div>


                <!-- =================================================
             未显示答案
             ================================================= -->
                <div v-if="!showAnswer" class="question-side">

                    <p class="card-label">
                        WORD
                    </p>


                    <h2 class="word-title">
                        {{ currentWord.word }}
                    </h2>


                    <p class="remember-hint">
                        先想一想它是什么意思。
                    </p>


                    <button type="button" class="reveal-button" @click="revealAnswer">
                        Show Answer

                        <span>
                            ↓
                        </span>
                    </button>

                </div>


                <!-- =================================================
             已显示答案
             ================================================= -->
                <div v-else class="answer-side">

                    <div class="answer-word">

                        <p class="card-label">
                            WORD
                        </p>

                        <h2 class="word-title smaller">
                            {{ currentWord.word }}
                        </h2>

                    </div>


                    <div class="answer-content">

                        <!-- Meaning -->
                        <div class="answer-block">

                            <span class="answer-label">
                                MEANING
                            </span>

                            <p class="meaning-text">
                                {{ currentWord.meaning }}
                            </p>

                        </div>


                        <!-- Example -->
                        <div v-if="currentWord.exampleSentence" class="answer-block example-block">

                            <span class="answer-label">
                                EXAMPLE
                            </span>

                            <p class="example-text">
                                {{ currentWord.exampleSentence }}
                            </p>

                        </div>

                    </div>


                    <!-- =================================================
               当前仍然使用现有业务逻辑。

               Still Learning：
               保持 LEARNING

               I Know：
               更新为 MASTERED

               后面升级复习算法时，
               再扩展成 Again / Hard / Good / Easy。
               ================================================= -->
                    <div class="answer-actions">

                        <button type="button" class="again-button" @click="handleStillLearning">
                            <span class="action-main">
                                Again
                            </span>

                            <span class="action-description">
                                还需要复习
                            </span>
                        </button>


                        <button type="button" class="know-button" @click="handleKnowWord">
                            <span class="action-main">
                                I Know
                            </span>

                            <span class="action-description">
                                暂时掌握
                            </span>
                        </button>

                    </div>

                </div>

            </article>


            <!-- =================================================
           底部辅助信息
           ================================================= -->
            <div class="practice-footer">

                <span>
                    {{ initialWordCount }} words in this session
                </span>

                <span>
                    {{ masteredThisSession }} mastered
                </span>

            </div>

        </section>

    </main>
</template>

<style scoped>
/* ============================================================
   Vocabulary Practice

   与 Home / Vocabulary 使用同一设计系统：

   - Maple Mono NF CN
   - 淡蓝
   - 冷白
   - Liquid Glass
   ============================================================ */

.practice-page {
    min-height: 100vh;

    box-sizing: border-box;

    padding:
        24px 34px 80px;

    color: #2c455c;

    background:
        radial-gradient(circle at 12% 10%,
            rgba(184, 218, 245, 0.55),
            transparent 27%),
        radial-gradient(circle at 88% 5%,
            rgba(214, 228, 250, 0.78),
            transparent 30%),
        linear-gradient(180deg,
            #edf6fc 0%,
            #f8fbfd 50%,
            #eef5fa 100%);

    /*
    中文和英文全部统一成 Maple Mono NF CN。
  */
    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;
}


/* ============================================================
   Header
   ============================================================ */

.practice-header {
    width: min(1040px, 100%);

    margin:
        76px auto 36px;

    display: flex;

    align-items: flex-end;

    justify-content: space-between;

    gap: 30px;
}


.section-label {
    margin:
        0 0 10px;

    color: #89a3ba;

    font-size: 0.7rem;

    font-weight: 600;

    letter-spacing: 0.12em;
}


.practice-header h1 {
    margin: 0;

    color: #304d66;

    font-size:
        clamp(2.8rem,
            5vw,
            4.6rem);

    font-weight: 600;

    letter-spacing: -0.08em;
}


.practice-description {
    margin:
        13px 0 0;

    color: #8197aa;

    font-size: 0.84rem;
}


.back-button {
    padding:
        11px 16px;

    color: #597a96;

    text-decoration: none;

    background:
        rgba(255,
            255,
            255,
            0.44);

    border:
        1px solid rgba(255,
            255,
            255,
            0.72);

    border-radius: 999px;

    font-size: 0.74rem;

    backdrop-filter:
        blur(14px);

    -webkit-backdrop-filter:
        blur(14px);
}


/* ============================================================
   State
   ============================================================ */

.state-card {
    width: min(1040px, 100%);

    box-sizing: border-box;

    margin:
        0 auto;

    padding:
        40px 30px;

    color: #8399ab;

    text-align: center;

    background:
        rgba(255,
            255,
            255,
            0.44);

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


.error-message {
    color: #a66c79;
}


/* ============================================================
   Practice Container
   ============================================================ */

.practice-shell {
    width: min(1040px, 100%);

    margin:
        0 auto;
}


/* ============================================================
   Progress
   ============================================================ */

.practice-progress {
    margin-bottom: 18px;

    padding:
        0 5px;
}


.progress-info {
    display: flex;

    justify-content: space-between;

    margin-bottom: 10px;

    color: #849caf;

    font-size: 0.7rem;
}


.progress-track {
    height: 6px;

    overflow: hidden;

    background:
        rgba(255,
            255,
            255,
            0.52);

    border:
        1px solid rgba(255,
            255,
            255,
            0.58);

    border-radius: 999px;
}


.progress-bar {
    height: 100%;

    background:
        linear-gradient(90deg,
            #91b9d9,
            #6f9bc2);

    border-radius: inherit;

    transition:
        width 0.35s ease;
}


/* ============================================================
   Flashcard
   ============================================================ */

.flashcard {
    position: relative;

    min-height: 520px;

    box-sizing: border-box;

    overflow: hidden;

    background:
        linear-gradient(135deg,
            rgba(255, 255, 255, 0.56),
            rgba(205, 230, 247, 0.31));

    border:
        1px solid rgba(255,
            255,
            255,
            0.78);

    border-radius: 40px;

    backdrop-filter:
        blur(28px) saturate(140%);

    -webkit-backdrop-filter:
        blur(28px) saturate(140%);

    box-shadow:
        0 32px 85px rgba(68,
            109,
            148,
            0.11),
        inset 0 1px 0 rgba(255,
            255,
            255,
            0.94);

    transition:
        min-height 0.3s ease,
        box-shadow 0.3s ease;
}


.flashcard.revealed {
    min-height: 580px;
}


/* ============================================================
   Card Glow
   ============================================================ */

.card-glow {
    position: absolute;

    border-radius: 50%;

    pointer-events: none;
}


.card-glow-one {
    width: 420px;
    height: 420px;

    top: -230px;
    right: -140px;

    background:
        radial-gradient(circle,
            rgba(177, 213, 241, 0.52),
            rgba(177, 213, 241, 0));
}


.card-glow-two {
    width: 300px;
    height: 300px;

    bottom: -180px;
    left: -120px;

    background:
        radial-gradient(circle,
            rgba(215, 227, 248, 0.54),
            rgba(215, 227, 248, 0));
}


/* ============================================================
   Question Side
   ============================================================ */

.question-side {
    position: relative;

    z-index: 1;

    min-height: 520px;

    display: flex;

    flex-direction: column;

    align-items: center;

    justify-content: center;

    box-sizing: border-box;

    padding:
        60px 40px;

    text-align: center;
}


.card-label {
    margin:
        0 0 26px;

    color: #90a9bd;

    font-size: 0.66rem;

    font-weight: 600;

    letter-spacing: 0.15em;
}


.word-title {
    margin: 0;

    color: #36546d;

    font-size:
        clamp(3.3rem,
            8vw,
            6.8rem);

    font-weight: 600;

    letter-spacing: -0.085em;

    line-height: 1.05;
}


.word-title.smaller {
    font-size:
        clamp(2.5rem,
            5vw,
            4.3rem);
}


.remember-hint {
    margin:
        24px 0 34px;

    color: #879dad;

    font-size: 0.82rem;
}


/* ============================================================
   Reveal Button
   ============================================================ */

button {
    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;
}


.reveal-button {
    display: inline-flex;

    align-items: center;

    gap: 11px;

    padding:
        14px 21px;

    color: #ffffff;

    background:
        linear-gradient(135deg,
            #7ca8cd,
            #668fb7);

    border: 0;

    border-radius: 999px;

    cursor: pointer;

    font-size: 0.78rem;

    font-weight: 600;

    box-shadow:
        0 13px 30px rgba(82,
            133,
            178,
            0.23);

    transition:
        transform 0.2s ease,
        box-shadow 0.2s ease;
}


.reveal-button:hover {
    transform:
        translateY(-2px);

    box-shadow:
        0 17px 35px rgba(82,
            133,
            178,
            0.28);
}


/* ============================================================
   Answer Side
   ============================================================ */

.answer-side {
    position: relative;

    z-index: 1;

    min-height: 580px;

    box-sizing: border-box;

    display: flex;

    flex-direction: column;

    padding:
        48px 54px;
}


.answer-word {
    padding-bottom: 34px;

    border-bottom:
        1px solid rgba(101,
            146,
            182,
            0.12);
}


.answer-content {
    flex: 1;

    display: grid;

    grid-template-columns:
        repeat(2,
            minmax(0, 1fr));

    gap: 22px;

    padding:
        34px 0;
}


.answer-block {
    padding:
        24px;

    background:
        rgba(255,
            255,
            255,
            0.36);

    border:
        1px solid rgba(255,
            255,
            255,
            0.62);

    border-radius: 24px;

    backdrop-filter:
        blur(15px);

    -webkit-backdrop-filter:
        blur(15px);
}


.answer-label {
    display: block;

    margin-bottom: 13px;

    color: #8ba4b8;

    font-size: 0.64rem;

    letter-spacing: 0.12em;
}


.meaning-text {
    margin: 0;

    color: #3e5d75;

    font-size:
        clamp(1.25rem,
            2vw,
            1.65rem);

    line-height: 1.7;
}


.example-text {
    margin: 0;

    color: #667f94;

    font-size: 0.9rem;

    line-height: 1.85;
}


/* ============================================================
   Answer Actions
   ============================================================ */

.answer-actions {
    display: grid;

    grid-template-columns:
        repeat(2,
            1fr);

    gap: 14px;
}


.again-button,
.know-button {
    min-height: 72px;

    display: flex;

    flex-direction: column;

    align-items: flex-start;

    justify-content: center;

    gap: 4px;

    padding:
        14px 20px;

    border: 0;

    border-radius: 20px;

    cursor: pointer;

    text-align: left;

    transition:
        transform 0.2s ease,
        box-shadow 0.2s ease,
        background 0.2s ease;
}


.again-button {
    color: #765e67;

    background:
        rgba(239,
            218,
            226,
            0.52);
}


.know-button {
    color: #466a87;

    background:
        rgba(201,
            224,
            241,
            0.62);
}


.again-button:hover,
.know-button:hover {
    transform:
        translateY(-2px);

    box-shadow:
        0 13px 26px rgba(70,
            107,
            140,
            0.08);
}


.action-main {
    font-size: 0.87rem;

    font-weight: 600;
}


.action-description {
    opacity: 0.68;

    font-size: 0.67rem;
}


/* ============================================================
   Practice Footer
   ============================================================ */

.practice-footer {
    display: flex;

    justify-content: space-between;

    gap: 20px;

    margin-top: 17px;

    padding:
        0 6px;

    color: #8da3b5;

    font-size: 0.66rem;
}


/* ============================================================
   Complete
   ============================================================ */

.complete-card {
    width: min(900px, 100%);

    min-height: 480px;

    box-sizing: border-box;

    margin:
        0 auto;

    display: flex;

    flex-direction: column;

    align-items: center;

    justify-content: center;

    padding:
        55px 35px;

    text-align: center;

    background:
        linear-gradient(135deg,
            rgba(255, 255, 255, 0.55),
            rgba(205, 230, 247, 0.32));

    border:
        1px solid rgba(255,
            255,
            255,
            0.78);

    border-radius: 38px;

    backdrop-filter:
        blur(26px);

    -webkit-backdrop-filter:
        blur(26px);

    box-shadow:
        0 30px 80px rgba(68,
            109,
            148,
            0.10);
}


.complete-label {
    margin:
        0 0 18px;

    color: #8aa3b8;

    font-size: 0.66rem;

    letter-spacing: 0.14em;
}


.complete-card h2 {
    margin: 0;

    color: #3b5972;

    font-size:
        clamp(2rem,
            4vw,
            3.3rem);

    font-weight: 600;

    letter-spacing: -0.06em;
}


.complete-description {
    margin:
        17px 0 36px;

    color: #8399ab;

    font-size: 0.82rem;
}


.complete-statistics {
    display: flex;

    align-items: center;

    gap: 30px;

    margin-bottom: 36px;
}


.complete-stat {
    min-width: 100px;

    display: flex;

    flex-direction: column;

    gap: 5px;
}


.complete-number {
    color: #45647e;

    font-size: 1.8rem;

    font-weight: 600;
}


.complete-stat-label {
    color: #91a5b6;

    font-size: 0.67rem;
}


.complete-divider {
    width: 1px;
    height: 36px;

    background:
        rgba(104,
            144,
            177,
            0.18);
}


.complete-button {
    display: inline-flex;

    align-items: center;

    gap: 10px;

    padding:
        13px 19px;

    color: #ffffff;

    text-decoration: none;

    background:
        linear-gradient(135deg,
            #7ca8cd,
            #668fb7);

    border-radius: 999px;

    font-size: 0.76rem;
}


/* ============================================================
   Tablet / Mobile
   ============================================================ */

@media (max-width: 760px) {

    .practice-page {
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


    .practice-header {
        align-items: flex-start;

        flex-direction: column;

        margin-top: 60px;
    }


    .practice-header h1 {
        font-size: 2.7rem;
    }


    .flashcard,
    .question-side {
        min-height: 470px;
    }


    .flashcard.revealed,
    .answer-side {
        min-height: 0;
    }


    .question-side {
        padding:
            45px 20px;
    }


    .word-title {
        font-size: 3rem;
    }


    .answer-side {
        padding:
            34px 22px;
    }


    .answer-content {
        grid-template-columns: 1fr;
    }


    .answer-actions {
        grid-template-columns: 1fr;
    }


    .statistics-row {
        gap: 15px;
    }


    .practice-footer {
        align-items: flex-start;

        flex-direction: column;
    }


    .complete-statistics {
        gap: 17px;
    }


    .complete-stat {
        min-width: 70px;
    }

}
</style>