const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml } = require('./test-utils');

test.describe('Front Matter Enhancement', () => {
  test('enhances pre/code block containing summary to front matter card', async ({ page }) => {
    const rawFm = `summary: "My Document Summary"\ntriggers: [auth, login]\nindex: true`;
    const html = buildHtml(`
      <pre><code>${rawFm}</code></pre>
      <h1>Document Heading</h1>
    `);
    await loadHtml(page, html);

    const card = page.locator('details.viewmd-frontmatter-card');
    await expect(card).toBeVisible();

    // Open card to view summary text and trigger chips
    await card.locator('summary').click();

    const summaryText = card.locator('text=My Document Summary');
    await expect(summaryText).toBeVisible();

    const trigger1 = card.locator('.viewmd-chip', { hasText: 'auth' });
    const trigger2 = card.locator('.viewmd-chip', { hasText: 'login' });
    await expect(trigger1).toBeVisible();
    await expect(trigger2).toBeVisible();
  });

  test('displays Not indexed chip when index is false', async ({ page }) => {
    const rawFm = `summary: "Unindexed Doc"\nindex: false`;
    const html = buildHtml(`
      <pre><code>${rawFm}</code></pre>
    `);
    await loadHtml(page, html);

    const notIndexedChip = page.locator('.viewmd-chip', { hasText: 'Not indexed' });
    await expect(notIndexedChip).toBeVisible();
  });

  test('enhances details.fm class to .viewmd-frontmatter-card', async ({ page }) => {
    const html = buildHtml(`
      <details class="fm">
        <summary>Front matter</summary>
        <p>Details content</p>
      </details>
    `);
    await loadHtml(page, html);

    const card = page.locator('details.viewmd-frontmatter-card');
    await expect(card).toBeVisible();
    await expect(card).toHaveClass(/viewmd-fm-processed/);
  });
});
