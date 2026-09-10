const fs = require("fs");
const path = require("path");

/**
 * ------------------------------------------------------------
 * convert-reading.js
 *
 * 作用：
 * 把“原始真实题库 JSON”
 *
 * 转换成：
 *
 * Spring Boot 的 ReadingImportService
 * 可以直接读取的标准 JSON。
 *
 * 当前第一版只支持：
 *
 * 1. MATCHING_HEADINGS
 * 2. MATCHING_FEATURES
 *
 * 先让 p1-high-01.json 成功跑通。
 * ------------------------------------------------------------
 */

/**
 * 读取命令行参数。
 *
 * 例如运行：
 *
 * node convert-reading.js input.json output.json
 *
 * 那么：
 *
 * process.argv[2] = input.json
 * process.argv[3] = output.json
 */
const inputFile = process.argv[2];
const outputFile = process.argv[3];

/**
 * 如果用户没有提供输入和输出文件，
 * 直接提示正确用法并结束程序。
 */
if (!inputFile || !outputFile) {
  console.error("Usage: node convert-reading.js <input.json> <output.json>");

  process.exit(1);
}

/**
 * 读取原始 JSON 文件。
 */
const source = JSON.parse(fs.readFileSync(inputFile, "utf-8"));

/**
 * ------------------------------------------------------------
 * 提取纯文本
 * ------------------------------------------------------------
 *
 * 原始题库中大量内容保存在 HTML 里面。
 *
 * 例如：
 *
 * <p>Choose the correct heading...</p>
 *
 * 后端暂时不需要完整 HTML，
 * 所以第一版 Converter 把 HTML 标签去掉，
 * 转成普通文本。
 *
 * 注意：
 * 这是第一版简单实现。
 * 后面如果遇到复杂 HTML，
 * 我们再升级成正式 HTML Parser。
 */
function stripHtml(html) {
  if (!html) {
    return "";
  }

  return html
    .replace(/<[^>]*>/g, " ")
    .replace(/&ndash;/g, "–")
    .replace(/&mdash;/g, "—")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/\s+/g, " ")
    .trim();
}

/**
 * ------------------------------------------------------------
 * 提取 Passage 正式标题
 * ------------------------------------------------------------
 *
 * 原始 meta.title 可能包含中文翻译：
 *
 * A Brief History of Tea 茶叶简史
 *
 * 但是 Passage HTML 中真正显示的 IELTS 标题是：
 *
 * <h3>A Brief History of Tea</h3>
 *
 * 所以这里优先从 Passage HTML 的 <h3> 中提取标题。
 *
 * 这样不会针对某一篇文章写死，
 * 后续其他 Reading Passage 也可以复用。
 */
function extractPassageTitle(blocks, fallbackTitle) {
  for (const block of blocks) {
    if (!block.html) {
      continue;
    }

    /**
     * 找第一个 <h3>...</h3>
     */
    const match = block.html.match(/<h3[^>]*>(.*?)<\/h3>/i);

    if (match) {
      return stripHtml(match[1]);
    }
  }

  /**
   * 如果 Passage HTML 中没有 <h3>，
   * 就退回 meta.title。
   *
   * 不能返回 passageTitle，
   * 因为 passageTitle 本身正是这个函数的返回结果，
   * 会形成错误的自引用。
   */
  return fallbackTitle;
}

/**
 * ------------------------------------------------------------
 * 提取 Passage 公共 instruction
 * ------------------------------------------------------------
 *
 * 当前真实 IELTS 数据中，Passage 开头会包含类似：
 *
 * You should spend about 20 minutes on Questions 1-13,
 * which are based on Reading Passage 1 on the following pages.
 *
 * 这句话属于整个 Passage，
 * Article 和 Questions 都共享，
 * 所以不应该继续混在 content 中。
 */
function extractPassageInstruction(text) {
  /**
   * 匹配：
   *
   * You should spend ...
   * ...
   * on the following pages.
   *
   * [\s\S]
   * 表示可以跨越普通字符和换行。
   */
  const match = text.match(/You should spend[\s\S]*?on the following pages\./i);

  if (!match) {
    /**
     * 如果以后遇到不同格式的题库，
     * 不在这里编造 instruction。
     */
    return null;
  }

  return match[0].replace(/\s+/g, " ").trim();
}

/**
 * ------------------------------------------------------------
 * 把 questionId 转成题号
 * ------------------------------------------------------------
 *
 * 原始数据：
 *
 * q1
 * q2
 * q13
 *
 * 转换后：
 *
 * 1
 * 2
 * 13
 */
function questionIdToNumber(questionId) {
  return Number(questionId.replace("q", ""));
}

