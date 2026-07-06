#!/usr/bin/env node
/**
 * bastion-repl.js
 *
 * 交互式长连接模式：登录堡垒机一次，保持会话，逐条执行命令。
 * 同一 shell 会话中 cd / export / 后台进程等状态跨命令保持，
 * 无需每条命令重新走 4 步登录流程。
 *
 * 用法:
 *   node bastion-repl.js                      使用默认节点进入交互会话
 *   node bastion-repl.js -n 10.224.26.7 -u root
 *
 * 交互中:
 *   输入命令回车执行；exit / quit / logout 或 Ctrl-D 退出会话。
 */

const { Client } = require('ssh2');
const fs = require('fs');
const path = require('path');
const os = require('os');
const readline = require('readline');

// ─── 默认登录参数（与 bastion-exec.js 一致）─────────────────────
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
      if (!hosts[currentHost]) hosts[currentHost] = {};
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
  if (rawPath.startsWith('~/')) return path.join(os.homedir(), rawPath.slice(2));
  if (rawPath.startsWith('~')) return path.join(os.homedir(), rawPath.slice(1));
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
  };

  let i = 0;
  while (i < args.length) {
    switch (args[i]) {
      case '--node': case '-n': opts.node = args[++i]; break;
      case '--account-type': case '-t': opts.accountType = args[++i]; break;
      case '--user': case '-u': opts.username = args[++i]; break;
      case '--password': case '-p': opts.password = args[++i]; break;
      case '--bastion': case '-b': opts.bastion = args[++i]; break;
      case '--help': case '-h': printUsage(); process.exit(0);
      default:
        console.error(`未知参数: ${args[i]}`);
        printUsage();
        process.exit(1);
    }
    i++;
  }
  return opts;
}

function printUsage() {
  console.log(`
用法: node bastion-repl.js [选项]

启动交互式长连接会话：登录堡垒机一次，保持会话，逐条执行命令。
同一 shell 会话中 cd / export 等状态跨命令保持。

选项:
  -n, --node <IP>        目标节点 IP（默认: ${DEFAULTS.targetNode}）
  -t, --account-type <n> 账号类型: 1=any, 2=self（默认: ${DEFAULTS.accountType}）
  -u, --user <用户名>    目标节点用户名（默认: ${DEFAULTS.username}）
  -p, --password <密码>  目标节点密码
  -b, --bastion <别名>   堡垒机 SSH config Host（默认: ${DEFAULTS.bastionHost}）
  -h, --help             显示此帮助

交互:
  输入命令回车执行；exit / quit / logout 或 Ctrl-D 退出会话。
`);
}

// ─── 主逻辑 ──────────────────────────────────────────────────────
async function main() {
  const opts = parseArgs();

  const sshConfigPath = path.join(os.homedir(), '.ssh', 'config');
  if (!fs.existsSync(sshConfigPath)) die(`SSH config 不存在: ${sshConfigPath}`);

  const sshCfg = parseSshConfig(sshConfigPath, opts.bastion);
  const sshHost = sshCfg.hostname || opts.bastion;
  const sshPort = parseInt(sshCfg.port || '22', 10);
  const sshUser = sshCfg.user || os.userInfo().username;
  const identityFile = sshCfg.identityfile ? resolvePath(sshCfg.identityfile) : null;

  if (!identityFile) die('SSH config 中未配置 IdentityFile');
  if (!fs.existsSync(identityFile)) die(`私钥文件不存在: ${identityFile}`);

  let hostKeyAlgos = undefined;
  if (sshCfg.hostkeyalgorithms) {
    hostKeyAlgos = sshCfg.hostkeyalgorithms
      .split(',').map(s => s.trim().replace(/^\+/, '')).filter(Boolean);
  }

  log(`堡垒机: ${sshUser}@${sshHost}:${sshPort}`);
  log(`目标节点: ${opts.node}`);
  log(`目标用户: ${opts.username}\n`);

  const steps = [
    { label: '节点',    value: opts.node,        triggers: ['请选择目标资产', '目标资产'] },
    { label: '账号类型', value: opts.accountType,  triggers: ['请选择登录账号', '登录账号'] },
    { label: '用户名',  value: opts.username,      triggers: ['login:', 'login as'] },
    { label: '密码',    value: opts.password,       triggers: ['password:'] },
  ];

  let session;
  try {
    session = await createSession({
      host: sshHost, port: sshPort, username: sshUser,
      privateKey: fs.readFileSync(identityFile), hostKeyAlgos,
    }, steps);
  } catch (e) {
    die(e.message);
  }

  await runRepl(session);
}

