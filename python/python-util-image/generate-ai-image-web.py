#!/usr/bin/env python3
"""
Right Code 图片生成 Web 界面
基于 /v1/images/generations 接口
提供可视化 Web 界面，支持重复操作、历史记录、图片预览与下载
"""

import json
import os
import sys
import urllib.request
import urllib.error
from datetime import datetime
from pathlib import Path

from flask import Flask, render_template_string, request, jsonify, send_from_directory

# ── 配置 ──────────────────────────────────────────────
BASE_URL = "https://www.right.codes/draw/v1/images/generations"
API_KEY_ENV = os.environ.get("RIGHT_CODES_API_KEY", "")
DOWNLOAD_DIR = Path(__file__).parent / "generated_images"
DOWNLOAD_DIR.mkdir(parents=True, exist_ok=True)
MAX_HISTORY = 50  # 最大历史记录数

# 预设分辨率
SIZE_PRESETS = [
    {"label": "1024×1024", "value": "1024x1024"},
    {"label": "2048×2048", "value": "2048x2048"},
    {"label": "4096×4096", "value": "4096x4096"},
]

# 预设模型
MODEL_PRESETS = [
    {"label": "gpt-image-2", "value": "gpt-image-2"},
    {"label": "gpt-image-2-vip", "value": "gpt-image-2-vip"},
    {"label": "nano-banana-2", "value": "nano-banana-2"},
    {"label": "nano-banana-pro", "value": "nano-banana-pro"},
]

# ── Flask App ──────────────────────────────────────────
app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = 16 * 1024 * 1024  # 16MB

# 内存中的生成历史（服务重启会清空）
generation_history: list[dict] = []

