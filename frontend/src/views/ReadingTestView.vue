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

async function submitAnswers() {
  submitting.value = true
  submitErrorMessage.value = ''

  try {
    submitResult.value = await submitReadingTest(currentTestId.value, {
      answers: answers.value,
    })
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

function isQuestionCorrect(question: {
  id: number
  correctAnswer: string
}) {
  const userAnswer = answers.value[question.id]

  if (!userAnswer) {
    return false
  }

  return (
    userAnswer.trim().toLowerCase() ===
    question.correctAnswer.trim().toLowerCase()
  )
}

watch(
  answers,
  (newAnswers) => {
    localStorage.setItem(storageKey.value, JSON.stringify(newAnswers))
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
                <h4>List of Headings</h4>

                <ul>
                  <li v-for="option in group.options" :key="option.id">
                    <strong>{{ option.optionValue }}</strong>
                    {{ option.optionText }}
                  </li>
                </ul>
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
                  <p class="question-text">
                    {{ question.questionNumber }}.
                    {{ question.questionText }}
                  </p>

                  <select v-if="
                    group.questionType === 'MATCHING_HEADINGS' ||
                    group.questionType === 'MATCHING_FEATURES'
                  " v-model="answers[question.id]" :disabled="submitResult !== null" class="answer-select">
                    <option value="" disabled>
                      请选择答案
                    </option>

                    <option v-for="option in group.options" :key="option.id" :value="option.optionValue">
                      {{ option.optionValue }}
                      -
                      {{ option.optionText }}
                    </option>
                  </select>

                  <input v-else v-model="answers[question.id]" :disabled="submitResult !== null" class="answer-input"
                    type="text" placeholder="请输入答案" />

                  <div v-if="submitResult" class="question-review" :class="{
                    correct: isQuestionCorrect(question),
                    incorrect: !isQuestionCorrect(question),
                  }">
                    <strong>
                      {{
                        isQuestionCorrect(question)
                          ? '✓ Correct'
                          : '✗ Incorrect'
                      }}
                    </strong>

                    <span>
                      Your answer:
                      {{ answers[question.id] || 'Not answered' }}
                    </span>

                    <span>
                      Correct answer:
                      {{ question.correctAnswer }}
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
</style>