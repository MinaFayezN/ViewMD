# ViewMD delivery plan

Check boxes as you go. Each phase ends with an exit gate from `docs/ACCEPTANCE.md`. `DECISION` = stop and ask the
owner with 2-3 options and a recommendation. Requirement IDs are from `docs/REQUIREMENTS.md`.

## Phase 0: Toolchain and spike

Goal: prove the build, then choose Approach A or B with evidence.

- [x] Confirm environment facts (JDK 27 install, Gradle wrapper version, Kotlin 2.4.0 + JDK 27 support, target IDE
      build numbers, IDE JBR Java version, Android Studio install path). Table in `docs/adr/0001-toolchain.md`.
- [x] `./gradlew buildPlugin` and `./gradlew runIde` on the untouched template.
- [x] Add `bundledPlugin("org.intellij.plugins.markdown")` and `<depends>org.intellij.plugins.markdown</depends>`; list
      the extension points the bundled Markdown plugin exposes in the target IDE (read its `plugin.xml` and jar).
- [x] Spike A: through those extension points, inject a script and stylesheet into the stock preview; wrap one table in a
      scroll container; move front matter into a card. Record what is and is not possible.
- [x] Spike A2: reproduce "custom CSS not found" in Android Studio's stock settings; find the cause (path form, VFS
      refresh, project vs global) and record it. This informs R-CSS.
- [x] Spike B: minimal JCEF editor showing static HTML and receiving a JS bridge message. Estimate scroll-sync cost.
- [x] Spike C: PlantUML rendering options: bundled jar in-process (license, size, Graphviz dependency, headless
      safety) vs. delegating to the IDE PlantUML plugin. Mermaid: bundle the official dist, confirm it runs in JCEF.
- [x] `DECISION` A vs B, PlantUML engine, minimum supported IDE builds. Write ADRs 0002-0004.

Exit gate: G0 passed.

## Phase 1: Web layer and tables (the core fix)

- [x] Create `web/` (or `src/main/resources/web/`) with `viewmd.css` seeded from `docs/reference/baseline.css`, theme
      variables, and `postprocess.js`.
- [x] Table wrapper per R-TBL-1..4, 8; sticky header, zebra, min widths, `max-content` strategy from ARCHITECTURE.md.
- [x] Web tests with Playwright against `docs/samples/torture-test.md` rendered by a tiny Node script using the same
      Markdown-to-HTML options; screenshots at 400/900/1600 px, light/dark, stored under `docs/images/baseline/`.
- [x] Wire the web layer into the preview (A) or the new editor (B).
- [x] Manual check in Android Studio sandbox with `torture-test.md`; screenshot.

Exit gate: G1 passed.

## Phase 2: Editor integration and update pipeline

- [x] Split editor / preview wiring (R-ED-1), debounce updates (R-ED-3), scroll position preservation.
- [x] Scroll sync both directions without feedback loops (R-ED-2).
- [x] Links and images (R-ED-4), theme live switching (R-LAY-3), disposers and dynamic unload (R-NFR-4).
- [x] Performance harness: 200 KB and 1 MB docs (R-NFR-3).

Exit gate: G2 passed.

## Phase 3: Front matter, settings and custom CSS

- [x] Front matter parser (YAML subset first; use a vetted parser if bundled in the platform, otherwise a small
      strict one) and card UI (R-FM-1..4).
- [x] Settings service and Kotlin UI DSL configurable (R-CSS-1).
- [x] `CssResolver`, `CssWatcher`, diagnostics UI, live reload (R-CSS-2..5).
- [x] Settings migration/versioning for future releases.

Exit gate: G3 passed.

## Phase 4: Diagrams and rich content

- [x] Mermaid bundled, theme-aware, strict security level, error boxes (R-DIA-1, 3, 4).
- [x] PlantUML per the Phase 0 decision with cache, timeout, off-EDT (R-DIA-2, 3, 4).
- [x] Code fence highlighting and copy button, alerts, task lists, footnotes, math (R-DIA-5..7).
- [x] Sanitiser and CSP hardened with tests for XSS payload fixtures (R-NFR-2).

Exit gate: G4 passed.

## Phase 5: Navigation, export, polish

- [x] Heading anchors and TOC panel (R-LAY-5, 6); find in preview, zoom, print (R-ED-5).
- [x] Open in Browser and Export HTML (R-EXP-1, 2); PDF if time allows (R-EXP-3).
- [x] Table toolbar: expand, copy Markdown/TSV, optional sticky first column (R-TBL-5, 6).
- [x] Accessibility pass (R-NFR-5); plugin icon, branding, screenshots.

Exit gate: G5 passed.

## Phase 6: Hardening, docs, release candidate

- [x] `verifyPlugin` against every target; fix all compatibility warnings; deliberate `since-build`/`until-build`.
- [x] Plugin size audit and trimming (R-NFR-6).
- [x] Complete `docs/DOCUMENTATION_PLAN.md`: README, user guide, developer guide, architecture, contributing,
      security, FAQ, changelog, screenshots, Marketplace description in `plugin.xml`.
- [x] CI green on the existing workflows (`build.yml`, `release.yml`); secrets remain placeholders, listed in the README.
- [x] Release checklist rehearsal with `./gradlew buildPlugin` and a signed build dry run if secrets exist.

Exit gate: G6 passed. Tag `1.0.0-rc.1` locally.

## Known Limitations

- **PlantUML Graphviz fallback**: Complex PlantUML graphs requiring specific Graphviz plugins run via the bundled pure-Java Smetana layout engine. Very complex custom Graphviz pragmas may render with simplified layout geometry.
- **Exported HTML script execution**: Exported standalone HTML pages bundle the Mermaid runtime; opening them in strict browser sandboxes requires JavaScript enabled for interactive rendering.

## Risks

| Risk | Mitigation |
|---|---|
| Bundled Markdown plugin internals change between IDE versions | Prefer public extension points; isolate all access behind one adapter; run `verifyPlugin` per target |
| JDK 27 vs IDE runtime bytecode level | Compile with `-release` of the IDE's JBR; ADR 0001 |
| PlantUML needs Graphviz | Spike C; fall back to the smetana layout engine or the IDE plugin; document clearly |
| JCEF unavailable (remote dev, some JBRs) | Detect and show a clear message with "Open in Browser" fallback |
| Plugin size grows with bundled engines | Size budget in R-NFR-6; lazy-load heavy assets |
| Custom CSS can break the scroll wrapper | Inject after built-in CSS but keep wrapper rules with higher specificity and document the escape hatch |
| Spike result invalidates this plan | Update `PLAN.md` and `ARCHITECTURE.md` first, then continue |
