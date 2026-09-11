<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import CompletionQuestionGroup from '@/components/reading/CompletionQuestionGroup.vue'
import MultipleChoiceQuestionGroup from '@/components/reading/MultipleChoiceQuestionGroup.vue'
import SummaryOptionsQuestionGroup from '@/components/reading/SummaryOptionsQuestionGroup.vue'

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

/**
 * 用户答案。
 * key = question.id
 * value = 用户输入答案
 */
const answers = ref<Record<number, string>>({})

const submitResult = ref<ReadingSubmitResponse | null>(null)
const submitting = ref(false)
const submitErrorMessage = ref('')

/**
 * 当前显示的 Passage。
 * 0 = Passage 1
 * 1 = Passage 2
 * 2 = Passage 3
 */
const currentPassageIndex = ref(0)

/**
 * 控制每个 Passage 是否显示译文。
 */
const translationVisible = ref<Record<number, boolean>>({})

/**
 * 当前在左侧原文中高亮的文本片段。
 *
 * 用户点击某道题的“定位原文”后，
 * 会从 answerHighlightJson 中提取真实答案句，
 * 然后把这些句子高亮显示。
 */
const highlightedArticleFragments = ref<string[]>([])

const currentTestId = computed(() => Number(route.params.testId))

const storageKey = computed(() => {
  return `reading-test-${currentTestId.value}-answers`
})

const currentPassage = computed(() => {
  if (!testData.value) {
    return null
  }

  return testData.value.passages[currentPassageIndex.value] ?? null
})

const answeredCount = computed(() => {
  return Object.values(answers.value).filter(
    (answer) => answer !== '',
  ).length
})

const totalQuestionCount = computed(() => {
  if (!testData.value) {
    return 0
  }

  return testData.value.passages.reduce(
    (total, passage) => {
      return total + passage.questions.length
    },
    0,
  )
})

/**
 * 把题库中的 HTML 转成可读纯文本。
 *
 * <br> 转换成换行；
 * HTML entity 由 DOMParser 自动解码。
 */
function htmlToReadableText(htmlText: string | null) {
  if (!htmlText) {
    return ''
  }

  const parser = new DOMParser()
  const document = parser.parseFromString(
    htmlText,
    'text/html',
  )

  document.body
    .querySelectorAll('br')
    .forEach((element) => {
      element.replaceWith('\n')
    })

  document.body
    .querySelectorAll('p, div')
    .forEach((element) => {
      element.append('\n\n')
    })

  return (
    document.body.textContent
      ?.replace(/\u00a0/g, ' ')
      .replace(/[ \t]+/g, ' ')
      .replace(/\n[ \t]+/g, '\n')
      .replace(/\n{3,}/g, '\n\n')
      .trim() ?? ''
  )
}


/**
 * 转义 HTML 特殊字符。
 *
 * 因为下面需要通过 v-html 显示“带 <mark> 的文章”，
 * 所以先把普通文章文字进行转义，
 * 防止文章本身被当成 HTML 执行。
 */
function escapeHtml(text: string) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

/**
 * 判断一个字符串是不是 UUID。
 *
 * answerHighlightJson 中有些字段的 text / id
 * 保存的是内部 UUID，而不是文章句子。
 * 这种值不应该拿来做原文定位。
 */
function isUuid(value: string) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
    value,
  )
}

/**
 * 从 answerHighlightJson 中递归收集可能的文章文本。
 *
 * 数据里不同题目的结构并不完全统一：
 * 有时真正文章句子在 id，
 * 有时在 text，
 * 还有时直接作为对象的 key。
 *
 * 所以这里不写死某一个字段，而是把可能的长文本都收集出来，
 * 最后再用“是否真的出现在当前文章中”进行过滤。
 */
