<script setup lang="ts">
import { computed } from 'vue'
import type { ReadingQuestion } from '@/api/reading'

/**
 * 一个 Completion 题组需要的数据。
 *
 * instruction:
 * Questions 1–7...
 *
 * questions:
 * 当前题组下面的所有 ReadingQuestion。
 *
 * answers:
 * 父页面统一维护的用户答案。
 */
const props = defineProps<{
    instruction: string | null
    questions: ReadingQuestion[]
    answers: Record<number, string>
    disabled?: boolean
}>()

/**
 * 子组件不能直接修改 props，
 * 所以通过 emit 把答案变化告诉父组件。
 */
const emit = defineEmits<{
    (
        event: 'update-answer',
        questionId: number,
        value: string,
    ): void
}>()

/**
 * ------------------------------------------------------------
 * HTML → 可读文本
 * ------------------------------------------------------------
 *
 * questionText 当前保存的是完整 questionsContent，
 * 里面包含：
 *
 * <div>
 * <strong>
 * <br>
 * &nbsp;
 * &middot;
 *
 * 这里先把这些 HTML 转换成正常文本。
 *
 * 注意：
 * 这一步不是最终 HTML Renderer。
 * 当前目标是先把 Completion 的题型结构正确显示出来。
 */
function htmlToReadableText(html: string | null) {
    if (!html) {
        return ''
    }

    const parser = new DOMParser()

    const document = parser.parseFromString(
        html,
        'text/html',
    )

    /**
     * <br> 转成换行。
     */
    document.body
        .querySelectorAll('br')
        .forEach((element) => {
            element.replaceWith('\n')
        })

    /**
     * div 后补换行，
     * 保留题组结构。
     */
    document.body
        .querySelectorAll('div')
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

/**
 * ------------------------------------------------------------
 * 当前 Completion 的题目模板
 * ------------------------------------------------------------
 *
 * Q1–7 的 questionText 实际上全部保存了同一份
 * questionsContent。
 *
 * 所以只取第一道题的 questionText 即可。
 */
const templateText = computed(() => {
    const firstQuestion = props.questions[0]

    if (!firstQuestion) {
        return ''
    }

    return htmlToReadableText(
        firstQuestion.questionText,
    )
})

/**
 * ------------------------------------------------------------
 * 把 "......" 拆成答案槽位
 * ------------------------------------------------------------
 *
 * 例如原始内容：
 *
 * their grandfather's wealth came from ......
 *
 * 转换后：
 *
 * [
 *   "their grandfather's wealth came from ",
 *   " and transportation businesses ..."
 * ]
 *
 * 第一个空位对应 questions[0]，
 * 第二个空位对应 questions[1]，
 * 依次类推。
 */
const templateParts = computed(() => {
    return templateText.value.split(/\.{5,}/)
})

/**
 * 输入答案。
 */
function updateAnswer(
    questionId: number,
    event: Event,
) {
    const input = event.target as HTMLInputElement

    emit(
        'update-answer',
        questionId,
        input.value,
    )
}
</script>

<template>
    <section class="completion-group">
        <!-- =====================================================
         题组说明
         ===================================================== -->

        <div v-if="instruction" class="completion-instruction">
            {{ htmlToReadableText(instruction) }}
        </div>

        <!-- =====================================================
         Completion 正文

         templateParts.length 应该比 questions 多 1。

         例如 7 个空：
         8 个文本片段。
         ===================================================== -->

        <div class="completion-body">
            <template v-for="(part, index) in templateParts" :key="index">
                <!-- 普通文本 -->
                <span class="completion-text">
                    {{ part }}
                </span>

                <!--
          每个文本片段后面插入一个答案框。

          index = 0
          → Question 1

          index = 1
          → Question 2
        -->
                <template v-if="index < questions.length">
                    <span v-if="questions[index]" :id="`reading-question-${questions[index]!.questionNumber}`"
                        class="completion-answer-slot">
                        <span class="completion-number">
                            {{ questions[index]!.questionNumber }}
                        </span>

                        <input class="completion-input" type="text" :value="answers[questions[index]!.id] ?? ''"
                            :disabled="disabled" @input="
                                updateAnswer(
                                    questions[index]!.id,
                                    $event,
                                )
                                " />
                    </span>
                </template>
            </template>
        </div>
    </section>
</template>

<style scoped>
.completion-group {
    margin-bottom: 28px;
}

/*
 * Questions 1–7 / Complete the notes...
 */
.completion-instruction {
    margin-bottom: 24px;

    white-space: pre-line;

    line-height: 1.65;

    font-size: 15px;
}

/*
 * Completion 题目主体。
 */
.completion-body {
    white-space: pre-wrap;

    line-height: 2.2;

    font-size: 15px;
}

/*
 * 普通模板文本。
 */
.completion-text {
    white-space: pre-wrap;
}

/*
 * 一个答案槽位：
 *
 * [1 输入框]
 */
.completion-answer-slot {
    display: inline-flex;

    align-items: center;

    gap: 5px;

    margin: 0 5px;

    vertical-align: middle;
}

/*
 * 题号。
 */
.completion-number {
    font-size: 13px;
    font-weight: 600;

    color: #607080;
}

/*
 * Completion 输入框。
 *
 * 不要占满整行，
 * 要嵌在句子中间。
 */
.completion-input {
    width: 135px;

    padding: 5px 8px;

    border: 1px solid #8f969c;
    border-radius: 3px;

    font: inherit;

    background: #fff;

    box-sizing: border-box;
}

.completion-input:focus {
    outline: 2px solid #c9d8e4;
    outline-offset: 1px;
}

.completion-input:disabled {
    background: #f4f4f4;

    cursor: not-allowed;
}
</style>