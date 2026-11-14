const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml } = require('./test-utils');

test.describe('Live Settings and Event Messaging', () => {
  test('handles THEME_CHANGED message event', async ({ page }) => {
    const html = buildHtml(`<h1>Testing Theme Changed</h1>`);
    await loadHtml(page, html);

    await page.evaluate(() => {
      window.postMessage(JSON.stringify({ type: 'THEME_CHANGED', isDark: true }), '*');
    });

    await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  });

  test('handles SETTINGS_CHANGED message and updates CSS variables', async ({ page }) => {
    const html = buildHtml(`<h1>Testing Settings Changed</h1>`);
    await loadHtml(page, html);

    await page.evaluate(() => {
      window.postMessage(JSON.stringify({
        type: 'SETTINGS_CHANGED',
        maxWidth: '750px',
        fontSizePx: 18,
        colMinChars: 16
      }), '*');
    });

    const styles = await page.evaluate(() => {
      const root = document.documentElement;
      return {
        maxWidth: root.style.getPropertyValue('--viewmd-max-width'),
        fontSize: root.style.getPropertyValue('--viewmd-font-size'),
        colMin: root.style.getPropertyValue('--viewmd-col-min')
      };
    });

    expect(styles.maxWidth).toBe('750px');
    expect(styles.fontSize).toBe('18px');
    expect(styles.colMin).toBe('16ch');
  });

  test('handles CUSTOM_CSS_CHANGED and injects user stylesheet', async ({ page }) => {
    const html = buildHtml(`<h1 id="header">User Styling</h1>`);
    await loadHtml(page, html);

    await page.evaluate(() => {
      window.postMessage(JSON.stringify({
        type: 'CUSTOM_CSS_CHANGED',
        css: '#header { color: rgb(255, 0, 128); }'
      }), '*');
    });

    const color = await page.evaluate(() => {
      const el = document.getElementById('header');
      return window.getComputedStyle(el).color;
    });

    expect(color).toBe('rgb(255, 0, 128)');
  });
});