function collectHighlightCandidates(
  value: unknown,
  result: string[],
) {
  if (typeof value === 'string') {
    const candidate = value
      .replace(/\u00a0/g, ' ')
      .trim()

    if (
      candidate.length >= 15 &&
      !isUuid(candidate)
    ) {
      result.push(candidate)
    }

    return
  }

  if (Array.isArray(value)) {
    value.forEach((item) => {
      collectHighlightCandidates(
        item,
        result,
      )
    })

    return
  }

  if (
    value !== null &&
    typeof value === 'object'
  ) {
    Object.entries(
      value as Record<string, unknown>,
    ).forEach(([key, nestedValue]) => {
      const cleanKey = key
        .replace(/\u00a0/g, ' ')
        .trim()

      if (
        cleanKey.length >= 15 &&
        !isUuid(cleanKey)
      ) {
        result.push(cleanKey)
      }

      collectHighlightCandidates(
        nestedValue,
        result,
      )
    })
  }
}

/**
 * 从一道题的 answerHighlightJson 中，
 * 找出“确实存在于当前文章正文”的文本片段。
 */
function getQuestionHighlightFragments(
  answerHighlightJson: string | null | undefined,
) {
  if (
    !answerHighlightJson ||
    !currentPassage.value
  ) {
    return []
  }

  try {
    const parsed = JSON.parse(
      answerHighlightJson,
    )

    const candidates: string[] = []

    collectHighlightCandidates(
      parsed,
      candidates,
    )

    const articleText = htmlToReadableText(
      currentPassage.value.content,
    )

    const uniqueCandidates = [
      ...new Set(candidates),
    ]

    return uniqueCandidates
      .filter((candidate) => {
        return articleText.includes(
          candidate,
        )
      })
      .sort(
        (a, b) => b.length - a.length,
      )
  } catch {
    return []
  }
}

/**
 * 当前左侧文章的 HTML。
 *
 * 平时只是安全转义后的普通文字；
 * 点击“定位原文”后，
 * 会把命中的答案句包成 <mark>。
 */
const highlightedArticleHtml = computed(() => {
  if (!currentPassage.value) {
    return ''
  }

  let articleText = htmlToReadableText(
    currentPassage.value.content,
  )

  if (
    highlightedArticleFragments.value.length ===
    0
  ) {
    return escapeHtml(articleText)
  }

  const placeholders = new Map<
    string,
    string
  >()

  highlightedArticleFragments.value.forEach(
    (fragment, index) => {
      if (!articleText.includes(fragment)) {
        return
      }

      const placeholder =
        `__READING_HIGHLIGHT_${index}__`

      placeholders.set(
        placeholder,
        fragment,
      )

      articleText = articleText.replace(
        fragment,
        placeholder,
      )
    },
  )

  let safeHtml = escapeHtml(
    articleText,
  )

  placeholders.forEach(
    (fragment, placeholder) => {
      safeHtml = safeHtml.replace(
        placeholder,
        `<mark class="answer-highlight">${escapeHtml(fragment)}</mark>`,
      )
    },
  )

  return safeHtml
})

/**
 * 点击 Review 中的“定位原文”：
 *
 * 1. 确保左侧显示英文原文；
 * 2. 读取该题 answerHighlightJson；
 * 3. 找到可以在文章中精确匹配的句子；
 * 4. 高亮；
 * 5. 自动滚动到高亮位置。
 */
async function locateQuestionInArticle(
  answerHighlightJson: string | null | undefined,
) {
  if (!currentPassage.value) {
    return
  }

  translationVisible.value[
    currentPassage.value.id
  ] = false

  highlightedArticleFragments.value =
    getQuestionHighlightFragments(
      answerHighlightJson,
    )

  await nextTick()

  const highlightElement =
    document.querySelector(
      '.answer-highlight',
    )

  highlightElement?.scrollIntoView({
    behavior: 'smooth',
    block: 'center',
  })
}

function toggleTranslation(passageId: number) {
  translationVisible.value[passageId] =
    !translationVisible.value[passageId]
}

function switchPassage(index: number) {
  if (!testData.value) {
    return
  }

  if (
    index < 0 ||
    index >= testData.value.passages.length
  ) {
    return
  }

  currentPassageIndex.value = index

  // 切换 Passage 后清除上一段文章的答案高亮。
  highlightedArticleFragments.value = []
}

/**
 * 获取一个 Passage 下的全部题号。
 */
function getPassageQuestionNumbers(
  passage: FullReadingTestResponse['passages'][number],
) {
  return passage.questions
    .map((question) => question.questionNumber)
    .sort((a, b) => a - b)
}

