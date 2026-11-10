(function () {
  'use strict';

  function wrapTables() {
    const tables = document.querySelectorAll('table:not(.viewmd-wrapped)');
    tables.forEach((table) => {
      if (table.parentElement && table.parentElement.classList.contains('viewmd-table-wrap')) {
        table.classList.add('viewmd-wrapped');
        return;
      }
      const wrap = document.createElement('div');
      wrap.className = 'viewmd-table-wrap';
      table.parentNode.insertBefore(wrap, table);
      wrap.appendChild(table);
      table.classList.add('viewmd-wrapped');
    });
  }

  function enhanceFrontMatter() {
    // If stock markdown rendered frontmatter as details.fm or pre block
    const detailsFm = document.querySelector('details.fm:not(.viewmd-fm-processed)');
    if (detailsFm) {
      detailsFm.classList.add('viewmd-fm-processed');
      detailsFm.classList.add('viewmd-frontmatter-card');
      return;
    }

    const preBlocks = document.querySelectorAll('pre > code');
    for (const code of preBlocks) {
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

  function escapeHtml(str) {
    return str.replace(/[&<>'"]/g, (tag) => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      "'": '&#39;',
      '"': '&quot;',
    }[tag] || tag));
  }

  function run() {
    wrapTables();
    enhanceFrontMatter();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', run);
  } else {
    run();
  }

  const observer = new MutationObserver(() => {
    wrapTables();
    enhanceFrontMatter();
  });

  observer.observe(document.body || document.documentElement, {
    childList: true,
    subtree: true,
  });
})();
