<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

import AppNavigation from '@/components/AppNavigation.vue'

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
 * 中的 history id。
 */
const route = useRoute()

/**
 * 保存后端返回的某一次 Reading Practice 详情。
 */
const historyDetail =
    ref<ReadingPracticeHistoryDetail | null>(null)

/**
 * 页面是否正在加载。
 */
const loading = ref(true)

/**
 * 如果请求失败，
 * 保存错误信息。
 */
const errorMessage = ref('')

/**
 * 把 submittedAt 时间戳转换成用户可读时间。
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

        <!-- 全站统一导航 -->
        <AppNavigation />


        <!-- =====================================================
             Loading
             ===================================================== -->
        <section v-if="loading" class="state-card">
            Loading history detail...
        </section>


        <!-- =====================================================
             Error
             ===================================================== -->
        <section v-else-if="errorMessage" class="state-card error-message">
            {{ errorMessage }}
        </section>


        <!-- =====================================================
             正常内容
             ===================================================== -->
        <template v-else-if="historyDetail">

            <!-- =================================================
                 Header
                 ================================================= -->
            <header class="detail-header">

                <div>

                    <p class="section-label">
                        READING REVIEW
                    </p>

                    <h1>
                        {{ historyDetail.testTitle }}
                    </h1>

                    <p class="submitted-time">
                        Submitted
                        ·
                        {{ formatSubmittedAt(historyDetail.submittedAt) }}
                    </p>

                </div>


                <RouterLink to="/reading/history" class="back-button">
                    ← Back to History
                </RouterLink>

            </header>


            <!-- =================================================
                 本次成绩

                 不使用传统两个方框 Dashboard。
                 改成一个完整的成绩主卡片。
                 ================================================= -->
            <section class="score-card">

                <div class="score-main">

                    <p class="score-label">
                        FINAL SCORE
                    </p>

                    <div class="score-value">

                        <strong>
                            {{ historyDetail.correctCount }}
                        </strong>

                        <span>
                            /
                            {{ historyDetail.totalQuestions }}
                        </span>

                    </div>

                    <p class="score-caption">
                        questions correct
                    </p>

                </div>


                <div class="score-divider"></div>


                <div class="accuracy-main">

                    <span class="accuracy-value">
                        {{ historyDetail.percentage.toFixed(1) }}%
                    </span>

                    <span class="accuracy-label">
                        Accuracy
                    </span>

                </div>

            </section>


            <!-- =================================================
                 Question Review Header
                 ================================================= -->
            <section class="review-heading">

                <div>

                    <p class="section-label">
                        QUESTION REVIEW
                    </p>

                    <h2>
                        逐题复盘
                    </h2>

                </div>


                <span class="question-count">
                    {{ historyDetail.answers.length }} questions
                </span>

            </section>


            <!-- =================================================
                 Question Review
                 ================================================= -->
            <section class="answer-list">

                <article v-for="answer in historyDetail.answers" :key="answer.questionId" class="answer-card" :class="{
                    correct: answer.correct,
                    incorrect: !answer.correct,
                }">

                    <!-- =========================================
                         Question Header
                         ========================================= -->
                    <div class="answer-header">

                        <div>

                            <p class="question-label">
                                QUESTION
                            </p>

                            <h3>
                                {{ answer.questionNumber }}
                            </h3>

                        </div>


                        <!--
                          正误状态。

                          正确：
                          淡蓝绿色

                          错误：
                          淡粉色

                          颜色保持克制，
                          不使用鲜艳红绿。
                        -->
                        <span class="result-badge" :class="{
                            correct: answer.correct,
                            incorrect: !answer.correct,
                        }">
                            {{
                                answer.correct
                                    ? '✓ Correct'
                                    : '× Incorrect'
                            }}
                        </span>

                    </div>


                    <!-- =========================================
                         Answer Comparison
                         ========================================= -->
                    <div class="answer-comparison">

                        <!-- Your Answer -->
                        <div class="answer-block">

                            <span class="answer-label">
                                YOUR ANSWER
                            </span>

                            <p class="answer-value" :class="{
                                wrong:
                                    !answer.correct,
                            }">
                                {{
                                    answer.userAnswer ||
                                    'Not answered'
                                }}
                            </p>

                        </div>


                        <!-- Correct Answer -->
                        <div class="answer-block">

                            <span class="answer-label">
                                CORRECT ANSWER
                            </span>

                            <p class="answer-value correct-answer">
                                {{ answer.correctAnswer }}
                            </p>

                        </div>

                    </div>


                    <!--
                      当前 GitHub main 的 History Detail API
                      没有 explanation 字段。

                      所以这一轮不创建假的 Explanation。

                      后面如果后端历史答案 DTO
                      增加 explanation，
                      再在这里加入解释区域。
                    -->

                </article>

            </section>


            <!-- =================================================
                 Bottom Action
                 ================================================= -->
            <div class="bottom-action">

                <RouterLink to="/reading" class="reading-button">
                    Back to Reading

                    <span>
                        →
                    </span>
                </RouterLink>

            </div>

        </template>

    </main>
</template>


<style scoped>
/* ============================================================
   Reading History Detail

   继续使用当前统一设计：

   - Maple Mono NF CN
   - 淡蓝背景
   - Liquid Glass
   - 柔和状态色
   ============================================================ */

