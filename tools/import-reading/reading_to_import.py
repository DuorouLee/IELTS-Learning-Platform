import html
import json
import re
from pathlib import Path


# ============================================================
# 批量输入 / 输出目录
# ============================================================
#
# raw/
#   c21-test-1.json
#   c21-test-1.meta.json
#   ...
#
# converted/
#   c21-test-1.json
#   c21-test-2.json
#   ...
#
# 注意：
# raw/ 建议加入 .gitignore，不提交原始题库。
#
SCRIPT_FILE = Path(__file__).resolve()
PROJECT_ROOT = SCRIPT_FILE.parents[2]

RAW_DIR = (
    PROJECT_ROOT
    / "backend"
    / "src"
    / "main"
    / "resources"
    / "data"
    / "reading"
    / "raw"
)

CONVERTED_DIR = (
    PROJECT_ROOT
    / "backend"
    / "src"
    / "main"
    / "resources"
    / "data"
    / "reading"
    / "converted"
)

SUMMARY_FILE = (
    CONVERTED_DIR
    / "conversion-summary.json"
)


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
    根据真实题目语义 + 结构识别平台内部 questionType。

    不直接把某个数字类型硬编码成某个 IELTS 题型，
    因为同一组原始数字在不同试题中可能代表不同语义。

    当前支持：
    - MATCHING_HEADINGS
    - MATCHING_SENTENCE_ENDINGS
    - MATCHING_INFORMATION
    - MATCHING_FEATURES
    - SUMMARY_COMPLETION_WITH_OPTIONS
    - TRUE_FALSE_NOT_GIVEN
    - YES_NO_NOT_GIVEN
    - MULTIPLE_CHOICE
    - COMPLETION
    """

    question_json = get_question_json(
        group
    )

    descriptions = html_to_plain_text(
        question_json.get(
            "descriptions"
        )
    ).lower()

    questions_content = (
        question_json.get(
            "questionsContent"
        )
    )

    questions_content_text = (
        html_to_plain_text(
            questions_content
        ).lower()
    )

    match_options = (
        question_json.get(
            "matchOptions"
        )
    )

    question_option_groups = (
        get_question_option_texts(
            question_json
        )
    )

    first_type = group.get(
        "firstQuestionType"
    )

    second_type = group.get(
        "secondQuestionType"
    )

    has_shared_options = (
        isinstance(
            match_options,
            list
        )
        and len(
            match_options
        ) > 0
    )

    # --------------------------------------------------------
    # 1. Matching Headings
    # --------------------------------------------------------
    if (
        has_shared_options
        and (
            "choose the correct heading"
            in descriptions
            or "list of headings"
            in descriptions
        )
    ):
        return "MATCHING_HEADINGS"

    # --------------------------------------------------------
    # 2. Matching Sentence Endings
    # --------------------------------------------------------
    if (
        has_shared_options
        and (
            "complete each sentence with the correct ending"
            in descriptions
        )
    ):
        return "MATCHING_SENTENCE_ENDINGS"

    # --------------------------------------------------------
    # 3. Matching Information
    # --------------------------------------------------------
    if (
        has_shared_options
        and (
            "which paragraph contains"
            in descriptions
            or "which section contains"
            in descriptions
        )
    ):
        return "MATCHING_INFORMATION"

    # --------------------------------------------------------
    # 4. Matching Features
    # --------------------------------------------------------
    if (
        has_shared_options
        and (
            (
                "match each "
                in descriptions
                and " with the correct "
                in descriptions
            )
            or (
                "look at the following"
                in descriptions
                and "match each"
                in descriptions
            )
        )
    ):
        return "MATCHING_FEATURES"

    # --------------------------------------------------------
    # 5. 带共享选项的 Summary Completion
    # --------------------------------------------------------
    if (
        "complete the summary using the list of words"
        in descriptions
        and has_shared_options
    ):
        return (
            "SUMMARY_COMPLETION_WITH_OPTIONS"
        )

    # --------------------------------------------------------
    # 6 / 7. 判断题
    # --------------------------------------------------------
    first_options = []

    if question_option_groups:
        first_options = [
            option.upper()
            for option
            in question_option_groups[0]
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
    # 8. Multiple Choice
    # --------------------------------------------------------
    source_questions = (
        question_json.get(
            "questions"
        )
    )

    if isinstance(
        source_questions,
        list
    ):
        has_individual_options = any(
            len(options) >= 2
            for options
            in question_option_groups
        )

        if has_individual_options:
            return "MULTIPLE_CHOICE"

    # --------------------------------------------------------
    # 9. Completion 家族
    # --------------------------------------------------------
    #
    # 当前前端的 CompletionQuestionGroup
    # 会把 questionsContent 中的 ...... 转成输入框，
    # 所以这些语义可以共用 COMPLETION：
    #
    # - Sentence Completion
    # - Notes Completion
    # - Summary Completion
    # - Table Completion
    # - Flow-chart Completion
    # - Diagram Label Completion
    # - Short Answer
    # --------------------------------------------------------
    has_blank = (
        isinstance(
            questions_content,
            str
        )
        and "....."
        in questions_content_text
    )

    explicit_completion_instruction = (
        "complete the notes"
        in descriptions
        or "complete the summary"
        in descriptions
        or "complete the sentences"
        in descriptions
        or "complete the sentence"
        in descriptions
        or "complete the table"
        in descriptions
        or "complete the flow-chart"
        in descriptions
        or "complete the flow chart"
        in descriptions
        or "label the diagram"
        in descriptions
        or "label the diagrams"
        in descriptions
        or "answer the questions below"
        in descriptions
    )

    if (
        has_blank
        and (
            explicit_completion_instruction
            or first_type == 0
        )
    ):
        return "COMPLETION"

    # --------------------------------------------------------
    # 10. 共享选项结构兜底
    # --------------------------------------------------------
    #
    # 极少数旧题说明文字只有：
    # "Choose the correct answer and move it into the gap."
    #
    # 但其结构仍然是：
    # questionsContent + matchOptions。
    #
    # 前面的 Headings / Information / Sentence Endings
    # 已经优先识别，因此这里安全地归为 Matching Features。
    # --------------------------------------------------------
    if (
        has_shared_options
        and first_type == 4
    ):
        return "MATCHING_FEATURES"

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
        "MATCHING_HEADINGS",
        "MATCHING_INFORMATION",
        "MATCHING_FEATURES",
        "MATCHING_SENTENCE_ENDINGS",
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

def clean_answer_highlight(value):
    """
    清洗答案定位数据中的明显文本错误。

    当前已确认有一处原文缺字：

        When we nd ways to be quiet

    正确原文应该是：

        When we find ways to be quiet

    这里对整个 answerHighlight 结构递归处理，
    不管错误文本出现在：
    - key
    - value
    - text
    - id

    都可以一起修正。
    """

    if isinstance(value, str):

        # 修复当前已确认的缺字问题。
        return value.replace(
            "When we nd ways to be quiet",
            "When we find ways to be quiet"
        )

    if isinstance(value, list):

        return [
            clean_answer_highlight(item)
            for item in value
        ]

    if isinstance(value, dict):

        cleaned = {}

        for key, nested_value in value.items():

            # key 自己也可能就是原文句子，
            # 所以 key 也需要清洗。
            cleaned_key = clean_answer_highlight(
                key
            )

            cleaned[
                cleaned_key
            ] = clean_answer_highlight(
                nested_value
            )

        return cleaned

    return value

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
    从整块 questionsContent 中拆出一条带题号的 Matching 题干。

    真实数据里的题号格式并不完全统一，例如：

        14 text...
        14. text...
        14) text...

    所以这里允许题号后面出现：
    - 空格
    - .
    - )

    如果当前文本本身没有题号，
    本函数返回 None，后面再使用按行拆分的兜底逻辑。
    """

    text = html_to_plain_text(
        questions_content
    )

    if not text:
        return None

    # 允许：
    # 14 text
    # 14. text
    # 14) text
    start_pattern = re.compile(
        rf"(?m)^\s*{re.escape(str(question_number))}"
        rf"\s*[\.\)]?\s+"
    )

    start_match = start_pattern.search(
        text
    )

    if start_match is None:
        return None

    start_position = start_match.end()

    if next_question_number is not None:

        next_pattern = re.compile(
            rf"(?m)^\s*{re.escape(str(next_question_number))}"
            rf"\s*[\.\)]?\s+"
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

    # 删除末尾用于放答案的 ......
    question_text = re.sub(
        r"\s*\.{5,}\s*$",
        "",
        question_text
    ).strip()

    return (
        question_text
        if question_text
        else None
    )


def extract_matching_question_text_by_line(
    questions_content,
    local_index,
):
    """
    Matching 旧题的兜底拆分。

    有些原始数据完全没有题号，例如：

        to remove trees that are diseased......
        to generate income across a number of years......
        to create a forest whose trees are close in age......

    这种情况下不能靠 Question 19 / 20 / 21 去搜索题号，
    只能按照 questionsContent 中的自然顺序拆分。

    local_index:
        当前题在这个 QuestionGroup 中的位置：
        0, 1, 2, ...

    返回：
        对应位置的一条题干。
    """

    text = html_to_plain_text(
        questions_content
    )

    if not text:
        return None

    lines = []

    for raw_line in text.splitlines():

        line = raw_line.strip()

        if not line:
            continue

        # 去掉末尾答案占位符。
        line = re.sub(
            r"\s*\.{5,}\s*$",
            "",
            line
        ).strip()

        # 如果这一行仍然带题号，
        # 顺手把开头题号去掉。
        line = re.sub(
            r"^\s*\d+\s*[\.\)]?\s*",
            "",
            line
        ).strip()

        if line:
            lines.append(
                line
            )

    if (
        0 <= local_index < len(lines)
    ):
        return lines[
            local_index
        ]

    return None


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

                # 某些真实题目的 questions[] 存在，
                # 但单题 content 可能是 null。
                #
                # 不能直接 return None，
                # 否则会违反数据库中 question_text 的 NOT NULL 约束。
                item_content = item.get(
                    "content"
                )

                if item_content is not None:
                    return item_content

    questions_content = question_json.get(
        "questionsContent"
    )

    # Matching 需要真正拆成单题，
    # 避免每一道题重复整块 questionsContent。
    if question_type in {
        "MATCHING_HEADINGS",
        "MATCHING_INFORMATION",
        "MATCHING_FEATURES",
        "MATCHING_SENTENCE_ENDINGS",
    }:

        # 第一层：
        # 优先按照真实题号拆分。
        matching_text = (
            extract_numbered_question_text(
                questions_content,
                question_number,
                question_number + 1,
            )
        )

        if matching_text is not None:
            return matching_text

        # 第二层：
        # 有些旧题 questionsContent 完全没有题号，
        # 按题组内部顺序逐行拆分。
        matching_text = (
            extract_matching_question_text_by_line(
                questions_content,
                local_index,
            )
        )

        if matching_text is not None:
            return matching_text

        # 第三层：
        # 极端异常数据至少保证数据库 NOT NULL。
        return f"Question {question_number}"

    # Completion / 特殊旧题优先使用整块模板。
    if questions_content is not None:
        return questions_content

    # 最后的数据安全兜底。
    # question_text 在数据库中是 NOT NULL。
    return f"Question {question_number}"


def build_questions(group):
    """
    把一个题组转换成 ReadingQuestionImportDto 列表。

    特别处理 IELTS 多答案选择题，例如：

        Questions 25 and 26
        Choose TWO letters, A-E.

    原始数据可能只有：

        questions = [一个题干]
        answerJson = [
            {
                "correctValue": [1, 2]
            }
        ]

    但 questionCount = 2。

    这里需要把：

        [1, 2]

    拆成两个平台题号：

        Q25 -> B
        Q26 -> C

    两个题号共享同一个题干和同一组选项。
    """

    question_json = get_question_json(
        group
    )

    answer_json = group.get(
        "answerJson"
    )

    if not isinstance(
        answer_json,
        list
    ):
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
        question_count = int(
            question_count
        )
    except (
        TypeError,
        ValueError,
    ):
        question_count = 0

    question_type = detect_question_type(
        group
    )

    group_option_values = (
        get_group_option_values(
            question_json
        )
    )

    # ========================================================
    # 判断是否属于“一道题干 + 多个答案框”的 Multiple Choice
    # ========================================================
    #
    # 例如：
    #
    # Questions 25 and 26
    # Choose TWO letters, A-E.
    #
    # answerJson:
    #
    # [
    #   {
    #     "correctValue": [1, 2]
    #   }
    # ]
    #
    # [1, 2] 是零基索引：
    #
    # 1 -> B
    # 2 -> C
    # ========================================================

    multi_answer_values = None
    multi_answer_item = None

    if (
        question_type
        == "MULTIPLE_CHOICE"
        and len(answer_json) == 1
        and isinstance(
            answer_json[0],
            dict
        )
    ):

        possible_values = (
            answer_json[0].get(
                "correctValue"
            )
        )

        if (
            isinstance(
                possible_values,
                list
            )
            and len(
                possible_values
            )
            == question_count
        ):
            multi_answer_values = (
                possible_values
            )

            multi_answer_item = (
                answer_json[0]
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
        correct_value = None

        # ----------------------------------------------------
        # 多答案 Multiple Choice：
        #
        # 一个 answerJson 对应多个题号。
        # ----------------------------------------------------
        if (
            multi_answer_values
            is not None
        ):

            answer_item = (
                multi_answer_item
                or {}
            )

            correct_value = (
                multi_answer_values[
                    local_index
                ]
            )

            # 多个答案框共享同一个题干 / 选项。
            source_local_index = 0

        # ----------------------------------------------------
        # 普通题型：
        #
        # 一个 answerJson 对应一个题号。
        # ----------------------------------------------------
        else:

            source_local_index = (
                local_index
            )

            if (
                local_index
                < len(
                    answer_json
                )
            ):

                possible_answer = (
                    answer_json[
                        local_index
                    ]
                )

                if isinstance(
                    possible_answer,
                    dict
                ):
                    answer_item = (
                        possible_answer
                    )

            correct_value = (
                answer_item.get(
                    "correctValue"
                )
            )

        question_options = (
            extract_question_options(
                question_json,
                source_local_index,
            )
        )

        question_text = (
            extract_question_text(
                question_json,
                source_local_index,
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

            "options":
                question_options,

            "correctAnswer":
                normalize_answer(
                    correct_value,
                    question_type,
                    question_options,
                    group_option_values,
                ),

            # 多答案 Multiple Choice 的两个答案框
            # 属于同一道原始题，所以可以共享解析。
            "explanation":
                answer_item.get(
                    "explain"
                ),

            # 同理，共享官方原文定位。
            "answerHighlight":
                clean_answer_highlight(
                    answer_item.get(
                        "articleSourceHighlight"
                    )
                ),
        })

    return result


def detect_allow_option_reuse(
    question_json
):
    """
    判断共享选项是否允许重复使用。

    IELTS Matching 中经常出现：

        NB You may use any letter more than once.

    如果题目说明中包含这句话，
    就返回 True。

    这个值最终会写入 QuestionGroup.allowOptionReuse，
    前端可以据此决定同一个选项是否允许被多个题目重复选择。
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


def count_questions(passages):
    """
    统计整套 Reading 的 Question 总数。
    """

    total = 0

    for passage in passages:
        for group in passage.get(
            "questionGroups",
            []
        ):
            total += len(
                group.get(
                    "questions",
                    []
                )
            )

    return total


def find_unknown_question_types(passages):
    """
    找出当前 converter 还不能识别的题型。
    """

    unknown_types = []

    for passage in passages:

        passage_number = passage.get(
            "passageNumber"
        )

        for group in passage.get(
            "questionGroups",
            []
        ):

            question_type = group.get(
                "questionType",
                ""
            )

            if str(
                question_type
            ).startswith(
                "UNKNOWN_"
            ):
                unknown_types.append({
                    "passageNumber":
                        passage_number,

                    "questionType":
                        question_type,
                })

    return unknown_types


def validate_converted_test(passages):
    """
    批量转换后的基础质量检查。

    当前完整 IELTS Reading 应满足：

    - Passage = 3
    - Question = 40
    - 不存在 UNKNOWN_xxx 题型

    返回：
        (True, [])
        或
        (False, ["错误1", "错误2"])
    """

    errors = []

    if len(passages) != 3:
        errors.append(
            f"Passage 数量不是 3，而是 {len(passages)}"
        )

    question_count = count_questions(
        passages
    )

    if question_count != 40:
        errors.append(
            f"Question 数量不是 40，而是 {question_count}"
        )

    unknown_types = (
        find_unknown_question_types(
            passages
        )
    )

    for item in unknown_types:
        errors.append(
            "存在未识别题型："
            f"Part {item['passageNumber']} "
            f"{item['questionType']}"
        )

    # --------------------------------------------------------
    # 数据库字段完整性检查
    # --------------------------------------------------------
    #
    # reading_question.question_text
    # reading_question.correct_answer
    #
    # 当前数据库都是 NOT NULL。
    #
    # 所以 converter 阶段就应该提前发现问题，
    # 不要等到 Spring Boot 导入时才由 SQLite 报错。
    # --------------------------------------------------------

    for passage in passages:

        passage_number = passage.get(
            "passageNumber"
        )

        for group in passage.get(
            "questionGroups",
            []
        ):

            for question in group.get(
                "questions",
                []
            ):

                question_number = (
                    question.get(
                        "questionNumber"
                    )
                )

                if question.get(
                    "questionText"
                ) is None:
                    errors.append(
                        "questionText 为空："
                        f"Part {passage_number} "
                        f"Q{question_number}"
                    )

                if question.get(
                    "correctAnswer"
                ) is None:
                    errors.append(
                        "correctAnswer 为空："
                        f"Part {passage_number} "
                        f"Q{question_number} "
                        f"{question.get('questionType')}"
                    )

    return (
        len(errors) == 0,
        errors,
    )


def load_metadata(
    raw_file,
):
    """
    读取与 raw 文件同名的 metadata。

    例如：

        c21-test-1.json
        c21-test-1.meta.json
    """

    metadata_file = (
        raw_file.parent
        / f"{raw_file.stem}.meta.json"
    )

    if not metadata_file.exists():
        return {
            "displayName":
                raw_file.stem,

            "externalPaperId":
                None,
        }

    with metadata_file.open(
        "r",
        encoding="utf-8",
    ) as file:

        metadata = json.load(
            file
        )

    if not isinstance(
        metadata,
        dict,
    ):
        return {
            "displayName":
                raw_file.stem,

            "externalPaperId":
                None,
        }

    return metadata


def build_external_id(
    raw_file,
):
    """
    使用项目内部中性文件名作为 externalId。

    例如：

        c21-test-1.json
        ->
        c21-test-1

    这样不会依赖外部 paperId。
    """

    return raw_file.stem


def convert_one_file(
    raw_file,
):
    """
    转换一套 raw Reading。

    返回：
        {
            "outputFile": ...,
            "displayName": ...,
            "passageCount": ...,
            "questionCount": ...,
        }

    如果校验失败，则抛出 ValueError。
    """

    with raw_file.open(
        "r",
        encoding="utf-8",
    ) as file:

        source_data = json.load(
            file
        )

    if not isinstance(
        source_data,
        dict,
    ):
        raise ValueError(
            "最外层不是 JSON Object"
        )

    content = source_data.get(
        "content"
    )

    if not isinstance(
        content,
        list,
    ):
        raise ValueError(
            "content 不是 list"
        )

    passages = []

    for passage in content:

        if not isinstance(
            passage,
            dict,
        ):
            continue

        passages.append(
            build_passage(
                passage
            )
        )

    valid, errors = (
        validate_converted_test(
            passages
        )
    )

    if not valid:
        raise ValueError(
            "；".join(errors)
        )

    metadata = load_metadata(
        raw_file
    )

    display_name = str(
        metadata.get(
            "displayName"
        )
        or raw_file.stem
    )

    result = {
        "externalId":
            build_external_id(
                raw_file
            ),

        "title":
            display_name,

        "source":
            None,

        "passages":
            passages,
    }

    CONVERTED_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    output_file = (
        CONVERTED_DIR
        / raw_file.name
    )

    with output_file.open(
        "w",
        encoding="utf-8",
    ) as file:

        json.dump(
            result,
            file,
            ensure_ascii=False,
            indent=2,
        )

    return {
        "displayName":
            display_name,

        "externalId":
            result["externalId"],

        "rawFile":
            raw_file.name,

        "outputFile":
            output_file.name,

        "passageCount":
            len(passages),

        "questionCount":
            count_questions(
                passages
            ),
    }


def get_raw_files():
    """
    获取所有真正的 raw Reading 文件。

    排除：
    - *.meta.json
    - capture-summary.json
    """

    result = []

    for file_path in sorted(
        RAW_DIR.glob("*.json")
    ):

        if file_path.name.endswith(
            ".meta.json"
        ):
            continue

        if file_path.name in {
            "capture-summary.json",
            "reading-question-type-report.json",
        }:
            continue

        # 只转换这次批量抓取的正式文件。
        # 自动排除旧的 reading-raw-xxx.json。
        if not re.fullmatch(
            r"c\d+-test-\d+\.json",
            file_path.name,
        ):
            continue

        result.append(
            file_path
        )

    return result


def save_batch_summary(
    success_items,
    failed_items,
):
    """
    保存批量转换结果。
    """

    CONVERTED_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    summary = {
        "total":
            len(success_items)
            + len(failed_items),

        "successCount":
            len(success_items),

        "failedCount":
            len(failed_items),

        "success":
            success_items,

        "failed":
            failed_items,
    }

    with SUMMARY_FILE.open(
        "w",
        encoding="utf-8",
    ) as file:

        json.dump(
            summary,
            file,
            ensure_ascii=False,
            indent=2,
        )


def main():
    """
    批量转换流程：

    raw/*.json
        ↓
    复用现有题型识别 / 答案转换逻辑
        ↓
    每套执行质量校验
        ↓
    converted/*.json
        ↓
    conversion-summary.json
    """

    print()
    print(
        "Reading Batch Converter"
    )
    print(
        "=" * 60
    )
    print()

    if not RAW_DIR.exists():

        print(
            "找不到 raw 目录："
        )

        print(
            RAW_DIR
        )

        return

    raw_files = get_raw_files()

    if not raw_files:

        print(
            "raw 目录中没有找到 Reading JSON。"
        )

        return

    print(
        f"找到 raw Test 数量："
        f"{len(raw_files)}"
    )

    print()

    success_items = []
    failed_items = []

    total = len(
        raw_files
    )

    for index, raw_file in enumerate(
        raw_files,
        start=1,
    ):

        print(
            f"[{index}/{total}] "
            f"{raw_file.name}"
        )

        try:
            result = convert_one_file(
                raw_file
            )

        except Exception as error:

            reason = str(
                error
            )

            print(
                "  转换失败："
                + reason
            )

            failed_items.append({
                "rawFile":
                    raw_file.name,

                "reason":
                    reason,
            })

            continue

        print(
            "  成功："
            f"{result['displayName']}"
        )

        print(
            "  "
            f"Passage={result['passageCount']} / "
            f"Question={result['questionCount']}"
        )

        success_items.append(
            result
        )

    save_batch_summary(
        success_items,
        failed_items,
    )

    print()
    print(
        "=" * 60
    )

    print(
        "批量转换完成。"
    )

    print()

    print(
        f"总数：{total}"
    )

    print(
        f"成功："
        f"{len(success_items)}"
    )

    print(
        f"失败："
        f"{len(failed_items)}"
    )

    print()

    print(
        "转换结果目录："
    )

    print(
        CONVERTED_DIR
    )

    print()

    print(
        "结果摘要："
    )

    print(
        SUMMARY_FILE
    )

    if failed_items:

        print()
        print(
            "失败项目："
        )

        for item in failed_items:
            print(
                "- "
                + item[
                    "rawFile"
                ]
                + ": "
                + item[
                    "reason"
                ]
            )


if __name__ == "__main__":
    main()
