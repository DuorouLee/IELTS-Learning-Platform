<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppNavigation from '@/components/AppNavigation.vue'

import {
  getReadingTests,
  type ReadingTest,
} from '@/api/reading'

/**
 * 后端返回的全部 Reading Test。
 */
const readingTests = ref<ReadingTest[]>([])

const loading = ref(true)
const errorMessage = ref('')

/**
 * 首页按 C21、C20、C19 ... 分组。
 */
type ReadingBookGroup = {
  bookNumber: number
  bookName: string
  tests: Array<{
    test: ReadingTest
    testNumber: number
  }>
}

/**
 * 只保留正式题库，并按照：
 *
 * C21 -> C20 -> ... -> C5
 * 每一行内部 Test 编号从小到大
 *
 * 例如：
 * C21  Test 1  Test 2  Test 3  Test 4
 */
const groupedReadingTests = computed<ReadingBookGroup[]>(() => {
  const groups = new Map<number, ReadingBookGroup>()

  for (const test of readingTests.value) {
    /**
     * 正式题目的 title 格式：
     *
     * C21 Test 1
     * C20 Test 4
     * C12 Test 8
     *
     * 旧测试（例如 A Brief History of Tea）
     * 不符合这个格式，因此不会显示。
     */
    const match = test.title.match(/^C(\d+)\s+Test\s+(\d+)$/i)

    if (!match) {
      continue
    }

    const bookNumber = Number(match[1])
    const testNumber = Number(match[2])

    if (!groups.has(bookNumber)) {
      groups.set(bookNumber, {
        bookNumber,
        bookName: `C${bookNumber}`,
        tests: [],
      })
    }

    groups.get(bookNumber)!.tests.push({
      test,
      testNumber,
    })
  }

  return Array.from(groups.values())
    .sort((a, b) => b.bookNumber - a.bookNumber)
    .map((group) => ({
      ...group,
      tests: group.tests.sort(
        (a, b) => a.testNumber - b.testNumber,
      ),
    }))
})

/**
 * 页面加载后读取 Reading 题库。
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
    <AppNavigation />

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

      <RouterLink
        to="/reading/history"
        class="history-button"
      >
        Practice History
        <span>→</span>
      </RouterLink>
    </header>

    <div
      v-if="loading"
      class="state-card"
    >
      Reading Tests 正在加载...
    </div>

    <div
      v-else-if="errorMessage"
      class="state-card error-message"
    >
      {{ errorMessage }}
    </div>

    <div
      v-else-if="groupedReadingTests.length === 0"
      class="state-card"
    >
      目前还没有 Reading Test。
    </div>

    <!--
      每一本 Cxx 占一行。

      桌面端效果类似：
      C21  [Test 1] [Test 2] [Test 3] [Test 4]
      C20  [Test 1] [Test 2] [Test 3] [Test 4]
    -->
    <section
      v-else
      class="book-list"
    >
      <article
        v-for="book in groupedReadingTests"
        :key="book.bookNumber"
        class="book-row"
      >
        <div class="book-title">
          <span class="book-label">
            CAMBRIDGE
          </span>

          <h2>
            {{ book.bookName }}
          </h2>
        </div>

        <div class="test-card-grid">
          <RouterLink
            v-for="item in book.tests"
            :key="item.test.id"
            :to="`/reading/tests/${item.test.id}`"
            class="test-card"
          >
            <span class="test-label">
              READING TEST
            </span>

            <strong class="test-name">
              Test {{ item.testNumber }}
            </strong>

            <span class="start-text">
              Start
              <span>→</span>
            </span>
          </RouterLink>
        </div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.reading-page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 24px 34px 70px;
  color: #263d52;

  background:
    radial-gradient(
      circle at 12% 8%,
      rgba(186, 218, 244, 0.55),
      transparent 27%
    ),
    linear-gradient(
      180deg,
      #edf6fc 0%,
      #f8fbfd 52%,
      #eef5fa 100%
    );

  font-family:
    "Microsoft YaHei",
    "PingFang SC",
    sans-serif;
}

.section-label,
.history-button,
.book-label,
.book-title h2,
.test-label,
.test-name,
.start-text {
  font-family:
    "Maple Mono NF CN",
    "Consolas",
    monospace;
}

/* 页面标题 */
.reading-header {
  width: min(1240px, 100%);
  margin: 95px auto 42px;

  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 30px;
}

