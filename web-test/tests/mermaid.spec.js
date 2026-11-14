const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml } = require('./test-utils');

const mockMermaidScript = `
window.mermaid = {
  initialize: function(cfg) { window.__mermaidConfig = cfg; },
  render: function(id, text) {
    if (text.includes('invalid')) {
      return Promise.reject(new Error('Syntax error'));
    }
    return Promise.resolve({
      svg: '<svg class="rendered-mermaid" id="' + id + '"><text>' + text + '</text></svg>'
    });
  }
};
`;

test.describe('Mermaid Diagrams Rendering', () => {
  test('renders pre.mermaid into SVG container', async ({ page }) => {
    const html = buildHtml(`
      <pre class="mermaid">graph TD; A-->B;</pre>
    `, { extraScript: mockMermaidScript });
    await loadHtml(page, html);

    const container = page.locator('.viewmd-diagram-container');
    await expect(container).toBeVisible();

    const svg = container.locator('svg.rendered-mermaid');
    await expect(svg).toBeVisible();
    await expect(svg).toContainText('graph TD; A-->B;');
  });

  test('displays error block for invalid mermaid syntax', async ({ page }) => {
    const html = buildHtml(`
      <pre class="mermaid">invalid diagram syntax</pre>
    `, { extraScript: mockMermaidScript });
    await loadHtml(page, html);

    const errorBlock = page.locator('.viewmd-diagram-error');
    await expect(errorBlock).toBeVisible();

    const errorTitle = errorBlock.locator('.viewmd-diagram-error-title');
    await expect(errorTitle).toHaveText('Mermaid rendering error');

    const errorSource = errorBlock.locator('.viewmd-diagram-error-source');
    await expect(errorSource).toContainText('invalid diagram syntax');
  });

  test('handles JetBrains Markdown base64 fence blocks', async ({ page }) => {
    // base64 for "graph LR; X-->Y;"
    const b64 = Buffer.from('graph LR; X-->Y;').toString('base64');
    const html = buildHtml(`
      <div class="mermaid" data-actual-fence-content="${b64}"></div>
    `, { extraScript: mockMermaidScript });
    await loadHtml(page, html);

    const container = page.locator('.viewmd-diagram-container');
    await expect(container).toBeVisible();
    await expect(container.locator('svg')).toContainText('graph LR; X-->Y;');
  });

  test('leaves diagram blocks untouched if mermaid library is not available', async ({ page }) => {
    const html = buildHtml(`
      <pre class="mermaid">graph TD; A-->B;</pre>
    `);
    await loadHtml(page, html);

    const pre = page.locator('pre.mermaid');
    await expect(pre).toBeVisible();
    await expect(page.locator('.viewmd-diagram-container')).toHaveCount(0);
  });
});
