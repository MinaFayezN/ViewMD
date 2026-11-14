const http = require('http');

let currentHtml = '<!doctype html><html><body>Ready</body></html>';

const server = http.createServer((req, res) => {
  if (req.method === 'POST' && req.url === '/set-content') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      currentHtml = body;
      res.writeHead(200, { 'Content-Type': 'text/plain' });
      res.end('OK');
    });
    return;
  }
  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(currentHtml);
});

const PORT = process.env.PORT || 4321;
server.listen(PORT, () => {
  console.log(`ViewMD test server listening on http://localhost:${PORT}`);
});