# ── HTML 模板 ─────────────────────────────────────────
HTML_TEMPLATE = r"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>AI 图片生成器</title>
<style>
  :root {
    --bg: #0f1117;
    --surface: #1a1d27;
    --surface2: #242836;
    --border: #2e3346;
    --text: #e4e6ed;
    --text2: #9498a8;
    --accent: #6c5ce7;
    --accent2: #a29bfe;
    --success: #00b894;
    --danger: #e17055;
    --radius: 12px;
  }
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    background: var(--bg);
    color: var(--text);
    min-height: 100vh;
    line-height: 1.6;
  }
  .container {
    max-width: 960px;
    margin: 0 auto;
    padding: 24px 16px;
  }
  header {
    text-align: center;
    margin-bottom: 32px;
  }
  header h1 {
    font-size: 28px;
    font-weight: 700;
    background: linear-gradient(135deg, var(--accent), var(--accent2));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  header p { color: var(--text2); font-size: 14px; margin-top: 4px; }

  /* 表单样式 */
  .form-card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 24px;
    margin-bottom: 24px;
  }
  .form-group { margin-bottom: 18px; }
  .form-group:last-child { margin-bottom: 0; }
  label {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: var(--text2);
    margin-bottom: 6px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  input[type="text"],
  input[type="password"],
  textarea,
  select {
    width: 100%;
    background: var(--surface2);
    border: 1px solid var(--border);
    border-radius: 8px;
    padding: 10px 14px;
    color: var(--text);
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;
  }
  input:focus, textarea:focus, select:focus {
    border-color: var(--accent);
  }
  textarea { resize: vertical; min-height: 80px; }

  /* 单选按钮组 */
  .radio-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  .radio-group input[type="radio"] { display: none; }
  .radio-group label {
    display: inline-block;
    padding: 8px 16px;
    background: var(--surface2);
    border: 1px solid var(--border);
    border-radius: 8px;
    cursor: pointer;
    font-size: 13px;
    font-weight: 500;
    color: var(--text2);
    text-transform: none;
    letter-spacing: 0;
    margin-bottom: 0;
    transition: all 0.2s;
  }
  .radio-group input[type="radio"]:checked + label {
    background: var(--accent);
    border-color: var(--accent);
    color: #fff;
  }
  .radio-group label:hover {
    border-color: var(--accent2);
  }
  .custom-size-input {
    margin-top: 10px;
    display: none;
  }
  .custom-size-input.active { display: block; }

  /* 生成按钮 */
  .btn-generate {
    width: 100%;
    padding: 14px;
    background: linear-gradient(135deg, var(--accent), var(--accent2));
    border: none;
    border-radius: 8px;
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: opacity 0.2s, transform 0.1s;
  }
  .btn-generate:hover { opacity: 0.9; }
  .btn-generate:active { transform: scale(0.98); }
  .btn-generate:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  /* API Key 行 */
  .api-key-row {
    display: flex;
    gap: 8px;
    align-items: center;
  }
  .api-key-row input { flex: 1; }
  .btn-toggle-visibility {
    padding: 10px 14px;
    background: var(--surface2);
    border: 1px solid var(--border);
    border-radius: 8px;
    color: var(--text2);
    cursor: pointer;
    font-size: 14px;
  }
  .btn-toggle-visibility:hover { border-color: var(--accent2); }

  /* 加载状态 */
  .loading-overlay {
    display: none;
    position: fixed;
    inset: 0;
    background: rgba(15, 17, 23, 0.8);
    z-index: 100;
    justify-content: center;
    align-items: center;
  }
  .loading-overlay.active { display: flex; }
  .loading-box {
    background: var(--surface);
    border-radius: var(--radius);
    padding: 40px 48px;
    text-align: center;
  }
  .spinner {
    width: 48px;
    height: 48px;
    border: 4px solid var(--border);
    border-top-color: var(--accent);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin: 0 auto 16px;
  }
  @keyframes spin { to { transform: rotate(360deg); } }

  /* 结果 */
  .result-card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 24px;
    margin-bottom: 24px;
    display: none;
  }
  .result-card.active { display: block; }
  .result-card.error { border-color: var(--danger); }
  .result-card .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
  .result-card h3 {
    font-size: 16px;
    font-weight: 600;
  }
  .result-meta {
    font-size: 12px;
    color: var(--text2);
    margin-bottom: 16px;
  }
  .result-meta span { margin-right: 16px; }
  .image-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 16px;
  }
  .image-item {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    background: var(--surface2);
  }
  .image-item img {
    width: 100%;
    display: block;
    cursor: zoom-in;
  }
  .image-actions {
    display: flex;
    gap: 8px;
    padding: 10px;
  }
  .btn-action {
    flex: 1;
    padding: 8px 12px;
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 6px;
    color: var(--text2);
    font-size: 12px;
    cursor: pointer;
    text-align: center;
    text-decoration: none;
    transition: all 0.2s;
  }
  .btn-action:hover {
    border-color: var(--accent);
    color: var(--text);
  }

  /* 图片灯箱 */
  .lightbox {
    display: none;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.9);
    z-index: 200;
    justify-content: center;
    align-items: center;
    cursor: zoom-out;
  }
  .lightbox.active { display: flex; }
  .lightbox img {
    max-width: 95vw;
    max-height: 95vh;
    object-fit: contain;
  }

  /* 历史记录 */
  .history-section { margin-top: 32px; }
  .history-section h2 {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 16px;
  }
  .history-list { display: flex; flex-direction: column; gap: 12px; }
  .history-item {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 16px;
    cursor: pointer;
    transition: border-color 0.2s;
  }
  .history-item:hover { border-color: var(--accent2); }
  .history-prompt {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .history-meta {
    font-size: 12px;
    color: var(--text2);
  }
  .history-meta span { margin-right: 12px; }

  /* Toast */
  .toast {
    position: fixed;
    bottom: 24px;
    left: 50%;
    transform: translateX(-50%) translateY(80px);
    background: var(--danger);
    color: #fff;
    padding: 12px 24px;
    border-radius: 8px;
    font-size: 14px;
    z-index: 300;
    opacity: 0;
    transition: all 0.3s;
  }
  .toast.show {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }

  @media (max-width: 600px) {
    .container { padding: 16px 12px; }
    .form-card { padding: 16px; }
    .image-grid { grid-template-columns: 1fr; }
  }
</style>
</head>
<body>

<div class="container">
  <header>
    <h1>🎨 AI 图片生成器</h1>
    <p>基于 Right Code API · 支持多种模型与分辨率</p>
  </header>

  <!-- 生成表单 -->
  <div class="form-card">
    <div class="form-group">
      <label>API Key</label>
      <div class="api-key-row">
        <input type="password" id="apiKey" placeholder="sk-xxxxx" value="">
        <button class="btn-toggle-visibility" onclick="toggleKeyVisibility()" title="显示/隐藏">👁</button>
      </div>
    </div>

    <div class="form-group">
      <label>提示词 (Prompt)</label>
      <textarea id="prompt" placeholder="描述你想要生成的图片..."></textarea>
    </div>

    <div class="form-group">
      <label>分辨率</label>
      <div class="radio-group">
        <input type="radio" name="size" id="size1" value="1024x1024" checked>
        <label for="size1">1024×1024</label>
        <input type="radio" name="size" id="size2" value="2048x2048">
        <label for="size2">2048×2048</label>
        <input type="radio" name="size" id="size3" value="4096x4096">
        <label for="size3">4096×4096</label>
        <input type="radio" name="size" id="size4" value="custom">
        <label for="size4">自定义</label>
      </div>
      <div class="custom-size-input" id="customSizeBox">
        <input type="text" id="customSize" placeholder="如 1536x1024" value="1536x1024">
      </div>
    </div>

    <div class="form-group">
      <label>模型</label>
      <div class="radio-group">
        <input type="radio" name="model" id="model1" value="gpt-image-2" checked>
        <label for="model1">gpt-image-2</label>
        <input type="radio" name="model" id="model2" value="gpt-image-2-vip">
        <label for="model2">gpt-image-2-vip</label>
        <input type="radio" name="model" id="model3" value="nano-banana-2">
        <label for="model3">nano-banana-2</label>
        <input type="radio" name="model" id="model4" value="nano-banana-pro">
        <label for="model4">nano-banana-pro</label>
      </div>
    </div>

    <button class="btn-generate" id="btnGenerate" onclick="generateImage()">
      ✨ 生成图片
    </button>
  </div>

  <!-- 结果展示 -->
  <div class="result-card" id="resultCard">
    <div class="result-header">
      <h3 id="resultTitle">生成结果</h3>
    </div>
    <div class="result-meta" id="resultMeta"></div>
    <div class="image-grid" id="imageGrid"></div>
  </div>

  <!-- 历史记录 -->
  <div class="history-section" id="historySection" style="display:none">
    <h2>📜 生成历史</h2>
    <div class="history-list" id="historyList"></div>
  </div>
</div>

<!-- 加载遮罩 -->
<div class="loading-overlay" id="loadingOverlay">
  <div class="loading-box">
    <div class="spinner"></div>
    <div>🎨 生成中，请稍候...</div>
  </div>
</div>

<!-- 灯箱 -->
<div class="lightbox" id="lightbox" onclick="closeLightbox()">
  <img id="lightboxImg" src="" alt="">
</div>

<!-- Toast -->
<div class="toast" id="toast"></div>

<script>
  // API Key 可见性切换
  function toggleKeyVisibility() {
    const input = document.getElementById('apiKey');
    input.type = input.type === 'password' ? 'text' : 'password';
  }

  // 自定义分辨率显示切换
  document.querySelectorAll('input[name="size"]').forEach(r => {
    r.addEventListener('change', () => {
      document.getElementById('customSizeBox')
        .classList.toggle('active', r.value === 'custom' && r.checked);
    });
  });

  // 获取当前选中的值
  function getSelectedSize() {
    const val = document.querySelector('input[name="size"]:checked').value;
    if (val === 'custom') {
      const custom = document.getElementById('customSize').value.trim().toLowerCase();
      if (/^\d+x\d+$/.test(custom)) return custom;
      showToast('自定义分辨率格式错误，请使用 WxH 格式');
      return null;
    }
    return val;
  }

  function getSelectedModel() {
    return document.querySelector('input[name="model"]:checked').value;
  }

  // Toast 提示
  function showToast(msg, duration = 3000) {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), duration);
  }

  // 灯箱
  function openLightbox(src) {
    document.getElementById('lightboxImg').src = src;
    document.getElementById('lightbox').classList.add('active');
  }
  function closeLightbox() {
    document.getElementById('lightbox').classList.remove('active');
  }
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeLightbox();
  });

  // 生成图片
  async function generateImage() {
    const apiKey = document.getElementById('apiKey').value.trim();
    if (!apiKey) { showToast('请输入 API Key'); return; }

    const prompt = document.getElementById('prompt').value.trim();
    if (!prompt) { showToast('请输入提示词'); return; }

    const size = getSelectedSize();
    if (!size) return;

    const model = getSelectedModel();

    // 显示加载
    const overlay = document.getElementById('loadingOverlay');
    const btn = document.getElementById('btnGenerate');
    overlay.classList.add('active');
    btn.disabled = true;

    try {
      const resp = await fetch('/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ api_key: apiKey, prompt, size, model }),
      });

      const data = await resp.json();

      if (!resp.ok || data.error) {
        showToast(data.error || '请求失败');
        return;
      }

      renderResult(data, prompt, model, size);
    } catch (err) {
      showToast('网络错误: ' + err.message);
    } finally {
      overlay.classList.remove('active');
      btn.disabled = false;
    }
  }

  // 渲染结果
  function renderResult(data, prompt, model, size) {
    const card = document.getElementById('resultCard');
    const grid = document.getElementById('imageGrid');
    const title = document.getElementById('resultTitle');
    const meta = document.getElementById('resultMeta');

    card.classList.remove('error');
    card.classList.add('active');
    title.textContent = '✅ 生成成功';

    // 元信息
    const usage = data.usage || {};
    meta.innerHTML = `
      <span>模型: ${model}</span>
      <span>分辨率: ${size}</span>
      <span>图片: ${data.images.length} 张</span>
      ${usage.total_tokens ? '<span>Token: ' + usage.total_tokens + '</span>' : ''}
    `;

    // 图片网格
    grid.innerHTML = '';
    data.images.forEach(img => {
      const item = document.createElement('div');
      item.className = 'image-item';
      item.innerHTML = `
        <img src="${img.url}" alt="${prompt}" onclick="openLightbox('${img.url}')">
        <div class="image-actions">
          <a class="btn-action" href="${img.url}" target="_blank">🔗 新标签打开</a>
          <a class="btn-action" href="/download/${img.filename}" download>⬇ 下载</a>
        </div>
      `;
      grid.appendChild(item);
    });

    // 加入历史
    addToHistory(prompt, model, size, data.images, data.usage);
  }

  // 历史记录
  function addToHistory(prompt, model, size, images, usage) {
    const section = document.getElementById('historySection');
    const list = document.getElementById('historyList');

    const item = document.createElement('div');
    item.className = 'history-item';
    const time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    item.innerHTML = `
      <div class="history-prompt">${escapeHtml(prompt)}</div>
      <div class="history-meta">
        <span>${time}</span>
        <span>${model}</span>
        <span>${size}</span>
        <span>${images.length} 张图片</span>
      </div>
    `;
    item.onclick = () => {
      // 点击历史项回填表单
      document.getElementById('prompt').value = prompt;
      // 选择对应模型
      document.querySelectorAll('input[name="model"]').forEach(r => {
        r.checked = r.value === model;
      });
      // 选择对应分辨率
      const sizeRadios = document.querySelectorAll('input[name="size"]');
      let found = false;
      sizeRadios.forEach(r => {
        if (r.value === size) { r.checked = true; found = true; }
      });
      if (!found) {
        document.getElementById('size4').checked = true;
        document.getElementById('customSize').value = size;
        document.getElementById('customSizeBox').classList.add('active');
      }
      window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    list.insertBefore(item, list.firstChild);

    // 限制历史大小
    while (list.children.length > {{ max_history }}) {
      list.removeChild(list.lastChild);
    }

    section.style.display = 'block';
  }

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  // 支持回车快捷键
  document.getElementById('prompt').addEventListener('keydown', e => {
    if (e.key === 'Enter' && e.ctrlKey) generateImage();
  });
</script>
</body>
</html>"""


# ── API 核心逻辑 ──────────────────────────────────────
def call_api(api_key: str, model: str, prompt: str, size: str) -> dict:
    """调用 Right Code 图片生成 API"""
    payload = json.dumps({
        "model": model,
        "prompt": prompt,
        "size": size,
        "response_format": "url",
    }).encode("utf-8")

    req = urllib.request.Request(
        BASE_URL,
        data=payload,
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {api_key}",
        },
        method="POST",
    )

    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        raise ValueError(f"API 请求失败 (HTTP {e.code}): {body}")
    except urllib.error.URLError as e:
        raise ValueError(f"网络错误: {e.reason}")


def download_image(url: str, dest: Path) -> Path:
    """下载图片到本地"""
    dest.parent.mkdir(parents=True, exist_ok=True)
    urllib.request.urlretrieve(url, dest)
    return dest


# ── 路由 ──────────────────────────────────────────────
@app.route("/")
def index():
    html = HTML_TEMPLATE.replace("{{ max_history }}", str(MAX_HISTORY))
    # 注入环境变量中的 API Key
    import re
    if API_KEY_ENV:
        html = re.sub(
            r'(id="apiKey"\s+placeholder="sk-xxxxx"\s+value=")[^"]*(")',
            rf'\1{API_KEY_ENV}\2',
            html,
        )
    return html


@app.route("/api/generate", methods=["POST"])
def api_generate():
    data = request.get_json(force=True)

    api_key = data.get("api_key", "").strip()
    prompt = data.get("prompt", "").strip()
    size = data.get("size", "1024x1024")
    model = data.get("model", "gpt-image-2")

    if not api_key:
        return jsonify({"error": "API Key 不能为空"}), 400
    if not prompt:
        return jsonify({"error": "提示词不能为空"}), 400

    try:
        result = call_api(api_key, model, prompt, size)
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        return jsonify({"error": f"未知错误: {e}"}), 500

    images = result.get("data", [])
    if not images:
        return jsonify({"error": "未返回图片数据"}), 400

    # 下载图片
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    downloaded = []
    for i, img in enumerate(images):
        url = img.get("url")
        if not url:
            continue
        ext = Path(url.split("?")[0]).suffix or ".png"
        filename = f"{timestamp}_{i + 1}{ext}"
        dest = DOWNLOAD_DIR / filename
        try:
            download_image(url, dest)
            # 构建本地预览 URL
            preview_url = f"/images/{filename}"
            downloaded.append({
                "url": preview_url,
                "original_url": url,
                "filename": filename,
            })
        except Exception as e:
            downloaded.append({
                "url": url,
                "original_url": url,
                "filename": "",
                "error": str(e),
            })

    # 保存历史
    generation_history.insert(0, {
        "prompt": prompt,
        "model": model,
        "size": size,
        "timestamp": timestamp,
        "images": downloaded,
        "usage": result.get("usage", {}),
    })
    if len(generation_history) > MAX_HISTORY:
        generation_history.pop()

    return jsonify({
        "images": downloaded,
        "usage": result.get("usage", {}),
    })


@app.route("/images/<path:filename>")
def serve_image(filename):
    """提供本地已下载图片的预览"""
    return send_from_directory(str(DOWNLOAD_DIR), filename)


@app.route("/download/<path:filename>")
def download_file(filename):
    """下载图片文件"""
    return send_from_directory(
        str(DOWNLOAD_DIR), filename, as_attachment=True
    )


# ── 入口 ──────────────────────────────────────────────
if __name__ == "__main__":
    print("=" * 52)
    print("  AI 图片生成器 Web 界面")
    print("  访问 http://127.0.0.1:5000")
    print("=" * 52)
    app.run(host="127.0.0.1", port=5000, debug=True)