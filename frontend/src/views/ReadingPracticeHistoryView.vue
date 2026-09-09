<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import AppNavigation from '@/components/AppNavigation.vue'

import {
    deleteReadingPracticeRecord,
    getReadingPracticeHistory,
    type ReadingPracticeRecord,
} from '@/api/reading'

/**
 * Reading Practice 历史记录列表。
 */
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
        ...records.value.map(
            record => record.percentage,
        ),
    )
})

/**
 * 页面加载状态。
 */
const loading = ref(true)

/**
 * 页面错误信息。
 */
const errorMessage = ref('')

/**
 * 把后端时间戳转换成更容易阅读的本地时间。
 */
function formatSubmittedAt(timestamp: number) {
    return new Date(timestamp).toLocaleString()
}

/**
 * 从后端重新加载 Reading Practice History。
 */
async function loadHistory() {
    loading.value = true
    errorMessage.value = ''

    try {
        records.value =
            await getReadingPracticeHistory()
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value =
                '加载 Reading Practice History 失败'
        }
    } finally {
        loading.value = false
    }
}

/**
 * 删除一条 Reading Practice History。
 *
 * 删除之前继续保留确认框，
 * 防止用户误删。
 */
async function deleteRecord(recordId: number) {
    const confirmed = window.confirm(
        '确定要删除这条 Reading Practice History 吗？',
    )

    if (!confirmed) {
        return
    }

    try {
        await deleteReadingPracticeRecord(recordId)

        /**
         * 后端删除成功以后，
         * 同步从当前页面删除对应记录。
         */
        records.value = records.value.filter(
            record => record.id !== recordId,
        )
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value =
                '删除 Reading Practice History 失败'
        }
    }
}

onMounted(() => {
    loadHistory()
})
</script>


<template>
    <main class="history-page">

        <!-- 全站统一顶部导航 -->
        <AppNavigation />


        <!-- =====================================================
             页面标题
             ===================================================== -->
        <header class="history-header">

            <div>
                <p class="section-label">
                    READING HISTORY
                </p>

                <h1>
                    Practice History
                </h1>

                <p class="header-description">
                    每一次练习，都会留下可以回看的进步轨迹。
                </p>
            </div>


            <RouterLink to="/reading" class="back-button">
                ← Back to Reading
            </RouterLink>

        </header>


        <!-- =====================================================
             Summary

             只有有历史记录时才显示。
             ===================================================== -->
        <section v-if="
            !loading &&
            !errorMessage &&
            records.length > 0
        " class="history-summary">

            <div class="summary-item">

                <span class="summary-number">
                    {{ practiceCount }}
                </span>

                <span class="summary-label">
                    Practices
                </span>

            </div>


            <div class="summary-divider"></div>


            <div class="summary-item">

                <span class="summary-number">
                    {{ averageAccuracy.toFixed(1) }}%
                </span>

                <span class="summary-label">
                    Average
                </span>

            </div>


            <div class="summary-divider"></div>


            <div class="summary-item">

                <span class="summary-number">
                    {{ bestAccuracy.toFixed(1) }}%
                </span>

                <span class="summary-label">
                    Best
                </span>

            </div>

        </section>


        <!-- Loading -->
        <section v-if="loading" class="state-card">
            Loading practice history...
        </section>


        <!-- Error -->
        <section v-else-if="errorMessage" class="state-card error-message">
            {{ errorMessage }}
        </section>


        <!-- Empty -->
        <section v-else-if="records.length === 0" class="empty-card">

            <span class="empty-index">
                00
            </span>

            <h2>
                还没有练习记录
            </h2>

            <p>
                完成一次 Reading Practice 后，
                记录会出现在这里。
            </p>


            <RouterLink to="/reading" class="start-reading-button">
                Start Reading
                <span>→</span>
            </RouterLink>

        </section>


        <!-- =====================================================
             History List
             ===================================================== -->
        <section v-else class="history-list">

            <article v-for="(record, index) in records" :key="record.id" class="history-card">

                <!-- 左侧编号 -->
                <div class="record-index">
                    {{
                        String(index + 1)
                            .padStart(2, '0')
                    }}
                </div>


                <!-- 中间 Test 信息 -->
                <div class="record-main">

                    <p class="record-label">
                        READING PRACTICE
                    </p>


                    <h2>
                        <RouterLink :to="`/reading/history/${record.id}`" class="test-link">
                            {{ record.testTitle }}
                        </RouterLink>
                    </h2>


                    <p class="submitted-at">
                        {{ formatSubmittedAt(record.submittedAt) }}
                    </p>

                </div>


                <!-- 成绩 -->
                <div class="record-score">

                    <strong>
                        {{ record.correctCount }}
                        /
                        {{ record.totalQuestions }}
                    </strong>

                    <span>
                        {{ record.percentage.toFixed(1) }}%
                    </span>

                </div>


                <!-- 操作 -->
                <div class="record-actions">

                    <RouterLink :to="`/reading/history/${record.id}`" class="detail-button">
                        View Detail
                        <span>→</span>
                    </RouterLink>


                    <button type="button" class="delete-button" @click="deleteRecord(record.id)">
                        Delete
                    </button>

                </div>

            </article>

        </section>

    </main>
