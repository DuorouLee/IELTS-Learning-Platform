<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import {
  getFullReadingTest,
  submitReadingTest,
  type FullReadingTestResponse,
  type ReadingSubmitResponse,
} from '@/api/reading'

const route = useRoute()

const testData = ref<FullReadingTestResponse | null>(null)
const loading = ref(true)
const errorMessage = ref('')

const answers = ref<Record<number, string>>({})

const submitResult = ref<ReadingSubmitResponse | null>(null)
const submitting = ref(false)
const submitErrorMessage = ref('')

const currentTestId = computed(() => Number(route.params.testId))

const storageKey = computed(() => {
  return `reading-test-${currentTestId.value}-answers`
})

const answeredCount = computed(() => {
  return Object.values(answers.value).filter((answer) => answer !== '').length
})

const totalQuestionCount = computed(() => {
  if (!testData.value) {
    return 0
  }

  return testData.value.passages.reduce((total, passage) => {
    return total + passage.questions.length
  }, 0)
})

function splitPassageContent(content: string) {
  return content
    .split(/(?=Paragraph [A-Z])/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean)
}

async function loadReadingTest() {
  loading.value = true
  errorMessage.value = ''

  try {
    testData.value = await getFullReadingTest(currentTestId.value)

    const savedAnswers = localStorage.getItem(storageKey.value)

    if (savedAnswers) {
      answers.value = JSON.parse(savedAnswers)
    } else {
      answers.value = {}
    }

    submitResult.value = null
    submitErrorMessage.value = ''
  } catch (error) {
    if (error instanceof Error) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = '加载 Reading Test 时发生未知错误'
    }
  } finally {
    loading.value = false
  }
}

/**
 * 提交当前 Reading Test 的答案。
 *
 * 提交成功后：
 * 1. 后端保存正式练习记录；
 * 2. 前端保存后端返回的判分结果；
 * 3. 删除当前 Test 的 localStorage 草稿。
 *
 * 这样可以保证：
 * - 未提交的练习可以恢复；
 * - 已提交的练习不会被当成草稿恢复；
 * - 再次进入同一个 Test 时会开始新的练习。
 */
