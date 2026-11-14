const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml } = require('./test-utils');

test.describe('Theme Switching', () => {
  test('theme toggle button flips data-theme and updates label & icon', async ({ page }) => {
    const html = buildHtml(`
      <button id="viewmd-theme-toggle" type="button">
        <span id="viewmd-theme-icon">🌙</span>
        <span id="viewmd-theme-text">Dark Mode</span>
      </button>
      <h1>Heading</h1>
    `);
    await loadHtml(page, html);

    const toggleBtn = page.locator('#viewmd-theme-toggle');
    const toggleIcon = page.locator('#viewmd-theme-icon');
    const toggleText = page.locator('#viewmd-theme-text');

    await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');

    // Click to switch to Dark
    await toggleBtn.click();
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
    await expect(toggleIcon).toHaveText('☀️');
    await expect(toggleText).toHaveText('Light Mode');

    // Click again to switch back to Light
    await toggleBtn.click();
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
    await expect(toggleIcon).toHaveText('🌙');
    await expect(toggleText).toHaveText('Dark Mode');
  });

  test('theme preference is persisted to localStorage', async ({ page }) => {
    const html = buildHtml(`
      <button id="viewmd-theme-toggle" type="button">
        <span id="viewmd-theme-icon">🌙</span>
        <span id="viewmd-theme-text">Dark Mode</span>
      </button>
    `);
    await loadHtml(page, html);

    await page.locator('#viewmd-theme-toggle').click();

    const storedTheme = await page.evaluate(() => localStorage.getItem('viewmd-theme'));
    expect(storedTheme).toBe('dark');
  });
});
