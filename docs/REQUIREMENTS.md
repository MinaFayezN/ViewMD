# ViewMD requirements

Priorities: **P0** must ship in 1.0, **P1** should ship in 1.0, **P2** later. IDs are referenced from
`docs/PLAN.md` and `docs/ACCEPTANCE.md`.

## Problems from the field

| ID | Problem seen | Root cause (known or suspected) | Requirement |
|---|---|---|---|
| F1 | Table columns squeezed, text wraps one letter per line | Tables use `width:100%` with `table-layout:auto` and no min width, so many columns shrink to the narrowest word | R-TBL-1..6 |
| F2 | Resizing the window does not help | Layout has no scroll container; table is clipped or reflowed to zero | R-TBL-2, R-LAY-1 |
| F3 | Android Studio could not find the custom CSS file | Path resolution or file watching of the Custom CSS setting; unclear error | R-CSS-1..4 |
| F4 | Prompted to install the PlantUML extension | Diagram renderers are separate downloads | R-DIA-1..3 |
| F5 | No Mermaid | Not bundled | R-DIA-1, R-DIA-4 |
| F6 | YAML front matter shows as raw text | Not recognised by the stock preview | R-FM-1..4 |
| F7 | Needed a browser view for wide docs | No export | R-EXP-1..3 |
| F8 | Prettier/qlty wrap or re-pad tables, so source tables look different from preview | Source formatting vs rendering | Out of scope, but fixtures must include padded tables |

## Functional requirements

### Layout and typography (R-LAY)

| ID | P | Requirement |
|---|---|---|
| R-LAY-1 | P0 | Content column width is configurable (default 980 px, "full width" option); reflows on every resize without reload |
| R-LAY-2 | P0 | Typography matches GitHub's rendering closely: heading scale, rule under h1/h2, blockquote bar, inline code chip, 15 px base, 1.6 line height |
| R-LAY-3 | P0 | Follows the IDE theme (light, dark, high contrast, new UI) and changes live when the theme changes |
| R-LAY-4 | P1 | Font family and size settings; zoom with Ctrl+wheel and Ctrl+0 reset, persisted per project |
| R-LAY-5 | P1 | Heading anchors (hover link icon), working `#fragment` links inside the doc |
| R-LAY-6 | P1 | Outline/TOC side panel, collapsible, synced with scroll position |

### Tables (R-TBL)

| ID | P | Requirement |
|---|---|---|
| R-TBL-1 | P0 | Every table is wrapped in a scroll container with `overflow-x:auto`; the page itself never scrolls sideways |
| R-TBL-2 | P0 | Columns have a readable minimum width (default 12 ch, first column 14 ch, configurable) and text wraps at word boundaries; no word is ever broken letter-by-letter unless it is a single token longer than the column (URLs, hashes) which use `overflow-wrap:anywhere` |
| R-TBL-3 | P0 | Small tables (fit in the content width) render at natural width, not stretched; wide tables scroll |
| R-TBL-4 | P0 | Sticky header row while scrolling a long table; zebra rows; `vertical-align:top` |
| R-TBL-5 | P1 | Optional sticky first column for tables wider than the viewport |
| R-TBL-6 | P1 | Per-table toolbar on hover: expand to full-window overlay, copy as Markdown, copy as TSV/CSV |
| R-TBL-7 | P2 | Click-to-sort columns (client-side, numeric-aware) |
| R-TBL-8 | P1 | Alignment (`:---`, `:---:`, `---:`) respected; inline code, links, `<br>` and escaped pipes inside cells render correctly |

### Front matter (R-FM)

| ID | P | Requirement |
|---|---|---|
| R-FM-1 | P0 | A leading `---` YAML block is removed from the body and shown as a collapsed card titled "Front matter" |
| R-FM-2 | P0 | Keys `summary` (text) and `triggers` (chips) get first-class display; unknown keys shown as a key/value list |
| R-FM-3 | P0 | Invalid YAML or an unclosed block shows a warning badge with the parse error and line; the doc body still renders |
| R-FM-4 | P1 | Setting to hide the card entirely or expand it by default; recognises `index: false` and shows an "Not indexed" tag |
| R-FM-5 | P2 | Optional lint hints for the team docs convention: summary <= 120 chars, 1-4 triggers on one line (configurable, off by default) |

### Diagrams and rich blocks (R-DIA)

