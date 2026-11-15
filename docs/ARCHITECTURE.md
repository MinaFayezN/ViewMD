# ViewMD architecture (proposal, to be confirmed in Phase 0)

Anything about IntelliJ or Markdown-plugin APIs below is **a hypothesis to verify** against the target IDE jars. Record
the findings as ADRs in `docs/adr/`.

## Two approaches

| | A. Extend the bundled Markdown plugin (preferred) | B. Own the editor and the renderer |
|---|---|---|
| Idea | Depend on `org.intellij.plugins.markdown`; contribute scripts, styles and code-fence providers to its preview through its extension points | Register our own `FileEditorProvider` with a JCEF browser; parse Markdown ourselves |
| Pros | Small code, inherits parsing, scroll sync, link handling and updates; survives IDE upgrades; users keep their normal editor | Total control of the DOM, the CSS cascade, update strategy and export; independent of the bundled plugin's internals |
| Cons | Limited by the extension points; some things (front matter, table wrapping) may need DOM post-processing in JS; internal API risk | Re-implement scroll sync, link navigation, find, HiDPI, themes; more code to maintain; two previews for the user to choose from |
| Spike question | Can we inject JS+CSS late enough to wrap `table` and move the front matter? Is the custom-CSS failure caused by the stock preview's path handling? | Cost of scroll sync and incremental DOM update with a parser that gives source positions |

Decision rule: pick A if the Phase 0 spike delivers R-TBL-1/2, R-FM-1 and R-DIA-1 through supported extension points
with a post-processing script. Otherwise pick B. In both cases the **web layer below is shared**, so most work is
reusable.

## Layers

```text
Editor (Kotlin)                      Web layer (TypeScript or plain JS, bundled)
 ├─ ViewMdFileEditor / extension      ├─ viewmd.css          (baseline from docs/reference/baseline.css)
 ├─ SettingsService (persistent)      ├─ postprocess.js      (table wrap, front matter card, anchors, alerts)
 ├─ CssResolver + CssWatcher          ├─ diagrams.js         (mermaid, plantuml SVG injection, error boxes)
 ├─ DiagramService (PlantUML, cache)  ├─ vendor/             (mermaid, katex, highlighter, offline)
 ├─ ExportService                     └─ bridge.js           (JS <-> Kotlin: scroll sync, link open, copy)
 └─ Actions, settings UI, tool menu
```

### Kotlin modules (single Gradle module is fine; keep packages clean)

| Package | Responsibility |
|---|---|
| `dev.mina.viewmd.editor` | Editor/preview wiring, split layout (Approach B) or extension registration (Approach A), toolbar actions |
| `dev.mina.viewmd.render` | Markdown to HTML (Approach B) or HTML post-hooks (A); front matter extraction; sanitiser; CSP |
| `dev.mina.viewmd.web` | Resource loading for the page (scheme handler serving bundled assets), JS bridge (`JBCefJSQuery`), theme variables |
| `dev.mina.viewmd.css` | `CssResolver` (absolute, `~`, project-relative), `CssWatcher` (VFS listener), diagnostics model |
| `dev.mina.viewmd.diagram` | Mermaid (client-side) and PlantUML (server-side in-process, cached by content hash, run off the EDT with timeout) |
| `dev.mina.viewmd.settings` | `PersistentStateComponent`, `Configurable` (Kotlin UI DSL), defaults |
| `dev.mina.viewmd.export` | HTML/PDF export, Open in Browser |
| `dev.mina.viewmd.util` | Logging, disposables, coroutines scope helper |

### Rules

- One coroutine scope per project service, cancelled on dispose (use the platform's service-scope injection).
- Rendering runs on a background dispatcher; only UI updates touch the EDT.
- Every `JBCefBrowser`, `JBCefJSQuery` and VFS listener is registered with a `Disposable` parent.
- The web layer is pure functions of (HTML, settings, theme) so it can be tested outside the IDE.
- Bundled assets are served through a custom scheme or `loadHTML` with a base URL; no `file://` reads of arbitrary
  paths. The only external file read is the user's custom CSS, through the resolver.

## Data flow (one update)

1. Document change -> debounce -> background: markdown to HTML (+ front matter split) -> sanitise.
2. Send `{html, frontMatter, settings, theme}` to the page through the bridge; `postprocess.js` patches the DOM in place
   (no full reload), restores scroll position.
3. `diagrams.js` renders Mermaid blocks; PlantUML blocks are rendered by `DiagramService`, returned as SVG, injected.
4. Custom CSS is injected last in a `<style id="viewmd-user-css">`; the resolver reports status to the settings page.

## Table layout strategy (the main bug)

```css
.viewmd-table-wrap { overflow-x: auto; max-width: 100%; }
.viewmd-table-wrap > table { width: max-content; min-width: 100%; border-collapse: collapse; table-layout: auto; }
.viewmd-table-wrap th, .viewmd-table-wrap td { min-width: var(--viewmd-col-min, 12ch); max-width: 60ch; overflow-wrap: break-word; vertical-align: top; }
.viewmd-table-wrap td code, .viewmd-table-wrap td a { overflow-wrap: anywhere; }
```

`max-content` + `min-width: 100%` makes small tables natural width and wide tables scroll; the per-cell `min-width`
prevents one-letter columns. Verify with the torture test in a 400 px, 900 px and 1600 px window.

## Compatibility and build

- Platform Gradle plugin 2.x. Declare `intellijIdea("2026.2.4")` for compile and add `androidStudio(...)` as a second
  verification target. Use `pluginVerification { ides { ... } }` for each supported product.
- Kotlin jvmTarget and `-release` follow the IDE's JBR, not the build JDK (configured in `gradle.properties` and `build.gradle.kts`).
- Gradle configuration cache and build cache are already enabled in `gradle.properties`; keep them working.
- Version catalog in `gradle/libs.versions.toml`; no versions inline in `build.gradle.kts`.

## Security model

- HTML from Markdown is sanitised with an allow-list (no `script`, `iframe`, `object`, event attributes, `javascript:`
  URLs). Raw HTML in Markdown is allowed only after sanitising.
- CSP on the page: `default-src 'none'; img-src data: <project-local scheme>; style-src 'self' 'unsafe-inline'; script-src 'self'`.
- Remote images blocked by default, one-click allow per document or a setting.
- Mermaid runs with `securityLevel: 'strict'`.
- No network, no telemetry, no eval of document content.

## Testing strategy

| Level | Tool | What |
|---|---|---|
| Unit | JUnit 4 (platform test framework) | front matter parser, CSS resolver, settings, sanitiser, export |
| Web | Node + Playwright (headless Chromium) or jsdom against the shared web layer | table wrapping, front matter card, alerts, anchors, resize behaviour; screenshots at 400/900/1600 px, light and dark |
| Integration | `BasePlatformTestCase` + JCEF where supported | editor opens, settings persist, CSS watcher reloads |
| UI | IntelliJ Starter / Driver (if feasible) | open `torture-test.md` in Android Studio sandbox and capture screenshots |
| Compatibility | `./gradlew verifyPlugin` | every target IDE |
| Performance | scripted 1 MB doc | render time and EDT stalls |
