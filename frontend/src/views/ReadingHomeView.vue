<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppNavigation from '@/components/AppNavigation.vue'

import {
    getReadingTests,
    type ReadingTest,
} from '@/api/reading'

/**
 * readingTests
 *
 * 保存后端返回的 Reading Test 列表。
 */
const readingTests = ref<ReadingTest[]>([])

/**
 * loading
 *
 * Reading 页面第一次进入时，
 * 后端数据还没有返回，所以默认是 true。
 */
const loading = ref(true)

/**
 * errorMessage
 *
 * 请求 Reading Test 失败时保存错误。
 */
const errorMessage = ref('')

/**
 * 页面加载后读取题库。
 */
onMounted(async () => {
    try {
        readingTests.value = await getReadingTests()
    } catch (error) {
        if (error instanceof Error) {
            errorMessage.value = error.message
        } else {
            errorMessage.value = '获取 Reading Test 列表失败'
        }
    } finally {
        loading.value = false
    }
})
</script>


<template>
    <main class="reading-page">
        <!-- 顶部导航 -->
        <AppNavigation />

        <!-- 页面标题 -->
        <header class="reading-header">
            <div>
                <p class="section-label">
                    IELTS READING
                </p>

                <h1>
                    Reading
                </h1>

                <p class="reading-description">
                    选择一套题目，开始一次完整的 IELTS Reading Practice。
                </p>
            </div>

            <RouterLink to="/reading/history" class="history-button">
                Practice History
                <span>→</span>
            </RouterLink>
        </header>

        <!-- Loading -->
        <div v-if="loading" class="state-card">
            Reading Tests 正在加载...
        </div>

        <!-- Error -->
        <div v-else-if="errorMessage" class="state-card error-message">
            {{ errorMessage }}
        </div>

        <!-- Empty -->
        <div v-else-if="readingTests.length === 0" class="state-card">
            目前还没有 Reading Test。
        </div>

        <!-- Reading Tests -->
        <section v-else class="test-grid">
            <article v-for="(test, index) in readingTests" :key="test.id" class="test-card">
                <span class="test-number">
                    {{
                        String(index + 1).padStart(2, '0')
                    }}
                </span>

                <div class="test-content">
                    <p class="test-label">
                        READING TEST
                    </p>

                    <h2>
                        {{ test.title }}
                    </h2>

                    <p class="test-source">
                        {{ test.source }}
                    </p>
                </div>

                <RouterLink :to="`/reading/tests/${test.id}`" class="start-button">
                    开始练习
                    <span>→</span>
                </RouterLink>
            </article>
        </section>
    </main>
</template>


<style scoped>
.reading-page {
    min-height: 100vh;

    box-sizing: border-box;

    padding:
        24px 34px 70px;

    color: #263d52;

    background:
        radial-gradient(circle at 12% 8%,
            rgba(186, 218, 244, 0.55),
            transparent 27%),
        linear-gradient(180deg,
            #edf6fc 0%,
            #f8fbfd 52%,
            #eef5fa 100%);

    font-family:
        "Microsoft YaHei",
        "PingFang SC",
        sans-serif;
}


/* 英文 UI */
.brand,
.navigation-link,
.section-label,
.history-button,
.test-label,
.test-number,
.start-button {
    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;
}

/* Header */
.reading-header {
    width: min(1240px, 100%);

    margin:
        95px auto 40px;

    display: flex;

    align-items: flex-end;

    justify-content: space-between;

    gap: 30px;
}


.section-label {
    margin:
        0 0 10px;

    color: #86a0b7;

    font-size: 0.72rem;

    letter-spacing: 0.12em;
}


.reading-header h1 {
    margin: 0;

    color: #304c65;

    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;

    font-size:
        clamp(3rem,
            6vw,
            5.5rem);

    font-weight: 600;

    letter-spacing: -0.08em;
}


.reading-description {
    margin:
        15px 0 0;

    color: #8195a8;

    font-size: 0.92rem;
}


.history-button {
    display: inline-flex;

    align-items: center;

    gap: 10px;

    padding:
        11px 17px;

    color: #5a7994;

    text-decoration: none;

    background:
        rgba(255,
            255,
            255,
            0.48);

    border:
        1px solid rgba(255,
            255,
            255,
            0.75);

    border-radius: 999px;
}


/* Tests */
.test-grid {
    width: min(1240px, 100%);

    margin:
        0 auto;

    display: grid;

    gap: 14px;
}


.test-card {
    display: grid;

    grid-template-columns:
        60px minmax(0, 1fr) auto;

    align-items: center;

    gap: 26px;

    padding:
        25px 26px;

    background:
        rgba(255,
            255,
            255,
            0.48);

    border:
        1px solid rgba(255,
            255,
            255,
            0.76);

    border-radius: 25px;

    backdrop-filter:
        blur(18px);

    -webkit-backdrop-filter:
        blur(18px);

    box-shadow:
        0 18px 45px rgba(74,
            112,
            146,
            0.07);
}


.test-number {
    color: #9bb1c3;

    font-size: 0.9rem;
}


.test-label {
    margin:
        0 0 7px;

    color: #93aabd;

    font-size: 0.65rem;

    letter-spacing: 0.1em;
}


.test-content h2 {
    margin:
        0 0 7px;

    color: #354e64;

    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;

    font-size: 1.1rem;

    font-weight: 600;
}


.test-source {
    margin: 0;

    color: #879cad;

    font-size: 0.8rem;
}


.start-button {
    display: inline-flex;

    align-items: center;

    gap: 9px;

    padding:
        11px 16px;

    color: #537691;

    text-decoration: none;

    background:
        rgba(211,
            230,
            245,
            0.74);

    border-radius: 999px;

    font-size: 0.76rem;
}


.state-card {
    width: min(1240px, 100%);

    box-sizing: border-box;

    margin:
        0 auto;

    padding: 35px;

    color: #8499ab;

    text-align: center;

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

    border-radius: 24px;
}


.error-message {
    color: #a66775;
}


@media (max-width: 700px) {

    .reading-page {
        padding:
            14px 14px 50px;
    }


    .brand-text {
        display: none;
    }


    .navigation-link {
        padding:
            9px 10px;

        font-size: 0.7rem;
    }


    .reading-header {
        align-items: flex-start;

        flex-direction: column;

        margin-top: 65px;
    }


    .test-card {
        grid-template-columns:
            40px minmax(0, 1fr);
    }


    .start-button {
        grid-column:
            1 / -1;

        justify-content: center;
    }

}
</style>