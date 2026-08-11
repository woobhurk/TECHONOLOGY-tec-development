---
name: bgi-bastion
description: Use when the user wants to execute commands on internal BGI servers accessible only through the bastion host (uomc-worker01.genomics.cn). Handles the interactive 4-step bastion login (node selection, account type, credentials) and returns clean output. Trigger for any task involving - executing commands on sap-uat/10.224.26.7, connecting to internal servers, bastion login, querying/counting errors in service logs, checking service status, viewing files on internal nodes, or any reference to /stomics/app/ paths or 10.224.x.x addresses - including when phrased in Chinese (堡垒机, 查日志, 统计异常, 内网服务器). Use this even when the user doesn't explicitly say 'bastion' - if the task needs internal BGI infrastructure access, this skill is the way in.
---

# 堡垒机命令执行

通过华大堡垒机（uomc-worker01.genomics.cn）登录到内网节点并执行命令。

## 概述

堡垒机 `uomc-worker01.genomics.cn` 只提供交互式菜单登录，不支持 `exec` 模式的 SSH 命令直传。
`scripts/bastion-exec.js` 使用 Node.js `ssh2` 库分配 PTY 伪终端，模拟人工交互完成堡垒机的四步登录流程，然后执行用户命令并返回干净的输出。

## 前置条件

- Node.js 可用（脚本使用 `ssh2` 包）
- `~/.ssh/config` 中已配置 `uomc-worker01.genomics.cn` 的 SSH 密钥认证
- 本地 Shell 为 **bash**、**PowerShell (pwsh)** 或 **cmd.exe**

`~/.ssh/config` 配置示例：

```ssh-config
Host uomc-worker01.genomics.cn
    HostName uomc-worker01.genomics.cn
    User wubaohui1
    Port 22
    IdentityFile ~/.ssh/id_rsa
    HostKeyAlgorithms +ssh-rsa
    PubkeyAcceptedAlgorithms +ssh-rsa
```

## 登录流程

交互式登录共 4 步，脚本自动完成：

| 步骤 | 发送内容 | 触发关键词 |
|------|---------|-----------|
| 1. 选择节点 | `10.224.26.7` | `请选择目标资产` |
| 2. 账号类型 | `1`（any） | `请选择登录账号` |
| 3. 用户名 | `STOmics_test` | `login:` |
| 4. 密码 | `F+4V{C7V3F2r@IpJau` | `Password:` |

## 用法

用 `-c` 指定命令，**三 shell（bash / pwsh / cmd）写法完全一致**。`-c` 后的参数自动拼接为完整命令，简单命令无需引号：

```bash
# 简单命令（无需引号）
node scripts/bastion-exec.js -c hostname
node scripts/bastion-exec.js -c grep -ci Exception /path/to/file.log

# 含管道、通配符等特殊字符时，用双引号包裹
node scripts/bastion-exec.js -c "zcat f.gz | grep -c Exception"

# 指定节点和用户
node scripts/bastion-exec.js -n 10.224.26.7 -u root -c "df -h"
```

> `-c` 是分隔符，其后所有参数都归为命令，因此**其他选项必须放在 `-c` 之前**。

### 选项

| 选项 | 说明 | 默认值 |
|------|------|--------|
| `-c, --command` | 要执行的命令（推荐，三 shell 通用） | - |
| `-n, --node` | 目标节点 IP | `10.224.26.7` |
| `-t, --account-type` | 账号类型：1=any, 2=self | `1` |
| `-u, --user` | 目标节点用户名 | `STOmics_test` |
| `-p, --password` | 密码 | `F+4V{C7V3F2r@IpJau` |
| `-b, --bastion` | SSH config Host 别名 | `uomc-worker01.genomics.cn` |

### 示例

