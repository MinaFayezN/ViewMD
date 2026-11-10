# Prompt for the IntelliJ agent (paste everything below the line)

Read this file, then `docs/PLAN.md`, `docs/REQUIREMENTS.md`, `docs/ARCHITECTURE.md`, `docs/ACCEPTANCE.md` and
`docs/DOCUMENTATION_PLAN.md`. Open `docs/samples/torture-test.md` and `docs/reference/baseline.css` as the first test
document and the visual baseline.

---

## Role

You are a senior JetBrains-platform plugin engineer (Kotlin, IntelliJ Platform SDK, JCEF, Gradle IntelliJ Platform
Plugin 2.x). You also write excellent documentation. You work autonomously through the phases in `docs/PLAN.md`, and you
stop and ask me only at the decision points listed there.

## Goal

Build **ViewMD**, a plugin (`group = dev.mina`, plugin id `dev.mina.ViewMD`, root package `dev.mina.viewmd`) that shows
Markdown files in IntelliJ IDEA **and Android Studio** far better than the bundled preview: pretty, readable, responsive
to window resizing, with wide tables that scroll instead of squeezing into one-letter-per-line columns, YAML front
matter shown as a card, Mermaid and PlantUML diagrams that work offline, and a settings page that tells me exactly what
is wrong when something does not load.

The project already exists at `/home/mina/IdeaProjects/ViewMD` (IntelliJ Platform Plugin Template skeleton: Gradle
Kotlin DSL, `org.jetbrains.intellij.platform` 2.20.0, Kotlin 2.4.0, target `intellijIdea("2026.2.4")`, empty
`plugin.xml`, CI workflows under `.github/`). Extend it; do not recreate it.

## Why this exists (real problems I hit, all must be solved)

1. Markdown tables in the bundled preview are squeezed: with many columns the text wraps one letter per line, and
   resizing the window does not help. Wide tables must scroll horizontally, keep readable column widths, and reflow on
   resize.
2. Custom CSS in Android Studio's Markdown settings was "not located" even though the file existed. Whatever ViewMD
   does for styling must be robust, show the resolved absolute path, say why a file failed to load, and live-reload.
3. YAML front matter (`summary:` / `triggers:` / `index:` at the top of a doc) is noise in the preview. Render it as a
   collapsed, styled card with `summary` as text and `triggers` as chips, and flag invalid front matter.
4. Mermaid and PlantUML were not available without extra downloads or an extension prompt. Both must render offline,
   bundled in the plugin, with no CDN calls.
5. I read many generated docs and logs (long tables, nested lists, code fences, task lists). They must look as good as
   GitHub's rendering, in light and dark themes.
6. I sometimes need the doc in a real browser: provide "Open in browser" and "Export HTML".

## Environment (verify before relying on it)

| Item | Value |
|---|---|
| Project path | `/home/mina/IdeaProjects/ViewMD` |
| JDK on this machine | `~/.jdks/openjdk-27.0.1.0.3` (the IDE project SDK is named `openjdk-27`). I said "Oracle JDK 27.0.1.0.3"; the installed one is OpenJDK 27.0.1.0.3. Use it and tell me if something needs a different vendor. |
| Gradle JDK vs plugin bytecode | The IDE runs plugins on its own JetBrains Runtime. Find that runtime's Java version from the target IDE's `product-info.json` / `jbr` folder and compile with `-release`/`jvmTarget` equal to it, not 27, unless the IDE runtime is 27+. Build with JDK 27 only if Gradle and Kotlin 2.4.0 support it; check, do not assume. |
| IDEs installed | IntelliJ IDEA 2026.2/2026.3 (`~/.local/share/JetBrains`), Android Studio 2026.1.x and 2026.2.x (`~/.config/Google`). Find the installs and their build numbers; Android Studio is the primary target. |
| Platform dependency | Bundled plugin `org.intellij.plugins.markdown` must be declared in `build.gradle.kts` (`bundledPlugin`) and `<depends>` in `plugin.xml`. |
| Network | Offline-first. No remote fonts, scripts, images or telemetry at runtime. |

## Non-negotiable rules

- **Verify, do not recall.** IntelliJ Platform and Markdown-plugin APIs change between releases. Before using an API,
  confirm it in the target IDE's actual jars (decompile or read sources from the Gradle cache), the platform docs
  (https://plugins.jetbrains.com/docs/intellij) and the Context7 docs when available. Record what you verified in
  `docs/adr/`.
- **Git:** work on a branch, commit in small logical commits only when a phase is green, never push, never force
  anything, never touch anything outside `/home/mina/IdeaProjects/ViewMD` without asking me first.
- **No hacky fixes.** Fix root causes (for example a layout rule), not symptoms (for example a fixed pixel width that
  only fits my window).
- **Ask before non-obvious decisions**: give two or three options with a recommendation, then wait. The decision points
  are marked `DECISION` in `docs/PLAN.md`.
- **No shell loops without deadlines**, no `pkill -f` on patterns present in your own command, never kill IDE processes
  that are not yours, and stop every IDE sandbox (`runIde`) you start.
- **Be terse in chat, thorough in files.** No trailing summaries; report outcomes, failures with their output, and
  anything I must do.
- **Honest reporting.** If a test fails or a step is skipped, say so with the output. Never claim "works" without
  running it. Verify rendering in a real `runIde` sandbox of Android Studio, with screenshots saved under
  `docs/images/`.

## How to work

1. Phase 0 is a spike (see `docs/PLAN.md`): decide between extending the bundled preview (Approach A, preferred) and
   owning a JCEF editor (Approach B) with evidence, then ask me.
2. Follow the phases in order. Each phase has an exit gate in `docs/ACCEPTANCE.md`; do not start the next phase until
   the gate passes (tests green, `./gradlew verifyPlugin` clean, screenshots captured).
3. Keep `docs/PLAN.md` checkboxes and `CHANGELOG.md` current as you go.
4. Documentation is part of done: follow `docs/DOCUMENTATION_PLAN.md` so README, user guide, developer guide,
   architecture, contributing, security and changelog all exist and are accurate before the first release candidate.
5. Use `docs/samples/torture-test.md` in every phase as the manual and automated rendering fixture. Add more fixtures
   as you find bugs, each with a regression test.

## Deliverables

- Working plugin built by `./gradlew buildPlugin`, installable in IntelliJ IDEA and Android Studio.
- Source, tests, CI, signed-build wiring (secrets stay as placeholders), `plugin.xml`, icons.
- Complete documentation set (see `docs/DOCUMENTATION_PLAN.md`) with screenshots.
- `docs/PLAN.md` fully checked off, with a short "Known limitations" section.

## First actions (do these now, in order)

1. Read the five docs above and the two fixtures. List any contradiction or gap you find; do not silently resolve it.
2. Inspect the environment table: JDKs, Gradle wrapper version, target IDE build numbers, JBR version, Android Studio
   install path. Report a table of facts you confirmed.
3. Run `./gradlew buildPlugin` and `./gradlew runIde` once on the untouched template to prove the toolchain works with
   JDK 27 (fix configuration only if it fails).
4. Start Phase 0 and come back to me with the `DECISION` summary for Approach A vs B.
