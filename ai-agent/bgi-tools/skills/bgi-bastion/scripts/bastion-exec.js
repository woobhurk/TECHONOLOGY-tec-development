#!/usr/bin/env node
/**
 * bastion-exec.js
 *
 * 通过堡垒机（uomc-worker01.genomics.cn）登录到内网节点并执行命令。
 * 使用 ~/.ssh/config 中配置的认证信息自动连接。
 *
 * 用法:
 *   node bastion-exec.js <命令>           在默认目标节点执行命令
 *   node bastion-exec.js --node 10.224.26.7 <命令>
 *   node bastion-exec.js --node 10.224.26.7 --user STOmics_test --password 'xxx' <命令>
 *
 * 示例:
 *   node bastion-exec.js hostname
 *   node bastion-exec.js "ls -la /data"
 *   node bastion-exec.js --node 10.224.26.7 --user root df -h
 */

const { Client } = require('ssh2');
const fs = require('fs');
const path = require('path');
const os = require('os');

// ─── 默认登录参数 ────────────────────────────────────────────────
const DEFAULTS = {
  bastionHost: 'uomc-worker01.genomics.cn',  // SSH config 中的 Host 别名
  targetNode: '10.224.26.7',                  // 默认目标节点
  accountType: '1',                            // 账号类型: 1=any, 2=self
  username: 'STOmics_test',                    // 目标节点用户名
  password: 'F+4V{C7V3F2r@IpJau',             // 目标节点密码
};

// ─── SSH Config 解析 ────────────────────────────────────────────
function parseSshConfig(configPath, hostAlias) {
  const content = fs.readFileSync(configPath, 'utf8');
  const lines = content.split(/\r?\n/);

  const hosts = {};
  let currentHost = null;

  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#')) continue;

    const normalized = line.replace(/=/g, ' ').replace(/\s+/g, ' ');
    const parts = normalized.split(' ');
    const keyword = parts[0].toLowerCase();
    const values = parts.slice(1);

    if (keyword === 'host') {
      currentHost = values[0];
      if (!hosts[currentHost]) {
        hosts[currentHost] = {};
      }
    } else if (currentHost && values.length > 0) {
      hosts[currentHost][keyword] = values.join(' ');
    }
  }

  if (hosts[hostAlias]) return hosts[hostAlias];

  for (const [host, config] of Object.entries(hosts)) {
    if (host === '*') return config;
  }

  throw new Error(`SSH config 中未找到 Host "${hostAlias}"`);
}

function resolvePath(rawPath) {
  if (rawPath.startsWith('~/')) {
    return path.join(os.homedir(), rawPath.slice(2));
  }
  if (rawPath.startsWith('~')) {
    return path.join(os.homedir(), rawPath.slice(1));
  }
  return rawPath;
}

// ─── 命令行参数解析 ──────────────────────────────────────────────
function parseArgs() {
  const args = process.argv.slice(2);
  const opts = {
    node: DEFAULTS.targetNode,
    accountType: DEFAULTS.accountType,
    username: DEFAULTS.username,
    password: DEFAULTS.password,
    bastion: DEFAULTS.bastionHost,
    command: null,
  };

  let i = 0;
  while (i < args.length) {
    switch (args[i]) {
      case '--node':
      case '-n':
        opts.node = args[++i];
        break;
      case '--account-type':
      case '-t':
        opts.accountType = args[++i];
        break;
      case '--user':
      case '-u':
        opts.username = args[++i];
        break;
      case '--password':
      case '-p':
        opts.password = args[++i];
        break;
      case '--bastion':
      case '-b':
        opts.bastion = args[++i];
        break;
      case '--help':
      case '-h':
        printUsage();
        process.exit(0);
        break;
      default:
        if (!opts.command) {
          opts.command = args[i];
        } else {
          opts.command += ' ' + args[i];
        }
        break;
    }
    i++;
  }

  return opts;
}

function printUsage() {
  console.log(`
用法: node bastion-exec.js [选项] <命令>

选项:
  -n, --node <IP>        目标节点 IP（默认: ${DEFAULTS.targetNode}）
  -t, --account-type <n> 账号类型: 1=any, 2=self（默认: ${DEFAULTS.accountType}）
  -u, --user <用户名>    目标节点用户名（默认: ${DEFAULTS.username}）
  -p, --password <密码>  目标节点密码
  -b, --bastion <别名>   堡垒机 SSH config Host（默认: ${DEFAULTS.bastionHost}）
  -h, --help             显示此帮助

示例:
  node bastion-exec.js hostname
  node bastion-exec.js "ls -la /data"
  node bastion-exec.js -n 10.224.26.7 -u root df -h
`);
}

