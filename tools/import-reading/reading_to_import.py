import json
from pathlib import Path


# ============================================================
# 输入 / 输出文件
# ============================================================

INPUT_FILE = Path(
    r"C:\Users\21931\Desktop\temp\examPassagesV2_real_response.json"
)

OUTPUT_FILE = Path(
    r"C:\Users\21931\Desktop\temp\reading_import_converted.json"
)


# ============================================================
# 雅思哥 questionType 映射
#
# 这里只映射目前真实 Response 中已经看到的类型。
# 后面遇到新题型，再根据真实数据继续补。
# ============================================================

QUESTION_TYPE_MAP = {
    # Completion 类：
    # 例如 Note Completion / Summary Completion
    0: "COMPLETION",

    # 标准 A/B/C/D 单选
    1: "MULTIPLE_CHOICE",

    # 当前真实数据中另一种选择题编码
    3: "MULTIPLE_CHOICE",

    # Matching 类题目
    4: "MATCHING",
}

def detect_question_type(group):
    """
    根据雅思哥真实题组结构判断我们平台使用的 questionType。

    为什么不能只依赖 group["questionType"]？

    因为当前真实 Reading 数据里，
    Completion 类型的内容可能保存在：

        questionJson.questionsContent

    里面，而且它是一整块 HTML，例如：

        their grandfather's wealth came from ...... and transportation businesses

    这种题的核心特征是：
    1. questionsContent 是字符串
    2. 里面存在连续多个英文句点，例如 "......"
    3. 这些点代表 IELTS Completion 的填写空格

    如果符合这个结构，
    我们优先判断为 COMPLETION。

    如果不符合，再回到原来已经验证过的 questionType 映射。

    这样不会直接把 questionType=0 全部改成 Completion，
    可以保护已经正常工作的 TRUE_FALSE_NOT_GIVEN。
    """

    question_json = group.get("questionJson") or {}

    questions_content = question_json.get("questionsContent")

    # --------------------------------------------------------
    # Completion 检测
    #
    # 雅思哥填空题模板目前表现为：
    #
    # ......
    #
    # 即至少 5 个连续英文句点。
    #
    # 这里不解析 HTML，
    # 只判断真实原始模板中是否存在填空占位符。
    # --------------------------------------------------------
    if isinstance(questions_content, str):
        if "....." in questions_content:
            return "COMPLETION"

    # --------------------------------------------------------
    # 如果不是 Completion，
    # 则继续使用目前已有的题型映射。
    # --------------------------------------------------------
    question_type_code = group.get("questionType")

    return QUESTION_TYPE_MAP.get(
        question_type_code,
        f"YASIGE_{question_type_code}"
    )

def normalize_answer(value, question_type):
    """
    把雅思哥答案转换成我们项目更容易使用的格式。

    对于单选题：
    雅思哥使用 0 / 1 / 2 / 3 表示第几个选项。

    我们转换成：
    0 -> A
    1 -> B
    2 -> C
    3 -> D

    其他题型暂时保持原值。
    """

    if value is None:
        return None

    if question_type == "MULTIPLE_CHOICE":
        option_letters = ["A", "B", "C", "D", "E", "F"]

        if isinstance(value, int):
            if 0 <= value < len(option_letters):
                return option_letters[value]

    return str(value)

def build_group_options(question_json):
    """
    提取 QuestionGroup 级别共享选项。

    当前只处理 Matching。

    原因：
    Matching 往往是一整个题组共享同一组选项。

    Multiple Choice 不在这里处理，
    因为 Multiple Choice 通常每一道题都有不同的 A/B/C/D，
    应该保存到 ReadingQuestion.options。
    """

    result = []

    match_options = question_json.get("matchOptions")

    if not isinstance(match_options, list):
        return result

    for index, option in enumerate(match_options, start=1):

        if not isinstance(option, dict):
            continue

        result.append({
            "optionValue": str(option.get("index", "")),
            "optionText": option.get("content", ""),
            "displayOrder": index,
        })

    return result


def extract_question_text(question_json, local_index):
    """
    尝试从 questionJson 中提取单题题干。

    雅思哥存在至少两种结构：

    1.
    questionJson.questions

    2.
    questionJson.questionsContent

    questionsContent 往往是一整块 HTML，
    所以第一版优先使用 questions[]。
    """

    questions = question_json.get("questions")

    if isinstance(questions, list):
        if local_index < len(questions):

            item = questions[local_index]

            if isinstance(item, dict):
                return item.get("content")

    # 如果没有拆分好的 questions，
    # 第一版先保留整个 questionsContent。
    return question_json.get("questionsContent")


