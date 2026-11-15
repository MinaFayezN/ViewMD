const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const { chromium } = require('playwright');

const CSS_PATH = path.resolve(__dirname, '../src/main/resources/web/viewmd.css');
const JS_PATH = path.resolve(__dirname, '../src/main/resources/web/postprocess.js');
const MERMAID_PATH = path.resolve(__dirname, '../src/main/resources/web/vendor/mermaid.min.js');
const SCREENSHOTS_DIR = path.resolve(__dirname, '../local/screenshots');
const DOCS_IMAGES_DIR = path.resolve(__dirname, '../docs/images');

fs.mkdirSync(SCREENSHOTS_DIR, { recursive: true });
fs.mkdirSync(path.join(SCREENSHOTS_DIR, 'video'), { recursive: true });
fs.mkdirSync(DOCS_IMAGES_DIR, { recursive: true });

const viewmdCss = fs.readFileSync(CSS_PATH, 'utf-8');
const postprocessJs = fs.readFileSync(JS_PATH, 'utf-8');
const mermaidJs = fs.readFileSync(MERMAID_PATH, 'utf-8');

function injectProductionAssets(htmlContent) {
  // Replace existing styles and scripts with latest production assets
  let modified = htmlContent;
  modified = modified.replace(/<style>[\s\S]*?<\/style>/i, () => `<style>${viewmdCss}</style>`);
  modified = modified.replace(/<script>[\s\S]*?<\/script>/gi, '');
  modified = modified.replace(
    '</body>',
    () => `<button id="viewmd-theme-toggle" class="viewmd-theme-toggle" type="button" style="display:none;"></button>
     <script>${mermaidJs}</script>
     <script>${postprocessJs}</script>
     </body>`
  );
  return modified;
}

