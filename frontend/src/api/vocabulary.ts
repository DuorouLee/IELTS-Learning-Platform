/**
 * 这个文件专门负责和后端 Vocabulary API 通信。
 *
 * 后端 Spring Boot 当前运行在 8080 端口。
 */
const API_BASE_URL = 'http://localhost:8080'

/**
 * VocabularyWord
 *
 * 对应后端 VocabularyWord 返回的 JSON 数据。
 *
 * 例如：
 *
 * {
 *   "id": 1,
 *   "word": "abandon",
 *   "meaning": "放弃；抛弃",
 *   "exampleSentence": "They had to abandon the plan.",
 *   "createdAt": "2026-09-09T15:00:00"
 * }
 */
export interface VocabularyWord {
  id: number
  word: string
  meaning: string
  exampleSentence: string | null
  createdAt: string
}

/**
 * CreateVocabularyWordRequest
 *
 * 前端创建单词时发送给后端的数据。
 *
 * 和 VocabularyWord 不一样：
 * 创建时还没有 id 和 createdAt，
 * 所以这里只保留用户需要填写的字段。
 */
export interface CreateVocabularyWordRequest {
  word: string
  meaning: string
  exampleSentence: string
}

/**
 * 查询全部 Vocabulary Word。
 *
 * 调用后端：
 *
 * GET /api/vocabulary/words
 */
export async function getVocabularyWords(): Promise<VocabularyWord[]> {
  const response = await fetch(`${API_BASE_URL}/api/vocabulary/words`)

  /**
   * fetch 即使遇到 404 / 500，
   * 默认也不会自动抛异常。
   *
   * 所以需要我们自己检查 response.ok。
   */
  if (!response.ok) {
    throw new Error(`获取 Vocabulary Word 列表失败：${response.status}`)
  }

  return response.json()
}

/**
 * 创建一个新的 Vocabulary Word。
 *
 * 调用后端：
 *
 * POST /api/vocabulary/words
 */
export async function createVocabularyWord(
  request: CreateVocabularyWordRequest,
): Promise<VocabularyWord> {
  const response = await fetch(`${API_BASE_URL}/api/vocabulary/words`, {
    method: 'POST',

    /**
     * 告诉 Spring Boot：
     * 请求体发送的是 JSON。
     */
    headers: {
      'Content-Type': 'application/json',
    },

    /**
     * JavaScript 对象不能直接作为 HTTP Body，
     * 所以要先转换成 JSON 字符串。
     */
    body: JSON.stringify(request),
  })

  /**
   * 如果是重复单词，
   * 后端当前会返回 400。
   */
  if (!response.ok) {
    const errorBody = await response.json()

    throw new Error(errorBody.message ?? `创建 Vocabulary Word 失败：${response.status}`)
  }

  return response.json()
}