def build_questions(group):
    """
    把一个雅思哥题组转换成
    ReadingQuestionImportDto 列表。
    """

    question_json = group.get("questionJson") or {}
    answer_json = group.get("answerJson") or []

    start_index = question_json.get("startIndex")

    if start_index is None:
        return []

    question_count = group.get("questionCount")

    if question_count is None:
        question_count = question_json.get("questionNum", 0)

    # ------------------------------------------------------------
    # 不再直接根据雅思哥数字 questionType 判断。
    #
    # Completion 需要结合 questionsContent 的真实结构判断。
    # 其他题型仍然走原来的映射。
    # ------------------------------------------------------------
    question_type = detect_question_type(group)

    result = []

    for local_index in range(question_count):

        question_number = start_index + local_index

        answer_item = {}

        if local_index < len(answer_json):
            possible_answer = answer_json[local_index]

            if isinstance(possible_answer, dict):
                answer_item = possible_answer

        question_text = extract_question_text(
            question_json,
            local_index
        )

        # ====================================================
        # 提取“当前这一道题自己的选项”
        #
        # 雅思哥 Multiple Choice：
        #
        # questionJson.questions[i].options
        #
        # 转成 ReadingQuestionImportDto.options：
        #
        # [
        #     "Option A",
        #     "Option B",
        #     "Option C",
        #     "Option D"
        # ]
        # ====================================================

        question_options = []

        source_questions = question_json.get("questions")

        if (
            isinstance(source_questions, list)
            and local_index < len(source_questions)
        ):
            source_question = source_questions[local_index]

            if isinstance(source_question, dict):

                source_options = source_question.get("options")

                if isinstance(source_options, list):

                    for option in source_options:

                        if not isinstance(option, dict):
                            continue

                        option_content = option.get("content")

                        if option_content is not None:
                            question_options.append(
                                str(option_content)
                            )

        result.append({
            "questionNumber": question_number,
            "questionType": question_type,
            "questionText": question_text,

            # 当前这道题自己的独立选项。
            "options": question_options,

            "correctAnswer": normalize_answer(
                answer_item.get("correctValue"),
                question_type
            ),

            # 题目解析。
            "explanation": answer_item.get("explain"),

            # 原文答案定位 / 高亮信息。
            #
            # 后续数据链路：
            #
            # articleSourceHighlight
            #   ↓
            # ReadingQuestionImportDto.answerHighlight
            #   ↓
            # ReadingImportService
            #   ↓
            # reading_question.answer_highlight_json
            #
            # 前端后续可以利用这些数据实现：
            # - 答案句高亮
            # - 原文定位
            # - Review 页面跳到对应位置
            "answerHighlight": answer_item.get(
                "articleSourceHighlight"
            ),
        })

    return result


def build_question_group(group):
    """
    转换一个雅思哥题组。
    """

    question_json = group.get("questionJson") or {}

    # QuestionGroup 和里面的 ReadingQuestion
    # 必须使用完全相同的题型判断逻辑。
    #
    # 否则可能出现：
    #
    # QuestionGroup = TRUE_FALSE_NOT_GIVEN
    # ReadingQuestion = COMPLETION
    #
    # 这种前后不一致的数据。
    question_type = detect_question_type(group)

    return {
        "questionType": question_type,

        # descriptions 是雅思哥题组说明。
        "instruction": question_json.get("descriptions"),

        # 第一版默认 false。
        # 等真实数据明确存在“选项允许重复”规则后再判断。
        "allowOptionReuse": False,

        "options": build_group_options(
            question_json
        ),

        "questions": build_questions(
            group
        ),
    }


def build_passage(passage):
    """
    转换一个 Reading Passage。
    """

    question_groups = []

    for group in passage.get("questions", []):

        if not isinstance(group, dict):
            continue

        question_groups.append(
            build_question_group(group)
        )

    return {
        "passageNumber": passage.get("part"),

        "title": passage.get("passagesQuestion"),

        "instruction": None,

        # 英文原文
        "content": passage.get("passagesContent"),

        # 中文译文
        # 对应雅思哥 Response 中的 articleTranslation
        "translation": passage.get("articleTranslation"),

        "questionGroups": question_groups,
    }


def main():

    if not INPUT_FILE.exists():
        print("找不到雅思哥 Response 文件：")
        print(INPUT_FILE)
        return

    # ========================================================
    # 1. 读取真实 Response
    # ========================================================

    with INPUT_FILE.open(
        "r",
        encoding="utf-8"
    ) as file:
        source_data = json.load(file)

    content = source_data.get("content")

    if not isinstance(content, list):
        print("content 不是 list，无法转换。")
        return

    # ========================================================
    # 2. 转换 3 个 Passage
    # ========================================================

    passages = []

    for passage in content:

        if not isinstance(passage, dict):
            continue

        passages.append(
            build_passage(passage)
        )

    # ========================================================
    # 3. 生成 ReadingImportDto 对应 JSON
    # ========================================================

    result = {
        # 直接使用真实 testPaperId，
        # 可以帮助避免重复导入。
        "externalId":
            "yasige-4202607160914251713",

        # 当前 Response 没有整套试卷标题，
        # 第一版先使用可识别名称。
        "title":
            "Yasige Reading 4202607160914251713",

        "source":
            "IELTSBro",

        "passages":
            passages,
    }

    # ========================================================
    # 4. 写入文件
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

    print("转换完成。")
    print()
    print("输出文件：")
    print(OUTPUT_FILE)

    print()
    print("Passage 数量：")
    print(len(passages))

    total_groups = sum(
        len(passage.get("questionGroups", []))
        for passage in passages
    )

    total_questions = sum(
        len(group.get("questions", []))
        for passage in passages
        for group in passage.get("questionGroups", [])
    )

    print("QuestionGroup 数量：")
    print(total_groups)

    print("Question 数量：")
    print(total_questions)


if __name__ == "__main__":
    main()