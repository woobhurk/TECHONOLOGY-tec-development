<#
.SYNOPSIS
    将指定源目录下的所有条目（文件及子目录）创建符号链接到目标目录。
.DESCRIPTION
    遍历 Src 目录下的每个条目（item，含文件和子目录），在 Target 目录下创建对应的符号链接。
    如果参数未指定，则交互式提示用户输入。
.PARAMETER Src
    源目录，包含各个条目。
.PARAMETER Target
    目标目录，将在此创建指向各条目的符号链接。
.PARAMETER Force
    覆盖已存在的同名链接或目录。
.EXAMPLE
    .\create-tem-links.ps1 -Src "D:\Projects" -Target "D:\LinkedProjects"
    .\create-tem-links.ps1 -Src "D:\Projects" -Target "D:\LinkedProjects" -Force
#>

param(
    [string]$Src,
    [string]$Target,
    [switch]$Force
)

# 交互式获取未指定的参数
if (-not $Src) {
    $Src = Read-Host -Prompt "请输入源目录路径 (Src)"
}
if (-not $Target) {
    $Target = Read-Host -Prompt "请输入目标目录路径 (Target)"
}

# 去除首尾引号和空格
$Src = $Src.Trim('"').Trim()
$Target = $Target.Trim('"').Trim()

# 验证源目录
if (-not (Test-Path -Path $Src -PathType Container)) {
    Write-Error "源目录不存在或不是目录: $Src"
    exit 1
}

# 确保目标目录存在
if (-not (Test-Path -Path $Target -PathType Container)) {
    New-Item -Path $Target -ItemType Directory -Force | Out-Null
    Write-Host "已创建目标目录: $Target"
}

# 获取源目录下的所有条目（文件和子目录）
$items = Get-ChildItem -Path $Src

if ($items.Count -eq 0) {
    Write-Warning "源目录下没有条目: $Src"
    exit 0
}

Write-Host "源目录  : $Src"
Write-Host "目标目录: $Target"
Write-Host "共发现 $($items.Count) 个条目"
Write-Host ("-" * 50)

$successCount = 0
$skipCount = 0
$failCount = 0

foreach ($item in $items) {
    $linkPath = Join-Path -Path $Target -ChildPath $item.Name

    # 已存在同名项
    if (Test-Path -Path $linkPath) {
        if ($Force) {
            Remove-Item -Path $linkPath -Recurse -Force
            Write-Host "已删除旧项: $linkPath"
        }
        else {
            Write-Warning "跳过 (已存在): $($item.Name)"
            $skipCount++
            continue
        }
    }

    try {
        New-Item -Path $linkPath -ItemType SymbolicLink -Target $item.FullName -Force | Out-Null
        Write-Host "已创建链接: $($item.Name) -> $($item.FullName)"
        $successCount++
    }
    catch {
        Write-Error "创建失败: $($item.Name) — $_"
        $failCount++
    }
}

Write-Host ("-" * 50)
Write-Host "完成: 成功 $successCount, 跳过 $skipCount, 失败 $failCount"