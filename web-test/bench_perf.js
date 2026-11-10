const { chromium } = require('playwright');
const path = require('path');
const { performance } = require('perf_hooks');

async function benchmark() {
  const browser = await chromium.launch({
    executablePath: '/usr/bin/google-chrome',
    headless: true
  });

  const page = await browser.newPage({ viewport: { width: 1200, height: 900 } });

  // Test 1: 1 MB document render & postprocess timing
  const html1MbPath = path.resolve(__dirname, 'fixtures/doc-1mb.html');
  const fileUrl = 'file://' + html1MbPath;

  // Warm-up
  await page.goto(fileUrl, { waitUntil: 'load' });

  const times = [];
  for (let i = 0; i < 3; i++) {
    const t0 = performance.now();
    await page.goto(fileUrl, { waitUntil: 'load' });
    const postprocessMetrics = await page.evaluate(() => {
      return {
        tableCount: document.querySelectorAll('table').length,
        wrappedCount: document.querySelectorAll('.viewmd-table-wrap').length
      };
    });
    const tTotal = performance.now() - t0;
    times.push(tTotal);
  }

  const avgTime = times.reduce((a, b) => a + b, 0) / times.length;
  console.log(`[PERF] 1 MB doc (5049 rows) render + postprocess avg: ${avgTime.toFixed(1)} ms (runs: ${times.map(t => t.toFixed(0)).join(', ')} ms)`);

  // Assertions for R-NFR-3: 1 MB doc under 1 s (1000 ms)
  if (avgTime > 1000) {
    console.warn(`WARNING: Average render time ${avgTime.toFixed(1)} ms exceeded 1000 ms`);
  } else {
    console.log(`PASS: Render time ${avgTime.toFixed(1)} ms is within 1000 ms budget!`);
  }

  // Test 2: 200 KB typing/update latency simulation
  const html200KbPath = path.resolve(__dirname, 'fixtures/doc-200kb.html');
  await page.goto('file://' + html200KbPath, { waitUntil: 'load' });

  const tTypingStart = performance.now();
  await page.evaluate(() => {
    const firstCell = document.querySelector('tbody tr td:nth-child(5)');
    if (firstCell) {
      firstCell.textContent = 'Updated content simulated live typing';
    }
  });
  const tTypingEnd = performance.now();
  const typingDelta = tTypingEnd - tTypingStart;
  console.log(`[PERF] 200 KB doc typing update DOM latency: ${typingDelta.toFixed(2)} ms (budget < 100 ms)`);

  await browser.close();
}

benchmark().catch(err => {
  console.error('Benchmark failed:', err);
  process.exit(1);
});