```bash
# 获取主机名
node scripts/bastion-exec.js -c hostname
# -> sap-uat

# 查看磁盘使用
node scripts/bastion-exec.js -c df -h /
# -> Filesystem      Size  Used Avail Use% Mounted on
# -> /dev/vda1        59G   15G   42G  26% /

# 查看用户信息
node scripts/bastion-exec.js -c id
# -> uid=1013(STOmics_test) gid=1013(STOmics_test) groups=1013(STOmics_test)
```

## 服务日志排查

### 系统与环境选择（强制）

执行任何日志命令前，先确定**系统**和**环境**两个维度：

| 用户表述 | 只能使用的系统路径前缀 |
|---|---|
| 云平台（STOmics 云平台、stomics-cloud 等） | `/stomics/app/stomics-cloud-backend/` |
| 病理云（DCS Path、dcs-path 等） | `/stomics/app/dcs-path/` |

1. 先解析系统和环境。用户提供的完整路径可以用于确认这两个维度，但必须核对路径前缀与文字描述一致。
2. 用户明确指定系统和环境时，只使用对应路径；不要用另一系统的示例命令、默认环境或上一次排查的路径。
3. 系统或环境仍未确定时，先询问再执行；不要把示例文件或默认值当作推断结果。
4. 系统名称与路径前缀冲突时，停止并指出冲突；不要静默切换系统。
5. 用户要求同时查询两个系统时，拆成两组独立命令和结果；每组命令只能出现本系统路径前缀。
6. 默认先查 `10.224.26.7`。目标路径不存在时报告路径不存在并停止，不要用另一系统路径代替，也不要因文件名中的 IP 自动切换 `-n` 节点。

### 执行前核对

在首次 `grep`/`zcat` 前，先只对已选系统和环境执行 `ls` 或目录存在性检查，并确认当天实际文件列表。向用户报告查询范围时明确写出 `系统`、`环境`、`节点` 和 `目录/文件模式`；路径检查失败时停留在当前系统和环境，不改查另一系统的路径。

### 日志位置与命名

日志默认存放在节点 `10.224.26.7` 上，按系统和环境区分；实际查询以目标路径存在性检查为准：

| 系统 | 测试环境日志路径 | 生产环境日志路径 |
|------|------------------|------------------|
| 云平台 | `/stomics/app/stomics-cloud-backend/test/logs/` | `/stomics/app/stomics-cloud-backend/prd/logs/` |
| 病理云 | `/stomics/app/dcs-path/test/logs/`（示例文件：`/stomics/app/dcs-path/test/logs/dcs-path-backend-20260810-10.224.28.5.log`） | `/stomics/app/dcs-path/prod/logs/` |

病理云测试路径中的文件名仅为示例。查询当天日志时，匹配该目录下所有对应日期的 `.log`/`.err` 文件，例如 `dcs-path-backend-<YYYYMMDD>-*.log`；不要只读取示例中的 `10.224.28.5` 文件。
云平台生产路径使用 `prd`，病理云生产路径使用 `prod`，不要互相替换。

文件命名格式：`<服务名>-<YYYYMMDD>-<IP>.<类别>[.gz]`

| 组成 | 说明 | 示例 |
|------|------|------|
| 服务名 | 微服务模块名 | 云平台：`dcs-cloud-billing-service`；病理云：`dcs-path-backend` |
| 日期 | 8 位日期 | `20260705` |
| IP | 服务运行节点 IP（仅标识来源，非日志存储位置） | `10.224.28.111` |
| 类别 | `.log`（业务日志）或 `.err`（错误日志） | `.log` |
| 压缩 | 非当天日志自动压缩为 `.gz` | `.log.gz` |

### 关键注意事项

