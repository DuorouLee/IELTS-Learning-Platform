// 这个文件专门负责和后端 Reading API 通信。

// 后端 Spring Boot 地址。
// 目前后端运行在 8080 端口。
const API_BASE_URL = 'http://localhost:8080'

// 定义 Question 的数据结构。
// 这里的字段要和后端返回的 JSON 对应。
export interface ReadingQuestion {
  id: number
  questionNumber: number
  questionType: string
  questionText: string
  correctAnswer: string
  explanation: string
}

// 定义 Passage 的数据结构。
export interface ReadingPassage {
  id: number
  passageNumber: number
  content: string
  questions: ReadingQuestion[]
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
