const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml, calculateContrastRatio } = require('./test-utils');

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

  test('dark mode alternate rows satisfy WCAG AA contrast and avoid white-on-white text (regression fix)', async ({ page }) => {
    const tableHtml = `
      <table>
        <thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>
        <tbody>
          <tr><td>Row 1 Cell 1</td><td>Row 1 Cell 2</td></tr>
          <tr><td>Row 2 Cell 1</td><td>Row 2 Cell 2</td></tr>
          <tr><td>Row 3 Cell 1</td><td>Row 3 Cell 2</td></tr>
        </tbody>
      </table>
    `;
    const html = buildHtml(tableHtml, { isDark: true });
    await loadHtml(page, html);

    const colors = await page.evaluate(() => {
      const evenTr = document.querySelector('tbody tr:nth-child(even)');
      const oddTr = document.querySelector('tbody tr:nth-child(odd)');
      const th = document.querySelector('th');
      const fg = window.getComputedStyle(document.body).color;
      return {
        fg,
        evenBg: window.getComputedStyle(evenTr).backgroundColor,
        evenFg: window.getComputedStyle(evenTr).color,
        oddBg: window.getComputedStyle(oddTr).backgroundColor,
        oddFg: window.getComputedStyle(oddTr).color,
        thBg: window.getComputedStyle(th).backgroundColor,
        thFg: window.getComputedStyle(th).color,
      };
    });

    // Specifically verify that dark even rows never receive light grey #f6f8fa
    expect(colors.evenBg).not.toBe('rgb(246, 248, 250)');
    expect(colors.thBg).not.toBe('rgb(246, 248, 250)');

    // WCAG AA contrast requires >= 4.5:1 for normal text
    const evenContrast = calculateContrastRatio(colors.evenBg, colors.evenFg);
    const oddContrast = calculateContrastRatio(colors.oddBg, colors.oddFg);
    const thContrast = calculateContrastRatio(colors.thBg, colors.thFg);

    expect(evenContrast).toBeGreaterThanOrEqual(4.5);
    expect(oddContrast).toBeGreaterThanOrEqual(4.5);
    expect(thContrast).toBeGreaterThanOrEqual(4.5);
  });

  test('implicit dark mode (dark body background without data-theme) auto-detects dark theme', async ({ page }) => {
    // Navigate and clear any residual localStorage from previous toggle tests
    await page.goto('http://localhost:4321');
    await page.evaluate(() => localStorage.clear());

    const html = `<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <style>
  ${require('fs').readFileSync(require('./test-utils').CSS_PATH, 'utf-8')}
  body { background: #0d1117; color: #e6edf3; }
  </style>
</head>
<body>
  <table>
    <thead><tr><th>Col A</th><th>Col B</th></tr></thead>
    <tbody>
      <tr><td>Item 1</td><td>Val 1</td></tr>
      <tr><td>Item 2</td><td>Val 2</td></tr>
    </tbody>
  </table>
  <script>
  ${require('fs').readFileSync(require('./test-utils').JS_PATH, 'utf-8')}
  </script>
</body>
</html>`;

    await loadHtml(page, html);

    const themeInfo = await page.evaluate(() => {
      const rootTheme = document.documentElement.getAttribute('data-theme');
      const evenTr = document.querySelector('tbody tr:nth-child(even)');
      return {
        rootTheme,
        evenBg: window.getComputedStyle(evenTr).backgroundColor
      };
    });

    expect(themeInfo.rootTheme).toBe('dark');
    expect(themeInfo.evenBg).toBe('rgb(22, 27, 34)');
  });
});