// ─── 主逻辑 ──────────────────────────────────────────────────────
async function main() {
  const opts = parseArgs();

  if (!opts.command) {
    console.error('错误: 请指定要执行的命令');
    console.error('用法: node bastion-exec.js <命令>');
    console.error('使用 --help 查看详细帮助');
    process.exit(1);
  }

  // 读取 SSH config
  const sshConfigPath = path.join(os.homedir(), '.ssh', 'config');
  if (!fs.existsSync(sshConfigPath)) {
    die(`SSH config 不存在: ${sshConfigPath}`);
  }

  const sshCfg = parseSshConfig(sshConfigPath, opts.bastion);

  const sshHost = sshCfg.hostname || opts.bastion;
  const sshPort = parseInt(sshCfg.port || '22', 10);
  const sshUser = sshCfg.user || os.userInfo().username;
  const identityFile = sshCfg.identityfile
    ? resolvePath(sshCfg.identityfile)
    : null;

  if (!identityFile) {
    die('SSH config 中未配置 IdentityFile');
  }
  if (!fs.existsSync(identityFile)) {
    die(`私钥文件不存在: ${identityFile}`);
  }

  // 解析 HostKeyAlgorithms
  let hostKeyAlgos = undefined;
  if (sshCfg.hostkeyalgorithms) {
    hostKeyAlgos = sshCfg.hostkeyalgorithms
      .split(',')
      .map(s => s.trim().replace(/^\+/, ''))
      .filter(Boolean);
  }

  log(`堡垒机: ${sshUser}@${sshHost}:${sshPort}`);
  log(`目标节点: ${opts.node}`);
  log(`目标用户: ${opts.username}`);
  log(`执行命令: ${opts.command}\n`);

  // 登录步骤 — 触发关键词对齐堡垒机实际菜单文案（见 SKILL.md 登录流程表）
  const steps = [
    { label: '节点',    value: opts.node,        triggers: ['请选择目标资产', '目标资产'] },
    { label: '账号类型', value: opts.accountType,  triggers: ['请选择登录账号', '登录账号'] },
    { label: '用户名',  value: opts.username,      triggers: ['login:', 'login as'] },
    { label: '密码',    value: opts.password,       triggers: ['password:'] },
  ];

  const result = await connectAndExec({
    host: sshHost,
    port: sshPort,
    username: sshUser,
    privateKey: fs.readFileSync(identityFile),
    hostKeyAlgos,
  }, steps, opts.command);

  if (result.success) {
    if (result.output) console.log(result.output);
  } else {
    const code = result.exitCode || 1;
    console.error(`\n命令执行失败${result.exitCode ? ` (退出码 ${result.exitCode})` : ''}`);
    if (result.output) console.error(result.output);
    process.exit(code);
  }
}