.section-label {
  margin: 0 0 10px;
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

  font-size: clamp(3rem, 6vw, 5.5rem);
  font-weight: 600;
  letter-spacing: -0.08em;
}

.reading-description {
  margin: 15px 0 0;
  color: #8195a8;
  font-size: 0.92rem;
}

.history-button {
  display: inline-flex;
  align-items: center;
  gap: 10px;

  padding: 11px 17px;

  color: #5a7994;
  text-decoration: none;

  background: rgba(255, 255, 255, 0.48);
  border: 1px solid rgba(255, 255, 255, 0.75);
  border-radius: 999px;

  transition:
    transform 0.18s ease,
    background 0.18s ease;
}

.history-button:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.72);
}

/* C21 / C20 / C19 ... */
.book-list {
  width: min(1240px, 100%);
  margin: 0 auto;

  display: grid;
  gap: 18px;
}

.book-row {
  display: grid;
  grid-template-columns: 125px minmax(0, 1fr);
  gap: 24px;

  padding: 22px;

  background: rgba(255, 255, 255, 0.42);
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 24px;

  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);

  box-shadow:
    0 16px 40px rgba(74, 112, 146, 0.055);
}

.book-title {
  display: flex;
  flex-direction: column;
  justify-content: center;

  padding-left: 6px;
}

.book-label {
  margin-bottom: 5px;

  color: #9ab0c2;
  font-size: 0.58rem;
  letter-spacing: 0.1em;
}

.book-title h2 {
  margin: 0;

  color: #3d5b73;
  font-size: 1.65rem;
  font-weight: 600;
}

/* 每一本书里面的 Test 小方块 */
.test-card-grid {
  display: grid;
  grid-template-columns:
    repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.test-card {
  min-height: 118px;
  box-sizing: border-box;

  display: flex;
  flex-direction: column;

  padding: 17px 18px;

  color: inherit;
  text-decoration: none;

  background: rgba(233, 244, 252, 0.8);
  border: 1px solid rgba(199, 222, 239, 0.78);
  border-radius: 18px;

  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    background 0.18s ease;
}

.test-card:hover {
  transform: translateY(-3px);

  background: rgba(242, 249, 254, 0.96);

  box-shadow:
    0 12px 26px rgba(73, 112, 145, 0.11);
}

.test-label {
  color: #9aafc0;
  font-size: 0.58rem;
  letter-spacing: 0.08em;
}

.test-name {
  margin-top: 9px;

  color: #3c5b74;
  font-size: 1.05rem;
  font-weight: 600;
}

.start-text {
  margin-top: auto;

  display: flex;
  align-items: center;
  justify-content: space-between;

  color: #6d8ca5;
  font-size: 0.68rem;
}

/* Loading / Error / Empty */
.state-card {
  width: min(1240px, 100%);
  box-sizing: border-box;

  margin: 0 auto;
  padding: 35px;

  color: #8499ab;
  text-align: center;

  background: rgba(255, 255, 255, 0.46);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
}

.error-message {
  color: #a66775;
}

/* 平板 */
@media (max-width: 900px) {
  .book-row {
    grid-template-columns: 95px minmax(0, 1fr);
  }

  .test-card-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }
}

/* 手机 */
@media (max-width: 700px) {
  .reading-page {
    padding: 14px 14px 50px;
  }

  .reading-header {
    align-items: flex-start;
    flex-direction: column;
    margin-top: 65px;
  }

  .book-row {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .book-title {
    padding-left: 2px;
  }

  .test-card-grid {
    grid-template-columns:
      repeat(2, minmax(0, 1fr));
  }

  .test-card {
    min-height: 105px;
  }
}

@media (max-width: 430px) {
  .test-card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
