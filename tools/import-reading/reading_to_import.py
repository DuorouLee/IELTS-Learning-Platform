import html
import json
import re
from pathlib import Path


# ============================================================
# 输入 / 输出文件
# ============================================================
#
# INPUT_FILE：
# 原始 Reading 接口响应。
#
# OUTPUT_FILE：
# 转换成后端 ReadingImportDto 可以读取的 JSON。
#
INPUT_FILE = Path(
    r"C:\Users\21931\Desktop\temp\examPassagesV2_real_response.json"
)

OUTPUT_FILE = Path(
    r"C:\Users\21931\Desktop\temp\reading_import_converted.json"
)


# ============================================================
# 当前完整 Reading Test 的基础信息
# ============================================================
#
# 这里使用中性的项目内部名称。
#
# 注意：
# 如果数据库中已经存在旧 externalId，
# 后续重新导入时需要同步处理数据库中的 externalId。
#
EXTERNAL_ID = "reading-test-01"
TEST_TITLE = "Reading Test 01"
TEST_SOURCE = None


def get_question_json(group):
    """
    安全取得题组中的 questionJson。

    正常情况下 questionJson 是 dict。
    如果以后遇到字符串形式的 JSON，
    这里也尝试解析，避免后面的转换逻辑重复判断。
    """

    question_json = group.get("questionJson")

    if isinstance(question_json, dict):
        return question_json

    if isinstance(question_json, str):
        try:
            parsed = json.loads(question_json)

            if isinstance(parsed, dict):
                return parsed

        except json.JSONDecodeError:
            pass

    return {}


def html_to_plain_text(value):
    """
    把简单 HTML 转成便于分析的纯文本。

    这个函数主要用于：
    1. 判断 descriptions 中的题型说明；
    2. 从 Matching 的 questionsContent 中提取单题题干。

    这里不会用于 Passage 正文显示，
    所以不会破坏前端原文 / 译文功能。
    """

    if not isinstance(value, str):
        return ""

    text = value

    # <br> / </div> / </p> 等块级标签转换成换行，
    # 方便后面按照题号拆分。
    text = re.sub(
        r"(?i)<br\s*/?>",
        "\n",
        text
    )

    text = re.sub(
        r"(?i)</(?:div|p|li|tr|h[1-6])>",
        "\n",
        text
    )

    # 删除其他 HTML 标签。
    text = re.sub(
        r"<[^>]+>",
        "",
        text
    )

    # 把 &nbsp; / &ndash; 等 HTML 实体恢复成正常字符。
    text = html.unescape(text)

    # 不换行的空白压缩成一个空格。
    text = re.sub(
        r"[ \t\r\f\v]+",
        " ",
        text
    )

    # 合并多余空行。
    text = re.sub(
        r"\n\s*\n+",
        "\n",
        text
    )

    return text.strip()


def get_question_option_texts(question_json):
    """
    读取 questions[] 中每一道题自己的 options。

    返回结构示例：

    [
        ["TRUE", "FALSE", "NOT GIVEN"],
        ["TRUE", "FALSE", "NOT GIVEN"]
    ]

    或：

    [
        ["Option A text", "Option B text", ...]
    ]
    """

    source_questions = question_json.get("questions")

    if not isinstance(source_questions, list):
        return []

    all_options = []

    for question in source_questions:

        current_options = []

        if isinstance(question, dict):
            source_options = question.get("options")

            if isinstance(source_options, list):

                for option in source_options:

                    if not isinstance(option, dict):
                        continue

                    content = option.get("content")

                    if content is not None:
                        current_options.append(
                            str(content).strip()
                        )

        all_options.append(current_options)

    return all_options