</template>


<style scoped>
/* ============================================================
   Reading Practice History

   继续沿用当前全站视觉：

   - Maple Mono NF CN
   - 淡蓝背景
   - Liquid Glass
   - 大留白
   ============================================================ */

.history-page {
    min-height: 100vh;

    box-sizing: border-box;

    padding:
        24px 34px 80px;

    color: var(--color-text-main, #304d66);

    background:
        radial-gradient(circle at 12% 8%,
            rgba(184, 218, 245, 0.55),
            transparent 27%),
        radial-gradient(circle at 90% 6%,
            rgba(211, 227, 249, 0.72),
            transparent 29%),
        linear-gradient(180deg,
            var(--color-bg-start, #edf6fc) 0%,
            var(--color-bg-middle, #f8fbfd) 50%,
            var(--color-bg-end, #eef5fa) 100%);

    font-family:
        var(--font-main,
            "Maple Mono NF CN",
            "Consolas",
            monospace);
}


/* ============================================================
   Header
   ============================================================ */

.history-header {
    width: min(1240px, 100%);

    margin:
        88px auto 42px;

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


.history-header h1 {
    margin: 0;

    color: #304d66;

    font-size:
        clamp(2.8rem,
            5vw,
            4.8rem);

    font-weight: 600;

    letter-spacing: -0.08em;

    line-height: 1.05;
}


.header-description {
    margin:
        16px 0 0;

    color: #8197aa;

    font-size: 0.84rem;

    line-height: 1.7;
}


.back-button {
    display: inline-flex;

    align-items: center;

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
    color: #4e708c;

    background:
        rgba(255,
            255,
            255,
            0.62);

    transform:
        translateY(-1px);
}


/* ============================================================
   Summary

   不做三个大 Dashboard Card。

   改成一条轻量统计带。
   ============================================================ */

.history-summary {
    width: min(1240px, 100%);

    min-height: 112px;

    margin:
        0 auto 30px;

    box-sizing: border-box;

    display: flex;

    align-items: center;

    justify-content: center;

    gap: 52px;

    padding:
        22px 34px;

    background:
        rgba(255,
            255,
            255,
            0.38);

    border:
        1px solid rgba(255,
            255,
            255,
            0.70);

    border-radius: 28px;

    backdrop-filter:
        blur(20px) saturate(135%);

    -webkit-backdrop-filter:
        blur(20px) saturate(135%);

    box-shadow:
        0 18px 50px rgba(77,
            116,
            151,
            0.06);
}


.summary-item {
    min-width: 120px;

    display: flex;

    flex-direction: column;

    align-items: center;

    gap: 5px;
}


.summary-number {
    color: #42617b;

    font-size: 1.65rem;

    font-weight: 600;

    letter-spacing: -0.05em;
}


.summary-label {
    color: #8ca2b4;

    font-size: 0.68rem;
}


.summary-divider {
    width: 1px;
    height: 38px;

    background:
        rgba(108,
            148,
            180,
            0.18);
}


/* ============================================================
   History List
   ============================================================ */

.history-list {
    width: min(1240px, 100%);

    margin:
        0 auto;

    display: grid;

    gap: 13px;
}


.history-card {
    display: grid;

    grid-template-columns:
        52px minmax(0, 1fr) auto auto;

    align-items: center;

    gap: 26px;

    padding:
        24px 26px;

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

    border-radius: 24px;

    backdrop-filter:
        blur(18px);

    -webkit-backdrop-filter:
        blur(18px);

    box-shadow:
        0 14px 38px rgba(73,
            111,
            146,
            0.055);

    transition:
        transform 0.2s ease,
        background 0.2s ease,
        box-shadow 0.2s ease;
}


.history-card:hover {
    transform:
        translateY(-2px);

    background:
        rgba(255,
            255,
            255,
            0.60);

    box-shadow:
        0 20px 44px rgba(73,
            111,
            146,
            0.08);
}


/* ============================================================
   Record Index
   ============================================================ */

.record-index {
    color: #99afc1;

    font-size: 0.82rem;

    letter-spacing: 0.06em;
}


/* ============================================================
   Record Main
   ============================================================ */

.record-main {
    min-width: 0;
}


.record-label {
    margin:
        0 0 7px;

    color: #91a9bc;

    font-size: 0.63rem;

    font-weight: 600;

    letter-spacing: 0.11em;
}


.record-main h2 {
    margin: 0;

    font-size: 1.05rem;

    font-weight: 600;
}


.test-link {
    color: #37546c;

    text-decoration: none;
}


.test-link:hover {
    color: #527b9d;
}


.submitted-at {
    margin:
        8px 0 0;

    color: #8ba0b1;

    font-size: 0.72rem;
}


/* ============================================================
   Score
   ============================================================ */

.record-score {
    min-width: 100px;

    display: flex;

    flex-direction: column;

    align-items: flex-end;

    gap: 4px;
}


.record-score strong {
    color: #405f78;

    font-size: 1.35rem;

    font-weight: 600;

    letter-spacing: -0.05em;
}


.record-score span {
    color: #7895aa;

    font-size: 0.72rem;
}


/* ============================================================
   Actions
   ============================================================ */

.record-actions {
    display: flex;

    align-items: center;

    gap: 8px;
}


.detail-button {
    display: inline-flex;

    align-items: center;

    gap: 8px;

    padding:
        10px 14px;

    color: #537792;

    text-decoration: none;

    background:
        rgba(210,
            230,
            245,
            0.68);

    border-radius: 999px;

    font-size: 0.7rem;

    font-weight: 600;

    transition:
        transform 0.2s ease,
        background 0.2s ease;
}


.detail-button:hover {
    color: #476b87;

    background:
        rgba(199,
            224,
            242,
            0.84);

    transform:
        translateY(-1px);
}


.delete-button {
    padding:
        10px 13px;

    color: #9c717b;

    background:
        transparent;

    border: 0;

    border-radius: 12px;

    cursor: pointer;

    font-family:
        var(--font-main,
            "Maple Mono NF CN",
            "Consolas",
            monospace);

    font-size: 0.69rem;

    transition:
        background 0.2s ease;
}


.delete-button:hover {
    background:
        rgba(239,
            216,
            223,
            0.48);
}


/* ============================================================
   Loading / Error
   ============================================================ */

.state-card,
.empty-card {
    width: min(1240px, 100%);

    box-sizing: border-box;

    margin:
        0 auto;

    padding:
        48px 32px;

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


.state-card {
    color: #8298aa;
}


.error-message {
    color: #a96f7c;
}


/* ============================================================
   Empty
   ============================================================ */

.empty-card {
    min-height: 320px;

    display: flex;

    flex-direction: column;

    align-items: center;

    justify-content: center;
}


.empty-index {
    margin-bottom: 18px;

    color: #a2b6c7;

    font-size: 0.75rem;
}


.empty-card h2 {
    margin: 0;

    color: #45647d;

    font-size: 1.6rem;

    font-weight: 600;
}


.empty-card p {
    margin:
        13px 0 26px;

    color: #869cad;

    font-size: 0.78rem;

    line-height: 1.7;
}


.start-reading-button {
    display: inline-flex;

    align-items: center;

    gap: 10px;

    padding:
        12px 18px;

    color: #ffffff;

    text-decoration: none;

    background:
        linear-gradient(135deg,
            var(--color-primary, #7ca8cd),
            var(--color-primary-dark, #668fb7));

    border-radius: 999px;

    font-size: 0.72rem;

    box-shadow:
        0 11px 27px rgba(82,
            133,
            178,
            0.21);
}


/* ============================================================
   Tablet
   ============================================================ */

@media (max-width: 900px) {

    .history-card {
        grid-template-columns:
            44px minmax(0, 1fr) auto;
    }


    .record-actions {
        grid-column:
            2 / -1;

        justify-content: flex-end;
    }

}


/* ============================================================
   Mobile
   ============================================================ */

@media (max-width: 650px) {

    .history-page {
        padding:
            14px 14px 55px;
    }


    .history-header {
        align-items: flex-start;

        flex-direction: column;

        margin-top: 62px;
    }


    .history-header h1 {
        font-size: 2.6rem;
    }


    .history-summary {
        gap: 14px;

        padding:
            18px 12px;
    }


    .summary-item {
        min-width: 0;

        flex: 1;
    }


    .summary-number {
        font-size: 1.2rem;
    }


    .summary-divider {
        height: 30px;
    }


    .history-card {
        grid-template-columns:
            36px minmax(0, 1fr);

        align-items: flex-start;

        gap: 16px;

        padding:
            21px 18px;
    }


    .record-score {
        grid-column:
            2 / -1;

        align-items: flex-start;
    }


    .record-actions {
        grid-column:
            1 / -1;

        width: 100%;
    }


    .detail-button,
    .delete-button {
        flex: 1;

        justify-content: center;

        text-align: center;
    }

}
</style>