| ID | P | Requirement |
|---|---|---|
| R-DIA-1 | P0 | Fenced ```mermaid blocks render offline from a bundled Mermaid build, theme-aware (light/dark) |
| R-DIA-2 | P0 | Fenced ```plantuml / ```puml blocks render offline; decide the engine in Phase 0 (bundled PlantUML jar in-process vs. the IDE PlantUML plugin if present) and document the choice |
| R-DIA-3 | P0 | A diagram that fails to render shows the error and the source, never a blank space or an IDE exception |
| R-DIA-4 | P1 | Diagram toolbar: zoom, fullscreen, copy source, save as SVG/PNG |
| R-DIA-5 | P1 | Math: `$...$` and `$$...$$` via bundled KaTeX |
| R-DIA-6 | P1 | GitHub alerts (`> [!NOTE]`, `[!TIP]`, `[!IMPORTANT]`, `[!WARNING]`, `[!CAUTION]`), task lists, footnotes, strikethrough, autolinks, emoji shortcodes, `<details>` |
| R-DIA-7 | P1 | Code fences: bundled syntax highlighting for common languages (kotlin, java, xml, json, yaml, bash, sql, gradle, diff), copy button, language label |

### Custom CSS and settings (R-CSS)

| ID | P | Requirement |
|---|---|---|
| R-CSS-1 | P0 | Settings page "Languages & Frameworks > ViewMD" with: width, fonts, table widths, front matter behaviour, diagrams, custom CSS path |
| R-CSS-2 | P0 | Custom CSS path field validates on edit: shows the resolved absolute path, "found / not found / not readable / empty", and the file size |
| R-CSS-3 | P0 | Custom CSS is live-reloaded on save (VFS listener), applied after the built-in CSS, scoped so it cannot break the scroll wrapper unless it says so |
| R-CSS-4 | P0 | Per-project CSS: relative paths resolve against the project root; `~` expands; both are shown in the diagnostics |
| R-CSS-5 | P1 | "Reset to defaults" and "Export current effective CSS" buttons for debugging |

### Editor integration (R-ED)

| ID | P | Requirement |
|---|---|---|
| R-ED-1 | P0 | Split editor (editor / preview / both) for `*.md`, `*.markdown`; works for read-only and non-project files; replaces or augments the bundled preview per the Phase 0 decision |
| R-ED-2 | P0 | Scroll sync editor <-> preview, both directions, no feedback loop |
| R-ED-3 | P0 | Updates as you type with debounce (default 150 ms), never blocks the EDT, keeps scroll position |
| R-ED-4 | P1 | Relative links open files in the IDE (with fragment); relative images load; external links open in the system browser; `http://` images blocked unless allowed |
| R-ED-5 | P1 | Find in preview (Ctrl+F), print, and actions in the editor toolbar and a tool menu |
| R-ED-6 | P1 | Preview-only "reader mode" for opening `.md` from the Project view double-click (setting) |

### Export (R-EXP)

| ID | P | Requirement |
|---|---|---|
| R-EXP-1 | P0 | Action "Open in Browser" writes a self-contained HTML (CSS and diagram assets inlined or beside it) to a temp dir and opens it |
| R-EXP-2 | P1 | Action "Export as HTML..." to a chosen path, single file, dark/light by setting |
| R-EXP-3 | P2 | "Export as PDF" through JCEF print-to-PDF |

## Non-functional requirements (R-NFR)

| ID | P | Requirement |
|---|---|---|
| R-NFR-1 | P0 | Compatible with IntelliJ IDEA 2026.2+ and Android Studio 2026.1+ / 2026.2+; `./gradlew verifyPlugin` passes against all targets; declare `since-build`/`until-build` deliberately |
| R-NFR-2 | P0 | No remote requests at runtime; a strict Content-Security-Policy in the preview page; markdown HTML sanitised; no script execution from document content |
| R-NFR-3 | P0 | 1 MB document renders in under 1 s after open on this machine, typing latency under 100 ms for a 200 KB doc; no EDT work longer than 50 ms |
| R-NFR-4 | P0 | Dynamic plugin: loads and unloads without restart (no static state leaks, disposers registered), no deprecated or internal API unless justified in an ADR |
| R-NFR-5 | P0 | Accessible: keyboard navigation in the preview, sufficient contrast, tables keep semantic `th`/`td`, diagram `<title>`/alt text |
| R-NFR-6 | P1 | Plugin size under 15 MB (bundled Mermaid, KaTeX, highlighter, PlantUML counted); document the breakdown |
| R-NFR-7 | P0 | No telemetry. Settings and caches only under the IDE config/system dirs |

## Out of scope for 1.0

Editing in the preview (WYSIWYG), collaboration, remote rendering services, a Markdown formatter/linter (Prettier,
markdownlint stay external), non-JetBrains IDEs.
