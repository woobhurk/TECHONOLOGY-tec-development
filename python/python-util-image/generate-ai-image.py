#!/usr/bin/env python3
"""
Right Code 图片生成脚本
基于 /v1/images/generations 接口
交互式输入提示词、分辨率、模型，生成图片并下载到本地
"""

import json
import os
import sys
import urllib.request
import urllib.error
from datetime import datetime
from pathlib import Path

# ── 配置 ──────────────────────────────────────────────
BASE_URL = "https://www.right.codes/draw/v1/images/generations"
# 优先从环境变量读取 API Key，也可直接写在这里
API_KEY = os.environ.get("RIGHT_CODES_API_KEY", "")

# 预设分辨率
SIZE_PRESETS = {
    "1": "1024x1024",
    "2": "2048x2048",
    "3": "4096x4096",
    "4": "custom",
}

# 预设模型
MODEL_PRESETS = {
    "1": "gpt-image-2",
    "2": "gpt-image-2-vip",
    "3": "nano-banana-2",
    "4": "nano-banana-pro",
}

# 下载目录
DOWNLOAD_DIR = Path(__file__).parent / "generated_images"


# ── 工具函数 ──────────────────────────────────────────
def prompt_input(label: str, default: str = "") -> str:
    hint = f" [{default}]" if default else ""
    value = input(f"{label}{hint}: ").strip()
    return value or default


def prompt_choice(label: str, options: dict[str, str]) -> str:
    print(f"\n{label}")
    for key, val in options.items():
        print(f"  {key}. {val}")
    while True:
        choice = input("请选择 (输入编号): ").strip()
        if choice in options:
            return options[choice]
        print(f"  ❌ 无效选项，请重新输入")


def prompt_custom_size() -> str:
    while True:
        raw = input("请输入自定义分辨率 (如 1536x1024): ").strip().lower()
        if "x" in raw:
            parts = raw.split("x")
            if len(parts) == 2 and all(p.isdigit() for p in parts):
                return raw
        print("  ❌ 格式错误，请使用 WxH 格式，如 1536x1024")


def generate_image(api_key: str, model: str, prompt: str, size: str) -> dict:
    payload = json.dumps({
        "model": model,
        "prompt": prompt,
        "size": size,
        "response_format": "url",
    }).encode("utf-8")

    req = urllib.request.Request(
        BASE_URL,
        data=payload,
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {api_key}",
        },
        method="POST",
    )

    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        print(f"\n❌ API 请求失败 (HTTP {e.code})")
        print(f"   响应: {body}")
        sys.exit(1)
    except urllib.error.URLError as e:
        print(f"\n❌ 网络错误: {e.reason}")
        sys.exit(1)


def download_image(url: str, dest: Path) -> Path:
    dest.parent.mkdir(parents=True, exist_ok=True)
    print(f"  ⬇ 下载中...")
    urllib.request.urlretrieve(url, dest)
    return dest


def main():
    print("=" * 52)
    print("  Right Code 图片生成工具")
    print("=" * 52)

    # ── API Key ──
    api_key = API_KEY
    if not api_key:
        api_key = input("\n请输入 API Key (sk-xxxxx): ").strip()
    if not api_key:
        print("❌ API Key 不能为空")
        sys.exit(1)

    # ── 提示词 ──
    prompt = input("\n请输入提示词: ").strip()
    if not prompt:
        print("❌ 提示词不能为空")
        sys.exit(1)

    # ── 分辨率 ──
    size_choice = prompt_choice("选择分辨率:", SIZE_PRESETS)
    if size_choice == "custom":
        size = prompt_custom_size()
    else:
        size = size_choice

    # ── 模型 ──
    model = prompt_choice("选择模型:", MODEL_PRESETS)

    # ── 确认 ──
    print(f"\n{'─' * 40}")
    print(f"  提示词 : {prompt}")
    print(f"  分辨率 : {size}")
    print(f"  模型   : {model}")
    print(f"{'─' * 40}")

    confirm = input("确认生成? (Y/n): ").strip().lower()
    if confirm in ("n", "no"):
        print("已取消")
        sys.exit(0)

    # ── 调用 API ──
    print("\n🎨 生成中，请稍候...")
    result = generate_image(api_key, model, prompt, size)

    # ── 解析结果 ──
    images = result.get("data", [])
    if not images:
        print("❌ 未返回图片数据")
        sys.exit(1)

    usage = result.get("usage", {})
    print(f"✅ 生成成功！共 {len(images)} 张图片")
    if usage:
        print(f"   Token 用量 — 输入: {usage.get('input_tokens', '?')}  输出: {usage.get('output_tokens', '?')}  合计: {usage.get('total_tokens', '?')}")

    # ── 下载 ──
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    downloaded = []
    for i, img in enumerate(images):
        url = img.get("url")
        if not url:
            print(f"  ⚠ 第 {i+1} 张无 URL，跳过")
            continue

        # 从 URL 推断扩展名
        ext = Path(url.split("?")[0]).suffix or ".png"
        filename = f"{timestamp}_{i+1}{ext}"
        dest = DOWNLOAD_DIR / filename

        print(f"\n📷 图片 {i+1}:")
        print(f"   URL: {url}")
        download_image(url, dest)
        print(f"   ✅ 已保存: {dest}")
        downloaded.append(dest)

    if downloaded:
        print(f"\n🎉 全部完成！{len(downloaded)} 张图片保存在: {DOWNLOAD_DIR}")
    else:
        print("\n⚠ 没有成功下载任何图片")


if __name__ == "__main__":
    main()
