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

  test('rejects unauthorized message types and malformed payloads', async ({ page }) => {
    const html = buildHtml(`<h1 id="header">Payload Guard</h1>`);
    await loadHtml(page, html);

    // Send unauthorized message type
    await page.evaluate(() => {
      window.postMessage(JSON.stringify({ type: 'MALICIOUS_ACTION', cmd: 'alert(1)' }), '*');
    });

    // Send invalid payload format (array)
    await page.evaluate(() => {
      window.postMessage(JSON.stringify(['THEME_CHANGED', true]), '*');
    });

    // Theme should still be default (light)
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  });

  test('sanitizes and clamps settings against CSS injection and extreme values', async ({ page }) => {
    const html = buildHtml(`<h1 id="header">Sanitization</h1>`);
    await loadHtml(page, html);

    // Attempt CSS breakout injection via fontSizePx and maxWidth
    await page.evaluate(() => {
      window.postMessage(JSON.stringify({
        type: 'SETTINGS_CHANGED',
        fontSizePx: '20px; color: red;',
        maxWidth: 'invalid-css-width',
        colMinChars: 9999 // Should clamp to 100
      }), '*');
    });

    const values = await page.evaluate(() => {
      const root = document.documentElement;
      return {
        fontSize: root.style.getPropertyValue('--viewmd-font-size'),
        maxWidth: root.style.getPropertyValue('--viewmd-max-width'),
        colMin: root.style.getPropertyValue('--viewmd-col-min')
      };
    });

    // Malicious values should be rejected (empty/unchanged)
    expect(values.fontSize).toBe('');
    expect(values.maxWidth).toBe('');
    // Clamped value
    expect(values.colMin).toBe('100ch');
  });

  test('resists prototype pollution payloads without altering Object prototype', async ({ page }) => {
    const html = buildHtml(`<h1 id="header">Pollution Guard</h1>`);
    await loadHtml(page, html);

    await page.evaluate(() => {
      window.postMessage('{"__proto__":{"viewmdPolluted":true},"type":"SETTINGS_CHANGED"}', '*');
    });

    const isPolluted = await page.evaluate(() => {
      return ({}).viewmdPolluted === true || Object.prototype.viewmdPolluted === true;
    });

    expect(isPolluted).toBe(false);
  });
});