.history-detail-page {
    min-height: 100vh;

    box-sizing: border-box;

    padding:
        24px 34px 80px;

    color:
        var(--color-text-main,
            #304d66);

    background:
        radial-gradient(circle at 12% 8%,
            rgba(184, 218, 245, 0.55),
            transparent 27%),
        radial-gradient(circle at 90% 6%,
            rgba(211, 227, 249, 0.72),
            transparent 29%),
        linear-gradient(180deg,
            var(--color-bg-start,
                #edf6fc) 0%,
            var(--color-bg-middle,
                #f8fbfd) 50%,
            var(--color-bg-end,
                #eef5fa) 100%);

    font-family:
        var(--font-main,
            "Maple Mono NF CN",
            "Consolas",
            monospace);
}


/* ============================================================
   State
   ============================================================ */

.state-card {
    width: min(1100px, 100%);

    box-sizing: border-box;

    margin:
        90px auto 0;

    padding:
        44px 30px;

    color: #8298aa;

    text-align: center;

    background:
        rgba(255,
            255,
            255,
            0.42);

    border:
        1px solid rgba(255,
            255,
            255,
            0.72);

    border-radius: 28px;

    backdrop-filter:
        blur(18px);

    -webkit-backdrop-filter:
        blur(18px);
}


.error-message {
    color: #a96f7c;
}


/* ============================================================
   Header
   ============================================================ */

.detail-header {
    width: min(1100px, 100%);

    margin:
        88px auto 38px;

    display: flex;

    align-items: flex-end;

    justify-content: space-between;

    gap: 30px;
}


.section-label {
    margin:
        0 0 10px;

    color: #88a2b9;

    font-size: 0.7rem;

    font-weight: 600;

    letter-spacing: 0.12em;
}


.detail-header h1 {
    max-width: 780px;

    margin: 0;

    color: #304d66;

    font-size:
        clamp(2.1rem,
            4vw,
            3.8rem);

    font-weight: 600;

    letter-spacing: -0.065em;

    line-height: 1.18;
}


.submitted-time {
    margin:
        16px 0 0;

    color: #8499ab;

    font-size: 0.72rem;
}


.back-button {
    display: inline-flex;

    align-items: center;

    padding:
        11px 16px;

    flex-shrink: 0;

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

    font-size: 0.72rem;

    backdrop-filter:
        blur(14px);

    -webkit-backdrop-filter:
        blur(14px);

    transition:
        transform 0.2s ease,
        background 0.2s ease;
}


.back-button:hover {
    color: #4f708c;

    background:
        rgba(255,
            255,
            255,
            0.63);

    transform:
        translateY(-1px);
}


/* ============================================================
   Score Card
   ============================================================ */

.score-card {
    position: relative;

    width: min(1100px, 100%);

    min-height: 220px;

    margin:
        0 auto 80px;

    box-sizing: border-box;

    display: flex;

    align-items: center;

    justify-content: center;

    gap: 62px;

    padding:
        40px 46px;

    overflow: hidden;

    background:
        linear-gradient(135deg,
            rgba(255, 255, 255, 0.54),
            rgba(204, 229, 247, 0.32));

    border:
        1px solid rgba(255,
            255,
            255,
            0.78);

    border-radius: 34px;

    backdrop-filter:
        blur(24px) saturate(140%);

    -webkit-backdrop-filter:
        blur(24px) saturate(140%);

    box-shadow:
        0 28px 70px rgba(69,
            109,
            146,
            0.095),
        inset 0 1px 0 rgba(255,
            255,
            255,
            0.94);
}


.score-main {
    min-width: 210px;

    text-align: center;
}


.score-label {
    margin:
        0 0 13px;

    color: #8ca5b9;

    font-size: 0.66rem;

    letter-spacing: 0.13em;
}


.score-value {
    display: flex;

    align-items: baseline;

    justify-content: center;

    gap: 8px;
}


.score-value strong {
    color: #3c5b74;

    font-size:
        clamp(4rem,
            7vw,
            6rem);

    font-weight: 500;

    letter-spacing: -0.1em;

    line-height: 1;
}


.score-value span {
    color: #88a0b3;

    font-size: 1.5rem;
}


.score-caption {
    margin:
        12px 0 0;

    color: #8fa4b5;

    font-size: 0.68rem;
}


.score-divider {
    width: 1px;
    height: 100px;

    background:
        rgba(104,
            147,
            180,
            0.18);
}


.accuracy-main {
    min-width: 190px;

    display: flex;

    flex-direction: column;

    align-items: center;

    gap: 7px;
}


.accuracy-value {
    color: #527b9a;

    font-size:
        clamp(2.5rem,
            5vw,
            4rem);

    font-weight: 500;

    letter-spacing: -0.07em;
}


.accuracy-label {
    color: #8da3b5;

    font-size: 0.7rem;
}


/* ============================================================
   Review Heading
   ============================================================ */

.review-heading {
    width: min(1100px, 100%);

    margin:
        0 auto 28px;

    display: flex;

    align-items: flex-end;

    justify-content: space-between;

    gap: 30px;
}


.review-heading h2 {
    margin: 0;

    color: #36536b;

    font-size:
        clamp(1.7rem,
            3vw,
            2.5rem);

    font-weight: 600;

    letter-spacing: -0.055em;
}


.question-count {
    padding:
        8px 13px;

    color: #7893aa;

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

    font-size: 0.68rem;
}


/* ============================================================
   Answer List
   ============================================================ */

.answer-list {
    width: min(1100px, 100%);

    margin:
        0 auto;

    display: grid;

    gap: 14px;
}


/* ============================================================
   Answer Card
   ============================================================ */

.answer-card {
    position: relative;

    padding:
        25px 27px;

    overflow: hidden;

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

    border-radius: 25px;

    backdrop-filter:
        blur(18px);

    -webkit-backdrop-filter:
        blur(18px);

    box-shadow:
        0 15px 40px rgba(72,
            111,
            146,
            0.055);

    transition:
        transform 0.2s ease,
        box-shadow 0.2s ease;
}


.answer-card:hover {
    transform:
        translateY(-2px);

    box-shadow:
        0 20px 45px rgba(72,
            111,
            146,
            0.08);
}


/*
  正确题顶部加入非常淡的蓝绿色光带。
*/
.answer-card.correct::before {
    content: "";

    position: absolute;

    left: 0;
    top: 0;

    width: 100%;
    height: 3px;

    background:
        linear-gradient(90deg,
            rgba(115, 176, 173, 0.70),
            rgba(115, 176, 173, 0));
}


/*
  错题使用柔和淡粉，
  不使用鲜红色。
*/
.answer-card.incorrect::before {
    content: "";

    position: absolute;

    left: 0;
    top: 0;

    width: 100%;
    height: 3px;

    background:
        linear-gradient(90deg,
            rgba(204, 134, 151, 0.76),
            rgba(204, 134, 151, 0));
}


/* ============================================================
   Answer Header
   ============================================================ */

.answer-header {
    display: flex;

    align-items: center;

    justify-content: space-between;

    gap: 20px;

    padding-bottom: 21px;

    border-bottom:
        1px solid rgba(105,
            147,
            180,
            0.10);
}


.question-label {
    margin:
        0 0 5px;

    color: #94aabd;

    font-size: 0.61rem;

    letter-spacing: 0.13em;
}


.answer-header h3 {
    margin: 0;

    color: #3e5c75;

    font-size: 1.35rem;

    font-weight: 600;
}


/* ============================================================
   Result Badge
   ============================================================ */

.result-badge {
    padding:
        7px 11px;

    border-radius: 999px;

    font-size: 0.66rem;

    font-weight: 600;
}


.result-badge.correct {
    color: #507b78;

    background:
        rgba(205,
            232,
            229,
            0.60);
}


.result-badge.incorrect {
    color: #956774;

    background:
        rgba(239,
            217,
            224,
            0.62);
}


/* ============================================================
   Answer Comparison
   ============================================================ */

.answer-comparison {
    display: grid;

    grid-template-columns:
        repeat(2,
            minmax(0, 1fr));

    gap: 14px;

    padding-top: 21px;
}


.answer-block {
    padding:
        19px 20px;

    background:
        rgba(255,
            255,
            255,
            0.34);

    border:
        1px solid rgba(255,
            255,
            255,
            0.60);

    border-radius: 18px;
}


.answer-label {
    display: block;

    margin-bottom: 10px;

    color: #91a7b9;

    font-size: 0.61rem;

    letter-spacing: 0.11em;
}


.answer-value {
    margin: 0;

    color: #4c687e;

    font-size: 0.9rem;

    font-weight: 600;

    line-height: 1.6;

    word-break: break-word;
}


.answer-value.wrong {
    color: #956775;
}


.correct-answer {
    color: #507b78;
}


/* ============================================================
   Bottom Action
   ============================================================ */

.bottom-action {
    width: min(1100px, 100%);

    margin:
        34px auto 0;

    display: flex;

    justify-content: flex-end;
}


.reading-button {
    display: inline-flex;

    align-items: center;

    gap: 10px;

    padding:
        12px 18px;

    color: #ffffff;

    text-decoration: none;

    background:
        linear-gradient(135deg,
            var(--color-primary,
                #7ca8cd),
            var(--color-primary-dark,
                #668fb7));

    border-radius: 999px;

    font-size: 0.72rem;

    box-shadow:
        0 11px 27px rgba(82,
            133,
            178,
            0.21);

    transition:
        transform 0.2s ease,
        box-shadow 0.2s ease;
}


.reading-button:hover {
    color: #ffffff;

    transform:
        translateY(-2px);

    box-shadow:
        0 15px 31px rgba(82,
            133,
            178,
            0.26);
}


/* ============================================================
   Mobile
   ============================================================ */

@media (max-width: 700px) {

    .history-detail-page {
        padding:
            14px 14px 55px;
    }


    .detail-header {
        align-items: flex-start;

        flex-direction: column;

        margin-top: 60px;
    }


    .detail-header h1 {
        font-size: 2.25rem;
    }


    .score-card {
        flex-direction: column;

        gap: 28px;

        margin-bottom: 60px;
    }


    .score-divider {
        width: 70px;
        height: 1px;
    }


    .review-heading {
        align-items: flex-start;

        flex-direction: column;
    }


    .answer-comparison {
        grid-template-columns: 1fr;
    }


    .answer-card {
        padding:
            22px 18px;
    }


    .bottom-action {
        justify-content: stretch;
    }


    .reading-button {
        width: 100%;

        justify-content: center;
    }

}
</style>