async function run() {
  console.log('--- Starting ViewMD Screenshot & Video Generator ---');

  const browser = await chromium.launch({
    executablePath: '/usr/bin/google-chrome',
    headless: true
  });

  // Prepare fixtures with production assets
  const lightSource = fs.readFileSync(path.resolve(__dirname, 'fixtures/showcase-light.html'), 'utf-8');
  const darkSource = fs.readFileSync(path.resolve(__dirname, 'fixtures/showcase-dark.html'), 'utf-8');

  const tmpLight = path.resolve(__dirname, 'fixtures/tmp-capture-light.html');
  const tmpDark = path.resolve(__dirname, 'fixtures/tmp-capture-dark.html');

  fs.writeFileSync(tmpLight, injectProductionAssets(lightSource));
  fs.writeFileSync(tmpDark, injectProductionAssets(darkSource));

  // Clean any stale video files
  const videoDir = path.join(SCREENSHOTS_DIR, 'video');
  fs.rmSync(videoDir, { recursive: true, force: true });
  fs.mkdirSync(videoDir, { recursive: true });

  // 1. Capture High-Resolution 1280x800 Screenshots
  const page = await browser.newPage({
    viewport: { width: 1280, height: 800 }
  });

  // Screenshot 1: Front Matter Card & Top Overview
  await page.goto('file://' + tmpLight, { waitUntil: 'load' });
  await page.waitForTimeout(400);

  // Open the frontmatter card to show rich metadata
  await page.evaluate(() => {
    const card = document.querySelector('.viewmd-frontmatter-card');
    if (card) card.open = true;
  });
  await page.waitForTimeout(200);
  const shot1 = path.join(SCREENSHOTS_DIR, '01-yaml-frontmatter-card.png');
  await page.screenshot({ path: shot1 });
  console.log('Saved:', shot1);

  // Screenshot 2: Responsive Wide Table with Toolbar
  const tableWrap = page.locator('.viewmd-table-wrap').first();
  await tableWrap.scrollIntoViewIfNeeded();
  await tableWrap.hover();
  await page.waitForTimeout(200);

  const shot2 = path.join(SCREENSHOTS_DIR, '02-responsive-table-toolbar.png');
  await page.screenshot({ path: shot2 });
  console.log('Saved:', shot2);

  // Screenshot 3: Dark Mode Preview
  await page.goto('file://' + tmpDark, { waitUntil: 'load' });
  await page.waitForTimeout(400);
  const darkTableWrap = page.locator('.viewmd-table-wrap').first();
  await darkTableWrap.scrollIntoViewIfNeeded();
  await darkTableWrap.hover();
  await page.waitForTimeout(200);

  const shot3 = path.join(SCREENSHOTS_DIR, '03-dark-theme-preview.png');
  await page.screenshot({ path: shot3 });
  console.log('Saved:', shot3);

  // Screenshot 4: Offline Diagrams (Mermaid & PlantUML)
  const diagramsHeader = page.locator('h2, h3', { hasText: /Diagram|Mermaid/i }).first();
  if (await diagramsHeader.count() > 0) {
    await diagramsHeader.scrollIntoViewIfNeeded();
    await page.waitForTimeout(1000);
    const shot4 = path.join(SCREENSHOTS_DIR, '04-offline-diagrams.png');
    await page.screenshot({ path: shot4 });
    fs.copyFileSync(shot4, path.join(DOCS_IMAGES_DIR, 'offline-diagrams.png'));
    console.log('Saved:', shot4);
  }

  await page.close();

  // 1b. Capture Clean Baseline Screenshots for Visual Testing (400, 900, 1600 px)
  const baselineDir = path.join(DOCS_IMAGES_DIR, 'baseline');
  fs.mkdirSync(baselineDir, { recursive: true });
  for (const mode of ['light', 'dark']) {
    const fixturePath = 'file://' + path.resolve(__dirname, `fixtures/torture-test-${mode}.html`);
    for (const width of [400, 900, 1600]) {
      const bPage = await browser.newPage({ viewport: { width, height: 1000 } });
      await bPage.goto(fixturePath, { waitUntil: 'load' });
      await bPage.waitForTimeout(200);
      const bShot = path.join(baselineDir, `torture-test-${width}-${mode}.png`);
      await bPage.screenshot({ path: bShot });
      await bPage.close();
      console.log('Saved baseline:', bShot);
    }
  }

  // 2. Record Video Demo (WebM -> MP4 & GIF)
  console.log('Recording interactive demonstration video...');
  const videoContext = await browser.newContext({
    viewport: { width: 1280, height: 800 },
    recordVideo: {
      dir: path.join(SCREENSHOTS_DIR, 'video'),
      size: { width: 1280, height: 800 }
    }
  });

  const vPage = await videoContext.newPage();
  await vPage.goto('file://' + tmpLight, { waitUntil: 'load' });
  await vPage.waitForTimeout(600);

  // Interact: Scroll to table, hover toolbar, scroll horizontally
  const vTableWrap = vPage.locator('.viewmd-table-wrap').first();
  await vTableWrap.scrollIntoViewIfNeeded();
  await vPage.waitForTimeout(400);

  await vTableWrap.hover();
  await vPage.waitForTimeout(400);

  // Smooth horizontal scroll right then left (clean demo without button click glitches)
  await vPage.evaluate(async () => {
    const wrap = document.querySelector('.viewmd-table-wrap');
    if (!wrap) return;
    for (let i = 0; i < 400; i += 20) {
      wrap.scrollLeft = i;
      await new Promise(r => setTimeout(r, 20));
    }
    await new Promise(r => setTimeout(r, 400));
    for (let i = 400; i >= 0; i -= 20) {
      wrap.scrollLeft = i;
      await new Promise(r => setTimeout(r, 20));
    }
  });
  await vPage.waitForTimeout(600);

  const videoPath = await vPage.video().path();
  await vPage.close();
  await videoContext.close();

  // Convert the newly recorded video file
  if (videoPath && fs.existsSync(videoPath)) {
    const mp4Output = path.join(SCREENSHOTS_DIR, 'demo-table-scrolling.mp4');
    const gifOutput = path.join(SCREENSHOTS_DIR, 'demo.gif');

    console.log('Converting WebM video to MP4 and animated GIF using ffmpeg from:', videoPath);
    try {
      execSync(`ffmpeg -y -i "${videoPath}" -c:v libx264 -pix_fmt yuv420p "${mp4Output}"`, { stdio: 'ignore' });
      execSync(`ffmpeg -y -i "${videoPath}" -vf "fps=12,scale=800:-1:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" -loop 0 "${gifOutput}"`, { stdio: 'ignore' });

      // Copy showcase demo to docs/images for README
      fs.copyFileSync(gifOutput, path.join(DOCS_IMAGES_DIR, 'demo.gif'));
      console.log('Generated MP4:', mp4Output);
      console.log('Generated GIF:', gifOutput);
      console.log('Copied demo.gif to docs/images/demo.gif');
    } catch (err) {
      console.warn('ffmpeg conversion warning:', err.message);
    }
  }

  // Copy key screenshots to docs/images/ for README
  fs.copyFileSync(shot1, path.join(DOCS_IMAGES_DIR, 'yaml-frontmatter-card.png'));
  fs.copyFileSync(shot2, path.join(DOCS_IMAGES_DIR, 'responsive-table-toolbar.png'));
  fs.copyFileSync(shot3, path.join(DOCS_IMAGES_DIR, 'dark-theme-preview.png'));

  // Clean up temp fixtures
  fs.unlinkSync(tmpLight);
  fs.unlinkSync(tmpDark);

  await browser.close();
  console.log('--- Screenshot and Video Capture Complete! ---');
}

run().catch(err => {
  console.error('Capture failed:', err);
  process.exit(1);
});
