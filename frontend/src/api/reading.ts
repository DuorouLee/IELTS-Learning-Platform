// 这个文件专门负责和后端 Reading API 通信。

// 后端 Spring Boot 地址。
// 目前后端运行在 8080 端口。
const API_BASE_URL = 'http://localhost:8080'

// 定义 Question 的数据结构。
// 这里的字段需要和 Spring Boot 返回的 ReadingQuestionResponse 对应。
export interface ReadingQuestion {
  id: number

  // IELTS 原始题号，例如 1、2、3...
  questionNumber: number

  // 题型，例如：
  // MATCHING_HEADINGS
  // MATCHING_FEATURES
  questionType: string

  // 题目正文
  questionText: string

  // 正确答案。
  // 目前前端虽然不会显示答案，
  // 但后端当前 DTO 仍然返回这个字段，所以先保留。
  correctAnswer: string

  // 题目解析。
  // 当前答题页面暂时不会使用，
  // 后续做判分 / Review 页面时会用到。
  explanation: string
}

// 定义 QuestionGroup 中的一个可选答案。
//
// 例如 MATCHING_HEADINGS：
// optionKey = "i"
// optionText = "The history of..."
//
// 例如 MATCHING_FEATURES：
// optionKey = "A"
// optionText = "Professor Smith"
export interface QuestionOption {
  id: number

  /**
   * 后端 QuestionOptionResponse 返回的字段名是 optionValue。
   *
   * 例如：
   * MATCHING_HEADINGS -> "i" / "ii" / "iii"
   * MATCHING_FEATURES -> "A" / "B" / "C"
   */
  optionValue: string

  /**
   * 选项显示文字。
   *
   * 例如：
   * "China"
   * "Japan"
   */
  optionText: string
}

// 定义一组 IELTS Questions。
//
// QuestionGroup 的作用是把：
// instruction
// options
// questions
// 组织在一起。
//
// 例如：
//
// QuestionGroup
// ├── MATCHING_HEADINGS
// ├── options
// │   ├── i ...
// │   ├── ii ...
// │   └── iii ...
// └── questions
//     ├── Question 1
//     ├── Question 2
//     └── Question 3
export interface QuestionGroup {
  id: number

  // 当前这一组题的 IELTS 题型
  questionType: string

  // IELTS 原始说明文字。
  // 例如：
  // "Choose the correct heading for each paragraph..."
  instruction: string

  // 当前题组所有可选择答案
  options: QuestionOption[]

  // 当前题组包含的题目
  questions: ReadingQuestion[]
}

// 定义 Passage 的数据结构。
export interface ReadingPassage {
  id: number
  passageNumber: number
  content: string

  // 旧结构继续保留。
  //
  // 后端目前为了兼容旧 API，
  // 仍然会返回 passages[].questions。
  questions: ReadingQuestion[]

  // 新结构。
  //
  // Vue 下一步会主要从这里读取：
  //
  // Passage
  // └── questionGroups
  //     ├── options
  //     └── questions
  questionGroups: QuestionGroup[]
}

// 定义 Reading Test 的基本信息。
export interface ReadingTest {
  id: number
  title: string
  source: string
  createdAt: string
}

// 定义完整 Reading Test API 返回的数据结构。
export interface FullReadingTestResponse {
  test: ReadingTest
  passages: ReadingPassage[]
}

// 调用后端接口，获取完整 Reading Test。
export async function getFullReadingTest(testId: number): Promise<FullReadingTestResponse> {
  // fetch 用来向 Spring Boot 发 HTTP 请求。
  const response = await fetch(`${API_BASE_URL}/api/reading/tests/${testId}/full`)

  // 如果后端没有返回 2xx 状态码，
  // 就主动抛出错误，方便页面捕获。
  if (!response.ok) {
    throw new Error(`获取 Reading Test 失败：${response.status}`)
  }

  // 把后端返回的 JSON 转成 JavaScript / TypeScript 对象。
  return response.json()
}
