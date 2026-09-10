<script setup lang="ts">
import { computed } from 'vue'

interface ReadingQuestion {
  id: number
  questionNumber: number
  questionText: string
  optionsJson: string | null
}

const props = defineProps<{
  instruction: string | null
  questions: ReadingQuestion[]
  answers: Record<number, string>
  disabled: boolean
}>()

const emit = defineEmits<{
  (event: 'update-answer', questionId: number, value: string): void
}>()

/**
 * 把题目自己的 optionsJson 转成字符串数组。
 *
 * 后端当前返回：
 * ["选项A文字", "选项B文字", "选项C文字", "选项D文字"]
 */
function parseOptions(optionsJson: string | null): string[] {
  if (!optionsJson) {
    return []
  }

  try {
    const value = JSON.parse(optionsJson)

    if (!Array.isArray(value)) {
      return []
    }

    return value.map((item) => String(item))
  } catch {
    return []
  }
}

/**
 * 把 HTML instruction 转成正常可读文字。
 */
const readableInstruction = computed(() => {
  if (!props.instruction) {
    return ''
  }

  const parser = new DOMParser()
  const document = parser.parseFromString(
    props.instruction,
    'text/html',
  )

  document.body
    .querySelectorAll('br')
    .forEach((element) => {
      element.replaceWith('\n')
    })

  return (
    document.body.textContent
      ?.replace(/\u00a0/g, ' ')
      .replace(/\n{3,}/g, '\n\n')
      .trim() ?? ''
  )
})

function optionLetter(index: number) {
  return String.fromCharCode(65 + index)
}

function updateAnswer(questionId: number, value: string) {
  emit('update-answer', questionId, value)
}
</script>

<template>
  <section class="multiple-choice-group">
    <p
      v-if="readableInstruction"
      class="group-instruction"
    >
      {{ readableInstruction }}
    </p>

    <div class="question-list">
      <div
        v-for="question in questions"
        :id="`reading-question-${question.questionNumber}`"
        :key="question.id"
        class="question-item"
      >
        <p class="question-text">
          <strong>{{ question.questionNumber }}.</strong>
          {{ question.questionText }}
        </p>

        <div class="choice-list">
          <label
            v-for="(option, index) in parseOptions(question.optionsJson)"
            :key="index"
            class="choice-card"
            :class="{
              selected:
                answers[question.id] === optionLetter(index),
              disabled,
            }"
          >
            <input
              type="radio"
              :name="`question-${question.id}`"
              :value="optionLetter(index)"
              :checked="answers[question.id] === optionLetter(index)"
              :disabled="disabled"
              @change="
                updateAnswer(
                  question.id,
                  optionLetter(index),
                )
              "
            />

            <span class="choice-letter">
              {{ optionLetter(index) }}
            </span>

            <span class="choice-text">
              {{ option }}
            </span>
          </label>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.multiple-choice-group {
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

.question-list {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.question-item {
  scroll-margin-top: 24px;
}

.question-text {
  margin: 0 0 12px;
  line-height: 1.65;
}

.choice-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.choice-card {
  display: grid;
  grid-template-columns: 20px 30px 1fr;
  align-items: start;
  gap: 8px;
  padding: 11px 13px;
  border: 1px solid #dbe4ec;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  line-height: 1.55;
}

.choice-card:hover {
  background: #f5faff;
  border-color: #bfd7ea;
}

.choice-card.selected {
  background: #eef7ff;
  border-color: #9fc7e5;
}

.choice-card.disabled {
  cursor: not-allowed;
  opacity: 0.78;
}

.choice-card input {
  margin-top: 4px;
}

.choice-letter {
  font-weight: 700;
}

.choice-text {
  min-width: 0;
}
</style>
