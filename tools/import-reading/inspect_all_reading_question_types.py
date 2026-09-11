import json
from collections import Counter, defaultdict
from pathlib import Path

import reading_to_import as converter


# ============================================================
# 项目目录
# ============================================================

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

REPORT_FILE = (
    RAW_DIR
    / "reading-question-type-report.json"
)


# ============================================================
# 辅助函数
# ============================================================

def get_raw_files():
    """
    只检查这次自动抓取并自动命名的 Reading 文件：

        c21-test-1.json
        c20-test-4.json
        ...

    这样可以自动排除之前遗留的：

        reading-raw-001.json
        reading-raw-002.json

    也不会把 *.meta.json / capture-summary.json
    当成题库文件。
    """

    files = []

    for file_path in sorted(
            RAW_DIR.glob("c*-test-*.json")
    ):

        # 排除 metadata 文件：
        # c21-test-1.meta.json
        if file_path.name.endswith(
                ".meta.json"
        ):
            continue

        files.append(
            file_path
        )

    return files


def safe_text(value, max_length=500):
    """
    把较长的 HTML / 文本压缩成适合报告查看的摘要。

    报告的目的只是帮助识别题型，
    不需要把整套题全部重复写进去。
    """

    if value is None:
        return None

    text = converter.html_to_plain_text(
        value
    )

    if len(text) <= max_length:
        return text

    return (
        text[:max_length]
        + "..."
    )


def summarize_questions(
    question_json,
):
    """
    提取 questions[] 的结构摘要。

    主要关注：
    - questions 数量
    - 第一题题干
    - 第一题选项
    """

    source_questions = (
        question_json.get(
            "questions"
        )
    )

    if not isinstance(
        source_questions,
        list,
    ):
        return {
            "count": 0,
            "firstQuestionText": None,
            "firstQuestionOptions": [],
        }

    first_question_text = None
    first_question_options = []

    if source_questions:

        first = source_questions[0]

        if isinstance(
            first,
            dict,
        ):
            first_question_text = (
                safe_text(
                    first.get(
                        "content"
                    )
                )
            )

            source_options = first.get(
                "options"
            )

            if isinstance(
                source_options,
                list,
            ):
                for option in source_options:

                    if not isinstance(
                        option,
                        dict,
                    ):
                        continue

                    content = option.get(
                        "content"
                    )

                    if content is not None:
                        first_question_options.append(
                            safe_text(
                                content,
                                max_length=200,
                            )
                        )

    return {
        "count":
            len(
                source_questions
            ),

        "firstQuestionText":
            first_question_text,

        "firstQuestionOptions":
            first_question_options,
    }


def summarize_match_options(
    question_json,
):
    """
    提取 matchOptions 的简要信息。
    """

    match_options = (
        question_json.get(
            "matchOptions"
        )
    )

    if not isinstance(
        match_options,
        list,
    ):
        return {
            "count": 0,
            "examples": [],
        }

    examples = []

    for option in match_options[:5]:

        if not isinstance(
            option,
            dict,
        ):
            continue

        examples.append({
            "index":
                option.get(
                    "index"
                ),

            "content":
                safe_text(
                    option.get(
                        "content"
                    ),
                    max_length=200,
                ),
        })

    return {
        "count":
            len(
                match_options
            ),

        "examples":
            examples,
    }


def build_group_sample(
    raw_file,
    passage,
    group,
):
    """
    为一个 UNKNOWN QuestionGroup
    生成结构化样本。
    """

    question_json = (
        converter.get_question_json(
            group
        )
    )

    detected_type = (
        converter.detect_question_type(
            group
        )
    )

    start_index = (
        question_json.get(
            "startIndex"
        )
    )

    question_count = (
        group.get(
            "questionCount"
        )
    )

    if question_count is None:
        question_count = (
            question_json.get(
                "questionNum"
            )
        )

    return {
        "file":
            raw_file.name,

        "part":
            passage.get(
                "part"
            ),

        "detectedType":
            detected_type,

        "firstQuestionType":
            group.get(
                "firstQuestionType"
            ),

        "secondQuestionType":
            group.get(
                "secondQuestionType"
            ),

        "startIndex":
            start_index,

        "questionCount":
            question_count,

        "viewMode":
            question_json.get(
                "viewMode"
            ),

        "descriptions":
            safe_text(
                question_json.get(
                    "descriptions"
                )
            ),

        "questionsContent":
            safe_text(
                question_json.get(
                    "questionsContent"
                ),
                max_length=800,
            ),

        "questions":
            summarize_questions(
                question_json
            ),

        "matchOptions":
            summarize_match_options(
                question_json
            ),

        "questionJsonKeys":
            sorted(
                question_json.keys()
            ),
    }


