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
    // If markdown rendered a leading YAML block into <pre><code> or details
    const preBlocks = document.querySelectorAll('pre > code');
    preBlocks.forEach((code) => {
      const text = code.textContent || '';
      if (text.startsWith('---') && text.includes('summary:')) {
        const pre = code.parentElement;
        if (pre && !pre.classList.contains('viewmd-fm-processed')) {
          pre.classList.add('viewmd-fm-processed');
          // Frontmatter card conversion
        }
      }
    });
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
  });

  observer.observe(document.body || document.documentElement, {
    childList: true,
    subtree: true,
  });
})();