async function submitAnswers() {
  submitting.value = true
  submitErrorMessage.value = ''

  try {
    submitResult.value = await submitReadingTest(currentTestId.value, {
      answers: answers.value,
    })

    // 只有后端提交成功以后才删除草稿。
    // 如果请求失败，草稿仍然保留，避免用户答案丢失。
    localStorage.removeItem(storageKey.value)
  } catch (error) {
    if (error instanceof Error) {
      submitErrorMessage.value = error.message
    } else {
      submitErrorMessage.value = '提交 Reading Test 时发生未知错误'
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 根据 questionId 找到后端返回的 Review 结果。
 */
function getQuestionReview(questionId: number) {
  return submitResult.value?.questions.find(
    (review) => review.questionId === questionId,
  )
}

/**
 * 自动保存未提交的 Reading 草稿。
 *
 * submitResult === null：
 * 当前仍然是未提交练习，可以继续保存草稿。
 *
 * submitResult !== null：
 * 当前练习已经正式提交，不再写回 localStorage。
 *
 * 这个判断非常重要，否则 submitAnswers() 删除草稿之后，
 * answers 后续发生变化时可能再次把已提交答案写回 localStorage。
 */
watch(
  answers,
  (newAnswers) => {
    if (submitResult.value !== null) {
      return
    }

    localStorage.setItem(
      storageKey.value,
      JSON.stringify(newAnswers),
    )
  },
  {
    deep: true,
  },
)

watch(
  () => route.params.testId,
  () => {
    loadReadingTest()
  },
  {
    immediate: true,
  },
)

</script>

<template>
  <main class="reading-page">
    <p v-if="loading">Loading...</p>

    <p v-else-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>

    <template v-else-if="testData">
      <header class="reading-header">
        <div>
          <h1>{{ testData.test.title }}</h1>
          <p class="source">
            来源：{{ testData.test.source }}
          </p>
        </div>

        <div class="reading-actions">
          <div class="reading-progress">
            已答：{{ answeredCount }} / {{ totalQuestionCount }}
          </div>

          <button class="submit-button" :disabled="submitting || submitResult !== null" @click="submitAnswers">
            {{
              submitResult
                ? '已提交'
                : submitting
                  ? '提交中...'
                  : '提交答案'
            }}
          </button>
        </div>
      </header>

      <section v-if="submitResult" class="result-card">
        <h2>Reading Result</h2>

        <div class="result-summary">
          <div>
            <strong>{{ submitResult.correctCount }}</strong>
            <span>Correct</span>
          </div>

          <div>
            <strong>{{ submitResult.incorrectCount }}</strong>
            <span>Incorrect</span>
          </div>

          <div>
            <strong>{{ submitResult.totalQuestions }}</strong>
            <span>Total</span>
          </div>

          <div>
            <strong>
              {{ submitResult.percentage.toFixed(2) }}%
            </strong>
            <span>Accuracy</span>
          </div>
        </div>
      </section>

      <p v-if="submitErrorMessage" class="submit-error">
        {{ submitErrorMessage }}
      </p>

      <section v-for="passage in testData.passages" :key="passage.id" class="passage-section">
        <h2 class="passage-heading">
          Reading Passage {{ passage.passageNumber }}
        </h2>

        <p v-if="passage.instruction" class="passage-instruction">
          {{ passage.instruction }}
        </p>

        <div class="reading-layout">
          <section class="article-panel">
            <h3 class="panel-title">Article</h3>

            <div class="article-content">
              <h3 class="article-title">
                {{ passage.title }}
              </h3>

              <p v-for="(paragraph, index) in splitPassageContent(passage.content)" :key="index"
                class="article-paragraph">
                {{ paragraph }}
              </p>
            </div>
          </section>

          <section class="questions-panel">
            <h3 class="panel-title">Questions</h3>

            <div v-for="group in passage.questionGroups" :key="group.id" class="question-group">
              <p class="group-instruction">
                {{ group.instruction }}
              </p>

              <div v-if="group.questionType === 'MATCHING_HEADINGS'" class="heading-options">
                <p class="heading-options-label">
                  MATCHING HEADINGS
                </p>

                <h4>
                  List of Headings
                </h4>

                <div class="heading-reference-list">
                  <div v-for="option in group.options" :key="option.id" class="heading-reference-item">
                    <span class="heading-reference-value">
                      {{ option.optionValue }}
                    </span>

                    <span>
                      {{ option.optionText }}
                    </span>
                  </div>
                </div>
              </div>

              <div v-else-if="group.questionType === 'MATCHING_FEATURES'" class="feature-options">
                <h4>Options</h4>

                <ul>
                  <li v-for="option in group.options" :key="option.id">
                    <strong>{{ option.optionValue }}</strong>
                    {{ option.optionText }}
                  </li>
                </ul>
              </div>

              <div class="question-list">

                <div v-for="question in group.questions" :key="question.id" class="question-item">

                  <!-- =====================================================
         Matching Headings
         ===================================================== -->
                  <div v-if="group.questionType === 'MATCHING_HEADINGS'" class="matching-heading-question">

                    <div class="matching-question-label">

                      <span class="matching-question-number">
                        {{ question.questionNumber }}
                      </span>

                      <span class="matching-question-text">
                        {{ question.questionText }}
                      </span>

                    </div>


                    <select v-model="answers[question.id]" :disabled="submitResult !== null" class="heading-select">

                      <option value="">
                        Select a heading
                      </option>

                      <option v-for="option in group.options" :key="option.id" :value="option.optionValue">
                        {{ option.optionValue }}
                        —
                        {{ option.optionText }}
                      </option>

                    </select>

                  </div>


                  <!-- =====================================================
                    Matching Features
                    ===================================================== -->
                  <template v-else-if="group.questionType === 'MATCHING_FEATURES'">
                    <div class="matching-feature-question">

                      <div class="matching-question-label">

                        <span class="matching-question-number">
                          {{ question.questionNumber }}
                        </span>

                        <span class="matching-question-text">
                          {{ question.questionText }}
                        </span>

                      </div>

                      <select v-model="answers[question.id]" :disabled="submitResult !== null" class="heading-select">
                        <option value="">
                          Select an option
                        </option>

                        <option v-for="option in group.options" :key="option.id" :value="option.optionValue">
                          {{ option.optionValue }}
                          —
                          {{ option.optionText }}
                        </option>
                      </select>

                    </div>
                  </template>


                  <!-- =====================================================
         其他题型
         ===================================================== -->
                  <template v-else>

                    <p class="question-text">
                      {{ question.questionNumber }}.
                      {{ question.questionText }}
                    </p>

                    <input v-model="answers[question.id]" :disabled="submitResult !== null" class="answer-input"
                      type="text" placeholder="请输入答案" />

                  </template>


                  <!-- =====================================================
         Submit 后 Review

         这一块保持你原来的逻辑。
         ===================================================== -->
                  <div v-if="getQuestionReview(question.id)" class="question-review" :class="{
                    correct:
                      getQuestionReview(question.id)?.correct,

                    incorrect:
                      !getQuestionReview(question.id)?.correct,
                  }">

                    <strong>
                      {{
                        getQuestionReview(question.id)?.correct
                          ? '✓ Correct'
                          : '✗ Incorrect'
                      }}
                    </strong>

                    <span>
                      Your answer:
                      {{
                        getQuestionReview(question.id)?.userAnswer ||
                        'Not answered'
                      }}
                    </span>

                    <span>
                      Correct answer:
                      {{
                        getQuestionReview(question.id)?.correctAnswer
                      }}
                    </span>

                  </div>

                </div>

              </div>
            </div>
          </section>
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
.reading-page,
.reading-page button,
.reading-page input,
.reading-page select,
.reading-page textarea,
.reading-page option {
  font-family:
    var(--font-main,
      "Maple Mono NF CN",
      "Consolas",
      monospace);
}

.reading-page {
  width: 100%;
  padding: 32px 36px;
  box-sizing: border-box;
}

.reading-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 28px;
}

.reading-header h1 {
  margin: 0 0 8px;
}

.source {
  margin: 0;
}

.reading-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reading-progress {
  padding: 8px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.submit-button {
  padding: 8px 16px;
  border: 1px solid #ccc;
  border-radius: 8px;
  cursor: pointer;
}

.submit-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.result-card {
  margin-bottom: 24px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 10px;
}

.result-card h2 {
  margin: 0 0 16px;
}

.result-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.result-summary div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.result-summary strong {
  font-size: 24px;
}

.result-summary span {
  font-size: 14px;
}

.submit-error,
.error-message {
  margin-bottom: 16px;
}

.passage-section {
  margin-top: 24px;
}

.passage-heading {
  margin-bottom: 14px;
}

.passage-instruction {
  margin-bottom: 20px;
}

.reading-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.article-panel,
.questions-panel {
  height: 620px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 24px;
  box-sizing: border-box;
}

.panel-title {
  margin: 0 0 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #e5e5e5;
}

.article-title {
  margin-top: 0;
  margin-bottom: 24px;
}

.article-paragraph {
  line-height: 1.8;
  margin-bottom: 20px;
}

.question-group {
  margin-bottom: 28px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e5e5e5;
}

.group-instruction {
  font-weight: 500;
  margin-bottom: 16px;
}

.heading-options,
.feature-options {
  margin-bottom: 24px;
  padding: 16px 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fafafa;
}

.heading-options h4,
.feature-options h4 {
  margin: 0 0 12px;
}

.heading-options ul,
.feature-options ul {
  margin: 0;
  padding-left: 24px;
}

.heading-options li,
.feature-options li {
  margin-bottom: 6px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-item {
  padding-bottom: 16px;
}

.question-text {
  margin: 0 0 8px;
}

.answer-select,
.answer-input {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 10px;
}

.answer-select:disabled,
.answer-input:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

.question-review {
  display: flex;
  flex-direction: column;
  gap: 4px;

  margin-top: 8px;
  padding: 10px 12px;

  border-radius: 8px;
  font-size: 14px;
}

.question-review.correct {
  background: #f0f8f2;
  border: 1px solid #b7d8bf;
}

.question-review.incorrect {
  background: #fff4f4;
  border: 1px solid #e2bcbc;
}

@media (max-width: 900px) {
  .reading-header {
    flex-direction: column;
  }

  .reading-layout {
    grid-template-columns: 1fr;
  }

  .article-panel,
  .questions-panel {
    height: auto;
    max-height: 620px;
  }

  .result-summary {
    grid-template-columns: repeat(2, 1fr);
  }
}

.matching-heading-question {
  display: grid;

  grid-template-columns:
    minmax(160px, 0.42fr) minmax(0, 1fr);

  align-items: center;

  gap: 18px;

  padding:
    16px 18px;

  margin-bottom: 12px;

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

  border-radius: 18px;

  backdrop-filter:
    blur(14px);

  -webkit-backdrop-filter:
    blur(14px);
}


.matching-question-label {
  display: flex;

  align-items: center;

  gap: 12px;
}


.matching-question-number {
  color: #8ca4b8;

  font-size: 0.7rem;

  flex-shrink: 0;
}


.matching-question-text {
  color: #465f75;

  font-size: 0.82rem;

  font-weight: 600;

  line-height: 1.45;
}


/* ============================================================
   Matching Headings Select

   用 Maple Mono NF CN，
   并且把原生 select 做得更像当前的淡蓝 UI。
   ============================================================ */

.heading-select {
  width: 100%;

  min-height: 48px;

  box-sizing: border-box;

  padding:
    11px 40px 11px 14px;

  color: #45637b;

  background:
    rgba(255,
      255,
      255,
      0.62);

  border:
    1px solid rgba(128,
      172,
      207,
      0.34);

  border-radius: 14px;

  outline: none;

  font-family:
    var(--font-main,
      "Maple Mono NF CN",
      "Consolas",
      monospace);

  font-size: 0.76rem;

  transition:
    border-color 0.18s ease,
    background 0.18s ease,
    box-shadow 0.18s ease;
}


.heading-select:hover {
  background:
    rgba(255,
      255,
      255,
      0.78);

  border-color:
    rgba(108,
      157,
      196,
      0.52);
}


.heading-select:focus {
  border-color:
    rgba(98,
      151,
      194,
      0.68);

  box-shadow:
    0 0 0 4px rgba(112,
      162,
      204,
      0.10);
}


.heading-select option {
  font-family:
    var(--font-main,
      "Maple Mono NF CN",
      "Consolas",
      monospace);

  color: #3f5c74;
}


@media (max-width: 700px) {
  .matching-heading-question {
    grid-template-columns: 1fr;
  }
}

/* ============================================================
   List of Headings
   ============================================================ */

.heading-reference-list {
  display: grid;
  gap: 8px;
  margin-top: 14px;
}

.heading-reference-item {
  display: grid;

  grid-template-columns:
    42px minmax(0, 1fr);

  gap: 12px;

  padding:
    10px 12px;

  color: #49657c;

  background:
    rgba(255, 255, 255, 0.58);

  border:
    1px solid rgba(128, 172, 207, 0.22);

  border-radius: 12px;

  font-size: 0.76rem;

  line-height: 1.5;
}

.heading-reference-value {
  color: #789ab5;

  font-weight: 700;
}

.matching-feature-question {
  display: grid;

  grid-template-columns:
    minmax(160px, 0.42fr) minmax(0, 1fr);

  align-items: center;

  gap: 18px;

  padding:
    16px 18px;

  margin-bottom: 12px;

  background:
    rgba(255, 255, 255, 0.42);

  border:
    1px solid rgba(255, 255, 255, 0.72);

  border-radius: 18px;

  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

@media (max-width: 700px) {
  .matching-feature-question {
    grid-template-columns: 1fr;
  }
}
</style>