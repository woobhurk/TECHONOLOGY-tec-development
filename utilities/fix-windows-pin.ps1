param(
    [switch]$Force,
    [switch]$NoPause
)
$ErrorActionPreference = 'Continue'

# ========== 管理员自提升 ==========
$wi = [Security.Principal.WindowsIdentity]::GetCurrent()
$isAdmin = (New-Object Security.Principal.WindowsPrincipal($wi)).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    $arg = "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
    if ($Force) { $arg += ' -Force' }
    if ($NoPause) { $arg += ' -NoPause' }
    Start-Process powershell.exe -ArgumentList $arg -Verb RunAs
    exit
}

Write-Host '================ Windows Hello PIN 容器修复 ================'
Write-Host ('运行时间: ' + (Get-Date))
Write-Host ''

# ========== 1. 故障检测 ==========
Write-Host '[检测] 查找近 36 小时内的容器加载失败事件...'
$cut = (Get-Date).AddHours(-36)
$fail = @(Get-WinEvent -FilterHashtable @{ LogName = 'Microsoft-Windows-HelloForBusiness/Operational'; Id = 7002; StartTime = $cut } -ErrorAction SilentlyContinue)
if ($fail.Count -gt 0) {
    Write-Host ('  检测到 {0} 条 "Failed to load existing container" 事件，符合已知故障特征。' -f $fail.Count)
    $fail | Select-Object -First 3 | ForEach-Object {
        $cid = (($_.Message -split "`r?`n") | Where-Object { $_ -match 'ID:' }) -replace '.*ID:\s*', ''
        Write-Host ('  ' + $_.TimeCreated.ToString('MM-dd HH:mm:ss') + '  容器 ' + $cid)
    }
}
elseif (-not $Force) {
    Write-Host '  近 36 小时内未检测到容器加载失败事件(7002)。'
    Write-Host '  如果 PIN 目前正常，无需修复。'
    Write-Host '  如果 PIN 确实已丢失，请加 -Force 参数运行: Fix-WindowsHelloPin.ps1 -Force'
    if (-not $NoPause) { Write-Host ''; Write-Host '按回车键关闭窗口...'; [void](Read-Host) }
    exit
}
else {
    Write-Host '  未检测到 7002 事件，但已指定 -Force，继续执行修复。'
}

# ========== 2. 停止 NGC 服务 ==========
Write-Host ''
Write-Host '[1/3] 停止 NGC 服务...'
try {
    Stop-Service ngcctnrsvc -Force -ErrorAction Stop
    Write-Host '  ngcctnrsvc (Passport 容器服务) 已停止'
}
catch {
    Write-Host ('  ngcctnrsvc 停止失败(可能已停止，可忽略): ' + $_.Exception.Message)
}
try {
    Stop-Service ngcsvc -Force -ErrorAction Stop
    Write-Host '  ngcsvc 已停止'
}
catch {
    Write-Host '  ngcsvc 无法单独停止(与其他服务共享 svchost，可忽略)'
}

# ========== 3. 重命名损坏的状态目录(保留备份) ==========
Write-Host ''
Write-Host '[2/3] 重命名损坏的 NGC/Crypto 状态目录...'
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$targets = @(
    'C:\Windows\ServiceProfiles\LocalService\AppData\Local\Microsoft\Ngc',
    'C:\Windows\ServiceProfiles\LocalService\AppData\Roaming\Microsoft\Crypto'
)
foreach ($p in $targets) {
    if (-not (Test-Path $p)) {
        Write-Host ('  跳过(不存在，可能已修复过): ' + $p)
        continue
    }
    $new = $p + '.bak_' + $stamp
    try {
        Rename-Item -Path $p -NewName $new -Force -ErrorAction Stop
        Write-Host ('  已重命名: ' + (Split-Path $p -Leaf) + '  ->  ' + (Split-Path $new -Leaf))
    }
    catch {
        Write-Host ('  直接重命名被拒(ACL 限制)，接管所有权后重试...')
        takeown /f $p /r /d y | Out-Null
        icacls $p /grant '*S-1-5-32-544:F' /t /c | Out-Null
        try {
            Rename-Item -Path $p -NewName $new -Force -ErrorAction Stop
            Write-Host ('  已重命名(接管所有权后): ' + (Split-Path $new -Leaf))
        }
        catch {
            Write-Host ('  重命名失败！请重启电脑后再运行一次本脚本。原因: ' + $_.Exception.Message)
            continue
        }
    }
}

# ========== 4. 清理过期备份(每类保留最近 3 份) ==========
Write-Host ''
Write-Host '[3/3] 清理过期备份(每类保留最近 3 份)...'
$pruneSets = @(
    @{ Parent = 'C:\Windows\ServiceProfiles\LocalService\AppData\Local\Microsoft'; Pattern = 'Ngc.bak*' },
    @{ Parent = 'C:\Windows\ServiceProfiles\LocalService\AppData\Roaming\Microsoft'; Pattern = 'Crypto.bak*' }
)
foreach ($set in $pruneSets) {
    $old = @(Get-ChildItem $set.Parent -Directory -Filter $set.Pattern -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending | Select-Object -Skip 3)
    foreach ($d in $old) {
        try {
            Remove-Item $d.FullName -Recurse -Force -ErrorAction Stop
            Write-Host ('  已删除旧备份: ' + $d.Name)
        }
        catch {
            Write-Host ('  旧备份删除失败(不影响使用，可忽略): ' + $d.Name)
        }
    }
}

# ========== 5. 完成提示 ==========
Write-Host ''
Write-Host '================ 修复完成 ================'
Write-Host '请按顺序执行:'
Write-Host '  1. 重启电脑'
Write-Host '  2. 设置 -> 账户 -> 登录选项 -> PIN (Windows Hello) -> 添加'
Write-Host '  3. 重启一次并用 PIN 登录验证；成功后再重启一次确认稳定'
Write-Host ''
Write-Host '说明: 旧状态目录已重命名为 .bak 备份(未删除)，确认 PIN 稳定'
Write-Host '      几天后可手动删除这些 .bak 目录。'
if (-not $NoPause) { Write-Host ''; Write-Host '按回车键关闭窗口...'; [void](Read-Host) }
