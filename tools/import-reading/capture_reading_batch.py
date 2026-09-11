import json
import re
import time
import urllib.request
from pathlib import Path
from urllib.parse import quote, urlsplit

import websocket


# ============================================================
# 配置
# ============================================================

DEBUGGER_URL = "http://127.0.0.1:9222/json"

# 真实题库列表接口名称。
LIST_API_KEYWORD = "lv2"

# 已经通过单套 Reading 验证成功的接口。
READING_API_BASE = (
    "https://hcp-server.ieltsbro.com/"
    "hcp/qsBank/passages/"
    "examPassagesV2"
)

# 两套题之间稍微等待一下。
REQUEST_INTERVAL_SECONDS = 0.4

# 已存在时是否覆盖。
OVERWRITE_EXISTING = False


# ============================================================
# 项目目录
# ============================================================

SCRIPT_FILE = Path(__file__).resolve()
PROJECT_ROOT = SCRIPT_FILE.parents[2]

RAW_OUTPUT_DIR = (
    PROJECT_ROOT
    / "backend"
    / "src"
    / "main"
    / "resources"
    / "data"
    / "reading"
    / "raw"
)

SUMMARY_FILE = (
    RAW_OUTPUT_DIR
    / "capture-summary.json"
)


# ============================================================
# CDP 消息 ID
# ============================================================

message_id = 0


def next_id():
    """
    每个 CDP 命令都需要唯一 id。
    """
    global message_id
    message_id += 1
    return message_id


def send_command(ws, method, params=None):
    """
    发送 CDP 命令，并等待这个命令对应的响应。
    """

    command_id = next_id()

    message = {
        "id": command_id,
        "method": method,
    }

    if params is not None:
        message["params"] = params

    ws.send(json.dumps(message))

    while True:
        raw = ws.recv()
        response = json.loads(raw)

        if response.get("id") == command_id:
            return response


def send_command_without_wait(
    ws,
    method,
    params=None,
):
    """
    只发送命令，不立即等待响应。

    Page.reload 后要马上监听 Network 事件，
    所以刷新页面时使用这个函数。
    """

    command_id = next_id()

    message = {
        "id": command_id,
        "method": method,
    }

    if params is not None:
        message["params"] = params

    ws.send(json.dumps(message))

    return command_id


def evaluate_javascript(
    ws,
    javascript,
):
    """
    在当前页面中执行 JavaScript。
    """

    result = send_command(
        ws,
        "Runtime.evaluate",
        {
            "expression": javascript,
            "awaitPromise": True,
            "returnByValue": True,
        },
    )

    runtime_result = result.get(
        "result",
        {},
    )

    if "exceptionDetails" in runtime_result:
        raise RuntimeError(
            json.dumps(
                runtime_result[
                    "exceptionDetails"
                ],
                ensure_ascii=False,
                indent=2,
            )
        )

    remote_object = runtime_result.get(
        "result",
        {},
    )

    return remote_object.get(
        "value"
    )


# ============================================================
# 浏览器页面
# ============================================================

def get_debug_targets():
    """
    获取当前 9222 下全部页面。
    """

    with urllib.request.urlopen(
        DEBUGGER_URL
    ) as response:

        return json.loads(
            response.read().decode(
                "utf-8"
            )
        )


def choose_target(targets):
    """
    选择题库列表页面。
    """

    candidates = []

    for target in targets:

        if target.get("type") != "page":
            continue

        url = str(
            target.get(
                "url",
                "",
            )
        )

        if not (
            url.startswith("http://")
            or url.startswith("https://")
        ):
            continue

        if not target.get(
            "webSocketDebuggerUrl"
        ):
            continue

        candidates.append(target)

    if not candidates:
        return None

    if len(candidates) == 1:
        return candidates[0]

    print(
        "检测到多个网页，请选择显示全部 Reading Test 的页面："
    )
    print()

    for index, target in enumerate(
        candidates,
        start=1,
    ):
        print(
            f"[{index}] "
            f"{target.get('title', '')}"
        )
        print(
            f"    {target.get('url', '')}"
        )

    print()

    while True:
        raw_value = input(
            "请输入页面编号："
        ).strip()

        try:
            selected_index = int(
                raw_value
            )
        except ValueError:
            print("请输入数字。")
            continue

        if (
            1
            <= selected_index
            <= len(candidates)
        ):
            return candidates[
                selected_index - 1
            ]

        print("编号超出范围。")


