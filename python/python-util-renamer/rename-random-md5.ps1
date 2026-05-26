<#
批量将目录下文件重命名为 {父级目录名}-{随机_md5}.后缀 的格式。
用法示例：
  # 仅列出（模拟执行）
  .\batch-rename-random-md5.ps1 -Path C:\temp -DryRun

  # 实际递归重命名并保留扩展名，生成映射文件 rename_map.csv
  .\batch-rename-random-md5.ps1 -Path C:\temp -Recurse

参数：
  -Path              目标目录（必需）
  -Recurse           递归处理子目录
  -PreserveExtension 是否保留原文件扩展名（默认开启）
  -DryRun            仅模拟输出，不执行重命名
  -MapFile           映射 CSV 文件名（相对于 Path，默认 rename_map.csv）
#>
[CmdletBinding()]
param(
    [Parameter(Position=0)]
    [string]$Path = '',
    [switch]$Recurse,
    [switch]
    $PreserveExtension = $true,
    [switch]$DryRun,
    [string]$MapFile = 'rename_map.csv'
)

Set-StrictMode -Version Latest

# 如果未指定路径，提示用户输入
if ([string]::IsNullOrWhiteSpace($Path)) {
    $Path = Read-Host "请输入目标目录路径"
}

if (-not (Test-Path -Path $Path)) {
    Write-Error "路径不存在： $Path"
    exit 1
}

function Get-RandomMd5 {
    $input = [guid]::NewGuid().ToString() + (Get-Random)
    $md5 = [System.Security.Cryptography.MD5]::Create()
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($input)
    $hash = $md5.ComputeHash($bytes)
    return ([System.BitConverter]::ToString($hash) -replace '-','').ToLower()
}

# 收集文件
if ($Recurse) {
    # 暂不支持递归，因为可能造成路径中的 null 字符
    #$files = Get-ChildItem -LiteralPath $Path -File -Recurse -ErrorAction Stop
    $files = Get-ChildItem -LiteralPath $Path -File -ErrorAction Stop
} else {
    $files = Get-ChildItem -LiteralPath $Path -File -ErrorAction Stop
}

if ($files.Count -eq 0) {
    Write-Host "未找到任何文件： $Path"
    exit 0
}

$mappings = New-Object System.Collections.Generic.List[object]

foreach ($file in $files) {
    # 获取父级目录名
    $parentDir = Split-Path -Path $file.DirectoryName -Leaf

    $attempt = 0
    do {
        $attempt++
        $randomPart = Get-RandomMd5
        # 格式：{父级目录名}-{随机_md5}
        $name = "$parentDir-$randomPart"
        if ($PreserveExtension -and $file.Extension) {
            $newName = $name + $file.Extension.TrimEnd("`0")
        } else {
            $newName = $name
        }
        $newPath = Join-Path -Path $file.DirectoryName -ChildPath $newName
        # 若文件名已存在则重试（极小概率）
        $exists = Test-Path -LiteralPath $newPath
        if ($attempt -gt 10) { break }
    } while ($exists)

    $mapObj = [pscustomobject]@{
        OldFullPath = $file.FullName
        NewFullPath = $newPath
        OldName = $file.Name
        NewName = $newName
        Directory = $file.DirectoryName
        Extension = $file.Extension
        Status = if ($DryRun) { 'DryRun' } else { '' }
    }

    if ($DryRun) {
        Write-Host "[DRY] $($file.FullName) -> $newName"
    } else {
        try {
            Rename-Item -LiteralPath $file.FullName.TrimEnd("`0") -NewName $newName -ErrorAction Stop
            $mapObj.Status = 'Renamed'
            Write-Host "Renamed: $($file.FullName) -> $newName"
        } catch {
            $mapObj.Status = 'Error: ' + ($_.Exception.Message)
            Write-Warning "重命名失败： $($file.FullName) -> $newName. $_"
        }
    }

    [void]$mappings.Add($mapObj)
}

## 导出映射表到目标目录
#$mapFull = Join-Path -Path $Path -ChildPath $MapFile
#try {
#    $mappings | Export-Csv -Path $mapFull -NoTypeInformation -Encoding UTF8
#    Write-Host "已写入映射文件： $mapFull"
#} catch {
#    Write-Warning "无法写入映射文件： $mapFull. $_"
#}