1. **日志节点与系统路径分开判断**：文件名中的 IP 是服务运行节点，不是日志存储节点。默认先在 `10.224.26.7` 上查目标系统路径；路径不存在时报告并停止，不用另一系统路径替代。
2. **当天 vs 非当天**：当天日志为未压缩 `.log`，历史日志为 `.log.gz` 压缩格式。查找历史日志时需用 `zcat`/`zgrep` 处理压缩文件。
3. **按排查目的选择日志类型**：普通业务统计可先查 `.log`；涉及失败、异常、堆栈或接口返回码时，必须同时检索同日期 `.log` 和 `.err`。
4. **大文件性能**：压缩日志可达数百 MB（解压后数 GB），避免对同一文件多次读取（如同时跑 `zgrep` 和 `zcat|grep`），单遍 `zcat | grep -c` 即可。

### 常用命令示例

命令示例按系统标注。执行前只替换同一系统的日期、服务名和文件名；先确认命令中的路径前缀与目标系统一致。

#### 云平台

```bash
# 测试环境：统计当天（未压缩）日志的 Exception 数量
node scripts/bastion-exec.js -c grep -ci Exception /stomics/app/stomics-cloud-backend/test/logs/dcs-cloud-billing-service-20260706-10.224.28.111.log

# 测试环境：统计历史（压缩）日志的 Exception 数量
node scripts/bastion-exec.js -c "zcat /stomics/app/stomics-cloud-backend/test/logs/dcs-cloud-billing-service-20260705*.log.gz | grep -ci Exception"

# 测试环境：列出某天的所有日志文件
node scripts/bastion-exec.js -c "ls -la /stomics/app/stomics-cloud-backend/test/logs/dcs-cloud-billing-service-20260705*"

# 生产环境：先列出实际日志文件
node scripts/bastion-exec.js -c "ls -la /stomics/app/stomics-cloud-backend/prd/logs/"
```

#### 病理云

```bash
# 测试环境：列出当天目录内所有后端日志；20260810 仅为日期示例
node scripts/bastion-exec.js -c "ls -la /stomics/app/dcs-path/test/logs/dcs-path-backend-20260810-*"

# 测试环境：查询当天后端日志中的删除/失败信息；示例文件不是当天唯一日志
node scripts/bastion-exec.js -c "grep -HniE '删除|失败|deleteTasks' /stomics/app/dcs-path/test/logs/dcs-path-backend-20260810-*.log /stomics/app/dcs-path/test/logs/dcs-path-backend-20260810-*.err 2>/dev/null"

# 生产环境：先确认目录中的实际文件，再按实际文件名检索
node scripts/bastion-exec.js -c "ls -la /stomics/app/dcs-path/prod/logs/"
```

## 交互式长连接模式 (bastion-repl.js)

`bastion-repl.js` 登录堡垒机一次后保持会话，逐条交互执行命令，适合连续操作。
同一 shell 会话中 `cd`、`export` 等状态跨命令保持，无需每条命令重新登录。

```bash
# 进入交互会话（默认节点）
node scripts/bastion-repl.js

# 指定节点和用户
node scripts/bastion-repl.js -n 10.224.26.7 -u root
```

交互中输入命令回车执行，`exit` / `quit` / `logout` 或 Ctrl-D 退出。选项与 `bastion-exec.js` 一致（`-n`/`-t`/`-u`/`-p`/`-b`）。

> 两个脚本的区别：`bastion-exec.js` 一次性执行单条命令后关闭连接；`bastion-repl.js` 保持连接、逐条交互，适合连续多条命令或保持 shell 状态的场景。

## 注意事项

- **命令超时**：脚本命令执行上限 120 秒。大文件操作（如解压数百 MB 的 .gz 日志）用单遍管道 `zcat | grep -c`，避免重复读取同一文件。当天未压缩日志可达数 GB，全量 `grep` 会超时，分块搜索策略见下方"大文件搜索策略"。
- **引号规则**：`-c` 后的参数自动拼接为完整命令，**简单命令无需引号**。仅当命令含 `|` `;` `>` `*` `$` 等特殊字符时，用双引号 `"..."` 包裹以防本地 shell 拦截。命令含 `$` 时 bash/pwsh 改用单引号。cmd 中 `%` 会被展开（双引号内也展开），含 `%` 的命令建议切换到 bash/pwsh。
- **调试模式**：设置环境变量 `BASTION_DEBUG=1` 可在 stderr 打印原始数据流。
  - bash：`BASTION_DEBUG=1 node scripts/bastion-exec.js -c hostname`
  - pwsh：`$env:BASTION_DEBUG=1; node scripts/bastion-exec.js -c hostname`
  - cmd：`set BASTION_DEBUG=1 && node scripts/bastion-exec.js -c hostname`

