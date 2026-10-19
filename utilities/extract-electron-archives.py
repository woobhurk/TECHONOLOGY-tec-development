#!/usr/bin/env python3
"""提取 Electron 安装包（NSIS / Squirrel 等）内嵌的压缩包。

用法:
    python extract_electron_archives.py [-y] <安装包> [<安装包> ...]

流程:
    1. 列出安装包内可提取的压缩包（第一层；找不到时向下递归一层）。
    2. 由用户选择要提取的项（支持序号/范围/全部）。
    3. 提取到安装包所在目录，命名为 <安装包主名>@<压缩包名>。

后端: 优先 7-Zip (7z.exe)，其次 Bandizip (bz.exe)。
"""

from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path

try:
    sys.stdout.reconfigure(errors="replace")
    sys.stderr.reconfigure(errors="replace")
except Exception:
    pass

# 视为压缩包的扩展名（小写，含点）
ARCHIVE_EXTS = {
    ".7z", ".zip", ".cab", ".msi", ".msp", ".msu", ".nupkg",
    ".rar", ".tar", ".gz", ".tgz", ".bz2", ".xz", ".zst", ".lz", ".lz4", ".lzma",
    ".jar", ".apk", ".ipa", ".iso", ".wim", ".vhd", ".dmg", ".pkg",
    ".appx", ".msix", ".deb", ".rpm", ".arj", ".lzh", ".cpio",
}

# 第一层找不到压缩包时，递归扫描内层的候选文件参数
NEST_SCAN_MAX = 8          # 最多尝试的文件数
NEST_MIN_SIZE = 1024       # 小于该字节数的文件跳过
NEST_SKIP_EXTS = {".exe", ".dll", ".nsh", ".bmp", ".ico", ".png", ".jpg"}


@dataclass
class Entry:
    """归档内的一个条目。size/comp 为 -1 表示未知。"""
    path: str  # posix 风格相对路径
    size: int
    comp: int
    is_dir: bool


@dataclass
class Candidate:
    """可提取的压缩包。nested 为嵌套时的中间层成员路径。"""
    path: str  # 归档内相对路径；嵌套时为 "中间层/压缩包"
    size: int  # 用于显示
    nested: str | None = None


def human_size(n: int) -> str:
    if n < 0:
        return "?"
    for unit in ("B", "KB", "MB", "GB"):
        if n < 1024 or unit == "GB":
            return f"{n:.1f} {unit}" if unit != "B" else f"{n} B"
        n /= 1024.0
    return "?"


class BackendError(Exception):
    pass


class SevenZipBackend:
    name = "7z"

    def __init__(self, exe: str):
        self.exe = exe

    def list_entries(self, archive: str) -> list[Entry]:
        proc = subprocess.run(
            [self.exe, "l", "-slt", "-ba", "--", archive],
            capture_output=True,
        )
        text = proc.stdout.decode("utf-8", errors="replace")
        if proc.returncode != 0:
            raise BackendError(_first_error_line(proc.stderr, proc.returncode))
        entries: list[Entry] = []
        block: dict[str, str] = {}
        for line in text.splitlines() + [""]:
            line = line.rstrip("\r")
            if line.strip():
                key, _, value = line.partition("=")
                block[key.strip()] = value.strip()
                continue
            if "Path" in block:
                path = block["Path"].replace("\\", "/")
                entries.append(Entry(
                    path=path,
                    size=_to_int(block.get("Size", "-1")),
                    comp=_to_int(block.get("Packed Size", "-1")),
                    is_dir=block.get("Folder") == "+",
                ))
            block = {}
        return entries

    def extract(self, archive: str, member: str, dest: Path) -> int:
        with open(dest, "wb") as out:
            proc = subprocess.run(
                [self.exe, "x", "-y", "-so", "--", archive, member],
                stdout=out,
                stderr=subprocess.PIPE,
            )
        if proc.returncode != 0:
            dest.unlink(missing_ok=True)
            raise BackendError(_first_error_line(proc.stderr, proc.returncode))
        return dest.stat().st_size