def detect_question_type(group):
    """
    根据“真实题组结构”判断平台内部 questionType。

    这里不再使用简单的：
        0 -> 某题型
        3 -> 某题型
        4 -> 某题型

    因为同一个原始数字类型可能对应不同的 IELTS 语义题型。

    当前判断顺序：

    1. SUMMARY_COMPLETION_WITH_OPTIONS
    2. COMPLETION
    3. TRUE_FALSE_NOT_GIVEN
    4. YES_NO_NOT_GIVEN
    5. MATCHING_INFORMATION
    6. MATCHING_FEATURES
    7. MULTIPLE_CHOICE
    8. UNKNOWN_xxx

    以后导入新的 Reading Test 时，
    如果遇到新结构，只需要继续扩展这个函数。
    """

    question_json = get_question_json(group)

    descriptions = html_to_plain_text(
        question_json.get("descriptions")
    ).lower()

    questions_content = question_json.get(
        "questionsContent"
    )

    match_options = question_json.get(
        "matchOptions"
    )

    question_option_groups = get_question_option_texts(
        question_json
    )

    # --------------------------------------------------------
    # 1. 带共享选项的 Summary Completion
    #
    # 示例：
    # Complete the summary using the list of words, A–I, below.
    # --------------------------------------------------------
    if (
        "complete the summary using the list of words"
        in descriptions
        and isinstance(match_options, list)
        and len(match_options) > 0
    ):
        return "SUMMARY_COMPLETION_WITH_OPTIONS"

    # --------------------------------------------------------
    # 2. 普通 Completion
    #
    # 当前真实数据包括：
    # - Complete the notes below.
    # - Complete the summary below.
    #
    # 并且 questionsContent 中含有 ...... 填空位置。
    # --------------------------------------------------------
    if (
        isinstance(questions_content, str)
        and "....." in questions_content
        and (
            "complete the notes" in descriptions
            or "complete the summary" in descriptions
        )
    ):
        return "COMPLETION"

    # --------------------------------------------------------
    # 3 / 4. TRUE/FALSE/NOT GIVEN 与 YES/NO/NOT GIVEN
    #
    # 直接检查 questions[] 的真实选项，
    # 比依赖原始数字 questionType 更可靠。
    # --------------------------------------------------------
    first_options = []

    if question_option_groups:
        first_options = [
            option.upper()
            for option in question_option_groups[0]
        ]

    if first_options == [
        "TRUE",
        "FALSE",
        "NOT GIVEN",
    ]:
        return "TRUE_FALSE_NOT_GIVEN"

    if first_options == [
        "YES",
        "NO",
        "NOT GIVEN",
    ]:
        return "YES_NO_NOT_GIVEN"

    # --------------------------------------------------------
    # 5. Matching Information
    #
    # 典型说明：
    # Which section contains the following information?
    # --------------------------------------------------------
    if (
        isinstance(match_options, list)
        and "which section contains" in descriptions
    ):
        return "MATCHING_INFORMATION"

    # --------------------------------------------------------
    # 6. Matching Features
    #
    # 典型说明：
    # Match each statement with the correct person...
    # --------------------------------------------------------
    if (
        isinstance(match_options, list)
        and "match each statement with the correct"
        in descriptions
    ):
        return "MATCHING_FEATURES"

    # --------------------------------------------------------
    # 7. 标准 Multiple Choice
    #
    # questions[] 存在，
    # 且每一道题都有自己的选项。
    # --------------------------------------------------------
    source_questions = question_json.get("questions")

    if isinstance(source_questions, list):

        has_individual_options = any(
            len(options) >= 2
            for options in question_option_groups
        )

        if has_individual_options:
            return "MULTIPLE_CHOICE"

    # --------------------------------------------------------
    # 8. 暂时无法识别的新题型
    #
    # 保留原始两个类型代码，方便以后定位，
    # 但不带任何外部来源名称。
    # --------------------------------------------------------
    first_type = group.get("firstQuestionType")
    second_type = group.get("secondQuestionType")

    return (
        f"UNKNOWN_{first_type}_{second_type}"
    )


def build_group_options(question_json):
    """
    提取题组级共享选项。

    适用于：
    - MATCHING_INFORMATION
    - MATCHING_FEATURES
    - SUMMARY_COMPLETION_WITH_OPTIONS

    原始结构：

    matchOptions = [
        {"index": "A", "content": "..."},
        {"index": "B", "content": "..."}
    ]

    转换后：

    [
        {
            "optionValue": "A",
            "optionText": "...",
            "displayOrder": 1
        }
    ]
    """

    result = []

    match_options = question_json.get(
        "matchOptions"
    )

    if not isinstance(match_options, list):
        return result

    for display_order, option in enumerate(
        match_options,
        start=1
    ):

        if not isinstance(option, dict):
            continue

        option_value = option.get("index")
        option_text = option.get("content")

        if option_value is None:
            option_value = ""

        if option_text is None:
            option_text = ""

        result.append({
            "optionValue": str(option_value),
            "optionText": str(option_text),
            "displayOrder": display_order,
        })

    return result


