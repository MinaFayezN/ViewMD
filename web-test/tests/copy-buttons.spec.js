const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml } = require('./test-utils');

test.describe('Code Block Copy Buttons', () => {
  test('adds copy button to pre code blocks', async ({ page }) => {
    const html = buildHtml(`
      <pre><code>const answer = 42;</code></pre>
    `);
    await loadHtml(page, html);

    const copyBtn = page.locator('.viewmd-copy-btn');
    await expect(copyBtn).toBeVisible();
    await expect(copyBtn).toHaveText('Copy');

    await copyBtn.click();
    await expect(copyBtn).toHaveText('Copied!');

    const clipboardText = await page.evaluate(() => navigator.clipboard.readText());
    expect(clipboardText).toBe('const answer = 42;');
  });

  test('does not add duplicate copy buttons on multiple observer triggers', async ({ page }) => {
    const html = buildHtml(`
      <pre id="pre1"><code>echo "test"</code></pre>
    `);
    await loadHtml(page, html);

    // Trigger mutation
    await page.evaluate(() => {
      const p = document.createElement('p');
      p.textContent = 'trigger mutation';
      document.body.appendChild(p);
    });

    await page.waitForTimeout(100);

    const buttons = page.locator('#pre1 .viewmd-copy-btn');
    await expect(buttons).toHaveCount(1);
  });
});
