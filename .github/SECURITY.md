# ViewMD Security Policy

## Threat Model & Principles

1. **Strict Local Isolation**: ViewMD runs with a strict Content Security Policy (`default-src 'none'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'`).
2. **Zero Runtime Telemetry**: ViewMD transmits no telemetry, analytics, or network payloads. All assets are bundled locally.
3. **Offline-First Diagrams**: Mermaid and PlantUML run in-process or client-side inside the JCEF sandbox without communicating with remote servers or CDNs.
4. **PlantUML Sandbox Mode**: In-process PlantUML operates under `PLANTUML_SECURITY_PROFILE=SANDBOX`. File inclusion directives (`!include`, `!import`) are strictly prohibited to prevent arbitrary local file disclosure.
5. **IPC Message Whitelist & Prototype Protection**: Webview message handlers validate event payloads against a strict type whitelist (`THEME_CHANGED`, `SETTINGS_CHANGED`, `CUSTOM_CSS_CHANGED`). Dimensions and style properties are sanitized and clamped, preventing prototype pollution and CSS injection.
6. **Custom CSS Boundary Validation**: User stylesheets must possess a `.css` extension, verify as regular files under 5MB, and resolve canonical symlinks securely.
7. **HTML Sanitization & Export Isolation**: Document titles are HTML entity-encoded, and user-provided stylesheets are neutralized against `</style>` tag breakout vectors in exported standalone documents.

## Reporting a Vulnerability

We take the security of ViewMD seriously. If you discover a vulnerability or security risk, please **do not open a public GitHub issue**.

Instead, please report it via one of the following private channels:

1. **GitHub Private Vulnerability Advisory**: Submit a private advisory at [https://github.com/MinaFayezN/ViewMD/security/advisories/new](https://github.com/MinaFayezN/ViewMD/security/advisories/new).
2. **Email**: Contact the maintainers directly at `security@mina.dev`.

Please include detailed reproduction steps, affected environment versions, and potential impact. We will acknowledge receipt within 48 hours and work with you on a coordinated fix and release before public disclosure.