def get_group_option_values(question_json):
    """
    取得 matchOptions 中真正用于保存答案的值。

    例如：

    [
        {"index": "A", ...},
        {"index": "B", ...},
        {"index": "C", ...}
    ]

    返回：

    ["A", "B", "C"]
    """

    result = []

    match_options = question_json.get(
        "matchOptions"
    )

    if not isinstance(match_options, list):
        return result

    for option in match_options:

        if not isinstance(option, dict):
            continue

        index = option.get("index")

        if index is not None:
            result.append(str(index))

    return result


def normalize_answer(
    value,
    question_type,
    question_options=None,
    group_option_values=None,
):
    """
    把原始答案转换成平台前端可以直接使用的值。

    原始选择类答案经常使用：
        0
        1
        2
        3

    它表示“第几个选项”，而不是答案文字。

    例如 TRUE / FALSE / NOT GIVEN：

        0 -> TRUE
        1 -> FALSE
        2 -> NOT GIVEN

    Multiple Choice：

        0 -> A
        1 -> B
        2 -> C
        3 -> D

    Matching / 带选项 Summary：

        0 -> matchOptions[0].index
        1 -> matchOptions[1].index
        ...
    """

    if value is None:
        return None

    question_options = question_options or []
    group_option_values = group_option_values or []

    # --------------------------------------------------------
    # TRUE / FALSE / NOT GIVEN
    # YES / NO / NOT GIVEN
    #
    # 直接使用当前单题 options 中的实际文字。
    # --------------------------------------------------------
    if question_type in {
        "TRUE_FALSE_NOT_GIVEN",
        "YES_NO_NOT_GIVEN",
    }:

        if isinstance(value, int):
            if 0 <= value < len(question_options):
                return str(
                    question_options[value]
                )

    # --------------------------------------------------------
    # Multiple Choice
    #
    # 正确答案使用 A / B / C / D...
    # --------------------------------------------------------
    if question_type == "MULTIPLE_CHOICE":

        option_letters = [
            "A", "B", "C", "D",
            "E", "F", "G", "H",
            "I", "J", "K", "L",
        ]

        if isinstance(value, int):
            if 0 <= value < len(option_letters):
                return option_letters[value]

    # --------------------------------------------------------
    # 共享选项题型
    #
    # 答案索引转换成 matchOptions 的 optionValue。
    # --------------------------------------------------------
    if question_type in {
        "MATCHING_INFORMATION",
        "MATCHING_FEATURES",
        "SUMMARY_COMPLETION_WITH_OPTIONS",
    }:

        if isinstance(value, int):
            if 0 <= value < len(group_option_values):
                return str(
                    group_option_values[value]
                )

    # --------------------------------------------------------
    # 普通 Completion 等字符串答案直接保留。
    # --------------------------------------------------------
    return str(value)


def extract_question_options(
    question_json,
    local_index,
):
    """
    提取“某一道题自己的独立选项”。

    主要用于：
    - TRUE_FALSE_NOT_GIVEN
    - YES_NO_NOT_GIVEN
    - MULTIPLE_CHOICE

    Matching / Summary with options 使用题组级 matchOptions，
    不走这里。
    """

    result = []

    source_questions = question_json.get(
        "questions"
    )

    if not isinstance(source_questions, list):
        return result

    if not (
        0 <= local_index < len(source_questions)
    ):
        return result

    source_question = source_questions[
        local_index
    ]

    if not isinstance(source_question, dict):
        return result

    source_options = source_question.get(
        "options"
    )

    if not isinstance(source_options, list):
        return result

    for option in source_options:

        if not isinstance(option, dict):
            continue

        content = option.get("content")

        if content is not None:
            result.append(str(content))

    return result


def extract_numbered_question_text(
    questions_content,
    question_number,
    next_question_number=None,
):
    """
    从整块 questionsContent 中拆出一条 Matching 题干。

    原始数据可能类似：

        22 It is unpleasant ... ......
        23 The trend ... ......
        24 When our body's senses ... ......

    我们先把 HTML 转成纯文本，
    再按照真实题号切片。

    返回结果不会包含：
    - <div>
    - <br>
    - &nbsp;
    - &middot;

    也不会把整块 HTML 重复显示给每一道题。
    """

    text = html_to_plain_text(
        questions_content
    )

    if not text:
        return None

    # 当前题号必须位于一条题目的开头。
    start_pattern = re.compile(
        rf"(?m)^\s*{re.escape(str(question_number))}\s+"
    )

    start_match = start_pattern.search(text)

    if start_match is None:
        return None

    start_position = start_match.end()

    if next_question_number is not None:

        next_pattern = re.compile(
            rf"(?m)^\s*{re.escape(str(next_question_number))}\s+"
        )

        next_match = next_pattern.search(
            text,
            start_position
        )

        if next_match is not None:
            end_position = next_match.start()
        else:
            end_position = len(text)

    else:
        end_position = len(text)

    question_text = text[
        start_position:end_position
    ].strip()

    # 删除 Matching 原文末尾用于放答案的 ......。
    question_text = re.sub(
        r"\s*\.{5,}\s*$",
        "",
        question_text
    ).strip()

    return question_text