## 常见问题

### 命令中不要包含 `exit`

远程 shell 执行到 `exit` 会直接退出会话，触发"目标 shell 已退出"错误。如需退出交互会话，使用 `bastion-repl.js` 的 `exit` 命令。

### 大文件搜索策略

当天未压缩日志可达数 GB 至数十 GB，全量 `grep` 会超出 120 秒命令超时。需要分块搜索并加速匹配。

**分块扫描**：用 `head -c`、`tail -c` 或 `dd skip` 只读取文件的一部分，避免扫全文。`tail -c` 和 `dd` 用 `lseek` 跳过前段数据，不实际读取：

```bash
# 搜前 3 GB（含管道，需引号）
node scripts/bastion-exec.js -c "head -c 3000000000 /path/to/huge.log | LC_ALL=C grep pattern"

# 搜末尾 3 GB（lseek 跳过前面部分，不读全文）
node scripts/bastion-exec.js -c "tail -c 3000000000 /path/to/huge.log | LC_ALL=C grep pattern"

# 搜中间段（跳过前 3 GB，读 6 GB）
node scripts/bastion-exec.js -c "dd bs=1M skip=3072 count=6144 if=/path/to/huge.log 2>/dev/null | LC_ALL=C grep pattern"
```

**加速 grep**：`LC_ALL=C` 让 grep 以字节模式匹配，比 UTF-8 locale 快 2-5 倍。搜索大文件时始终加上：

```bash
# ❌ 慢：默认 locale 逐字符解码
LC_ALL=C grep 'pattern' huge.log

# ✅ 快：C locale 逐字节匹配
LC_ALL=C grep 'pattern' huge.log
```

**限制匹配数**：`grep -m N` 找到 N 条匹配后立即停止。若匹配在文件前部则大幅节省时间；若无匹配仍需扫完全文：

```bash
LC_ALL=C grep -m 20 'pattern' huge.log
```

**减少登录开销**：每次调用 `bastion-exec.js` 都重新走 4 步登录（约 15 秒）。需要多次搜索时，改用 `bastion-repl.js` 交互模式保持连接，所有命令共用一次登录：

```bash
node scripts/bastion-repl.js
# 登录后在交互中逐条执行
> head -c 3000000000 /path/to/huge.log | LC_ALL=C grep pattern
> tail -c 3000000000 /path/to/huge.log | LC_ALL=C grep pattern
```

## 实现原理

脚本通过 `ssh2` 库的 PTY shell 模式连接堡垒机，用状态机驱动交互：

1. 解析 `~/.ssh/config` 获取认证参数（User、IdentityFile 等）
2. SSH 连接并打开 PTY 伪终端
3. 监听输出流，用关键词匹配检测当前菜单阶段（关键词对齐堡垒机实际菜单文案）
4. 自动发送对应的登录输入（选节点 -> 账号类型 -> 用户名 -> 密码）
5. 密码发送后等待 shell 提示符（`$ `、`# ` 等）出现，确认登录成功
6. 用哨兵标记包裹用户命令发送：`echo __START__; <命令>; __rc=$?; echo __END__$__rc`
7. 检测 END 标记携带的退出码，提取 START 与 END 之间的纯净输出（去除回显、ANSI、提示符，归一化 CRLF 与软换行）
8. 进程以远程命令的真实退出码退出；命令若导致 shell 退出（如 `exit`）则快速报错
