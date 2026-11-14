const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml } = require('./test-utils');

test.describe('Table wrapping', () => {
  test('wraps a bare table in .viewmd-table-wrap and marks it .viewmd-wrapped', async ({ page }) => {
    const html = buildHtml(`
      <table>
        <thead><tr><th>Col A</th><th>Col B</th></tr></thead>
        <tbody><tr><td>1</td><td>2</td></tr></tbody>
      </table>
    `);
    await loadHtml(page, html);

    const table = page.locator('table');
    await expect(table).toHaveClass(/viewmd-wrapped/);

    const parent = page.locator('table').locator('xpath=..');
    await expect(parent).toHaveClass(/viewmd-table-wrap/);
  });

  test('does not double wrap table that is already wrapped', async ({ page }) => {
    const html = buildHtml(`
      <div class="viewmd-table-wrap">
        <table class="viewmd-wrapped">
          <tr><td>Pre-wrapped</td></tr>
        </table>
      </div>
    `);
    await loadHtml(page, html);

    const wraps = page.locator('.viewmd-table-wrap');
    await expect(wraps).toHaveCount(1);
  });

  test('wraps multiple tables independently', async ({ page }) => {
    const html = buildHtml(`
      <table id="t1"><tr><td>Table 1</td></tr></table>
      <p>Separating text</p>
      <table id="t2"><tr><td>Table 2</td></tr></table>
    `);
    await loadHtml(page, html);

    const wraps = page.locator('.viewmd-table-wrap');
    await expect(wraps).toHaveCount(2);

    await expect(page.locator('#t1')).toHaveClass(/viewmd-wrapped/);
    await expect(page.locator('#t2')).toHaveClass(/viewmd-wrapped/);
  });

  test('dynamically inserted table is wrapped via MutationObserver', async ({ page }) => {
    const html = buildHtml(`<div id="container"></div>`);
    await loadHtml(page, html);

    await page.evaluate(() => {
      const container = document.getElementById('container');
      const table = document.createElement('table');
      table.innerHTML = '<tr><td>Dynamic</td></tr>';
      container.appendChild(table);
    });

    const wrap = page.locator('.viewmd-table-wrap');
    await expect(wrap).toBeVisible();
    await expect(page.locator('.viewmd-table-wrap table')).toHaveClass(/viewmd-wrapped/);
  });
});