// ─── 会话抽象：登录一次，可复用 exec，最后 close ─────────────────
function createSession(sshParams, steps) {
  // 哨兵标记：可靠划定命令输出边界并捕获真实退出码。
  // 登录成功通过 shell 提示符判定（提前发送命令会被登录流程吞掉）。
  const START = '__BASTION_EXEC_START__';
  const END = '__BASTION_EXEC_END__';
  const END_RE = new RegExp(END.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '(\\d+)');
  const FAIL_RE = /password:|login incorrect|permission denied|authentication failed/i;
  const EXIT_RE = /会话已结束|Connection closed|目标资产列表|请选择目标资产/;

  const TIMEOUTS = { stage: 30000, command: 120000, nudge: 5000 };

  return new Promise((resolveSession, rejectSession) => {
    const conn = new Client();
    let stream = null;
    let stepIndex = 0;
    let buffer = '';
    let phase = 'menu';        // menu -> shell_wait -> idle -> command -> idle ...
    let commandOutput = '';
    let stageTimer = null;
    let commandTimer = null;
    let nudgeTimer = null;
    let dead = false;
    let pendingExec = null;

    function stripAnsi(s) {
      return s.replace(/\x1b\[[0-9;?]*[a-zA-Z]/g, '');
    }

    function clearTimers() {
      clearTimeout(stageTimer);
      clearTimeout(commandTimer);
      clearTimeout(nudgeTimer);
      stageTimer = commandTimer = nudgeTimer = null;
    }

    function armStageTimer(label) {
      clearTimeout(stageTimer);
      stageTimer = setTimeout(() => {
        if (phase === 'menu' || phase === 'shell_wait') {
          fail(`登录阶段超时: 等待 "${label}"`);
        }
      }, TIMEOUTS.stage);
    }

    function armCommandTimer() {
      clearTimeout(commandTimer);
      commandTimer = setTimeout(() => {
        // 命令超时：发 Ctrl-C 尝试中断，报错但保留会话
        try { stream.write('\x03'); } catch (_) { /* ignore */ }
        finishExec({ success: false, exitCode: 1, output: `命令执行超时 (${TIMEOUTS.command / 1000}s)` });
      }, TIMEOUTS.command);
    }

    // 登录阶段失败
    function fail(msg) {
      if (dead) return;
      dead = true;
      clearTimers();
      try { conn.end(); } catch (_) { /* ignore */ }
      rejectSession(new Error(msg));
    }

    // 连接意外断开（登录后）
    function handleDisconnect(msg) {
      if (dead) return;
      dead = true;
      clearTimers();
      if (pendingExec) {
        const p = pendingExec; pendingExec = null;
        p.resolve({ success: false, exitCode: 1, output: msg });
      } else if (phase === 'menu' || phase === 'shell_wait') {
        rejectSession(new Error(msg));
      }
      // idle: 仅标记 dead，REPL 下次 exec 或检测时发现
    }

    function finishExec(result) {
      clearTimeout(commandTimer); commandTimer = null;
      phase = 'idle';
      commandOutput = '';
      if (pendingExec) { const p = pendingExec; pendingExec = null; p.resolve(result); }
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
      armStageTimer('shell');
    }

    function detectPrompt(buf) {
      // bash 默认 PS1 结尾形如 ]$ / $ / #
      return /[#$]\s*$/.test(buf);
    }

    function onLoggedIn() {
      clearTimeout(stageTimer); stageTimer = null;
      phase = 'idle';
      log('✓ 已登录到目标节点');
      resolveSession({
        exec: (cmd) => execOne(cmd),
        close: () => closeSession(),
        get dead() { return dead; },
      });
    }

    function execOne(cmd) {
      return new Promise((resolve) => {
        if (dead) return resolve({ success: false, exitCode: 1, output: '会话已断开' });
        if (phase !== 'idle') return resolve({ success: false, exitCode: 1, output: '上一条命令仍在执行' });
        phase = 'command';
        commandOutput = '';
        pendingExec = { resolve };
        // 哨兵包裹：echo START 划定起点；命令结束后 echo END$? 携带退出码划定终点
        const wrapped = `echo ${START}; ${cmd}; __rc=$?; echo ${END}$__rc`;
        log(`▶ 执行: ${cmd}`);
        stream.write(wrapped + '\n');
        armCommandTimer();
      });
    }

    function closeSession() {
      if (dead) { try { conn.end(); } catch (_) { /* ignore */ } return Promise.resolve(); }
      dead = true;
      clearTimers();
      try { stream && stream.write('exit\n'); } catch (_) { /* ignore */ }
      try { conn.end(); } catch (_) { /* ignore */ }
      return Promise.resolve();
    }

    function handleCommandData(str) {
      commandOutput += str;
      const clean = stripAnsi(commandOutput);

      // 命令若退出 shell（如含 exit），堡垒机会提示会话结束、END 永不出现
      if (EXIT_RE.test(clean)) {
        dead = true;
        finishExec({ success: false, exitCode: 1,
                     output: '命令执行失败: 目标 shell 已退出（命令可能包含 exit）' });
        return;
      }

      // 在真正 START 之后查找 END+退出码
      const startIdx = clean.lastIndexOf(START);
      const searchFrom = startIdx === -1 ? 0 : startIdx + START.length;
      const m = END_RE.exec(clean.slice(searchFrom));
      if (m) {
        const rc = parseInt(m[1], 10);
        finishExec({ success: rc === 0, exitCode: rc,
                     output: extractOutput(clean, startIdx, searchFrom + m.index) });
      }
    }

    function extractOutput(clean, startIdx, endIdx) {
      // startIdx 指向真正输出的 START（跳过回显行中的 START，因为它出现得更早）
      let out = startIdx === -1
        ? clean.slice(0, endIdx)
        : clean.slice(startIdx + START.length, endIdx);
      out = out.replace(/\r\n/g, '\n').replace(/\r/g, '');  // CRLF→LF，软换行 \r 合并
      out = out.replace(/^\n/, '');      // 去掉 START 行尾换行
      out = out.replace(/\n+$/, '');     // 去掉末尾换行
      return out;
    }

    // ── SSH 事件 ──
    conn.on('ready', () => {
      log('SSH 连接已建立，打开交互式 Shell ...');
      conn.shell({ pty: { term: 'xterm-256color', cols: 200, rows: 40 } }, (err, _stream) => {
        if (err) { fail(`Shell 打开失败: ${err.message}`); return; }
        stream = _stream;

        // 若 5s 内无任何输出，发送换行促发堡垒机菜单
        nudgeTimer = setTimeout(() => {
          if (phase === 'menu' && stepIndex === 0 && !buffer) stream.write('\n');
        }, TIMEOUTS.nudge);

        stream.on('data', (data) => {
          const str = data.toString();
          if (process.env.BASTION_DEBUG) process.stderr.write(`[raw:${phase}] ${JSON.stringify(str)}\n`);
          if (phase === 'menu') {
            buffer += str;
            tryMenuStep();
          } else if (phase === 'shell_wait') {
            buffer += str;
            const clean = stripAnsi(buffer);
            if (FAIL_RE.test(clean)) {
              fail('登录失败: 用户名或密码错误');
            } else if (detectPrompt(clean)) {
              onLoggedIn();
            }
          } else if (phase === 'command') {
            handleCommandData(str);
          }
          // idle: 忽略（等待用户输入下条命令）
        });

        stream.stderr.on('data', (data) => {
          if (phase === 'menu') { buffer += data.toString(); tryMenuStep(); }
        });

        stream.on('close', () => handleDisconnect('SSH 会话意外关闭'));
      });
    });

    conn.on('error', (err) => handleDisconnect(`SSH 连接错误: ${err.message}`));

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

// ─── REPL 循环 ──────────────────────────────────────────────────
function runRepl(session) {
  return new Promise((resolve) => {
    const rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout,
      prompt: 'bastion> ',
    });

    let busy = false;
    let closing = false;
    const queue = [];

    process.stderr.write('已进入交互会话，输入命令执行，exit/quit/Ctrl-D 退出\n');
    safePrompt();

    // readline 已关闭（管道 EOF / Ctrl-D）后不再提示
    function safePrompt() { if (!rl.closed) rl.prompt(); }

    // 队列空且已收到退出信号时，真正关闭会话
    function maybeFinish() {
      if (closing && !busy && queue.length === 0) {
        if (!rl.closed) rl.close();
        session.close().then(() => resolve());
        return true;
      }
      return false;
    }

    // 串行执行：命令入队，逐条跑完再处理下一条，避免并发 exec
    async function drain() {
      if (busy) return;
      if (maybeFinish()) return;
      const cmd = queue.shift();
      if (cmd === undefined) { safePrompt(); return; }

      busy = true;
      let result;
      try {
        result = await session.exec(cmd);
      } catch (e) {
        result = { success: false, exitCode: 1, output: e.message };
      }
      busy = false;

      if (session.dead) {
        if (result.output) console.error(result.output);
        console.error('\n会话已断开，退出 REPL');
        session.close().then(() => resolve());
        return;
      }

      if (result.success) {
        if (result.output) console.log(result.output);
      } else {
        console.error(`(退出码 ${result.exitCode || 1})`);
        if (result.output) console.error(result.output);
      }
      if (maybeFinish()) return;
      safePrompt();
      drain();
    }

    rl.on('line', (line) => {
      const cmd = line.trim();
      if (!cmd) { safePrompt(); return; }
      if (['exit', 'quit', 'logout'].includes(cmd.toLowerCase())) {
        closing = true;
        drain();                 // 尝试排空后关闭
        return;
      }
      queue.push(cmd);
      drain();
    });

    rl.on('close', () => {
      closing = true;
      drain();                   // EOF：排空队列后关闭
    });
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
