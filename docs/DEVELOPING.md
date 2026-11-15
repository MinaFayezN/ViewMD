---
summary: "Developer Guide for building, testing, and debugging ViewMD"
triggers: [developer guide, building ViewMD, running tests, gradle commands]
---

# ViewMD Developer Guide

This guide is for contributors working on ViewMD.

## Prerequisites

- JDK 25+ (configured via toolchain or local OpenJDK distribution)
- Node.js LTS (for web layer Playwright verification)

## Automated Regression Testing Mandate

> [!IMPORTANT]
> **Core Engineering Rule**: Every bug fix, user feedback item, security mitigation, and UI change **must** include a dedicated automated regression test covering that exact scenario before merging or release.
>
> - **Kotlin layer changes**: Add JUnit 4 unit tests in `src/test/kotlin/dev/mina/viewmd/` or platform integration tests (`BasePlatformTestCase`).
> - **Web/DOM/CSS changes**: Add Playwright end-to-end tests in `web-test/tests/`.
> - **Layout/Responsiveness changes**: Run and verify across the torture-test multi-viewport harness (`npm run test:torture`).
>
> PRs and code updates without automated regression tests will not be accepted.

## Building the Plugin

```bash
./gradlew buildPlugin
```
Distribution archive will be generated at `build/distributions/ViewMD-1.0.0.zip`.

## Running Tests

### Kotlin Unit & Platform Tests (68 tests)
```bash
./gradlew test
```

### Playwright Web & E2E Tests (29 tests)
```bash
cd web-test
npx playwright test
```

### Visual Multi-Viewport Torture Tests (6 viewports)
```bash
cd web-test
npm run test:torture
```

### Performance Benchmark Suite
```bash
cd web-test
node bench_perf.js
```

## Security & Sanitization Architecture

ViewMD enforces strict security boundaries across IPC, diagram rendering, and stylesheets:
1. **IPC & Messaging (`postprocess.js`)**: All `postMessage` events are strictly validated against an allowed-type whitelist (`THEME_CHANGED`, `SETTINGS_CHANGED`, `CUSTOM_CSS_CHANGED`). CSS variables and numeric dimensions are clamped and sanitized to prevent prototype pollution or CSS injection.
2. **Custom CSS Isolation (`CssResolver.kt`)**: Custom stylesheet paths must have a `.css` extension, must be regular files, must not exceed 5MB, and resolve canonical symlink targets.
3. **PlantUML Containment (`PlantUmlRenderer.kt`)**: Runs in in-process `SANDBOX` profile with external preprocessor inclusion directives (`!include`, `!import`) disabled to prevent local file reads.
4. **HTML Export Sanitization (`HtmlExporter.kt`)**: Document titles are HTML entity-encoded, and user stylesheets have closing `</style>` tags escaped.

## Launching the IDE Sandbox

```bash
./gradlew runIde
```
This starts an isolated sandbox of IntelliJ IDEA with ViewMD loaded.
