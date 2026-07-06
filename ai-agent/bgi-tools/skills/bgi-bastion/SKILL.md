---
name: bgi-bastion
description: Use when the user wants to execute commands on internal BGI servers accessible only through the bastion host (uomc-worker01.genomics.cn). Handles the interactive 4-step bastion login (node selection, account type, credentials) and returns clean output. Trigger for any task involving: executing commands on sap-uat/10.224.26.7, connecting to internal servers, bastion login, querying/counting errors in service logs, checking service status, viewing files on internal nodes, or any reference to /stomics/app/ paths or 10.224.x.x addresses — including when phrased in Chinese (堡垒机, 查日志, 统计异常, 内网服务器). Use this even when the user doesn't explicitly say 'bastion' — if the task needs internal BGI infrastructure access, this skill is the way in.
---

# 堡垒机命令执行

通过华大堡垒机（uomc-worker01.genomics.cn）登录到内网节点并执行命令。

## 概述

堡垒机 `uomc-worker01.genomics.cn` 只提供交互式菜单登录，不支持 `exec` 模式的 SSH 命令直传。
`scripts/bastion-exec.js` 使用 Node.js `ssh2` 库分配 PTY 伪终端，模拟人工交互完成堡垒机的四步登录流程，然后执行用户命令并返回干净的输出。

## 前置条件

- Node.js 可用（脚本使用 `ssh2` 包）
- `~/.ssh/config` 中已配置 `uomc-worker01.genomics.cn` 的 SSH 密钥认证

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

从本技能目录执行（`node_modules/ssh2` 位于本目录）：

```bash
# 基本：在默认节点执行命令
node scripts/bastion-exec.js <命令>

# 指定节点和用户
node scripts/bastion-exec.js -n 10.224.26.7 -u root <命令>
```

### 选项

| 选项 | 说明 | 默认值 |
|------|------|--------|
| `-n, --node` | 目标节点 IP | `10.224.26.7` |
| `-t, --account-type` | 账号类型：1=any, 2=self | `1` |
| `-u, --user` | 目标节点用户名 | `STOmics_test` |
| `-p, --password` | 密码 | `F+4V{C7V3F2r@IpJau` |
| `-b, --bastion` | SSH config Host 别名 | `uomc-worker01.genomics.cn` |

### 示例

```bash
# 获取主机名
node scripts/bastion-exec.js hostname
# → sap-uat

# 查看磁盘使用
node scripts/bastion-exec.js "df -h /"
# → Filesystem      Size  Used Avail Use% Mounted on
# → /dev/vda1        59G   15G   42G  26% /

# 查看用户信息
node scripts/bastion-exec.js "id"
# → uid=1013(STOmics_test) gid=1013(STOmics_test) groups=1013(STOmics_test)
```

## 服务日志排查

### 日志位置与命名

日志统一存放在默认节点 `10.224.26.7` 上，路径按环境区分：

| 环境 | 日志目录 |
|------|---------|
| 测试 | `/stomics/app/stomics-cloud-backend/test/logs/` |
| 生产 | `/stomics/app/stomics-cloud-backend/prd/logs/` |

文件命名格式：`<服务名>-<YYYYMMDD>-<IP>.<类别>[.gz]`

| 组成 | 说明 | 示例 |
|------|------|------|
| 服务名 | 微服务模块名 | `dcs-cloud-billing-service` |
| 日期 | 8 位日期 | `20260705` |
| IP | 服务运行节点 IP（仅标识来源，非日志存储位置） | `10.224.28.111` |
| 类别 | `.log`（业务日志）或 `.err`（错误日志） | `.log` |
| 压缩 | 非当天日志自动压缩为 `.gz` | `.log.gz` |

### 关键注意事项

1. **日志都在默认节点**：文件名中的 IP 是服务运行节点，不是日志存储节点。所有日志统一在默认节点 `10.224.26.7` 上，无需切换 `-n` 节点。
2. **当天 vs 非当天**：当天日志为未压缩 `.log`，历史日志为 `.log.gz` 压缩格式。查找历史日志时需用 `zcat`/`zgrep` 处理压缩文件。
3. **只查 .log**：日志分为 `.log`（业务日志）和 `.err`（错误日志）两类，一般只查找 `.log`。
4. **大文件性能**：压缩日志可达数百 MB（解压后数 GB），避免对同一文件多次读取（如同时跑 `zgrep` 和 `zcat|grep`），单遍 `zcat | grep -c` 即可。

### 常用命令示例

```bash
# 列出某天的所有日志文件（含 .err）
node scripts/bastion-exec.js "ls -la /stomics/app/stomics-cloud-backend/test/logs/dcs-cloud-billing-service-20260705*"

# 统计当天（未压缩）日志的 Exception 数量
node scripts/bastion-exec.js "grep -ci Exception /stomics/app/stomics-cloud-backend/test/logs/dcs-cloud-billing-service-20260706-10.224.28.111.log"

# 统计历史（压缩）日志的 Exception 数量——单遍 zcat|grep
node scripts/bastion-exec.js "zcat /stomics/app/stomics-cloud-backend/test/logs/dcs-cloud-billing-service-20260705*.log.gz | grep -ci Exception"

# 同时覆盖 .log 和 .log.gz（通配符自动匹配，2>/dev/null 跳过不存在的文件）
node scripts/bastion-exec.js "{ cat /stomics/app/stomics-cloud-backend/test/logs/dcs-cloud-billing-service-20260705*.log 2>/dev/null; zcat /stomics/app/stomics-cloud-backend/test/logs/dcs-cloud-billing-service-20260705*.log.gz 2>/dev/null; } | grep -ci Exception"
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

- **命令超时**：脚本命令执行上限 120 秒。大文件操作（如解压数百 MB 的 .gz 日志）用单遍管道 `zcat | grep -c`，避免重复读取同一文件。
- **命令引号**：命令含管道符 `|`、分号 `;`、重定向 `>` 等特殊字符时，用双引号包裹整个命令传给脚本。
- **调试模式**：设置环境变量 `BASTION_DEBUG=1` 可在 stderr 打印原始数据流，排查登录或输出异常。

## 实现原理

脚本通过 `ssh2` 库的 PTY shell 模式连接堡垒机，用状态机驱动交互：

1. 解析 `~/.ssh/config` 获取认证参数（User、IdentityFile 等）
2. SSH 连接并打开 PTY 伪终端
3. 监听输出流，用关键词匹配检测当前菜单阶段（关键词对齐堡垒机实际菜单文案）
4. 自动发送对应的登录输入（选节点 → 账号类型 → 用户名 → 密码）
5. 密码发送后等待 shell 提示符（`$ `、`# ` 等）出现，确认登录成功
6. 用哨兵标记包裹用户命令发送：`echo __START__; <命令>; __rc=$?; echo __END__$__rc`
7. 检测 END 标记携带的退出码，提取 START 与 END 之间的纯净输出（去除回显、ANSI、提示符，归一化 CRLF 与软换行）
8. 进程以远程命令的真实退出码退出；命令若导致 shell 退出（如 `exit`）则快速报错