/**
 * 点击底部题号：
 * 1. 切换到对应 Passage
 * 2. 等待 Vue 完成渲染
 * 3. 滚动到对应题目
 */
async function goToQuestion(
  passageIndex: number,
  questionNumber: number,
) {
  currentPassageIndex.value = passageIndex
  highlightedArticleFragments.value = []

  await nextTick()

  const element = document.getElementById(
    `reading-question-${questionNumber}`,
  )

  if (!element) {
    return
  }

  element.scrollIntoView({
    behavior: 'smooth',
    block: 'center',
  })
}

async function loadReadingTest() {
  loading.value = true
  errorMessage.value = ''

  try {
    testData.value = await getFullReadingTest(
      currentTestId.value,
    )

    currentPassageIndex.value = 0
    translationVisible.value = {}
    highlightedArticleFragments.value = []

    const savedAnswers = localStorage.getItem(
      storageKey.value,
    )

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
      errorMessage.value =
        '加载 Reading Test 时发生未知错误'
    }
  } finally {
    loading.value = false
  }
}

async function submitAnswers() {
  submitting.value = true
  submitErrorMessage.value = ''

  try {
    submitResult.value = await submitReadingTest(
      currentTestId.value,
      {
        answers: answers.value,
      },
    )
  } catch (error) {
    if (error instanceof Error) {
      submitErrorMessage.value = error.message
    } else {
      submitErrorMessage.value =
        '提交 Reading Test 时发生未知错误'
    }
  } finally {
    submitting.value = false
  }
}

function getQuestionReview(questionId: number) {
  return submitResult.value?.questions.find(
    (review) => review.questionId === questionId,
  )
}

/**
 * 把：
 * C21-Test 1-Passage 1
 *
 * 显示成：
 * C21-Test 1
 */
function formatPassageTitle(title: string) {
  return title
    .replace(/-Passage\s+\d+$/i, '')
    .trim()
}

