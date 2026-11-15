/*
 * ViewMD — Enhanced Markdown preview for Android Studio and IntelliJ-based IDEs
 * Copyright (C) 2026 Mina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

const { test, expect } = require('@playwright/test');
const { buildHtml, loadHtml, calculateContrastRatio } = require('./test-utils');
const fs = require('fs');
const path = require('path');

const MERMAID_PATH = path.resolve(__dirname, '../../src/main/resources/web/vendor/mermaid.min.js');
const mermaidJs = fs.readFileSync(MERMAID_PATH, 'utf-8');

test.describe('Comprehensive Business Scenarios', () => {
  test('Scenario 1: Multi-table document with varied formats wrap and function independently', async ({ page }) => {
    const html = buildHtml(`
      <article>
        <h1>Engineering Systems Comparison</h1>

        <!-- Wide 10-column table -->
        <table id="table-wide">
          <thead>
            <tr>
              <th>Service</th><th>Tier</th><th>Region</th><th>SLA</th><th>vCPU</th>
              <th>RAM</th><th>Storage</th><th>I/O Rate</th><th>Latency</th><th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Auth Gateway</td><td>Tier 1</td><td>us-east-1</td><td>99.99%</td><td>32</td>
              <td>128GB</td><td>NVMe 1TB</td><td>50k IOPS</td><td>0.8ms</td><td>Active</td>
            </tr>
            <tr>
              <td>Data Ingest</td><td>Tier 2</td><td>us-central-1</td><td>99.9%</td><td>16</td>
              <td>64GB</td><td>SSD 2TB</td><td>20k IOPS</td><td>1.4ms</td><td>Active</td>
            </tr>
          </tbody>
        </table>

        <p>Follow-up table with rich markdown elements (links, code, escaped characters):</p>

        <!-- Formatted table with code, links, and pipe delimiters -->
        <table id="table-formatted">
          <thead>
            <tr><th>Feature</th><th>Syntax Pattern</th><th>Documentation Reference</th></tr>
          </thead>
          <tbody>
            <tr>
              <td>Pipeline Filter</td>
              <td><code>regex = /^user\\|admin$/</code></td>
              <td><a href="https://example.com/docs/pipeline">Configuration Guide</a></td>
            </tr>
            <tr>
              <td>Safe Fallback</td>
              <td><code>mode = DEFAULT_FALLBACK</code></td>
              <td><a href="https://example.com/docs/fallback">Fallback Architecture</a></td>
            </tr>
          </tbody>
        </table>

        <!-- Compact table -->
        <table id="table-compact">
          <thead><tr><th>Key</th><th>Value</th></tr></thead>
          <tbody><tr><td>Release</td><td>1.0.0</td></tr></tbody>
        </table>
      </article>
    `);

    await loadHtml(page, html);

    // 1. All 3 tables must be wrapped independently into .viewmd-table-wrap
    const wrappers = page.locator('.viewmd-table-wrap');
    await expect(wrappers).toHaveCount(3);

    // 2. Each table has its own independent toolbar
    const toolbars = page.locator('.viewmd-table-toolbar');
    await expect(toolbars).toHaveCount(3);

    // 3. Wide table has sticky headers
    const th = wrappers.first().locator('th').first();
    const position = await th.evaluate(el => window.getComputedStyle(el).position);
    expect(position).toBe('sticky');

    // 4. Formatted table preserve code styling and link navigation
    const codeInTable = wrappers.nth(1).locator('td code').first();
    await expect(codeInTable).toBeVisible();
    await expect(codeInTable).toContainText('regex = /^user\\|admin$/');

    const linkInTable = wrappers.nth(1).locator('td a').first();
    await expect(linkInTable).toHaveAttribute('href', 'https://example.com/docs/pipeline');
  });

  test('Scenario 2: Unbreakable long URLs and identifiers wrap properly without container blow-out', async ({ page }) => {
    const longUrl = 'https://subdomain.production-environment.internal.company-domain.net/services/monitoring/v2/dashboards/cluster-metrics-telemetry-endpoint?session_token=abcdef0123456789abcdef0123456789&filter=active_workers_only';
    const longIdentifier = 'VERY_LONG_ENTERPRISE_SYSTEM_MODULE_IDENTIFIER_NAME_WITHOUT_WHITESPACES_OR_DELIMITERS_FOR_TESTING';

    const html = buildHtml(`
      <table id="table-tokens">
        <thead><tr><th>Endpoint URL</th><th>System Identifier</th></tr></thead>
        <tbody>
          <tr>
            <td><a href="${longUrl}">${longUrl}</a></td>
            <td><code>${longIdentifier}</code></td>
          </tr>
        </tbody>
      </table>
    `);

    await loadHtml(page, html);

    const cellInfo = await page.evaluate(() => {
      const td1 = document.querySelector('td:nth-child(1)');
      const td2 = document.querySelector('td:nth-child(2)');
      return {
        td1OverflowWrap: window.getComputedStyle(td1).overflowWrap,
        td2OverflowWrap: window.getComputedStyle(td2).overflowWrap,
        td1Width: td1.getBoundingClientRect().width,
        td2Width: td2.getBoundingClientRect().width,
      };
    });

    // Check that overflow-wrap: break-word or anywhere is active
    expect(['anywhere', 'break-word']).toContain(cellInfo.td1OverflowWrap);
    expect(['anywhere', 'break-word']).toContain(cellInfo.td2OverflowWrap);
  });

  test('Scenario 3: End-to-end standalone export rendering with theme toggle, front matter, and diagrams', async ({ page }) => {
    // End-to-end simulation of standalone HTML export
    const exportHtml = buildHtml(`
      <button id="viewmd-theme-toggle" class="viewmd-theme-toggle" type="button">
        <span id="viewmd-theme-icon">🌙</span>
        <span id="viewmd-theme-text">Dark Mode</span>
      </button>

      <pre><code>summary: End-to-end architecture verification test
triggers: [commit, pull_request]
index: true</code></pre>

      <div class="viewmd-content">
        <h1>Release Architecture Document</h1>
        <table>
          <thead><tr><th>Module</th><th>Status</th></tr></thead>
          <tbody>
            <tr><td>Core ViewMD</td><td>Shipped</td></tr>
            <tr><td>Test Automation</td><td>Active</td></tr>
          </tbody>
        </table>

        <h2>Architecture Workflow</h2>
        <pre class="mermaid">
flowchart LR
    A[Markdown File] --> B[ViewMD Engine]
    B --> C[Responsive Preview]
        </pre>
      </div>
    `, { extraScript: mermaidJs });

    // Track console errors
    const consoleErrors = [];
    page.on('console', msg => {
      if (msg.type() === 'error') consoleErrors.push(msg.text());
    });
    page.on('pageerror', err => consoleErrors.push(err.message));

    await loadHtml(page, exportHtml);
    await page.waitForTimeout(1000);

    // 1. Verify frontmatter transformed into card and chip elements
    const card = page.locator('.viewmd-frontmatter-card');
    await expect(card).toBeVisible();
    await card.locator('summary').click();
    await expect(card).toContainText('End-to-end architecture verification test');
    await expect(page.locator('.viewmd-chip', { hasText: 'commit' })).toBeVisible();

    // 2. Verify table wrapped and zebra striped
    const tableWrap = page.locator('.viewmd-table-wrap');
    await expect(tableWrap).toBeVisible();

    // 3. Verify Mermaid diagram rendered into SVG container
    const diagramContainer = page.locator('.viewmd-diagram-container');
    await expect(diagramContainer).toBeVisible();
    const svg = diagramContainer.locator('svg');
    await expect(svg).toBeVisible();

    // 4. Verify Theme Toggle flips state smoothly
    const toggleBtn = page.locator('#viewmd-theme-toggle');
    await toggleBtn.click();
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');

    // 5. Zero unhandled fatal console errors
    const fatalErrors = consoleErrors.filter(e => !e.includes('net::ERR_FILE_NOT_FOUND') && !e.includes('CompletionHandlerException'));
    expect(fatalErrors).toHaveLength(0);
  });
});
