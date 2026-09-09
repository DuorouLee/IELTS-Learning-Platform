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

              <!--
                Matching Features 的可选项参考区。

                这里和 Matching Headings 使用相同的列表结构，
                这样两种 Matching 题型在视觉上保持一致。

                注意：
                这里只改变展示方式，
                不改变 optionValue，
                所以不会影响答案保存、localStorage 或后端判分。
              -->
              <div v-else-if="group.questionType === 'MATCHING_FEATURES'" class="feature-options">
                <p class="heading-options-label">
                  MATCHING FEATURES
                </p>

                <h4>List of Options</h4>

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
                    其他 Matching 类型

                    这些题型和 Matching Features 的共同点是：
                    - 后端提供 group.options
                    - 用户选择一个 optionValue 作为答案
                    - 仍然可以复用现有 localStorage / submit / 判分逻辑

                    当前先支持常见的三类：
                    MATCHING_INFORMATION
                    MATCHING_NAMES
                    MATCHING_SENTENCE_ENDINGS
                    ===================================================== -->
                  <template v-else-if="
                    group.questionType === 'MATCHING_INFORMATION' ||
                    group.questionType === 'MATCHING_NAMES' ||
                    group.questionType === 'MATCHING_SENTENCE_ENDINGS'
                  ">
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
                    TRUE / FALSE / NOT GIVEN

                    这类题目应该使用固定选项，
                    而不是让用户手动输入文字。

                    answers[question.id] 仍然保存字符串，
                    所以不会影响：
                    - localStorage
                    - submit API
                    - 后端判分
                    ===================================================== -->
                  <template v-else-if="group.questionType === 'TRUE_FALSE_NOT_GIVEN'">
                    <div class="choice-question">
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
                          Select an answer
                        </option>

                        <option value="TRUE">
                          TRUE
                        </option>

                        <option value="FALSE">
                          FALSE
                        </option>

                        <option value="NOT GIVEN">
                          NOT GIVEN
                        </option>
                      </select>
                    </div>
                  </template>


                  <!-- =====================================================
                    YES / NO / NOT GIVEN

                    和 TFNG 使用相同的 UI 结构，
                    区别只在选项内容不同。
                    ===================================================== -->
                  <template v-else-if="group.questionType === 'YES_NO_NOT_GIVEN'">
                    <div class="choice-question">
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
                          Select an answer
                        </option>

                        <option value="YES">
                          YES
                        </option>

                        <option value="NO">
                          NO
                        </option>

                        <option value="NOT GIVEN">
                          NOT GIVEN
                        </option>
                      </select>
                    </div>
                  </template>

                  <!-- =====================================================
                    MULTIPLE CHOICE

                    选择题的选项来自后端 group.options。

                    answers[question.id] 仍然保存 optionValue，
                    例如 A / B / C / D，
                    所以可以直接兼容现有：
                    - localStorage
                    - submit API
                    - 后端字符串判分
                    ===================================================== -->
                  <template v-else-if="group.questionType === 'MULTIPLE_CHOICE'">
                    <div class="choice-question">
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
                          Select an answer
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

/*
 * Matching Headings / Matching Features
 * 共用的参考选项区域。
 *
 * 两种题型使用同一套玻璃卡片，
 * 避免用户在不同 Matching 题型之间产生视觉割裂。
 */
.heading-options,
.feature-options {
  margin-bottom: 24px;
  padding: 18px 20px;

  background: rgba(255, 255, 255, 0.46);

  border: 1px solid rgba(128, 172, 207, 0.26);
  border-radius: 18px;

  box-shadow:
    0 10px 30px rgba(96, 139, 174, 0.06);

  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.heading-options h4,
.feature-options h4 {
  margin: 4px 0 12px;

  color: #45637b;
  font-size: 0.9rem;
}

.heading-options-label {
  margin: 0;

  color: #8aa6bc;
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.heading-options h4,
.feature-options h4 {
  margin: 0 0 12px;
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

/*
 * TFNG / YNNG 共用的题目布局。
 *
 * 这里刻意和 Matching Headings / Matching Features
 * 保持一致，让整个 Reading Practice 的选择题视觉统一。
 */
.choice-question {
  display: grid;
  grid-template-columns:
    minmax(160px, 0.42fr) minmax(0, 1fr);

  align-items: center;
  gap: 18px;

  padding: 16px 18px;
  margin-bottom: 12px;

  background: rgba(255, 255, 255, 0.42);

  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 18px;

  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

@media (max-width: 700px) {
  .choice-question {
    grid-template-columns: 1fr;
  }
}
</style>