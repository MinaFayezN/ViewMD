# Acceptance criteria and exit gates

A gate passes only when every line is verified by running something, with the evidence (command output or screenshot)
saved or quoted in the phase notes. "Looks right" is not evidence for layout: capture screenshots at 400, 900 and
1600 px wide, in light and dark.

## G0: Toolchain and spike

- [ ] `./gradlew buildPlugin` exits 0 on JDK 27; output zip exists.
- [ ] `./gradlew runIde` launches the target IDE sandbox; the plugin is listed.
- [ ] ADRs 0001-0004 exist and each cites the jar/class/doc it was verified against.
- [ ] The owner has answered the A vs B and PlantUML decisions.

## G1: Tables and web layer

- [ ] `torture-test.md` wide table (12 columns, long text): scrolls horizontally, no column narrower than the minimum,
      zero single-letter-per-line wrapping, header sticky.
- [ ] Narrow table (3 short columns) renders at natural width, not stretched.
- [ ] Table with URLs, hashes, inline code, `\|` and `<br>`: readable, no overflow outside its container.
- [ ] Page never gets a horizontal scrollbar at 400 px.
- [ ] Web tests pass in CI mode; screenshots committed under `docs/images/baseline/`.
- [ ] Matches `docs/reference/baseline.css` look (spot check against `mdview` output screenshot).

## G2: Editor integration

- [ ] Typing in a 200 KB doc updates the preview within 300 ms; no EDT block over 50 ms (log or profiler evidence).
- [ ] Scroll sync works both ways; no oscillation.
- [ ] Relative links open IDE files; relative images show; external links open the system browser.
- [ ] Switching IDE theme updates the preview without reopening.
- [ ] Disable/enable the plugin without restart works; no leaked browsers (check the disposer test).

## G3: Front matter, settings, CSS

- [ ] Valid front matter: card collapsed by default, summary text and trigger chips shown.
- [ ] Invalid front matter (unquoted `: ` in summary, unclosed block): warning with line and message; body still renders.
- [ ] `index: false` shows the "Not indexed" tag.
- [ ] Custom CSS: absolute, `~/`, and project-relative paths all resolve; diagnostics show the resolved path and state;
      editing the file live-updates the preview within 1 s; deleting it shows "not found" and falls back cleanly.
- [ ] Settings persist across IDE restarts; "Reset to defaults" works.

## G4: Diagrams and rich content

- [ ] Mermaid flowchart, sequence and class diagrams render offline (network disabled) in light and dark.
- [ ] PlantUML sequence and class diagrams render offline; invalid source shows an error box with the source.
- [ ] Alerts, task lists, footnotes, math, code highlighting and copy buttons work.
- [ ] XSS fixtures (script tags, `onerror`, `javascript:` links, `<iframe>`, SVG with script) are neutralised; test proves it.
- [ ] No outbound network calls during a full torture-test render (verified with a proxy or network namespace).

## G5: Navigation, export, polish

- [ ] Anchors and TOC work; `#fragment` links scroll.
- [ ] Open in Browser produces a self-contained page that looks the same as the preview.
- [ ] Table toolbar: expand, copy Markdown, copy TSV produce correct output.
- [ ] Keyboard-only navigation works; contrast checked in both themes.

## G6: Release candidate

- [ ] `./gradlew verifyPlugin` clean for every target IDE (IntelliJ IDEA and Android Studio).
- [ ] Plugin zip under 15 MB or the overage is justified in an ADR.
- [ ] Documentation set complete per `docs/DOCUMENTATION_PLAN.md`; every command in the docs was executed once.
- [ ] CI workflows pass locally where reproducible (`./gradlew check`, `buildPlugin`).
- [ ] `CHANGELOG.md` has the 1.0.0 entry; `plugin.xml` description, vendor and change notes are real, not template text.
- [ ] Known limitations section written, honest.
