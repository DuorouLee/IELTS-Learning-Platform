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
