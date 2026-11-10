---
summary: "Security model, Content-Security-Policy, and vulnerability disclosure policy"
triggers: [security, CSP, XSS protection, reporting vulnerabilities]
---

# ViewMD Security Policy

## Threat Model & Principles

1. **Strict Local Isolation**: ViewMD runs with a strict Content Security Policy (`default-src 'none'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'`).
2. **Zero Runtime Telemetry**: ViewMD transmits no telemetry, analytics, or network payloads.
3. **Offline-First Diagrams**: Mermaid and PlantUML run in-process or client-side inside the JCEF sandbox without communicating with remote servers or CDNs.
4. **HTML Sanitization**: Raw HTML embedded in Markdown is sanitised before insertion to prevent script execution and cross-site scripting (XSS) vectors.

## Reporting Vulnerabilities

If you identify a security issue, please contact the maintainers directly at `security@mina.dev` instead of filing public issue tickets.