watch(
  answers,
  (newAnswers) => {
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
    <p v-if="loading">
      Loading...
    </p>

    <p v-else-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>

    <template v-else-if="testData">
      <!-- 顶部只保留答题进度和提交按钮 -->
      <header class="reading-header">
        <div class="reading-actions">
          <div class="reading-progress">
            已答：{{ answeredCount }} / {{ totalQuestionCount }}
          </div>

          <button class="submit-button" :disabled="submitting ||
            submitResult !== null
            " @click="submitAnswers">
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
            <strong>{{ submitResult.percentage.toFixed(2) }}%</strong>
            <span>Accuracy</span>
          </div>
        </div>
      </section>

      <p v-if="submitErrorMessage" class="submit-error">
        {{ submitErrorMessage }}
      </p>

      <section v-if="currentPassage" class="passage-section">
        <div class="passage-header">
          <h2 class="passage-heading">
            Reading Passage {{ currentPassage.passageNumber }}
          </h2>

          <span class="question-range">
            Questions
            {{ getPassageQuestionNumbers(currentPassage)[0] }}
            –
            {{ getPassageQuestionNumbers(currentPassage).at(-1) }}
          </span>
        </div>

        <p v-if="currentPassage.instruction" class="passage-instruction">
          {{ currentPassage.instruction }}
        </p>

        <div class="reading-layout">
          <!-- 左侧文章 -->
          <section class="article-panel">
            <div class="article-toolbar">
              <h3 class="panel-title">
                Article
              </h3>

              <button v-if="currentPassage.translation" type="button" class="translation-button" @click="
                toggleTranslation(
                  currentPassage.id,
                )
                ">
                {{
                  translationVisible[currentPassage.id]
                    ? '查看原文'
                    : '查看译文'
                }}
              </button>
            </div>

            <div class="article-content">
              <h3 class="article-title">
                {{ formatPassageTitle(currentPassage.title) }}
              </h3>

              <p
                v-if="
                  !translationVisible[
                    currentPassage.id
                  ]
                "
                class="article-paragraph article-readable-text"
                v-html="highlightedArticleHtml"
              ></p>

              <p v-else class="article-paragraph translation-paragraph">
                {{
                  htmlToReadableText(
                    currentPassage.translation,
                  )
                }}
              </p>
            </div>
          </section>

          <!-- 右侧题目 -->
          <section class="questions-panel">
            <h3 class="panel-title">
              Questions
            </h3>

            <div v-for="group in currentPassage.questionGroups" :key="group.id" class="question-group">
              <!-- Completion 专用渲染 -->
              <CompletionQuestionGroup v-if="group.questionType === 'COMPLETION'" :instruction="group.instruction"
                :questions="group.questions" :answers="answers" :disabled="submitResult !== null" @update-answer="
                  (questionId, value) => {
                    answers[questionId] = value
                  }
                " />

              <!-- Multiple Choice：每一道题使用自己的 A/B/C/D 选项 -->
              <MultipleChoiceQuestionGroup
                v-else-if="group.questionType === 'MULTIPLE_CHOICE'"
                :instruction="group.instruction"
                :questions="group.questions"
                :answers="answers"
                :disabled="submitResult !== null"
                @update-answer="
                  (questionId, value) => {
                    answers[questionId] = value
                  }
                "
              />

              <!-- 带共享选项的 Summary Completion -->
              <SummaryOptionsQuestionGroup
                v-else-if="
                  group.questionType ===
                  'SUMMARY_COMPLETION_WITH_OPTIONS'
                "
                :instruction="group.instruction"
                :questions="group.questions"
                :options="group.options"
                :answers="answers"
                :disabled="submitResult !== null"
                @update-answer="
                  (questionId, value) => {
                    answers[questionId] = value
                  }
                "
              />

              <!-- 其他已经支持的题型 -->
              <template v-else>
                <p
                  v-if="group.instruction"
                  class="group-instruction"
                >
                  {{ htmlToReadableText(group.instruction) }}
                </p>

                <div
                  v-if="
                    group.questionType === 'MATCHING_HEADINGS' ||
                    group.questionType === 'MATCHING_FEATURES' ||
                    group.questionType === 'MATCHING_INFORMATION' ||
                    group.questionType === 'MATCHING_SENTENCE_ENDINGS'
                  "
                  class="feature-options"
                >
                  <h4>Options</h4>

                  <ul>
                    <li
                      v-for="option in group.options"
                      :key="option.id"
                    >
                      <strong>{{ option.optionValue }}</strong>
                      {{ option.optionText }}
                    </li>
                  </ul>
                </div>

                <div class="question-list">
                  <div
                    v-for="question in group.questions"
                    :id="`reading-question-${question.questionNumber}`"
                    :key="question.id"
                    class="question-item"
                  >
                    <p class="question-text">
                      {{ question.questionNumber }}.
                      {{ question.questionText }}
                    </p>

                    <!-- Matching 共用题组级选项 -->
                    <select
                      v-if="
                        group.questionType === 'MATCHING_HEADINGS' ||
                        group.questionType === 'MATCHING_FEATURES' ||
                        group.questionType === 'MATCHING_INFORMATION' ||
                    group.questionType === 'MATCHING_SENTENCE_ENDINGS'
                      "
                      v-model="answers[question.id]"
                      :disabled="submitResult !== null"
                      class="answer-select"
                    >
                      <option value="" disabled>
                        请选择答案
                      </option>

                      <option
                        v-for="option in group.options"
                        :key="option.id"
                        :value="option.optionValue"
                      >
                        {{
                          option.optionText
                            ? `${option.optionValue} - ${option.optionText}`
                            : option.optionValue
                        }}
                      </option>
                    </select>

                    <!-- 判断题使用真正的选项，不再手输答案 -->
                    <select
                      v-else-if="
                        group.questionType === 'TRUE_FALSE_NOT_GIVEN' ||
                        group.questionType === 'YES_NO_NOT_GIVEN'
                      "
                      v-model="answers[question.id]"
                      :disabled="submitResult !== null"
                      class="answer-select"
                    >
                      <option value="" disabled>
                        请选择答案
                      </option>

                      <template
                        v-if="
                          group.questionType === 'TRUE_FALSE_NOT_GIVEN'
                        "
                      >
                        <option value="TRUE">TRUE</option>
                        <option value="FALSE">FALSE</option>
                        <option value="NOT GIVEN">NOT GIVEN</option>
                      </template>

                      <template v-else>
                        <option value="YES">YES</option>
                        <option value="NO">NO</option>
                        <option value="NOT GIVEN">NOT GIVEN</option>
                      </template>
                    </select>

                    <!-- 兜底：以后遇到尚未做专用 UI 的题型仍可以输入 -->
                    <input
                      v-else
                      v-model="answers[question.id]"
                      :disabled="submitResult !== null"
                      class="answer-input"
                      type="text"
                      placeholder="请输入答案"
                    />

                  </div>
                </div>
              </template>

              <!--
                提交答案后的统一 Review 区域。

                为什么放在这里？
                因为现在不同题型已经拆成多个 Vue 组件。
                如果把 Review 写在某一个题型分支里，
                其他题型就看不到解析。

                所以这里统一遍历当前 QuestionGroup 的 questions，
                展示：
                - Correct / Incorrect
                - Your answer
                - Correct answer
                - Explanation
              -->
              <div
                v-if="submitResult"
                class="group-review-list"
              >
                <div
                  v-for="question in group.questions"
                  :key="`review-${question.id}`"
                  class="question-review"
                  :class="{
                    correct:
                      getQuestionReview(question.id)?.correct,
                    incorrect:
                      !getQuestionReview(question.id)?.correct,
                  }"
                >
                  <div class="review-heading">
                    <strong>
                      Question {{ question.questionNumber }}
                    </strong>

                    <strong>
                      {{
                        getQuestionReview(question.id)?.correct
                          ? '✓ Correct'
                          : '✗ Incorrect'
                      }}
                    </strong>
                  </div>

                  <div class="review-answer-row">
                    <span>
                      <strong>Your answer:</strong>
                      {{
                        getQuestionReview(question.id)?.userAnswer ||
                        'Not answered'
                      }}
                    </span>

                    <span>
                      <strong>Correct answer:</strong>
                      {{
                        getQuestionReview(question.id)?.correctAnswer
                      }}
                    </span>
                  </div>

                  <div
                    v-if="question.explanation"
                    class="review-explanation"
                  >
                    <div class="explanation-heading">
                      <strong>Explanation</strong>

                      <button
                        v-if="question.answerHighlightJson"
                        type="button"
                        class="locate-button"
                        @click="
                          locateQuestionInArticle(
                            question.answerHighlightJson,
                          )
                        "
                      >
                        定位原文
                      </button>
                    </div>

                    <p>
                      {{
                        htmlToReadableText(
                          question.explanation,
                        )
                      }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- 底部 Part + 题号导航 -->
        <nav class="part-navigator">
          <div v-for="(passage, passageIndex) in testData.passages" :key="passage.id" class="part-navigation-group"
            :class="{
              active:
                passageIndex ===
                currentPassageIndex,
            }">
            <button type="button" class="part-button" @click="
              switchPassage(
                passageIndex,
              )
              ">
              Part {{ passage.passageNumber }}
            </button>

            <div class="part-question-numbers">
              <button v-for="
questionNumber in
                    getPassageQuestionNumbers(
                      passage,
                    )
                " :key="questionNumber" type="button" class="question-number-button" :class="{
                  answered:
                    Object.entries(
                      answers,
                    ).some(
                      ([questionId, answer]) => {
                        const question =
                          passage.questions.find(
                            (item) =>
                              item.id ===
                              Number(
                                questionId,
                              ),
                          )

                        return (
                          question?.questionNumber ===
                          questionNumber &&
                          answer !== ''
                        )
                      },
                    ),
                }" @click="
                  goToQuestion(
                    passageIndex,
                    questionNumber,
                  )
                  ">
                {{ questionNumber }}
              </button>
            </div>
          </div>
        </nav>
      </section>
    </template>
  </main>
</template>

<style scoped>
.reading-page {
  width: 100%;
  padding: 24px 32px 110px;
  box-sizing: border-box;
}

.reading-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
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
  margin-top: 16px;
}