# ============================================================
# 主分析流程
# ============================================================

def main():
    """
    扫描 68 套 Reading raw 文件，
    找出当前 converter 无法识别的题型。

    输出：

        raw/reading-question-type-report.json

    报告中会按 UNKNOWN_xxx 分组，
    并保留少量真实样本帮助我们判断语义题型。
    """

    print()
    print(
        "Reading Question Type Inspector"
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
            "没有找到 c*-test-*.json。"
        )
        return

    print(
        f"扫描 Test 数量："
        f"{len(raw_files)}"
    )
    print()

    unknown_counter = Counter()

    # 每一种 UNKNOWN 最多保留 8 个样本，
    # 足够帮助判断，不让报告过大。
    samples_by_type = defaultdict(
        list
    )

    total_groups = 0
    unknown_groups = 0

    for raw_file in raw_files:

        with raw_file.open(
            "r",
            encoding="utf-8",
        ) as file:

            source_data = json.load(
                file
            )

        content = source_data.get(
            "content"
        )

        if not isinstance(
            content,
            list,
        ):
            print(
                f"跳过结构异常文件："
                f"{raw_file.name}"
            )
            continue

        for passage in content:

            if not isinstance(
                passage,
                dict,
            ):
                continue

            source_groups = (
                passage.get(
                    "questions"
                )
            )

            if not isinstance(
                source_groups,
                list,
            ):
                continue

            for group in source_groups:

                if not isinstance(
                    group,
                    dict,
                ):
                    continue

                total_groups += 1

                detected_type = (
                    converter.detect_question_type(
                        group
                    )
                )

                if not str(
                    detected_type
                ).startswith(
                    "UNKNOWN_"
                ):
                    continue

                unknown_groups += 1

                unknown_counter[
                    detected_type
                ] += 1

                if (
                    len(
                        samples_by_type[
                            detected_type
                        ]
                    )
                    < 8
                ):
                    samples_by_type[
                        detected_type
                    ].append(
                        build_group_sample(
                            raw_file,
                            passage,
                            group,
                        )
                    )

    unknown_types = []

    for question_type, count in (
        unknown_counter.most_common()
    ):

        unknown_types.append({
            "questionType":
                question_type,

            "count":
                count,

            "samples":
                samples_by_type[
                    question_type
                ],
        })

    report = {
        "testCount":
            len(
                raw_files
            ),

        "questionGroupCount":
            total_groups,

        "unknownQuestionGroupCount":
            unknown_groups,

        "unknownTypeCount":
            len(
                unknown_counter
            ),

        "unknownTypes":
            unknown_types,
    }

    with REPORT_FILE.open(
        "w",
        encoding="utf-8",
    ) as file:

        json.dump(
            report,
            file,
            ensure_ascii=False,
            indent=2,
        )

    print(
        f"QuestionGroup 总数："
        f"{total_groups}"
    )

    print(
        f"UNKNOWN Group 数量："
        f"{unknown_groups}"
    )

    print(
        f"UNKNOWN 类型数量："
        f"{len(unknown_counter)}"
    )

    print()

    print(
        "UNKNOWN 类型统计："
    )

    for question_type, count in (
        unknown_counter.most_common()
    ):
        print(
            f"- {question_type}: "
            f"{count}"
        )

    print()
    print(
        "报告已生成："
    )
    print(
        REPORT_FILE
    )


if __name__ == "__main__":
    main()