class BandizipBackend:
    name = "Bandizip"

    def __init__(self, exe: str):
        self.exe = exe

    def list_entries(self, archive: str) -> list[Entry]:
        proc = subprocess.run(
            [self.exe, "l", "-y", archive],
            capture_output=True,
        )
        text = proc.stdout.decode("utf-8", errors="replace")
        if proc.returncode != 0:
            raise BackendError(_first_error_line(proc.stderr, proc.returncode))
        entries: list[Entry] = []
        # 形如: 2026-10-03 00:00:36 ____           -1    114655992 $PLUGINSDIR\app-arm64.7z
        # Attr 列为 4 字符（如 ____），用 \S+ 以兼容目录标志等变体。
        row = re.compile(
            r"^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})\s+(\S+)\s+(-?\d+)\s+(-?\d+)\s+(.+?)\s*$"
        )
        total = re.compile(r"\d+ files?, \d+ folders?")
        for line in text.splitlines():
            m = row.match(line)
            if not m:
                continue
            attr, size, comp, name = m.group(2), int(m.group(3)), int(m.group(4)), m.group(5)
            if total.search(name):
                continue
            name = name.replace("\\", "/").rstrip("/")
            entries.append(Entry(
                path=name,
                size=size,
                comp=comp,
                is_dir=attr.startswith("D") or line.rstrip().endswith(("\\", "/")),
            ))
        return entries

    def extract(self, archive: str, member: str, dest: Path) -> int:
        member_win = member.replace("/", "\\")
        with tempfile.TemporaryDirectory(prefix="elec_x_") as tmp:
            proc = subprocess.run(
                [self.exe, "x", "-y", f"-o:{tmp}", archive, member_win],
                capture_output=True,
            )
            src = Path(tmp) / member_win
            if proc.returncode != 0 or not src.is_file():
                raise BackendError(_first_error_line(proc.stderr, proc.returncode)
                                   or f"提取失败: {member}")
            shutil.move(str(src), dest)
        return dest.stat().st_size


def _to_int(s: str | None) -> int:
    try:
        return int(s) if s else -1
    except ValueError:
        return -1


def _first_error_line(stderr: bytes, rc: int) -> str:
    text = stderr.decode("utf-8", errors="replace").strip()
    for line in text.splitlines():
        line = line.strip()
        if line and not line.startswith(("7-Zip", "Bandizip", "bz ")):
            return line
    return text.splitlines()[0] if text else f"退出码 {rc}"