.passage-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: 12px;
}

.passage-heading {
  margin: 0;
  font-size: 22px;
}

.question-range {
  font-size: 14px;
  color: #6d7780;
}

.passage-instruction {
  margin: 0 0 16px;
  padding: 12px 16px;
  border: 1px solid #e1e5e8;
  border-radius: 6px;
  background: #f8f8f5;
  line-height: 1.5;
}

.reading-layout {
  display: grid;
  grid-template-columns:
    minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  height: calc(100vh - 280px);
  min-height: 560px;
}

.article-panel,
.questions-panel {
  height: 100%;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 22px;
  box-sizing: border-box;
  background: #fff;
}

.article-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #e5e5e5;
}

.panel-title {
  margin: 0 0 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #e5e5e5;
}

.article-toolbar .panel-title {
  margin: 0;
  padding: 0;
  border: none;
}

.translation-button {
  flex-shrink: 0;
  padding: 7px 12px;
  border: 1px solid #d7e0e8;
  border-radius: 7px;
  background: #fff;
  color: #526b80;
  font-size: 13px;
  cursor: pointer;
}

.translation-button:hover {
  background: #f5f8fa;
}

.article-title {
  margin-top: 0;
  margin-bottom: 24px;
}

.article-paragraph {
  margin-bottom: 20px;
  line-height: 1.8;
}