/**
 * ------------------------------------------------------------
 * 判断 QuestionGroup 的具体题型
 * ------------------------------------------------------------
 *
 * 原始文件中的 kind 只有：
 *
 * "matching"
 *
 * 但这还不够精确。
 *
 * 因为 matching 里面实际上至少有：
 *
 * MATCHING_HEADINGS
 * MATCHING_FEATURES
 *
 * 所以我们根据 bodyHtml 的原题说明进一步判断。
 */
function detectQuestionType(group) {
  const text = stripHtml(group.bodyHtml).toLowerCase();

  /**
   * 如果题目中出现：
   *
   * "correct heading"
   * 或
   * "list of headings"
   *
   * 就识别为 MATCHING_HEADINGS。
   */
  if (text.includes("correct heading") || text.includes("list of headings")) {
    return "MATCHING_HEADINGS";
  }

  /**
   * 如果出现：
   *
   * "match each statement"
   *
   * 当前真实题库里对应的是
   * 国家 / 人物 / 特征匹配。
   */
  if (text.includes("match each statement")) {
    return "MATCHING_FEATURES";
  }

  /**
   * 如果暂时无法识别，
   * 不要静默猜测。
   *
   * 直接报错，
   * 方便我们后面继续扩展题型。
   */
  throw new Error(`Unsupported matching question group: ${group.groupId}`);
}

/**
 * ------------------------------------------------------------
 * 提取 instruction
 * ------------------------------------------------------------
 *
 * 第一版先针对当前真实文件中的两种题型。
 */
function extractInstruction(group, questionType) {
  const text = stripHtml(group.bodyHtml);

  if (questionType === "MATCHING_HEADINGS") {
    return "Choose the correct heading for each paragraph from the list of headings below.";
  }

  if (questionType === "MATCHING_FEATURES") {
    return "Match each statement with the correct country, A–G.";
  }

  return text;
}

/**
 * ------------------------------------------------------------
 * 提取 MATCHING_HEADINGS 的选项
 * ------------------------------------------------------------
 *
 * 原 HTML 类似：
 *
 * data-heading="i">i. Not enough tea to meet demand
 *
 * 我们需要转换成：
 *
 * {
 *   optionValue: "i",
 *   optionText: "Not enough tea to meet demand",
 *   displayOrder: 1
 * }
 */
function extractHeadingOptions(bodyHtml) {
  const options = [];

  /**
   * 这个正则会寻找：
   *
   * data-heading="..."
   *
   * 然后读取 div 中的文字。
   */
  const regex = /data-heading="([^"]+)"[^>]*>(.*?)<\/div>/g;

  let match;
  let order = 1;

  while ((match = regex.exec(bodyHtml)) !== null) {
    const optionValue = match[1];

    let optionText = stripHtml(match[2]);

    /**
     * 原文字可能是：
     *
     * i. Not enough tea...
     *
     * 我们不希望 optionText 再包含 i.
     *
     * 因为 i 已经保存到 optionValue。
     */
    optionText = optionText.replace(
      new RegExp(`^${optionValue}\\.?\\s*`, "i"),
      "",
    );

    options.push({
      optionValue,
      optionText,
      displayOrder: order,
    });

    order++;
  }

  return options;
}

/**
 * ------------------------------------------------------------
 * 提取 MATCHING_FEATURES 的选项
 * ------------------------------------------------------------
 *
 * 原 HTML：
 *
 * data-option="A">A China
 *
 * 转换成：
 *
 * {
 *   optionValue: "A",
 *   optionText: "China",
 *   displayOrder: 1
 * }
 */
function extractFeatureOptions(bodyHtml) {
  const options = [];

  const regex = /data-option="([^"]+)"[^>]*>(.*?)<\/div>/g;

  let match;
  let order = 1;

  while ((match = regex.exec(bodyHtml)) !== null) {
    const optionValue = match[1];

    let optionText = stripHtml(match[2]);

    /**
     * 删除开头重复的 A / B / C。
     *
     * 例如：
     *
     * A China
     *
     * 变成：
     *
     * China
     */
    optionText = optionText.replace(new RegExp(`^${optionValue}\\s*`, "i"), "");

    options.push({
      optionValue,
      optionText,
      displayOrder: order,
    });

    order++;
  }

  return options;
}

/**
 * ------------------------------------------------------------
 * 提取 QuestionText
 * ------------------------------------------------------------
 */