def extract_question_text(
    question_json,
    local_index,
    question_number,
    question_type,
):
    """
    提取当前题目的 questionText。

    分三种结构处理：

    1. questions[]
       原始数据已经拆成单题，直接读取 content。

    2. Matching
       questionsContent 是整块题目，
       这里根据真实题号拆成单题纯文本。

    3. Completion / Summary Completion
       暂时保留完整 questionsContent。

       原因：
       当前 CompletionQuestionGroup.vue
       需要完整模板来把多个 ...... 替换成输入框。

       后续如果 QuestionGroup 增加 content/template 字段，
       再把完整模板从 ReadingQuestion 移到 QuestionGroup。
    """

    source_questions = question_json.get(
        "questions"
    )

    if isinstance(source_questions, list):

        if 0 <= local_index < len(source_questions):

            item = source_questions[
                local_index
            ]

            if isinstance(item, dict):
                return item.get("content")

    questions_content = question_json.get(
        "questionsContent"
    )

    # Matching 需要真正拆成单题，
    # 避免每一道题重复整块 HTML。
    if question_type in {
        "MATCHING_INFORMATION",
        "MATCHING_FEATURES",
    }:

        return extract_numbered_question_text(
            questions_content,
            question_number,
            question_number + 1,
        )

    # Completion 类当前仍使用整块模板。
    return questions_content


def build_questions(group):
    """
    把一个题组转换成 ReadingQuestionImportDto 列表。
    """

    question_json = get_question_json(group)

    answer_json = group.get(
        "answerJson"
    )

    if not isinstance(answer_json, list):
        answer_json = []

    start_index = question_json.get(
        "startIndex"
    )

    if start_index is None:
        return []

    question_count = group.get(
        "questionCount"
    )

    if question_count is None:
        question_count = question_json.get(
            "questionNum",
            0
        )

    try:
        question_count = int(question_count)
    except (TypeError, ValueError):
        question_count = 0

    question_type = detect_question_type(
        group
    )

    group_option_values = (
        get_group_option_values(
            question_json
        )
    )

    result = []

    for local_index in range(
        question_count
    ):

        question_number = (
            int(start_index)
            + local_index
        )

        answer_item = {}

        if local_index < len(answer_json):

            possible_answer = answer_json[
                local_index
            ]

            if isinstance(
                possible_answer,
                dict
            ):
                answer_item = possible_answer

        question_options = (
            extract_question_options(
                question_json,
                local_index,
            )
        )

        question_text = (
            extract_question_text(
                question_json,
                local_index,
                question_number,
                question_type,
            )
        )

        result.append({
            "questionNumber":
                question_number,

            "questionType":
                question_type,

            "questionText":
                question_text,

            # 当前题自己的独立选项。
            # Matching 的共享选项不会重复存到这里。
            "options":
                question_options,

            "correctAnswer":
                normalize_answer(
                    answer_item.get(
                        "correctValue"
                    ),
                    question_type,
                    question_options,
                    group_option_values,
                ),

            # 答案解析。
            "explanation":
                answer_item.get(
                    "explain"
                ),

            # 原文答案定位 / 高亮信息。
            "answerHighlight":
                answer_item.get(
                    "articleSourceHighlight"
                ),
        })

    return result


def detect_allow_option_reuse(
    question_json
):
    """
    判断共享选项是否允许重复使用。

    IELTS Matching 中经常会出现：

        NB You may use any letter more than once.

    这种情况下返回 True。
    """

    descriptions = html_to_plain_text(
        question_json.get(
            "descriptions"
        )
    ).lower()

    return (
        "may use any letter more than once"
        in descriptions
    )