# ============================================================
# 监听真实 lv2 Response
# ============================================================

def capture_paper_list_response(ws):
    """
    监听浏览器真实发送的 lv2 请求，
    然后直接读取它已经收到的 Response Body。

    这样不需要重新 fetch lv2，
    也不需要猜它原来的 method / payload。
    """

    # 开启 Network 监听。
    send_command(
        ws,
        "Network.enable",
        {
            "maxTotalBufferSize":
                100 * 1024 * 1024,

            "maxResourceBufferSize":
                50 * 1024 * 1024,

            "maxPostDataSize":
                10 * 1024 * 1024,
        },
    )

    print(
        "正在刷新当前题库列表页..."
    )
    print()

    send_command_without_wait(
        ws,
        "Page.reload",
        {
            "ignoreCache": True
        },
    )

    target_request_id = None
    target_request_url = None

    print(
        "正在等待真实 lv2 请求..."
    )
    print()

    while True:

        raw = ws.recv()
        message = json.loads(raw)

        method = message.get(
            "method"
        )

        # ----------------------------------------------------
        # 找到 lv2 请求
        # ----------------------------------------------------

        if method == "Network.requestWillBeSent":

            params = message.get(
                "params",
                {},
            )

            request = params.get(
                "request",
                {},
            )

            url = str(
                request.get(
                    "url",
                    "",
                )
            )

            http_method = str(
                request.get(
                    "method",
                    "",
                )
            )

            if (
                LIST_API_KEYWORD in url
                and http_method != "OPTIONS"
            ):
                target_request_id = (
                    params.get(
                        "requestId"
                    )
                )

                target_request_url = url

                print(
                    "找到真实题库列表请求："
                )
                print(
                    f"{http_method} {url}"
                )
                print()

        # ----------------------------------------------------
        # 等 lv2 请求完整加载
        # ----------------------------------------------------

        elif method == "Network.loadingFinished":

            params = message.get(
                "params",
                {},
            )

            request_id = params.get(
                "requestId"
            )

            if (
                target_request_id
                and request_id
                == target_request_id
            ):
                print(
                    "题库列表 Response 已加载完成。"
                )
                print()
                break

        # ----------------------------------------------------
        # lv2 请求失败
        # ----------------------------------------------------

        elif method == "Network.loadingFailed":

            params = message.get(
                "params",
                {},
            )

            request_id = params.get(
                "requestId"
            )

            if (
                target_request_id
                and request_id
                == target_request_id
            ):
                raise RuntimeError(
                    "题库列表请求失败："
                    + str(
                        params.get(
                            "errorText"
                        )
                    )
                )

    if not target_request_id:
        raise RuntimeError(
            "没有捕获到 lv2 请求。"
        )

    # --------------------------------------------------------
    # 直接读取浏览器刚刚收到的 Response Body
    # --------------------------------------------------------

    body_result = send_command(
        ws,
        "Network.getResponseBody",
        {
            "requestId":
                target_request_id
        },
    )

    if "error" in body_result:
        raise RuntimeError(
            "读取 lv2 Response Body 失败："
            + str(
                body_result[
                    "error"
                ]
            )
        )

    result = body_result.get(
        "result",
        {},
    )

    body = result.get(
        "body",
        "",
    )

    if result.get(
        "base64Encoded",
        False,
    ):
        import base64

        body = base64.b64decode(
            body
        ).decode(
            "utf-8",
            errors="replace",
        )

    try:
        response_data = json.loads(
            body
        )

    except json.JSONDecodeError as error:
        raise RuntimeError(
            "lv2 Response Body 不是有效 JSON。"
        ) from error

    paper_list = (
        response_data
        .get("content", {})
        .get("paperList")
    )

    if not isinstance(
        paper_list,
        list,
    ):
        raise RuntimeError(
            "lv2 Response 中没有找到 "
            "content.paperList。"
        )

    return {
        "requestUrl":
            target_request_url,

        "responseData":
            response_data,
    }


