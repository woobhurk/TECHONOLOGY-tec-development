# NGC PIN 失效（PCR7 变动）诊断与修复 Runbook
# 机器: tyz-pc-omen | 适用: Win11 26H2 (26300.x)
#
# 根因链（2026-09-22 定案）:
#   26H2 Secure Boot CA 2023 自动迁移（TpmTasks.dll SBServicing，\Microsoft\Windows\PI\Secure-Boot-Update
#   任务，开机+5min 且每 12h 重复）无视 AvailableUpdates=0；每次重大服务事件（CU 处理、待处理维护重启）
#   推进迁移/换引导组件 -> SB 变量变 -> PCR7 变 -> NGC 密钥解封失败(7002, 0x80070002) -> PIN 作废。
#   1796 (SBAT 0x800700c1) 是纯 Windows 机器的固定无害失败。
#
# 用法:
#   检查:   powershell -File Pin-Fix-Runbook.ps1
#   修复:   powershell -File Pin-Fix-Runbook.ps1 -Repair   （需管理员；完成后重启+重设 PIN）
#
# 修复后动作: 重启 -> 用密码登录 -> 等 10 分钟 -> 确认基线稳定 -> 重设 PIN -> 再重启验证。
param([switch]$Repair)
$ErrorActionPreference = "Continue"
$dir = "C:\Users\tyfanchz\Documents\NGC-PIN-Fix"
$baseFile = Join-Path $dir "sbvars_baseline_20260922.txt"
$ngc = "C:\Windows\ServiceProfiles\LocalService\AppData\Local\Microsoft\Ngc"
$crypto = "C:\Windows\ServiceProfiles\LocalService\AppData\Roaming\Microsoft\Crypto"

function Get-SbHashes {
    foreach ($v in @("PK","KEK","db","dbx")) {
        $u = Get-SecureBootUEFI $v
        $sha = [System.Security.Cryptography.SHA256]::Create()
        "{0} sha256={1}" -f $v, [BitConverter]::ToString($sha.ComputeHash($u.Bytes)).Replace("-","")
    }
}

function Compare-Hashes {
    $base = Get-Content $baseFile
    foreach ($line in (Get-SbHashes)) {
        $v = ($line -split " ")[0]
        $h = ($line -split "sha256=")[1]
        $old = ($base | Where-Object { $_ -match ("^" + $v + "\s") })
        $oldh = ($old -split "sha256=")[1]
        if ($oldh -eq $h) { "$v : UNCHANGED" } else { "$v : CHANGED! base=$oldh now=$h" }
    }
}

Write-Host "== 上次开机:" (Get-CimInstance Win32_OperatingSystem).LastBootUpTime
Write-Host "== SB 变量哈希 vs 基线:"
Compare-Hashes
$boot = (Get-CimInstance Win32_OperatingSystem).LastBootUpTime
$n7002 = (Get-WinEvent -FilterHashtable @{LogName="Microsoft-Windows-HelloForBusiness/Operational"; Id=7002; StartTime=$boot} -ErrorAction SilentlyContinue | Measure-Object).Count
Write-Host "== 本次开机 HelloForBusiness 7002 (容器加载失败) 次数: $n7002"
Write-Host "== 近期 TPM-WMI 事件 (1808=CA状态上报 1796=SBAT固定失败):"
Get-WinEvent -FilterHashtable @{LogName="System"; ProviderName="Microsoft-Windows-TPM-WMI"} -MaxEvents 6 -ErrorAction SilentlyContinue |
    ForEach-Object { "{0}  Id={1}" -f $_.TimeCreated, $_.Id }
Write-Host "== Ngc/Crypto 目录存在: $(Test-Path $ngc) / $(Test-Path $crypto)"

if (-not $Repair) {
    Write-Host "判读: 哈希有 CHANGED = PCR7 已变异, 旧 PIN 密钥作废 -> 执行 -Repair 配方后重设 PIN。"
    Write-Host "      全部 UNCHANGED 且 7002=0 = 状态健康, PIN 丢失请勿动 SB 配置, 先查重设失败日志。"
    return
}

Write-Host "== 开始修复: 停服务 + 接管 + 重命名 Ngc/Crypto"
sc.exe stop NgcSvc | Out-Null
sc.exe stop NgcCtnrSvc | Out-Null
Start-Sleep 3
$stamp = Get-Date -Format "yyyyMMdd_HHmm"
foreach ($d in @($ngc, $crypto)) {
    if (Test-Path $d) {
        & takeown.exe /f $d /r /d y | Out-Null
        & icacls.exe $d /grant "*S-1-5-32-544:F" /t | Out-Null
        $name = Split-Path $d -Leaf
        Rename-Item -LiteralPath $d -NewName ($name + ".bak_" + $stamp)
        Write-Host "已重命名: $d -> $name.bak_$stamp"
    } else {
        Write-Host "不存在, 跳过: $d"
    }
}
Write-Host "== 完成。请重启 -> 密码登录 -> 等 10 分钟 -> 重设 PIN -> 再重启一次验证。"