def build_question_group(group):
    """
    转换一个 QuestionGroup。
    """

    question_json = get_question_json(
        group
    )

    question_type = detect_question_type(
        group
    )

    return {
        "questionType":
            question_type,

        # 题组公共答题说明。
        "instruction":
            question_json.get(
                "descriptions"
            ),

        # Matching 是否允许重复使用选项。
        "allowOptionReuse":
            detect_allow_option_reuse(
                question_json
            ),

        # QuestionGroup 级共享选项。
        "options":
            build_group_options(
                question_json
            ),

        # 当前题组的全部题目。
        "questions":
            build_questions(
                group
            ),
    }


def build_passage(passage):
    """
    转换一个 Reading Passage。
    """

    question_groups = []

    source_groups = passage.get(
        "questions"
    )

    if not isinstance(source_groups, list):
        source_groups = []

    for group in source_groups:

        if not isinstance(group, dict):
            continue

        question_groups.append(
            build_question_group(
                group
            )
        )

    return {
        "passageNumber":
            passage.get("part"),

        "title":
            passage.get(
                "passagesQuestion"
            ),

        "instruction":
            None,

        # 英文原文。
        "content":
            passage.get(
                "passagesContent"
            ),

        # 中文译文。
        "translation":
            passage.get(
                "articleTranslation"
            ),

        "questionGroups":
            question_groups,
    }


def print_conversion_summary(passages):
    """
    打印转换结果摘要。

    除了数量，还打印每个 QuestionGroup 的：
    - 题号范围
    - questionType
    - questions 数量

    这样每次修改 converter 后，
    可以先在终端验证，而不用马上导数据库。
    """

    print()
    print("转换完成。")
    print()

    print("输出文件：")
    print(OUTPUT_FILE)
    print()

    print(
        f"Passage 数量：{len(passages)}"
    )

    total_groups = 0
    total_questions = 0

    for passage in passages:

        passage_number = passage.get(
            "passageNumber"
        )

        groups = passage.get(
            "questionGroups",
            []
        )

        for group in groups:

            total_groups += 1

            questions = group.get(
                "questions",
                []
            )

            total_questions += len(
                questions
            )

            if questions:
                start_number = questions[0].get(
                    "questionNumber"
                )

                end_number = questions[-1].get(
                    "questionNumber"
                )
            else:
                start_number = "?"
                end_number = "?"

            print(
                f"Part {passage_number} "
                f"Q{start_number}-{end_number}: "
                f"{group.get('questionType')} "
                f"({len(questions)} questions)"
            )

    print()
    print(
        f"QuestionGroup 数量：{total_groups}"
    )

    print(
        f"Question 数量：{total_questions}"
    )


def main():
    """
    主转换流程：
    原始 Reading JSON
        ↓
    Passage / QuestionGroup / Question
        ↓
    ReadingImportDto JSON
    """

    if not INPUT_FILE.exists():

        print("找不到 Reading Response 文件：")
        print(INPUT_FILE)
        return

    # ========================================================
    # 1. 读取原始 Response
    # ========================================================

    with INPUT_FILE.open(
        "r",
        encoding="utf-8"
    ) as file:

        source_data = json.load(
            file
        )

    # 当前真实接口结构：
    #
    # {
    #     "status": ...,
    #     "message": ...,
    #     "content": [
    #         Passage 1,
    #         Passage 2,
    #         Passage 3
    #     ]
    # }
    content = source_data.get(
        "content"
    )

    if not isinstance(content, list):

        print(
            "content 不是 list，无法转换。"
        )

        return

    # ========================================================
    # 2. 转换 Passage
    # ========================================================

    passages = []

    for passage in content:

        if not isinstance(
            passage,
            dict
        ):
            continue

        passages.append(
            build_passage(
                passage
            )
        )

    # ========================================================
    # 3. 生成后端 ReadingImportDto 对应 JSON
    # ========================================================

    result = {
        "externalId":
            EXTERNAL_ID,

        "title":
            TEST_TITLE,

        "source":
            TEST_SOURCE,

        "passages":
            passages,
    }

    # ========================================================
    # 4. 写入转换结果
    # ========================================================

    with OUTPUT_FILE.open(
        "w",
        encoding="utf-8"
    ) as file:

        json.dump(
            result,
            file,
            ensure_ascii=False,
            indent=2
        )

    # ========================================================
    # 5. 输出检查摘要
    # ========================================================

    print_conversion_summary(
        passages
    )


if __name__ == "__main__":
    main()
