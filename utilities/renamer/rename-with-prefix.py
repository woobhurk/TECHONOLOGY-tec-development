#!/usr/bin/env python3
"""
批量重命名文件/文件夹：去除名称前后空格，并在最前面添加首字符的大写首字母 + 空格。

重命名规则：
    - 去除文件名/文件夹名前后的空格
    - 取名称第一个有效字符的首字母（大写），添加为前缀 + 空格
    - 中文取拼音首字母，英文取大写字母，日韩文/符号跳过
    - 若已存在该前缀则跳过，避免重复添加

用法：
    python rename-with-prefix.py -d <目录路径> [选项]

选项：
    -d, --dir       目标目录路径（必填）
    -e, --ext       处理后缀名，逗号分隔，如 txt,md,py（默认处理所有文件）
    -r, --recursive 递归处理子目录（默认仅处理顶层）
    -t, --type      处理类型：file（文件，默认）或 dir（文件夹）

示例：
    # 非递归处理目录下所有文件
    python rename-with-prefix.py -d /path/to/dir

    # 递归处理指定后缀的文件
    python rename-with-prefix.py -d /path/to/dir -e txt,md,py -r

    # 非递归处理顶层文件夹
    python rename-with-prefix.py -d /path/to/dir -t dir

    # 递归处理所有子文件夹
    python rename-with-prefix.py -d /path/to/dir -t dir -r

注意：
    - 处理文件夹（--type dir）时不支持 --ext 参数
    - 需要 pypinyin 库支持中文：pip install pypinyin
"""

import argparse
import os
import sys
import unicodedata

try:
    from pypinyin import Style, lazy_pinyin
    HAS_PYPINYIN = True
except ImportError:
    HAS_PYPINYIN = False


def is_chinese(char: str) -> bool:
    """判断字符是否为中文。"""
    cp = ord(char)
    return (
        0x4E00 <= cp <= 0x9FFF
        or 0x3400 <= cp <= 0x4DBF
        or 0x20000 <= cp <= 0x2A6DF
    )


def is_japanese(char: str) -> bool:
    """判断字符是否为日文（假名）。"""
    cp = ord(char)
    return 0x3040 <= cp <= 0x309F or 0x30A0 <= cp <= 0x30FF


def is_korean(char: str) -> bool:
    """判断字符是否为韩文。"""
    cp = ord(char)
    return 0xAC00 <= cp <= 0xD7AF or 0x1100 <= cp <= 0x11FF


def is_english(char: str) -> bool:
    """判断字符是否为英文字母。"""
    return char.isascii() and char.isalpha()


def is_symbol(char: str) -> bool:
    """判断字符是否为符号/标点/数字等。"""
    cat = unicodedata.category(char)
    return cat.startswith(('P', 'S', 'N', 'M', 'Z'))


def get_initial(char: str) -> str | None:
    """获取字符的大写首字母。不支持则返回 None。"""
    if is_english(char):
        return char.upper()
    if is_chinese(char):
        if HAS_PYPINYIN:
            py = lazy_pinyin(char, style=Style.FIRST_LETTER)
            if py and py[0]:
                return py[0].upper()
        return None
    if is_japanese(char) or is_korean(char) or is_symbol(char):
        return None
    return None


def should_rename(name: str) -> str | None:
    """判断名称是否需要重命名，返回新名称；不需要则返回 None。"""
    stripped = name.strip()
    if not stripped:
        return None

    first_char = stripped[0]
    initial = get_initial(first_char)
    if initial is None:
        return None

    # 避免重复添加前缀
    if len(stripped) >= 2 and stripped[0] == initial and stripped[1] == ' ':
        return None

    new_name = f"{initial} {stripped}"
    if name == new_name:
        return None

    return new_name