# ============================================================
# 整理 Test 列表
# ============================================================

def flatten_papers(response_data):
    """
    把 paperList 展平：

        C21
          Test 1
          Test 2

    变成：

        [
          {
            topicName: C21,
            paperName: Test 1,
            paperId: ...
          }
        ]
    """

    paper_list = (
        response_data
        .get("content", {})
        .get("paperList", [])
    )

    result = []

    for topic in paper_list:

        if not isinstance(
            topic,
            dict,
        ):
            continue

        topic_name = str(
            topic.get(
                "topicName",
                "",
            )
        ).strip()

        test_papers = topic.get(
            "testPapers"
        )

        if not isinstance(
            test_papers,
            list,
        ):
            continue

        for paper in test_papers:

            if not isinstance(
                paper,
                dict,
            ):
                continue

            paper_id = paper.get(
                "paperId"
            )

            paper_name = str(
                paper.get(
                    "paperName",
                    "",
                )
            ).strip()

            if paper_id is None:
                continue

            result.append({
                "topicName":
                    topic_name,

                "paperName":
                    paper_name,

                "paperId":
                    str(paper_id),
            })

    return result


# ============================================================
# 自动命名
# ============================================================

def build_display_name(
    topic_name,
    paper_name,
):
    """
    C21 + Test 1
    ->
    C21 Test 1
    """

    return (
        f"{topic_name} {paper_name}"
        .strip()
    )


def build_file_stem(
    topic_name,
    paper_name,
):
    """
    C21 + Test 1
    ->
    c21-test-1
    """

    raw_name = (
        f"{topic_name}-{paper_name}"
        .lower()
        .strip()
    )

    file_stem = re.sub(
        r"[^a-z0-9]+",
        "-",
        raw_name,
    )

    return file_stem.strip("-")


# ============================================================
# Reading 详情接口
# ============================================================

def build_reading_api_url(
    list_request_url,
    paper_id,
):
    """
    根据 paperId 构造 Reading 详情接口。

    list_request_url 目前保留这个参数，
    是为了尽量少改现有调用代码。

    Reading API 的地址已经通过之前
    C21 Test 1 的真实抓取验证成功，
    所以这里直接使用固定的 Reading API。
    """

    return (
        READING_API_BASE
        + "?testPaperId="
        + quote(
            str(paper_id),
            safe="",
        )
    )


def fetch_reading_response(
    ws,
    api_url,
):
    """
    让当前登录页面自己请求 Reading 数据。
    """

    javascript = f"""
    (async () => {{
        try {{
            const response = await fetch(
                {json.dumps(api_url)},
                {{
                    method: 'GET',
                    credentials: 'include'
                }}
            );

            const text =
                await response.text();

            return {{
                ok: response.ok,
                status: response.status,
                body: text
            }};

        }} catch (error) {{
            return {{
                ok: false,
                status: 0,
                error: String(error),
                body: ''
            }};
        }}
    }})()
    """

    return evaluate_javascript(
        ws,
        javascript,
    )


def validate_reading_data(data):
    """
    抓取阶段只做基础检查。
    """

    if not isinstance(
        data,
        dict,
    ):
        return (
            False,
            "最外层不是 JSON Object",
        )

    content = data.get(
        "content"
    )

    if not isinstance(
        content,
        list,
    ):
        return (
            False,
            "content 不是 list",
        )

    if not content:
        return (
            False,
            "content 是空列表",
        )

    return (
        True,
        None,
    )


# ============================================================
# 保存
# ============================================================

def ensure_output_directory():
    """
    确保 raw 目录存在。
    """

    RAW_OUTPUT_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )


def save_json(
    file_path,
    value,
):
    """
    保存 JSON。
    """

    with file_path.open(
        "w",
        encoding="utf-8",
    ) as file:

        json.dump(
            value,
            file,
            ensure_ascii=False,
            indent=2,
        )