// ─── 核心连接与执行 ──────────────────────────────────────────────
function connectAndExec(sshParams, steps, command) {
  // 哨兵标记：可靠划定命令输出边界并捕获真实退出码。
  // 登录成功通过 shell 提示符判定（提前发送命令会被登录流程吞掉）。
  const START = '__BASTION_EXEC_START__';
  const END = '__BASTION_EXEC_END__';
  const END_RE = new RegExp(END.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '(\\d+)');
  const FAIL_RE = /password:|login incorrect|permission denied|authentication failed/i;

  const TIMEOUTS = {
    stage: 30000,    // 单个登录阶段等待上限
    command: 120000, // 命令执行上限
    idle: 120000,    // 无数据上限
    nudge: 5000,     // 首次无数据时发送换行促发菜单
  };

  return new Promise((resolve) => {
    const conn = new Client();
    let stream = null;
    let stepIndex = 0;
    let buffer = '';
    let phase = 'menu';        // 'menu' -> 'shell_wait' -> 'command' -> 'done'
    let commandOutput = '';
    let lastDataTime = Date.now();
    let stageTimer = null;
    let commandTimer = null;
    let idleTimer = null;
    let nudgeTimer = null;
    let settled = false;

    function stripAnsi(s) {
      return s.replace(/\x1b\[[0-9;?]*[a-zA-Z]/g, '');
    }

    function clearTimers() {
      if (stageTimer) { clearTimeout(stageTimer); stageTimer = null; }
      if (commandTimer) { clearTimeout(commandTimer); commandTimer = null; }
      if (idleTimer) { clearInterval(idleTimer); idleTimer = null; }
      if (nudgeTimer) { clearTimeout(nudgeTimer); nudgeTimer = null; }
    }

    function finish(result) {
      if (settled) return;
      settled = true;
      phase = 'done';
      clearTimers();
      try { stream && stream.write('exit\n'); } catch (_) { /* ignore */ }
      try { conn.end(); } catch (_) { /* ignore */ }
      resolve(result);
    }

    function armStageTimer(label) {
      clearTimeout(stageTimer);
      stageTimer = setTimeout(() => {
        if (phase === 'menu' || phase === 'shell_wait') {
          finish({ success: false, output: `登录阶段超时: 等待 "${label}"` });
        }
      }, TIMEOUTS.stage);
    }

    function armCommandTimer() {
      clearTimeout(commandTimer);
      commandTimer = setTimeout(() => {
        finish({ success: false, output: `命令执行超时 (${TIMEOUTS.command / 1000}s)` });
      }, TIMEOUTS.command);
    }

    function sendStep() {
      const current = steps[stepIndex];
      const display = current.label === '密码' ? '******' : current.value;
      log(`→ 步骤 ${stepIndex + 1}/${steps.length} (${current.label}): ${display}`);
      stream.write(current.value + '\n');
      stepIndex++;
      buffer = '';
      const nextLabel = stepIndex < steps.length ? steps[stepIndex].label : 'shell';
      armStageTimer(nextLabel);
    }

    function tryMenuStep() {
      if (phase !== 'menu' || stepIndex >= steps.length) return;
      const current = steps[stepIndex];
      const lower = buffer.toLowerCase();
      if (!current.triggers.some(t => lower.includes(t.toLowerCase()))) return;
      sendStep();
      if (stepIndex >= steps.length) enterShellWait();
    }

    function enterShellWait() {
      // 密码发送后需等待 shell 提示符再发命令；提前发送的字节会被登录流程吞掉。
      phase = 'shell_wait';
      clearTimeout(stageTimer);
      armStageTimer('shell');
    }

    function detectPrompt(buf) {
      // bash 默认 PS1 结尾形如 ]$ / $ / #
      return /[#$]\s*$/.test(buf);
    }

    function sendCommand() {
      phase = 'command';
      clearTimeout(stageTimer);
      commandOutput = '';
      // 哨兵包裹：echo START 划定起点；命令结束后 echo END$? 携带退出码划定终点
      const wrapped = `echo ${START}; ${command}; __rc=$?; echo ${END}$__rc`;
      log(`▶ 执行: ${command}`);
      stream.write(wrapped + '\n');
      armCommandTimer();
    }

    function extractOutput(clean, startIdx, endIdx) {
      // startIdx 指向真正输出的 START（跳过回显行中的 START，因为它出现得更早）
      let out = startIdx === -1
        ? clean.slice(0, endIdx)
        : clean.slice(startIdx + START.length, endIdx);
      out = out.replace(/\r\n/g, '\n').replace(/\r/g, '');  // CRLF→LF，软换行 \r 合并（目标 tty 80 列会折行）
      out = out.replace(/^\n/, '');      // 去掉 START 行尾换行
      out = out.replace(/\n+$/, '');     // 去掉末尾换行
      return out;
    }

    function handleCommandData(str) {
      commandOutput += str;
      const clean = stripAnsi(commandOutput);

      // 命令若退出 shell（如含 exit），堡垒机会提示会话结束、END 永不出现
      if (/会话已结束|Connection closed|目标资产列表|请选择目标资产/.test(clean)) {
        finish({ success: false, exitCode: 1,
                 output: '命令执行失败: 目标 shell 已退出（命令可能包含 exit）' });
        return;
      }

      // 在真正 START 之后查找 END+退出码
      const startIdx = clean.lastIndexOf(START);
      const searchFrom = startIdx === -1 ? 0 : startIdx + START.length;
      const m = END_RE.exec(clean.slice(searchFrom));
      if (m) {
        const rc = parseInt(m[1], 10);
        const endIdx = searchFrom + m.index;
        finish({ success: rc === 0, exitCode: rc, output: extractOutput(clean, startIdx, endIdx) });
      }
    }

    // ── SSH 事件 ──
    conn.on('ready', () => {
      log('SSH 连接已建立，打开交互式 Shell ...');
      conn.shell({ pty: { term: 'xterm-256color', cols: 200, rows: 40 } }, (err, _stream) => {
        if (err) {
          finish({ success: false, output: `Shell 打开失败: ${err.message}` });
          return;
        }
        stream = _stream;
        lastDataTime = Date.now();

        // 若 5s 内无任何输出，发送换行促发堡垒机菜单
        nudgeTimer = setTimeout(() => {
          if (phase === 'menu' && stepIndex === 0 && !buffer) {
            stream.write('\n');
          }
        }, TIMEOUTS.nudge);

        stream.on('data', (data) => {
          const str = data.toString();
          if (process.env.BASTION_DEBUG) process.stderr.write(`[raw:${phase}] ${JSON.stringify(str)}\n`);
          lastDataTime = Date.now();
          if (phase === 'menu') {
            buffer += str;
            tryMenuStep();
          } else if (phase === 'shell_wait') {
            buffer += str;
            const clean = stripAnsi(buffer);
            if (FAIL_RE.test(clean)) {
              finish({ success: false, exitCode: 1, output: '登录失败: 用户名或密码错误' });
            } else if (detectPrompt(clean)) {
              log('✓ 已登录到目标节点');
              sendCommand();
            }
          } else if (phase === 'command') {
            handleCommandData(str);
          }
        });

        stream.stderr.on('data', (data) => {
          lastDataTime = Date.now();
          if (phase === 'menu') {
            buffer += data.toString();
            tryMenuStep();
          }
        });

        stream.on('close', () => {
          if (settled) return;
          if (phase === 'command' && commandOutput) {
            const clean = stripAnsi(commandOutput);
            const startIdx = clean.lastIndexOf(START);
            const searchFrom = startIdx === -1 ? 0 : startIdx + START.length;
            const m = END_RE.exec(clean.slice(searchFrom));
            if (m) {
              const rc = parseInt(m[1], 10);
              finish({ success: rc === 0, exitCode: rc,
                       output: extractOutput(clean, startIdx, searchFrom + m.index) });
              return;
            }
          }
          finish({ success: false, output: 'SSH 会话意外关闭' });
        });
      });
    });

    conn.on('error', (err) => {
      finish({ success: false, output: `SSH 连接错误: ${err.message}` });
    });

    // 全局无数据看门狗
    idleTimer = setInterval(() => {
      if (phase === 'done') return;
      if (Date.now() - lastDataTime > TIMEOUTS.idle) {
        finish({ success: false, output: `超时: ${TIMEOUTS.idle / 1000}s 内无数据` });
      }
    }, 5000);

    const connectParams = {
      host: sshParams.host,
      port: sshParams.port,
      username: sshParams.username,
      privateKey: sshParams.privateKey,
      readyTimeout: 30000,
      keepaliveInterval: 10000,
      algorithms: {
        kex: [
          'diffie-hellman-group14-sha256',
          'diffie-hellman-group14-sha1',
          'diffie-hellman-group-exchange-sha256',
          'diffie-hellman-group-exchange-sha1',
          'ecdh-sha2-nistp256',
          'ecdh-sha2-nistp384',
          'ecdh-sha2-nistp521',
          'curve25519-sha256',
        ],
      },
    };

    if (sshParams.hostKeyAlgos && sshParams.hostKeyAlgos.length > 0) {
      connectParams.algorithms.serverHostKey = sshParams.hostKeyAlgos;
    }

    conn.connect(connectParams);
  });
}

// ─── 工具函数 ────────────────────────────────────────────────────
function log(msg) {
  process.stderr.write(`[bastion] ${msg}\n`);
}

function die(msg) {
  process.stderr.write(`[bastion] 错误: ${msg}\n`);
  process.exit(1);
}

// ─── 入口 ────────────────────────────────────────────────────────
main().catch((err) => {
  process.stderr.write(`[bastion] 未捕获错误: ${err.message}\n`);
  process.exit(1);
});
