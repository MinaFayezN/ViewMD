const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml } = require('./test-utils');

test.describe('Table Toolbar', () => {
  test('adds table toolbar with Copy Markdown and Copy TSV buttons', async ({ page }) => {
    page.on('console', m => console.log('PAGE LOG:', m.text()));
    page.on('pageerror', e => console.log('PAGE ERROR:', e.message));

    const html = buildHtml(`
      <table>
        <thead><tr><th>Col 1</th><th>Col 2</th></tr></thead>
        <tbody><tr><td>A</td><td>B</td></tr></tbody>
      </table>
    `);
    await loadHtml(page, html);

    const wrap = page.locator('.viewmd-table-wrap');
    await expect(wrap).toBeVisible();

    const toolbar = wrap.locator('.viewmd-table-toolbar');
    await expect(toolbar).toBeAttached();

    const copyMdBtn = toolbar.locator('button', { hasText: 'Copy Markdown' });
    const copyTsvBtn = toolbar.locator('button', { hasText: 'Copy TSV' });

    // Use evaluate to click and inspect
    const result = await page.evaluate(async () => {
      const btn = document.querySelector('.viewmd-table-toolbar button');
      btn.click();
      await new Promise(r => setTimeout(r, 50));
      return {
        text: btn.textContent,
        clipboard: await navigator.clipboard.readText()
      };
    });
    console.log('Evaluate result:', result);

    expect(result.text).toBe('Copied!');
    expect(result.clipboard).toContain('| Col 1 | Col 2 |');

    // Test TSV button
    const tsvResult = await page.evaluate(async () => {
      const btns = document.querySelectorAll('.viewmd-table-toolbar button');
      const tsvBtn = btns[1];
      tsvBtn.click();
      await new Promise(r => setTimeout(r, 50));
      return {
        text: tsvBtn.textContent,
        clipboard: await navigator.clipboard.readText()
      };
    });
    console.log('TSV Evaluate result:', tsvResult);

    expect(tsvResult.text).toBe('Copied!');
    expect(tsvResult.clipboard).toContain('Col 1\tCol 2');
  });

  test('copy button reverts to original label after 1500ms timeout', async ({ page }) => {
    const html = buildHtml(`
      <table>
        <thead><tr><th>Col 1</th></tr></thead>
        <tbody><tr><td>Val</td></tr></tbody>
      </table>
    `);
    await loadHtml(page, html);

    const copyBtn = page.locator('.viewmd-table-toolbar button').first();
    await expect(copyBtn).toHaveText('Copy Markdown');

    await copyBtn.click();
    await expect(copyBtn).toHaveText('Copied!');

    // Wait past the 1500ms timeout
    await page.waitForTimeout(1600);
    await expect(copyBtn).toHaveText('Copy Markdown');
  });
});
