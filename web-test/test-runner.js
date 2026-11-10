const { chromium } = require('playwright');
const path = require('path');
const assert = require('assert');

async function run() {
  const browser = await chromium.launch({
    executablePath: '/usr/bin/google-chrome',
    headless: true
  });

  const widths = [400, 900, 1600];
  const modes = ['light', 'dark'];

  for (const mode of modes) {
    for (const width of widths) {
      const page = await browser.newPage({
        viewport: { width, height: 1000 }
      });

      const fixturePath = 'file://' + path.resolve(__dirname, `fixtures/torture-test-${mode}.html`);
      await page.goto(fixturePath, { waitUntil: 'load' });
      await page.waitForTimeout(100);

      const metrics = await page.evaluate(() => {
        const docScrollWidth = document.documentElement.scrollWidth;
        const docClientWidth = document.documentElement.clientWidth;
        const tables = Array.from(document.querySelectorAll('table')).map((t, idx) => {
          const wrap = t.parentElement;
          const isWrapped = wrap && wrap.classList.contains('viewmd-table-wrap');
          const wrapScrollWidth = wrap ? wrap.scrollWidth : 0;
          const wrapClientWidth = wrap ? wrap.clientWidth : 0;
          const firstTh = t.querySelector('th, td');
          const firstColWidth = firstTh ? firstTh.getBoundingClientRect().width : 0;

          return {
            idx,
            isWrapped,
            wrapScrolls: wrapScrollWidth > wrapClientWidth,
            tableWidth: t.getBoundingClientRect().width,
            wrapWidth: wrap ? wrap.getBoundingClientRect().width : 0,
            firstColWidth
          };
        });

        const frontmatterCard = document.querySelector('.viewmd-frontmatter-card');

        return {
          pageHasSidewaysScroll: docScrollWidth > docClientWidth,
          docScrollWidth,
          docClientWidth,
          tables,
          hasFrontmatterCard: !!frontmatterCard
        };
      });

      console.log(`[${mode.toUpperCase()}] ${width}px: pageHasSidewaysScroll=${metrics.pageHasSidewaysScroll}, docScroll=${metrics.docScrollWidth}, docClient=${metrics.docClientWidth}, tables count=${metrics.tables.length}`);
      
      // Assertions per G1 exit gate
      assert.strictEqual(metrics.pageHasSidewaysScroll, false, `Page must never have horizontal scrollbar at ${width}px (${mode})`);
      assert.ok(metrics.tables.length > 0, 'Tables must be detected');
      assert.ok(metrics.tables.every(t => t.isWrapped), 'All tables must be wrapped in .viewmd-table-wrap');
      assert.ok(metrics.hasFrontmatterCard, 'YAML frontmatter card must be rendered');

      // The wide table (index 0) must scroll when viewport width is 400 or 900
      if (width <= 900) {
        assert.ok(metrics.tables[0].wrapScrolls, `Table 0 must scroll inside wrapper at ${width}px`);
      }

      await page.close();
    }
  }

  await browser.close();
  console.log('ALL PLAYWRIGHT WEB TESTS PASSED CLEANLY!');
}

run().catch(err => {
  console.error('Test run failed:', err);
  process.exit(1);
});
