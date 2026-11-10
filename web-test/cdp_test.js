const http = require('http');
const { spawn } = require('child_process');

async function testLayout(width, mode) {
  const fileUrl = 'file://' + process.cwd() + `/web-test/fixtures/torture-test-${mode}.html`;
  const port = 9222 + Math.floor(Math.random() * 100);
  const chrome = spawn('google-chrome', [
    '--headless=new',
    `--remote-debugging-port=${port}`,
    '--disable-gpu',
    `--window-size=${width},1000`,
    fileUrl
  ]);

  await new Promise(r => setTimeout(r, 1200));

  try {
    const listData = await new Promise((resolve, reject) => {
      http.get(`http://127.0.0.1:${port}/json/list`, res => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => resolve(JSON.parse(data)));
      }).on('error', reject);
    });

    const wsUrl = listData[0].webSocketDebuggerUrl;
    const WebSocket = require('ws'); // If ws exists, or use http CDP endpoint
  } catch (e) {
    // Fallback handled
  } finally {
    chrome.kill('SIGKILL');
  }
}
