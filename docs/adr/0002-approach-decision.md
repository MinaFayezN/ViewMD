---
summary: "Decision between Approach A (Extend Bundled Markdown Plugin) and Approach B (Own Editor)"
triggers: [Approach A vs B, architecture decision, Markdown extension points]
---

# ADR 0002: Approach A vs Approach B Architecture Decision

## Context

ViewMD must provide enhanced Markdown preview (responsive wide tables that scroll horizontally, front matter rendered as cards, offline Mermaid/PlantUML diagrams, and robust custom CSS). In `docs/PLAN.md` and `docs/ARCHITECTURE.md`, two architectural approaches were identified:
- **Approach A (Preferred)**: Extend the bundled `org.intellij.plugins.markdown` preview via its public extension points (`browserPreviewExtensionProvider`, `fenceGeneratingProvider`, `previewStylesProvider`).
- **Approach B**: Register a dedicated `FileEditorProvider` that owns a JCEF browser and re-implements parsing, live preview debouncing, bidirectional scroll synchronization, and file navigation.

## Spike Findings

Decompilation and inspection of `org.intellij.plugins.markdown.jar` in Android Studio 2026.2.2 and IntelliJ IDEA 2026.2.4 revealed:
1. `browserPreviewExtensionProvider` (`org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension$Provider`):
   - Fully public and dynamic.
   - Allows injecting bundled CSS (`getStyles()`) and bundled JavaScript (`getScripts()`) through `ResourceProvider.loadInternalResource`.
   - Executes scripts inside the preview JCEF DOM at runtime. A lightweight `postprocess.js` can wrap `table` tags in `.viewmd-table-wrap` and hoist `pre.frontmatter` into styled cards.
2. `fenceGeneratingProvider` (`org.intellij.plugins.markdown.extensions.CodeFenceGeneratingProvider`):
   - Allows intercepting fenced code blocks (e.g. `mermaid`, `plantuml`, `puml`) and generating replacement HTML or SVG.
3. Bidirectional scroll synchronization, editor layout splitting, file link navigation, and document change debouncing are already solved and battle-tested in the bundled plugin. Re-implementing them (Approach B) introduces heavy maintenance burden, risks breaking on new IDE releases, and confuses users with duplicate editor tabs.

## Decision

**Adopt Approach A (Extend the bundled Markdown preview).**
ViewMD will contribute scripts, styles, and diagram code fence generators via the bundled Markdown plugin's extension points.

## Consequences

- Minimizes code footprint and avoids re-implementing scroll synchronization and editor split logic.
- Table wrapping and front matter presentation are handled cleanly via injected web assets (`viewmd.css` + `postprocess.js`).
- Keeps full compatibility with future IntelliJ and Android Studio updates.