def _registry_install_dirs() -> list[Path]:
    """从卸载注册表收集已安装软件目录（处理自定义安装位置）。"""
    import winreg

    dirs: list[Path] = []
    keys = (
        (winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall"),
        (winreg.HKEY_LOCAL_MACHINE, r"SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall"),
        (winreg.HKEY_CURRENT_USER, r"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall"),
    )
    for hive, sub in keys:
        try:
            root = winreg.OpenKey(hive, sub)
        except OSError:
            continue
        with root:
            i = 0
            while True:
                try:
                    item = winreg.EnumKey(root, i)
                except OSError:
                    break
                i += 1
                try:
                    with winreg.OpenKey(root, item) as k:
                        for value_name in ("InstallLocation", "DisplayIcon"):
                            try:
                                v = winreg.QueryValueEx(k, value_name)[0]
                            except OSError:
                                continue
                            v = v.strip().strip('"').split(",")[0]
                            p = Path(v)
                            if p.is_file():
                                p = p.parent
                            if p.is_dir():
                                dirs.append(p)
                except OSError:
                    continue
    return dirs


def _find_exe(names: tuple[str, ...]) -> str | None:
    for name in names:
        found = shutil.which(name)
        if found:
            return found
    for d in _registry_install_dirs():
        for name in names:
            candidate = d / name
            if candidate.is_file():
                return str(candidate)
    return None


def find_backend():
    # 7z.exe 只认 PATH 中的，避免捞出其他软件自带的 7z（如 Rime weasel）。
    exe = shutil.which("7z.exe")
    if exe:
        return SevenZipBackend(exe)
    exe = _find_exe(("bz.exe",))
    if exe:
        return BandizipBackend(exe)
    return None


def candidates_from_entries(entries: list[Entry]) -> list[Candidate]:
    result = []
    for e in entries:
        if e.is_dir:
            continue
        ext = Path(e.path).suffix.lower()
        if ext in ARCHIVE_EXTS:
            result.append(Candidate(path=e.path, size=e.comp if e.comp >= 0 else e.size))
    result.sort(key=lambda c: c.path.lower())
    return result


def _sort_key_for_scan(e: Entry) -> int:
    return max(e.comp, e.size, 0)


def discover(backend, archive: str) -> list[Candidate]:
    """列出归档内可提取的压缩包；第一层没有时向下递归一层。"""
    entries = backend.list_entries(archive)
    cands = candidates_from_entries(entries)
    if cands:
        return cands

    scan = [e for e in entries
            if not e.is_dir
            and _sort_key_for_scan(e) >= NEST_MIN_SIZE
            and Path(e.path).suffix.lower() not in NEST_SKIP_EXTS]
    scan.sort(key=_sort_key_for_scan, reverse=True)
    for e in scan[:NEST_SCAN_MAX]:
        with tempfile.TemporaryDirectory(prefix="elec_nest_") as tmp:
            middle = Path(tmp) / "middle.bin"
            backend.extract(archive, e.path, middle)
            inner = candidates_from_entries(backend.list_entries(str(middle)))
        for c in inner:
            cands.append(Candidate(path=f"{e.path}/{c.path}", size=c.size, nested=e.path))
    return cands


def extract_candidate(backend, archive: str, cand: Candidate, dest: Path) -> int:
    if cand.nested is None:
        return backend.extract(archive, cand.path, dest)
    with tempfile.TemporaryDirectory(prefix="elec_nest_") as tmp:
        middle = Path(tmp) / "middle.bin"
        backend.extract(archive, cand.nested, middle)
        return backend.extract(str(middle), cand.path[len(cand.nested) + 1:], dest)


def parse_selection(text: str, count: int) -> list[int]:
    """解析 'a' / '1,3-5' / 'q' 形式的选择，返回升序索引；q/无效返回空。"""
    text = text.strip().lower()
    if text in ("a", "all"):
        return list(range(count))
    if text in ("", "q", "s", "quit", "skip"):
        return []
    picked: set[int] = set()
    for part in text.replace("，", ",").split(","):
        part = part.strip()
        if not part:
            continue
        if "-" in part:
            lo, _, hi = part.partition("-")
            if lo.strip().lstrip("-").isdigit() and hi.strip().lstrip("-").isdigit():
                a, b = int(lo), int(hi)
                if a > b:
                    a, b = b, a
                if 1 <= a and b <= count:
                    picked.update(range(a - 1, b))
                    continue
        elif part.isdigit() and 1 <= int(part) <= count:
            picked.add(int(part) - 1)
            continue
        print(f"  无效输入: {part!r}")
        return []
    return sorted(picked)


def unique_dest_names(installer: Path, cands: list[Candidate]) -> list[Path]:
    """生成 <安装包主名>@<压缩包名>；重名时用完整内层路径消歧。"""
    stem = installer.stem
    used: set[str] = set()
    names: list[str] = []
    for c in cands:
        flat = Path(c.path).name
        if flat.lower() in used:
            flat = c.path.replace("/", "#")
        used.add(flat.lower())
        names.append(f"{stem}@{flat}")
    return [installer.parent / n for n in names]


def process_installer(backend, installer: Path, assume_yes: bool) -> bool:
    print(f"\n=== {installer} ===")
    try:
        cands = discover(backend, str(installer))
    except (BackendError, OSError) as exc:
        print(f"[失败] 无法读取安装包: {exc}")
        return False
    if not cands:
        print("[跳过] 未发现可提取的压缩包。")
        return True

    dests = unique_dest_names(installer, cands)
    print(f"发现 {len(cands)} 个可提取的压缩包:")
    for i, (c, d) in enumerate(zip(cands, dests), 1):
        note = f"  (嵌套于 {c.nested})" if c.nested else ""
        print(f"  {i:>3}. {human_size(c.size):>10}  {c.path} -> {d.name}{note}")

    if assume_yes:
        picked = list(range(len(cands)))
    else:
        while True:
            try:
                text = input("选择要提取的项 (如 1,3-5；a=全部；回车=全部；q=跳过): ")
            except EOFError:
                return True
            picked = parse_selection(text, len(cands))
            if picked or text.strip().lower() in ("", "q", "s", "quit", "skip", "a", "all"):
                break
            print("  请重新输入。")
        if not picked:
            print("[跳过] 未选择任何压缩包。")
            return True

    ok = True
    for idx in picked:
        cand, dest = cands[idx], dests[idx]
        if dest.exists():
            if assume_yes:
                print(f"[跳过] 已存在: {dest.name}")
                continue
            try:
                answer = input(f"目标已存在，覆盖 {dest.name}? [y/N]: ").strip().lower()
            except EOFError:
                answer = ""
            if answer not in ("y", "yes"):
                print(f"[跳过] 已存在: {dest.name}")
                continue
        try:
            size = extract_candidate(backend, str(installer), cand, dest)
        except (BackendError, OSError) as exc:
            print(f"[失败] {cand.path}: {exc}")
            ok = False
            continue
        print(f"[完成] {cand.path} -> {dest.name} ({human_size(size)})")
    return ok


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(
        description="提取 Electron 安装包内嵌的压缩包（NSIS / Squirrel 等）。")
    parser.add_argument("installers", nargs="+", help="安装包路径 (exe/msi/zip 等)")
    parser.add_argument("-y", "--yes", action="store_true",
                        help="非交互：全部提取，已存在的目标跳过")
    args = parser.parse_args(argv)

    backend = find_backend()
    if backend is None:
        print("错误: 未找到 7z.exe 或 bz.exe。请安装 7-Zip 或 Bandizip，"
              "或将其加入 PATH。")
        return 2
    print(f"使用后端: {backend.name} ({backend.exe})")

    all_ok = True
    for raw in args.installers:
        installer = Path(raw)
        if not installer.is_file():
            print(f"\n[失败] 文件不存在: {installer}")
            all_ok = False
            continue
        all_ok = process_installer(backend, installer.resolve(), args.yes) and all_ok
    return 0 if all_ok else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