def collect_renames(
    root_dir: str,
    extensions: set[str] | None,
    recursive: bool,
    target_type: str,
) -> list[tuple[str, str]]:
    """收集需要重命名的项目，返回 [(原路径, 新路径), ...]"""
    renames: list[tuple[str, str]] = []

    dot_exts = None
    if extensions:
        dot_exts = {e.strip() if e.strip().startswith('.') else f'.{e.strip()}'
                    for e in extensions if e.strip()}

    for dirpath, dirnames, filenames in os.walk(root_dir):
        if target_type == "file":
            for filename in filenames:
                if dot_exts is not None:
                    _, ext = os.path.splitext(filename)
                    if ext.lower() not in dot_exts:
                        continue

                new_name = should_rename(filename)
                if new_name is None:
                    continue

                old_path = os.path.join(dirpath, filename)
                new_path = os.path.join(dirpath, new_name)
                renames.append((old_path, new_path))
        else:
            for dirname in dirnames:
                new_name = should_rename(dirname)
                if new_name is None:
                    continue

                old_path = os.path.join(dirpath, dirname)
                new_path = os.path.join(dirpath, new_name)
                renames.append((old_path, new_path))

        if not recursive:
            dirnames.clear()

    # 处理文件夹时，按深度倒序排列，确保先重命名深层目录
    if target_type == "dir":
        renames.sort(key=lambda x: x[0].count(os.sep), reverse=True)

    return renames


def main():
    parser = argparse.ArgumentParser(
        description="批量重命名文件/文件夹：去除前后空格，添加首字符的大写首字母前缀"
    )
    parser.add_argument(
        "-d", "--dir", required=True,
        help="目标目录路径"
    )
    parser.add_argument(
        "-e", "--ext", default=None,
        help="需要处理的后缀名，逗号分隔，如 txt,md,py。不指定则处理所有文件（仅 --type file 时有效）"
    )
    parser.add_argument(
        "-r", "--recursive", action="store_true", default=False,
        help="递归处理子目录（默认仅处理顶层）"
    )
    parser.add_argument(
        "-t", "--type", choices=("file", "dir"), default="file",
        help="处理类型：file（文件）或 dir（文件夹），默认为 file"
    )
    args = parser.parse_args()

    root_dir = os.path.abspath(args.dir)
    if not os.path.isdir(root_dir):
        print(f"错误：目录不存在 —— {root_dir}")
        sys.exit(1)

    target_type = args.type

    if args.ext is not None and target_type == "dir":
        print("警告：--type dir 时不支持 --ext 参数，已忽略后缀名过滤。")
        extensions = None
    elif args.ext is not None:
        extensions = {e.strip().lower() for e in args.ext.split(',') if e.strip()}
        if not extensions:
            print("错误：未提供有效的后缀名")
            sys.exit(1)
    else:
        extensions = None

    if not HAS_PYPINYIN:
        print("警告：未安装 pypinyin 库，中文文件名将跳过处理。")
        print("      安装命令：pip install pypinyin")
        print()

    renames = collect_renames(root_dir, extensions, args.recursive, target_type)

    if not renames:
        print("没有需要处理的项目。")
        return

    # 展示对比
    item_label = "文件夹" if target_type == "dir" else "文件"
    print(f"共找到 {len(renames)} 个需要处理的{item_label}：\n")
    max_show = max(len(os.path.basename(old)) for old, _ in renames)
    for old, new in renames:
        old_name = os.path.basename(old)
        new_name = os.path.basename(new)
        print(f"  {old_name:<{max_show}}  →  {new_name}")

    # 检查冲突
    new_names = [os.path.basename(n) for _, n in renames]
    if len(new_names) != len(set(new_names)):
        print("\n警告：存在重命名后的名称冲突，冲突项将跳过。")

    # 用户确认
    print()
    try:
        answer = input("确认执行重命名？(y/n): ").strip().lower()
    except (EOFError, KeyboardInterrupt):
        print("\n已取消。")
        return

    if answer not in ('y', 'yes'):
        print("已取消。")
        return

    # 执行重命名
    success = 0
    skipped = 0
    for old, new in renames:
        try:
            if os.path.exists(new):
                print(f"跳过（目标已存在）：{os.path.basename(old)}")
                skipped += 1
                continue
            os.rename(old, new)
            success += 1
        except OSError as e:
            print(f"失败：{os.path.basename(old)} —— {e}")
            skipped += 1

    print(f"\n完成：成功 {success} 个，跳过 {skipped} 个。")


if __name__ == "__main__":
    main()
