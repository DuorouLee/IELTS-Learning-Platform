import json
from pathlib import Path


# ============================================================
# 雅思哥真实接口响应文件
# ============================================================

SOURCE_FILE = Path(
    r"C:\Users\21931\Desktop\temp\examPassagesV2_real_response.json"
)


def shorten(value, max_length=800):
    """
    防止 HTML / JSON 内容太长把终端刷爆。
    """

    if value is None:
        return None

    text = str(value)

    if len(text) <= max_length:
        return text

    return text[:max_length] + "...[省略]"


def parse_question_json(group):
    """
    questionJson 正常情况下已经是 dict。

    但为了以后兼容其他接口数据，
    如果它是字符串，也尝试解析成 JSON。
    """

    question_json = group.get("questionJson")

    if isinstance(question_json, dict):
        return question_json

    if isinstance(question_json, str):
        try:
            return json.loads(question_json)
        except json.JSONDecodeError:
            return {}

    return {}


def print_group(
        passage_number,
        group_number,
        group
):
    """
    打印一个真实 QuestionGroup 的关键结构。
    """

    question_json = parse_question_json(group)

    questions = question_json.get("questions")
    match_options = question_json.get("matchOptions")

    print()
    print("=" * 100)

    print(
        f"Passage {passage_number} "
        f"/ Group {group_number}"
    )

    print("=" * 100)

    # --------------------------------------------------------
    # 最关键的题组识别字段
    # --------------------------------------------------------

    print(
        "questionNavigation:",
        group.get("questionNavigation")
    )

    print(
        "questionCount:",
        group.get("questionCount")
    )

    print(
        "questionType:",
        group.get("questionType")
    )

    print(
        "firstQuestionType:",
        group.get("firstQuestionType")
    )

    print(
        "secondQuestionType:",
        group.get("secondQuestionType")
    )

    print()

    # --------------------------------------------------------
    # questionJson 内部字段
    # --------------------------------------------------------

    print(
        "questionJson.questionType:",
        question_json.get("questionType")
    )

    print(
        "startIndex:",
        question_json.get("startIndex")
    )

    print(
        "endIndex:",
        question_json.get("endIndex")
    )

    print(
        "questionNum:",
        question_json.get("questionNum")
    )

    print(
        "viewMode:",
        question_json.get("viewMode")
    )

    print(
        "indexType:",
        question_json.get("indexType")
    )

    print(
        "optionsStyleType:",
        question_json.get("optionsStyleType")
    )

    print(
        "showIndex:",
        question_json.get("showIndex")
    )

    print()

    # --------------------------------------------------------
    # IELTS 题目说明
    # --------------------------------------------------------

    print("descriptions:")
    print(
        shorten(
            question_json.get("descriptions")
        )
    )

    print()

    # --------------------------------------------------------
    # questionsContent
    # --------------------------------------------------------

    print("questionsContent:")
    print(
        shorten(
            question_json.get("questionsContent"),
            1200
        )
    )

    print()

    # --------------------------------------------------------
    # questions[]
    #
    # Multiple Choice 等题型通常会有这个数组。
    # --------------------------------------------------------

    if isinstance(questions, list):

        print(
            f"questions[]: YES, count={len(questions)}"
        )

        for index, question in enumerate(
                questions,
                start=1
        ):
            print()
            print(f"  question[{index}]")

            if not isinstance(question, dict):
                print("   ", question)
                continue

            print(
                "    number:",
                question.get("number")
            )

            print(
                "    content:",
                shorten(
                    question.get("content"),
                    300
                )
            )

            options = question.get("options")

            if isinstance(options, list):

                print(
                    f"    options count={len(options)}"
                )

                for option_index, option in enumerate(
                        options,
                        start=1
                ):
                    print(
                        f"      option {option_index}:",
                        option
                    )

            else:
                print("    options: NONE")

    else:

        print("questions[]: NO")

    print()

    # --------------------------------------------------------
    # matchOptions
    #
    # Matching 类型非常关键。
    # --------------------------------------------------------

    if isinstance(match_options, list):

        print(
            f"matchOptions: YES, count={len(match_options)}"
        )

        for option in match_options:
            print(
                "   ",
                option
            )

    else:

        print("matchOptions: NO")

    print()

    # --------------------------------------------------------
    # answerJson
    #
    # 这里只打印答案类型，
    # 不打印长 explanation。
    # --------------------------------------------------------

    answers = group.get("answerJson")

    if isinstance(answers, list):

        print(
            f"answerJson count={len(answers)}"
        )

        for index, answer in enumerate(
                answers,
                start=1
        ):

            if not isinstance(answer, dict):
                continue

            correct_value = answer.get(
                "correctValue"
            )

            print(
                f"  answer {index}: "
                f"{repr(correct_value)} "
                f"(type={type(correct_value).__name__})"
            )

    else:

        print("answerJson: NONE")


def main():

    if not SOURCE_FILE.exists():

        raise FileNotFoundError(
            f"找不到文件：\n{SOURCE_FILE}"
        )


    with SOURCE_FILE.open(
            "r",
            encoding="utf-8"
    ) as file:

        data = json.load(file)


    # ========================================================
    # 真实接口结构：
    #
    # root
    #   ↓
    # content
    #   ↓
    # Passage[]
    # ========================================================

    passages = data.get("content")

    if not isinstance(passages, list):

        raise ValueError(
            "原始 JSON 中 content 不是 list"
        )


    print()
    print(
        f"找到 Passage 数量：{len(passages)}"
    )


    total_groups = 0


    # ========================================================
    # 每个 Passage 中：
    #
    # passage["questions"]
    #
    # 就是 QuestionGroup 列表。
    # ========================================================

    for passage_index, passage in enumerate(
            passages,
            start=1
    ):

        groups = passage.get("questions")

        if not isinstance(groups, list):
            groups = []


        print()
        print("#" * 100)

        print(
            f"Passage {passage_index} "
            f"QuestionGroup 数量：{len(groups)}"
        )

        print("#" * 100)


        for group_index, group in enumerate(
                groups,
                start=1
        ):

            total_groups += 1

            print_group(
                passage_index,
                group_index,
                group
            )


    print()
    print("=" * 100)

    print("检查完成")

    print(
        f"Passage 数量：{len(passages)}"
    )

    print(
        f"QuestionGroup 总数量：{total_groups}"
    )

    print("=" * 100)


if __name__ == "__main__":
    main()