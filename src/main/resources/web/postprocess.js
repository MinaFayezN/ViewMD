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

    lines.forEach((line) => {
      const matchSummary = line.match(/^summary:\s*["']?(.*?)["']?$/);
      const matchTriggers = line.match(/^triggers:\s*\[(.*?)\]/);
      const matchIndex = line.match(/^index:\s*(false|true)/i);

      if (matchSummary) {
        summary = matchSummary[1];
      } else if (matchTriggers) {
        triggers = matchTriggers[1].split(',').map((t) => t.trim().replace(/^["']|["']$/g, ''));
      } else if (matchIndex) {
        indexed = matchIndex[1].toLowerCase() === 'true';
      } else if (line.trim().length > 0) {
        others.push(line.trim());
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

  function renderMermaidDiagrams() {
    if (typeof window.mermaid === 'undefined') return;

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    try {
      window.mermaid.initialize({
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

    window.mermaid.render(id, source).then(({ svg }) => {
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
        // SVG styling updates smoothly via CSS variables, and diagrams with stored source re-render if needed
      });
    }
  }

  function initThemeToggle() {
    const toggleBtn = document.getElementById('viewmd-theme-toggle');
    if (!toggleBtn || toggleBtn.classList.contains('viewmd-toggle-initialized')) return;
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

    // Check localStorage preference on load in browser
    try {
      const savedTheme = localStorage.getItem('viewmd-theme');
      if (savedTheme === 'dark' || savedTheme === 'light') {
        applyTheme(savedTheme === 'dark');
      }
    } catch (err) {}
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
    if (!settings) return;
    const root = document.documentElement;

    if (settings.maxWidth) {
      root.style.setProperty('--viewmd-max-width', settings.maxWidth);
    }
    if (settings.fontSizePx) {
      root.style.setProperty('--viewmd-font-size', settings.fontSizePx + 'px');
      document.body.style.fontSize = settings.fontSizePx + 'px';
    }
    if (settings.fontFamily) {
      root.style.setProperty('--viewmd-font-sans', settings.fontFamily);
      document.body.style.fontFamily = settings.fontFamily;
    }
    if (settings.colMinChars) {
      root.style.setProperty('--viewmd-col-min', settings.colMinChars + 'ch');
    }
    if (settings.colFirstMinChars) {
      root.style.setProperty('--viewmd-col-first-min', settings.colFirstMinChars + 'ch');
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

  function handleViewMdPayload(payload) {
    if (!payload) return;
    if (payload.type === 'THEME_CHANGED') {
      applyTheme(payload.isDark);
    } else if (payload.type === 'SETTINGS_CHANGED') {
      applySettings(payload);
    } else if (payload.type === 'CUSTOM_CSS_CHANGED') {
      applyCustomCss(payload.css);
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

  function run() {
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
        await navigator.clipboard.writeText(mdText);
        copyMdBtn.textContent = 'Copied!';
        setTimeout(() => { copyMdBtn.textContent = 'Copy Markdown'; }, 1500);
      });

      const copyTsvBtn = document.createElement('button');
      copyTsvBtn.textContent = 'Copy TSV';
      copyTsvBtn.addEventListener('click', async (e) => {
        e.stopPropagation();
        const table = wrap.querySelector('table');
        if (!table) return;
        const tsvText = tableToTsv(table);
        await navigator.clipboard.writeText(tsvText);
        copyTsvBtn.textContent = 'Copied!';
        setTimeout(() => { copyTsvBtn.textContent = 'Copy TSV'; }, 1500);
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