def save_one_test(
    paper,
    parsed_json,
):
    """
    保存：

        c21-test-1.json
        c21-test-1.meta.json
    """

    display_name = build_display_name(
        paper["topicName"],
        paper["paperName"],
    )

    file_stem = build_file_stem(
        paper["topicName"],
        paper["paperName"],
    )

    raw_file = (
        RAW_OUTPUT_DIR
        / f"{file_stem}.json"
    )

    metadata_file = (
        RAW_OUTPUT_DIR
        / f"{file_stem}.meta.json"
    )

    save_json(
        raw_file,
        parsed_json,
    )

    metadata = {
        "displayName":
            display_name,

        "topicName":
            paper["topicName"],

        "paperName":
            paper["paperName"],

        "externalPaperId":
            paper["paperId"],

        "rawFile":
            raw_file.name,
    }

    save_json(
        metadata_file,
        metadata,
    )

    return (
        raw_file,
        metadata_file,
    )


# ============================================================
# 主程序
# ============================================================

def main():

    print()
    print(
        "Reading Batch Capture"
    )
    print(
        "=" * 60
    )
    print()

    ensure_output_directory()

    # --------------------------------------------------------
    # 1. 找浏览器页面
    # --------------------------------------------------------

    try:
        targets = get_debug_targets()

    except Exception as error:

        print(
            "无法连接 Remote Debugging。"
        )
        print()
        print(error)
        return

    target = choose_target(
        targets
    )

    if target is None:

        print(
            "没有找到可用网页。"
        )
        return

    print()
    print(
        "当前使用页面："
    )
    print(
        target.get(
            "title",
            "",
        )
    )
    print()

    websocket_url = target.get(
        "webSocketDebuggerUrl"
    )

    try:
        ws = websocket.create_connection(
            websocket_url,
            suppress_origin=True,
            timeout=90,
        )

    except Exception as error:

        print(
            "连接当前页面失败："
        )
        print(error)
        return

    try:

        send_command(
            ws,
            "Runtime.enable",
        )

        # ----------------------------------------------------
        # 2. 捕获真实 lv2 Response
        # ----------------------------------------------------

        try:
            list_result = (
                capture_paper_list_response(
                    ws
                )
            )

        except Exception as error:

            print(
                "读取题库列表失败："
            )
            print(error)
            return

        response_data = (
            list_result[
                "responseData"
            ]
        )

        list_request_url = (
            list_result[
                "requestUrl"
            ]
        )

        papers = flatten_papers(
            response_data
        )

        if not papers:

            print(
                "paperList 中没有找到 Test。"
            )
            return

        print(
            f"找到 Test 数量：{len(papers)}"
        )
        print()

        # ----------------------------------------------------
        # 3. 批量抓取
        # ----------------------------------------------------

        success_items = []
        skipped_items = []
        failed_items = []

        total = len(papers)

        for index, paper in enumerate(
            papers,
            start=1,
        ):

            display_name = (
                build_display_name(
                    paper["topicName"],
                    paper["paperName"],
                )
            )

            file_stem = (
                build_file_stem(
                    paper["topicName"],
                    paper["paperName"],
                )
            )

            raw_file = (
                RAW_OUTPUT_DIR
                / f"{file_stem}.json"
            )

            metadata_file = (
                RAW_OUTPUT_DIR
                / f"{file_stem}.meta.json"
            )

            print(
                f"[{index}/{total}] "
                f"{display_name}"
            )

            if (
                not OVERWRITE_EXISTING
                and raw_file.exists()
                and metadata_file.exists()
            ):
                print(
                    "  已存在，跳过。"
                )

                skipped_items.append({
                    "displayName":
                        display_name,

                    "paperId":
                        paper["paperId"],
                })

                continue

            api_url = (
                build_reading_api_url(
                    list_request_url,
                    paper["paperId"],
                )
            )

            try:
                fetch_result = (
                    fetch_reading_response(
                        ws,
                        api_url,
                    )
                )

            except Exception as error:

                reason = str(error)

                print(
                    "  抓取失败："
                    + reason
                )

                failed_items.append({
                    "displayName":
                        display_name,

                    "paperId":
                        paper["paperId"],

                    "reason":
                        reason,
                })

                continue

            if not isinstance(
                fetch_result,
                dict,
            ):

                reason = (
                    "浏览器没有返回有效结果"
                )

                print(
                    "  抓取失败："
                    + reason
                )

                failed_items.append({
                    "displayName":
                        display_name,

                    "paperId":
                        paper["paperId"],

                    "reason":
                        reason,
                })

                continue

            if not fetch_result.get(
                "ok"
            ):

                reason = (
                    "HTTP "
                    + str(
                        fetch_result.get(
                            "status"
                        )
                    )
                )

                if fetch_result.get(
                    "error"
                ):
                    reason += (
                        " / "
                        + str(
                            fetch_result.get(
                                "error"
                            )
                        )
                    )

                print(
                    "  抓取失败："
                    + reason
                )

                failed_items.append({
                    "displayName":
                        display_name,

                    "paperId":
                        paper["paperId"],

                    "reason":
                        reason,
                })

                continue

            body = fetch_result.get(
                "body",
                "",
            )

            try:
                parsed_json = json.loads(
                    body
                )

            except json.JSONDecodeError:

                reason = (
                    "Response Body 不是有效 JSON"
                )

                print(
                    "  抓取失败："
                    + reason
                )

                failed_items.append({
                    "displayName":
                        display_name,

                    "paperId":
                        paper["paperId"],

                    "reason":
                        reason,
                })

                continue

            valid, error_message = (
                validate_reading_data(
                    parsed_json
                )
            )

            if not valid:

                print(
                    "  数据异常："
                    + str(
                        error_message
                    )
                )

                failed_items.append({
                    "displayName":
                        display_name,

                    "paperId":
                        paper["paperId"],

                    "reason":
                        str(
                            error_message
                        ),
                })

                continue

            try:
                (
                    saved_raw_file,
                    saved_metadata_file,
                ) = save_one_test(
                    paper,
                    parsed_json,
                )

            except Exception as error:

                reason = (
                    "保存文件失败："
                    + str(error)
                )

                print(
                    "  "
                    + reason
                )

                failed_items.append({
                    "displayName":
                        display_name,

                    "paperId":
                        paper["paperId"],

                    "reason":
                        reason,
                })

                continue

            passage_count = len(
                parsed_json.get(
                    "content",
                    [],
                )
            )

            print(
                f"  成功，Passage："
                f"{passage_count}"
            )

            print(
                f"  {saved_raw_file.name}"
            )

            success_items.append({
                "displayName":
                    display_name,

                "paperId":
                    paper["paperId"],

                "rawFile":
                    saved_raw_file.name,

                "metadataFile":
                    saved_metadata_file.name,

                "passageCount":
                    passage_count,
            })

            time.sleep(
                REQUEST_INTERVAL_SECONDS
            )

        # ----------------------------------------------------
        # 4. 保存汇总
        # ----------------------------------------------------

        summary = {
            "total":
                total,

            "successCount":
                len(success_items),

            "skippedCount":
                len(skipped_items),

            "failedCount":
                len(failed_items),

            "success":
                success_items,

            "skipped":
                skipped_items,

            "failed":
                failed_items,
        }

        save_json(
            SUMMARY_FILE,
            summary,
        )

        # ----------------------------------------------------
        # 5. 输出结果
        # ----------------------------------------------------

        print()
        print(
            "=" * 60
        )

        print(
            "批量抓取完成。"
        )
        print()

        print(
            f"总数：{total}"
        )

        print(
            f"成功：{len(success_items)}"
        )

        print(
            f"跳过：{len(skipped_items)}"
        )

        print(
            f"失败：{len(failed_items)}"
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
                        "displayName"
                    ]
                    + ": "
                    + item[
                        "reason"
                    ]
                )

    finally:
        ws.close()


if __name__ == "__main__":
    main()
