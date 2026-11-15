const fs = require('fs');
const path = require('path');

const CSS_PATH = path.resolve(__dirname, '../../src/main/resources/web/viewmd.css');
const JS_PATH = path.resolve(__dirname, '../../src/main/resources/web/postprocess.js');

function buildHtml(bodyContent, options = {}) {
  const viewmdCss = fs.readFileSync(CSS_PATH, 'utf-8');
  const postprocessJs = fs.readFileSync(JS_PATH, 'utf-8');
  const { isDark = false, extraHead = '', extraScript = '' } = options;
  return `<!doctype html>
<html data-theme="${isDark ? 'dark' : 'light'}">
<head>
  <meta charset="utf-8">
  <style>
  ${viewmdCss}
  </style>
  ${extraHead}
  ${extraScript ? `<script>${extraScript}</script>` : ''}
</head>
<body>
  ${bodyContent}
  <script>
  ${postprocessJs}
  </script>
</body>
</html>`;
}

async function loadHtml(page, html) {
  await page.request.post('http://localhost:4321/set-content', {
    data: html,
  });
  await page.goto('http://localhost:4321');
}

function getLuminance(r, g, b) {
  const [rs, gs, bs] = [r, g, b].map((c) => {
    c = c / 255;
    return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
  });
  return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs;
}

function calculateContrastRatio(rgb1Str, rgb2Str) {
  function parseRgb(str) {
    const m = (str || '').match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/);
    if (!m) return [0, 0, 0];
    return [parseInt(m[1], 10), parseInt(m[2], 10), parseInt(m[3], 10)];
  }
  const [r1, g1, b1] = parseRgb(rgb1Str);
  const [r2, g2, b2] = parseRgb(rgb2Str);
  const l1 = getLuminance(r1, g1, b1);
  const l2 = getLuminance(r2, g2, b2);
  const brightest = Math.max(l1, l2);
  const darkest = Math.min(l1, l2);
  return (brightest + 0.05) / (darkest + 0.05);
}

module.exports = {
  buildHtml,
  loadHtml,
  calculateContrastRatio,
  CSS_PATH,
  JS_PATH
};
