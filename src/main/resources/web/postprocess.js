/*
 * ViewMD — Enhanced Markdown preview for Android Studio and IntelliJ-based IDEs
 * Copyright (C) 2026 Mina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

(function () {
  'use strict';

  function wrapTables() {
    const tables = document.querySelectorAll('table:not(.viewmd-wrapped)');
    if (tables.length === 0) return;

    for (let i = 0; i < tables.length; i++) {
      const table = tables[i];
      const parent = table.parentElement;
      if (parent && parent.classList.contains('viewmd-table-wrap')) {
        table.classList.add('viewmd-wrapped');
        continue;
      }
      const wrap = document.createElement('div');
      wrap.className = 'viewmd-table-wrap';
      parent.insertBefore(wrap, table);
      wrap.appendChild(table);
      table.classList.add('viewmd-wrapped');
    }
  }

  function enhanceFrontMatter() {
    const detailsFm = document.querySelector('details.fm:not(.viewmd-fm-processed)');
    if (detailsFm) {
      detailsFm.classList.add('viewmd-fm-processed');
      detailsFm.classList.add('viewmd-frontmatter-card');
      return;
    }

    const preBlocks = document.querySelectorAll('pre > code');
    for (let i = 0; i < Math.min(preBlocks.length, 3); i++) {
      const code = preBlocks[i];
      const text = (code.textContent || '').trim();
      if (text.startsWith('summary:') || (text.startsWith('---') && text.includes('summary:'))) {
        const pre = code.parentElement;
        if (pre && !pre.classList.contains('viewmd-fm-processed')) {
          pre.classList.add('viewmd-fm-processed');
          renderFrontMatterCard(pre, text);
          break;
        }
      }
    }
  }

  function renderFrontMatterCard(container, rawText) {
    const lines = rawText.replace(/^---/, '').replace(/---$/, '').trim().split('\n');
    let summary = '';
    let triggers = [];
    let indexed = true;
    const others = [];

    let inTriggers = false;
    lines.forEach((line) => {
      const trimmed = line.trim();
      const matchSummary = line.match(/^summary:\s*["']?(.*?)["']?$/);
      const matchTriggers = line.match(/^triggers:\s*(?:\[(.*?)\]|"?([^"\]]+)"?)?$/);
      const matchIndex = line.match(/^index:\s*(false|true)/i);

      if (matchSummary) {
        summary = matchSummary[1];
        inTriggers = false;
      } else if (matchTriggers && (matchTriggers[1] || matchTriggers[2])) {
        const val = matchTriggers[1] || matchTriggers[2];
        triggers = val.split(',').map((t) => t.trim().replace(/^["']|["']$/g, ''));
        inTriggers = false;
      } else if (trimmed === 'triggers:') {
        inTriggers = true;
      } else if (inTriggers && trimmed.startsWith('-')) {
        triggers.push(trimmed.replace(/^-\s*/, '').replace(/^["']|["']$/g, '').trim());
      } else if (matchIndex) {
        indexed = matchIndex[1].toLowerCase() === 'true';
        inTriggers = false;
      } else if (trimmed.length > 0) {
        others.push(trimmed);
        inTriggers = false;
      }
    });

    const card = document.createElement('details');
    card.className = 'viewmd-frontmatter-card';
    card.open = false;

    const summaryEl = document.createElement('summary');
    summaryEl.className = 'viewmd-frontmatter-header';
    summaryEl.innerHTML = `<span>Front matter</span> ${!indexed ? '<span class="viewmd-chip" style="background:#cf222e;color:#fff;">Not indexed</span>' : ''}`;
    card.appendChild(summaryEl);

    const bodyEl = document.createElement('div');
    bodyEl.style.marginTop = '8px';

    if (summary) {
      const sumDiv = document.createElement('div');
      sumDiv.style.marginBottom = '6px';
      sumDiv.innerHTML = `<strong>Summary:</strong> ${escapeHtml(summary)}`;
      bodyEl.appendChild(sumDiv);
    }

    if (triggers.length > 0) {
      const trigDiv = document.createElement('div');
      trigDiv.style.marginBottom = '6px';
      trigDiv.innerHTML = '<strong>Triggers:</strong> ';
      triggers.forEach((t) => {
        const chip = document.createElement('span');
        chip.className = 'viewmd-chip';
        chip.textContent = t;
        trigDiv.appendChild(chip);
      });
      bodyEl.appendChild(trigDiv);
    }

    if (others.length > 0) {
      const otherDiv = document.createElement('pre');
      otherDiv.style.margin = '4px 0 0';
      otherDiv.style.padding = '4px 8px';
      otherDiv.textContent = others.join('\n');
      bodyEl.appendChild(otherDiv);
    }

    card.appendChild(bodyEl);
    container.parentNode.replaceChild(card, container);
  }

  function resolveMermaidApi() {
    if (typeof window.mermaid !== 'undefined' && typeof window.mermaid.render === 'function') {
      return window.mermaid;
    }
    if (typeof window.extension === 'object' && window.extension) {
      if (typeof window.extension.mermaid === 'object' && typeof window.extension.mermaid.render === 'function') {
        window.mermaid = window.extension.mermaid;
        return window.mermaid;
      }
    }
    return null;
  }

  function renderMermaidDiagrams() {
    const mermaidApi = resolveMermaidApi();
    if (!mermaidApi) return;

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    try {
      mermaidApi.initialize({
        startOnLoad: false,
        theme: isDark ? 'dark' : 'default',
        securityLevel: 'loose'
      });
    } catch (e) {
      // Ignore if already initialized
    }

    // 1. Standalone pre.mermaid blocks
    const preBlocks = document.querySelectorAll('pre.mermaid:not(.viewmd-mermaid-rendered)');
    preBlocks.forEach((block, index) => {
      const source = (block.textContent || '').trim();
      renderMermaidElement(block, source, `mermaid-pre-${Date.now()}-${index}`);
    });

    // 2. JetBrains Markdown mermaid fence blocks (<div class="mermaid" data-actual-fence-content="...">)
    const jbDivBlocks = document.querySelectorAll('div.mermaid[data-actual-fence-content]:not(.viewmd-mermaid-rendered)');
    jbDivBlocks.forEach((block, index) => {
      let source = '';
      const b64 = block.getAttribute('data-actual-fence-content');
      if (b64) {
        try {
          source = decodeURIComponent(escape(window.atob(b64))).trim();
        } catch (e) {
          try {
            source = window.atob(b64).trim();
          } catch (e2) {
            source = (block.textContent || '').trim();
          }
        }
      } else {
        source = (block.textContent || '').trim();
      }

      // Also clean up any wrapping pre/code parent generated by JetBrains Markdown
      const parentCode = block.closest('code.language-mermaid');
      const targetElement = parentCode && parentCode.parentElement && parentCode.parentElement.tagName === 'PRE'
        ? parentCode.parentElement
        : (parentCode || block);

      renderMermaidElement(targetElement, source, `mermaid-jb-${Date.now()}-${index}`, block);
    });

    // 3. Generic code.language-mermaid blocks
    const codeBlocks = document.querySelectorAll('code.language-mermaid:not(.viewmd-mermaid-rendered)');
    codeBlocks.forEach((block, index) => {
      if (block.querySelector('div.mermaid[data-actual-fence-content]')) return;
      const source = (block.textContent || '').trim();
      const targetElement = block.parentElement && block.parentElement.tagName === 'PRE'
        ? block.parentElement
        : block;
      renderMermaidElement(targetElement, source, `mermaid-code-${Date.now()}-${index}`, block);
    });
  }

  function renderMermaidElement(targetElement, source, id, originalBlock) {
    if (!source) return;
    targetElement.classList.add('viewmd-mermaid-rendered');
    if (originalBlock) originalBlock.classList.add('viewmd-mermaid-rendered');

    const mermaidApi = resolveMermaidApi();
    if (!mermaidApi) return;

    mermaidApi.render(id, source).then(({ svg }) => {
      const container = document.createElement('div');
      container.className = 'viewmd-diagram-container';
      container.innerHTML = svg;
      targetElement.parentNode.replaceChild(container, targetElement);
    }).catch((err) => {
      console.warn('Mermaid rendering failed for:', source, err);
      const errDiv = document.createElement('div');
      errDiv.className = 'viewmd-diagram-error';
      errDiv.innerHTML = `
        <div class="viewmd-diagram-error-title">Mermaid rendering error</div>
        <pre class="viewmd-diagram-error-source"><code>${escapeHtml(source)}</code></pre>
      `;
      targetElement.parentNode.replaceChild(errDiv, targetElement);
    });
  }

  function escapeHtml(str) {
    return str.replace(/[&<>'"]/g, (tag) => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      "'": '&#39;',
      '"': '&quot;',
    }[tag] || tag));
  }

  function applyTheme(isDark) {
    const root = document.documentElement;
    if (isDark) {
      root.setAttribute('data-theme', 'dark');
      root.style.setProperty('color-scheme', 'dark');
      root.style.setProperty('--viewmd-fg', '#e6edf3');
      root.style.setProperty('--viewmd-bg', '#0d1117');
      root.style.setProperty('--viewmd-soft', '#161b22');
      root.style.setProperty('--viewmd-line', '#3d444d');
      root.style.setProperty('--viewmd-code', '#21262d');
      root.style.setProperty('--viewmd-link', '#4493f8');
      root.style.setProperty('--viewmd-table-border', '#30363d');
      root.style.setProperty('--viewmd-table-stripe', '#161b22');
    } else {
      root.setAttribute('data-theme', 'light');
      root.style.setProperty('color-scheme', 'light');
      root.style.setProperty('--viewmd-fg', '#1f2328');
      root.style.setProperty('--viewmd-bg', '#ffffff');
      root.style.setProperty('--viewmd-soft', '#f6f8fa');
      root.style.setProperty('--viewmd-line', '#d1d9e0');
      root.style.setProperty('--viewmd-code', '#eff1f3');
      root.style.setProperty('--viewmd-link', '#0969da');
      root.style.setProperty('--viewmd-table-border', '#d1d9e0');
      root.style.setProperty('--viewmd-table-stripe', '#f6f8fa');
    }

    const toggleBtn = document.getElementById('viewmd-theme-toggle');
    const toggleIcon = document.getElementById('viewmd-theme-icon');
    const toggleText = document.getElementById('viewmd-theme-text');
    if (toggleIcon) toggleIcon.textContent = isDark ? '☀️' : '🌙';
    if (toggleText) toggleText.textContent = isDark ? 'Light Mode' : 'Dark Mode';

    if (typeof window.mermaid !== 'undefined') {
      try {
        window.mermaid.initialize({
          theme: isDark ? 'dark' : 'default',
          securityLevel: 'loose'
        });
      } catch (e) {
      }
      // Trigger diagram color refresh on existing SVGs
      document.querySelectorAll('.viewmd-diagram-container').forEach((c) => {
        // SVG styling updates smoothly via CSS variables
      });
    }
  }

  function detectDarkBackground() {
    try {
      let el = document.body;
      let bg = el ? window.getComputedStyle(el).backgroundColor : null;
      if (!bg || bg === 'transparent' || bg === 'rgba(0, 0, 0, 0)') {
        el = document.documentElement;
        bg = el ? window.getComputedStyle(el).backgroundColor : null;
      }
      if (!bg || bg === 'transparent' || bg === 'rgba(0, 0, 0, 0)') {
        return false;
      }
      const match = bg.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/);
      if (match) {
        const r = parseInt(match[1], 10);
        const g = parseInt(match[2], 10);
        const b = parseInt(match[3], 10);
        const luminance = 0.299 * r + 0.587 * g + 0.114 * b;
        return luminance < 128;
      }
    } catch (e) {}
    return false;
  }

  function initThemeToggle() {
    const toggleBtn = document.getElementById('viewmd-theme-toggle');
    if (toggleBtn && !toggleBtn.classList.contains('viewmd-toggle-initialized')) {
      toggleBtn.classList.add('viewmd-toggle-initialized');

      toggleBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newIsDark = currentTheme !== 'dark';
        applyTheme(newIsDark);
        try {
          localStorage.setItem('viewmd-theme', newIsDark ? 'dark' : 'light');
        } catch (err) {}
      });
    }

    // Check theme preference or auto-detection
    let themeApplied = false;
    try {
      const savedTheme = localStorage.getItem('viewmd-theme');
      if (savedTheme === 'dark' || savedTheme === 'light') {
        applyTheme(savedTheme === 'dark');
        themeApplied = true;
      }
    } catch (err) {}

    if (!themeApplied) {
      const explicitTheme = document.documentElement.getAttribute('data-theme');
      if (explicitTheme === 'dark' || explicitTheme === 'light') {
        applyTheme(explicitTheme === 'dark');
        themeApplied = true;
      }
    }

    if (!themeApplied) {
      if (detectDarkBackground()) {
        applyTheme(true);
        themeApplied = true;
      } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        applyTheme(true);
        themeApplied = true;
      }
    }
  }

  let customStyleEl = null;

  function applyCustomCss(cssText) {
    if (!customStyleEl) {
      customStyleEl = document.createElement('style');
      customStyleEl.id = 'viewmd-custom-user-style';
      document.head.appendChild(customStyleEl);
    }
    customStyleEl.textContent = cssText || '';
  }

  function applySettings(settings) {
    if (!settings || typeof settings !== 'object' || Array.isArray(settings)) return;
    const root = document.documentElement;

    if (typeof settings.maxWidth === 'string' && /^(\d{2,5}(px|%)|none)$/.test(settings.maxWidth.trim())) {
      root.style.setProperty('--viewmd-max-width', settings.maxWidth.trim());
    }
    if (typeof settings.fontSizePx === 'number' || (typeof settings.fontSizePx === 'string' && /^\d+$/.test(settings.fontSizePx))) {
      const size = Math.max(10, Math.min(60, parseInt(settings.fontSizePx, 10)));
      root.style.setProperty('--viewmd-font-size', size + 'px');
      document.body.style.fontSize = size + 'px';
    }
    if (typeof settings.fontFamily === 'string' && /^[a-zA-Z0-9\s,._"'-]+$/.test(settings.fontFamily.trim())) {
      const family = settings.fontFamily.trim();
      root.style.setProperty('--viewmd-font-sans', family);
      document.body.style.fontFamily = family;
    }
    if (typeof settings.colMinChars === 'number' || (typeof settings.colMinChars === 'string' && /^\d+$/.test(settings.colMinChars))) {
      const cols = Math.max(4, Math.min(100, parseInt(settings.colMinChars, 10)));
      root.style.setProperty('--viewmd-col-min', cols + 'ch');
    }
    if (typeof settings.colFirstMinChars === 'number' || (typeof settings.colFirstMinChars === 'string' && /^\d+$/.test(settings.colFirstMinChars))) {
      const firstCols = Math.max(4, Math.min(100, parseInt(settings.colFirstMinChars, 10)));
      root.style.setProperty('--viewmd-col-first-min', firstCols + 'ch');
    }

    if (typeof settings.hideFrontMatterCard === 'boolean') {
      const cards = document.querySelectorAll('.viewmd-frontmatter-card');
      cards.forEach((card) => {
        card.style.display = settings.hideFrontMatterCard ? 'none' : '';
      });
    }

    if (typeof settings.expandFrontMatterCard === 'boolean') {
      const cards = document.querySelectorAll('.viewmd-frontmatter-card');
      cards.forEach((card) => {
        if (!settings.hideFrontMatterCard) {
          card.open = settings.expandFrontMatterCard;
        }
      });
    }

    if (typeof settings.customCss === 'string') {
      applyCustomCss(settings.customCss);
    }
  }

  const ALLOWED_MESSAGE_TYPES = ['THEME_CHANGED', 'SETTINGS_CHANGED', 'CUSTOM_CSS_CHANGED'];

  function handleViewMdPayload(payload) {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) return;
    if (!Object.prototype.hasOwnProperty.call(payload, 'type')) return;
    if (typeof payload.type !== 'string' || !ALLOWED_MESSAGE_TYPES.includes(payload.type)) return;

    if (payload.type === 'THEME_CHANGED') {
      if (typeof payload.isDark === 'boolean') {
        applyTheme(payload.isDark);
      }
    } else if (payload.type === 'SETTINGS_CHANGED') {
      applySettings(payload);
    } else if (payload.type === 'CUSTOM_CSS_CHANGED') {
      if (typeof payload.css === 'string') {
        applyCustomCss(payload.css);
      }
    }
  }

  window.addEventListener('message', (event) => {
    if (!event.data) return;
    try {
      const payload = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
      handleViewMdPayload(payload);
    } catch (e) {
    }
  });

  // Subscribe to JetBrains JCEF MessagePipe if present
  if (window.__IntelliJTools && window.__IntelliJTools.messagePipe) {
    try {
      window.__IntelliJTools.messagePipe.subscribe('viewmd:event', (data) => {
        try {
          const payload = typeof data === 'string' ? JSON.parse(data) : data;
          handleViewMdPayload(payload);
        } catch (e) {}
      });
    } catch (e) {}
  }

  function addCopyButtonsToCodeBlocks() {
    const pres = document.querySelectorAll('pre:not(.viewmd-copy-ready)');
    pres.forEach((pre) => {
      pre.classList.add('viewmd-copy-ready');
      const btn = document.createElement('button');
      btn.className = 'viewmd-copy-btn';
      btn.textContent = 'Copy';
      btn.addEventListener('click', async (e) => {
        e.stopPropagation();
        const code = pre.querySelector('code');
        const text = code ? code.textContent : pre.textContent;
        try {
          await navigator.clipboard.writeText(text);
          btn.textContent = 'Copied!';
          setTimeout(() => { btn.textContent = 'Copy'; }, 1500);
        } catch (err) {
          btn.textContent = 'Failed';
        }
      });
      pre.style.position = 'relative';
      pre.appendChild(btn);
    });
  }

  function addTableToolbar() {
    const wraps = document.querySelectorAll('.viewmd-table-wrap:not(.viewmd-toolbar-ready)');
    wraps.forEach((wrap) => {
      wrap.classList.add('viewmd-toolbar-ready');
      wrap.style.position = 'relative';

      const toolbar = document.createElement('div');
      toolbar.className = 'viewmd-table-toolbar';

      const copyMdBtn = document.createElement('button');
      copyMdBtn.textContent = 'Copy Markdown';
      copyMdBtn.addEventListener('click', async (e) => {
        e.stopPropagation();
        const table = wrap.querySelector('table');
        if (!table) return;
        const mdText = tableToMarkdown(table);
        try {
          await navigator.clipboard.writeText(mdText);
          copyMdBtn.textContent = 'Copied!';
          setTimeout(() => { copyMdBtn.textContent = 'Copy Markdown'; }, 1500);
        } catch (err) {
          copyMdBtn.textContent = 'Failed';
        }
      });

      const copyTsvBtn = document.createElement('button');
      copyTsvBtn.textContent = 'Copy TSV';
      copyTsvBtn.addEventListener('click', async (e) => {
        e.stopPropagation();
        const table = wrap.querySelector('table');
        if (!table) return;
        const tsvText = tableToTsv(table);
        try {
          await navigator.clipboard.writeText(tsvText);
          copyTsvBtn.textContent = 'Copied!';
          setTimeout(() => { copyTsvBtn.textContent = 'Copy TSV'; }, 1500);
        } catch (err) {
          copyTsvBtn.textContent = 'Failed';
        }
      });

      toolbar.appendChild(copyMdBtn);
      toolbar.appendChild(copyTsvBtn);
      wrap.appendChild(toolbar);
    });
  }

  function tableToMarkdown(table) {
    const rows = Array.from(table.querySelectorAll('tr'));
    return rows.map((r) => {
      const cells = Array.from(r.querySelectorAll('th, td')).map((c) => (c.textContent || '').trim().replace(/\|/g, '\\|'));
      return `| ${cells.join(' | ')} |`;
    }).join('\n');
  }

  function tableToTsv(table) {
    const rows = Array.from(table.querySelectorAll('tr'));
    return rows.map((r) => {
      const cells = Array.from(r.querySelectorAll('th, td')).map((c) => (c.textContent || '').trim().replace(/\t/g, ' '));
      return cells.join('\t');
    }).join('\n');
  }

  function cleanExternalBrowserArtifacts() {
    const isExternalBrowser = typeof window.__IntelliJTools === 'undefined';
    if (isExternalBrowser) {
      // 1. Remove IDE command runner links that scroll to # with broken images
      document.querySelectorAll('a[data-command], a.run-icon, a.run-icon-line').forEach((el) => el.remove());
      // 2. Remove duplicate/broken IDE copy buttons
      document.querySelectorAll('.code-fence-highlighter-copy-button').forEach((el) => el.remove());
    }

    // 3. Neutralize broken alert icons pointing to relative alertIcons/*.png
    document.querySelectorAll('img.markdown-alert-icon').forEach((img) => {
      const src = img.getAttribute('src') || '';
      if (src.includes('alertIcons/')) {
        const match = src.match(/alertIcons\/([a-zA-Z]+)\.png/i);
        const type = match ? match[1].toLowerCase() : 'note';
        const emojiMap = { note: 'ℹ️', tip: '💡', important: '❗', warning: '⚠️', caution: '🛑' };
        const span = document.createElement('span');
        span.className = 'viewmd-alert-icon';
        span.style.marginRight = '8px';
        span.textContent = emojiMap[type] || 'ℹ️';
        img.parentNode.replaceChild(span, img);
      }
    });

    // 4. Remove any commandRunner images or broken images inside anchors
    document.querySelectorAll('img[src*="commandRunner/"]').forEach((img) => {
      const parentLink = img.closest('a');
      if (parentLink && (parentLink.getAttribute('href') === '#' || parentLink.hasAttribute('data-command'))) {
        parentLink.remove();
      } else {
        img.remove();
      }
    });
  }

  function run() {
    cleanExternalBrowserArtifacts();
    initThemeToggle();
    wrapTables();
    enhanceFrontMatter();
    renderMermaidDiagrams();
    addTableToolbar();
    addCopyButtonsToCodeBlocks();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', run);
  } else {
    run();
  }

  let debounceTimer = null;
  const observer = new MutationObserver(() => {
    if (debounceTimer) return;
    debounceTimer = setTimeout(() => {
      debounceTimer = null;
      cleanExternalBrowserArtifacts();
      initThemeToggle();
      wrapTables();
      renderMermaidDiagrams();
      addTableToolbar();
      addCopyButtonsToCodeBlocks();
    }, 50);
  });

  observer.observe(document.body || document.documentElement, {
    childList: true,
    subtree: true,
  });
})();