.article-readable-text,
.translation-paragraph {
  white-space: pre-wrap;
  line-height: 1.85;
}

.question-group {
  margin-bottom: 28px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e5e5e5;
}

.group-instruction {
  margin-bottom: 16px;
  font-weight: 500;
  line-height: 1.6;
  white-space: pre-line;
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
  line-height: 1.6;
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

.group-review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.question-review {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.question-review.correct {
  background: #f0f8f2;
  border: 1px solid #b7d8bf;
}

.question-review.incorrect {
  background: #fff4f4;
  border: 1px solid #e2bcbc;
}

.review-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.review-answer-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 24px;
}

.review-explanation {
  padding-top: 10px;
  border-top: 1px solid rgba(70, 90, 105, 0.14);
}

.review-explanation p {
  margin: 6px 0 0;
  white-space: pre-wrap;
  line-height: 1.75;
}

.explanation-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.locate-button {
  padding: 5px 10px;
  border: 1px solid #b9d3e6;
  border-radius: 6px;
  background: #eef7ff;
  color: #35586f;
  font-size: 13px;
  cursor: pointer;
}

.locate-button:hover {
  background: #e1f1ff;
}

/**
 * answer-highlight 是通过 v-html 动态插入的，
 * scoped CSS 默认不会直接作用到它，
 * 所以这里使用 :deep()。
 */
.article-readable-text :deep(.answer-highlight) {
  padding: 2px 3px;
  border-radius: 4px;
  background: #fff1a8;
  box-shadow: 0 0 0 2px rgba(245, 205, 80, 0.18);
}

.part-navigator {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  min-height: 76px;
  padding: 10px 24px;
  border-top: 1px solid #dfe3e6;
  background: #fff;
  box-sizing: border-box;
  box-shadow:
    0 -4px 12px rgba(0, 0, 0, 0.04);
}

.part-navigation-group {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding: 4px 12px;
  border-bottom: 3px solid transparent;
}

.part-navigation-group.active {
  border-bottom-color: #374957;
}

.part-button {
  flex-shrink: 0;
  padding: 6px 8px;
  border: none;
  background: transparent;
  font-weight: 600;
  cursor: pointer;
}

.part-question-numbers {
  display: flex;
  align-items: center;
  gap: 2px;
  min-width: 0;
  overflow-x: auto;
}

.question-number-button {
  min-width: 28px;
  height: 28px;
  padding: 0 5px;
  border: none;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
}

.question-number-button.answered {
  background: #eef2f5;
  font-weight: 600;
}

.question-number-button:hover {
  background: #e9edf0;
}

@media (max-width: 1100px) {
  .part-navigator {
    grid-template-columns: 1fr;
  }

  .part-navigation-group:not(.active) {
    display: none;
  }
}

@media (max-width: 900px) {
  .reading-page {
    padding: 20px 18px 100px;
  }

  .reading-header {
    flex-direction: column;
  }

  .reading-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .article-panel,
  .questions-panel {
    height: auto;
    max-height: 620px;
  }

  .result-summary {
    grid-template-columns:
      repeat(2, 1fr);
  }

  .passage-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
