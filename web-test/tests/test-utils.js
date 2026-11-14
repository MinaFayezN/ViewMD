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

module.exports = {
  buildHtml,
  loadHtml,
  CSS_PATH,
  JS_PATH
};
