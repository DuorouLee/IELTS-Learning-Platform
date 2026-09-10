<script setup lang="ts">
import { computed } from 'vue'

interface ReadingQuestion {
  id: number
  questionNumber: number
  questionText: string
}

interface GroupOption {
  id: number
  optionValue: string
  optionText: string
  displayOrder?: number
}

interface SummarySegment {
  type: 'text' | 'answer'
  text?: string
  questionNumber?: number
  questionId?: number
}

const props = defineProps<{
  instruction: string | null
  questions: ReadingQuestion[]
  options: GroupOption[]
  answers: Record<number, string>
  disabled: boolean
}>()

const emit = defineEmits<{
  (event: 'update-answer', questionId: number, value: string): void
}>()

function htmlToReadableText(value: string | null) {
  if (!value) {
    return ''
  }

  const parser = new DOMParser()
  const document = parser.parseFromString(
    value,
    'text/html',
  )

  document.body
    .querySelectorAll('br')
    .forEach((element) => {
      element.replaceWith('\n')
    })

  document.body
    .querySelectorAll('div, p')
    .forEach((element) => {
      element.append('\n')
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

const readableInstruction = computed(() => {
  return htmlToReadableText(props.instruction)
})

/**
 * 这一类题的完整 Summary 模板被保存在每一道题的 questionText 中，
 * 内容其实完全一样。
 *
 * 因此这里只取第一题的模板一次，
 * 避免 Q31、Q32、Q33... 重复显示整块 Summary。
 */
const summaryTemplate = computed(() => {
  return props.questions[0]?.questionText ?? ''
})

/**
 * 把：
 *
 * depended on 31...... .
 * ... 32...... continued.
 *
 * 拆成：
 *
 * 文本
 * → Q31 下拉框
 * → 文本
 * → Q32 下拉框
 *
 * 这样输入控件会真正出现在 Summary 的空格位置。
 */
const segments = computed<SummarySegment[]>(() => {
  const text = htmlToReadableText(
    summaryTemplate.value,
  )

  if (!text) {
    return []
  }

  const questionMap = new Map(
    props.questions.map((question) => [
      question.questionNumber,
      question.id,
    ]),
  )

  const result: SummarySegment[] = []
  const pattern = /(\d+)\s*\.{5,}/g

  let lastIndex = 0
  let match: RegExpExecArray | null

  while ((match = pattern.exec(text)) !== null) {
    if (match.index > lastIndex) {
      result.push({
        type: 'text',
        text: text.slice(lastIndex, match.index),
      })
    }

    const questionNumber = Number(match[1])
    const questionId = questionMap.get(
      questionNumber,
    )

    if (questionId !== undefined) {
      result.push({
        type: 'answer',
        questionNumber,
        questionId,
      })
    } else {
      result.push({
        type: 'text',
        text: match[0],
      })
    }

    lastIndex = pattern.lastIndex
  }

  if (lastIndex < text.length) {
    result.push({
      type: 'text',
      text: text.slice(lastIndex),
    })
  }

  return result
})

function updateAnswer(questionId: number, value: string) {
  emit('update-answer', questionId, value)
}
</script>

<template>
  <section class="summary-group">
    <p v-if="readableInstruction" class="group-instruction">
      {{ readableInstruction }}
    </p>

    <div class="option-box">
      <div v-for="option in options" :key="option.id" class="option-row">
        <strong>{{ option.optionValue }}</strong>
        <span>{{ option.optionText }}</span>
      </div>
    </div>

    <div class="summary-card">
      <template v-for="(segment, index) in segments" :key="index">
        <span v-if="segment.type === 'text'" class="summary-text">
          {{ segment.text }}
        </span>

        <span v-else-if="
          segment.questionId !== undefined &&
          segment.questionNumber !== undefined
        " :id="`reading-question-${segment.questionNumber}`" class="inline-answer">
          <strong class="answer-number">
            {{ segment.questionNumber }}
          </strong>

          <select class="summary-select" :value="answers[segment.questionId] ?? ''" :disabled="disabled" @change="
            updateAnswer(
              segment.questionId,
              ($event.target as HTMLSelectElement).value,
            )
            ">
            <option value="" disabled>
              请选择
            </option>

            <option v-for="option in options" :key="option.id" :value="option.optionValue">
              {{ option.optionValue }}
            </option>
          </select>
        </span>
      </template>
    </div>
  </section>
</template>

<style scoped>
.summary-group {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.group-instruction {
  margin: 0;
  line-height: 1.65;
  white-space: pre-line;
  font-weight: 500;
}

.option-box {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
  padding: 16px 18px;
  border: 1px solid #dbe4ec;
  border-radius: 8px;
  background: #f8fbfe;
}

.option-row {
  display: grid;
  grid-template-columns: 24px 1fr;
  gap: 8px;
  line-height: 1.5;
}

.summary-card {
  padding: 18px;
  border: 1px solid #dbe4ec;
  border-radius: 8px;
  background: #fff;
  line-height: 2.25;
  white-space: pre-wrap;
}

.summary-text {
  white-space: pre-wrap;
}

.inline-answer {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin: 0 4px;
  scroll-margin-top: 24px;
}

.answer-number {
  font-size: 13px;
}

.summary-select {
  width: 68px;
  padding: 5px 7px;
  border: 1px solid #aebdca;
  border-radius: 6px;
  background: #fff;
}

.summary-select:focus {
  outline: 2px solid #dbeeff;
  border-color: #8eb9d9;
}

@media (max-width: 700px) {
  .option-box {
    grid-template-columns: 1fr;
  }
}
</style>