function extractQuestionText(group, questionId, questionType) {
  const questionNumber = questionIdToNumber(questionId);

  /**
   * MATCHING_HEADINGS
   *
   * 你的源文件中，
   * q1-q8 对应 Passage 的 Paragraph A-H。
   */
  if (questionType === "MATCHING_HEADINGS") {
    const index = group.questionIds.indexOf(questionId);

    const paragraphLetter = String.fromCharCode("A".charCodeAt(0) + index);

    return `Paragraph ${paragraphLetter}`;
  }

  /**
   * MATCHING_FEATURES
   *
   * 题目文字直接存在 bodyHtml 中。
   *
   * 例如：
   *
   * <strong>9</strong>
   * Claims that tea might be harmful...
   */
  if (questionType === "MATCHING_FEATURES") {
    const escapedNumber = questionNumber;

    const regex = new RegExp(`<strong>${escapedNumber}<\\/strong>\\s*([^<]+)`);

    const match = group.bodyHtml.match(regex);

    if (match) {
      return stripHtml(match[1]);
    }
  }

  return `Question ${questionNumber}`;
}

/**
 * ------------------------------------------------------------
 * 转换一个 QuestionGroup
 * ------------------------------------------------------------
 */
function convertQuestionGroup(group) {
  const questionType = detectQuestionType(group);

  /**
   * 根据题型提取不同类型的 Options。
   */
  let options = [];

  if (questionType === "MATCHING_HEADINGS") {
    options = extractHeadingOptions(group.bodyHtml);
  } else if (questionType === "MATCHING_FEATURES") {
    options = extractFeatureOptions(group.bodyHtml);
  }

  /**
   * 把 q1 / q2 / q3...
   *
   * 转换成后端 ReadingQuestionImportDto
   * 所需要的数据。
   */
  const questions = group.questionIds.map((questionId) => {
    const questionNumber = questionIdToNumber(questionId);

    return {
      questionNumber,

      questionType,

      questionText: extractQuestionText(group, questionId, questionType),

      /**
       * 正确答案直接来自源文件：
       *
       * answerKey
       */
      correctAnswer: source.answerKey[questionId],

      /**
       * 原始文件目前没有单独的 explanation，
       * 所以先使用 null。
       *
       * 不要编造解析。
       */
      explanation: null,
    };
  });

  return {
    questionType,

    instruction: extractInstruction(group, questionType),

    allowOptionReuse: group.allowOptionReuse === true,

    options,

    questions,
  };
}

/**
 * 从 Passage HTML 中提取正式英文标题。
 */
const passageTitle = extractPassageTitle(
  source.passage.blocks,
  source.meta.title,
);

/**
 * 先得到原始 Passage 全部文本。
 */
const rawPassageContent = source.passage.blocks
  .map((block) => stripHtml(block.html))
  .join("\n\n");

/**
 * 从完整文本中提取 Passage 公共说明。
 */
const passageInstruction = extractPassageInstruction(rawPassageContent);

/**
 * 先从正文中删除已经独立提取出来的 instruction。
 */
let passageContent = passageInstruction
  ? rawPassageContent.replace(passageInstruction, "").trim()
  : rawPassageContent;

/**
 * 删除正文开头重复的：
 *
 * READING PASSAGE 1
 *
 * 因为 passageNumber 已经单独保存，
 * Vue 会显示：
 *
 * Reading Passage 1
 */
passageContent = passageContent.replace(/^READING PASSAGE \d+\s*/i, "").trim();

/**
 * 删除正文开头重复的文章标题。
 *
 * 例如：
 *
 * A Brief History of Tea
 *
 * 因为 title 已经单独保存到：
 *
 * passage.title
 *
 * Vue 会单独显示文章标题。
 */
passageContent = passageContent
  .replace(new RegExp(`^${passageTitle}\\s*`, "i"), "")
  .trim();

/**
 * ------------------------------------------------------------
 * 构建 Spring Boot ReadingImportDto 所需要的数据。
 * ------------------------------------------------------------
 */
const result = {
  /**
   * 使用源文件 examId 作为 externalId。
   *
   * 例如：
   *
   * p1-high-01
   */
  externalId: source.examId,

  title: passageTitle,

  /**
   * 当前 source 字段可以先记录
   * 原题库文件名。
   */
  source: source.meta.pdfFilename,

  passages: [
    {
      passageNumber: 1,

      title: passageTitle,

      /**
       * Passage 公共说明。
       *
       * JSON
       * ↓
       * ReadingPassageImportDto
       * ↓
       * ReadingImportService
       * ↓
       * SQLite
       */
      instruction: passageInstruction,

      content: passageContent,

      questionGroups: source.questionGroups.map(convertQuestionGroup),

      /**
       * 新格式已经把 questions 放在
       * questionGroups 中。
       *
       * 所以旧 questions 暂时不给数据。
       */
      questions: [],
    },
  ],
};

/**
 * 把转换后的 JSON 写入文件。
 */
fs.writeFileSync(outputFile, JSON.stringify(result, null, 2), "utf-8");

console.log(`Reading file converted successfully: ${outputFile}`);
