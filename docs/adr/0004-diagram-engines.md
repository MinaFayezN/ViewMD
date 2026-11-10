---
summary: "Offline diagram rendering strategy: client-side Mermaid and in-process PlantUML"
triggers: [diagram engines, offline Mermaid, PlantUML, Smetana]
---

# ADR 0004: Diagram Rendering Engines (Offline Mermaid & PlantUML)

## Context

Requirement R-DIA-1 through R-DIA-4 requires rendering Mermaid and PlantUML diagrams completely offline with zero CDN dependencies and without prompting users to install external tools.

## Spike Analysis (Spike C)

1. **Mermaid**:
   - Official Mermaid JS bundle runs client-side inside JCEF.
   - Evaluated in `postprocess.js` / `diagrams.js` against `<code class="language-mermaid">` blocks.
   - Runs with strict CSP (`securityLevel: 'strict'`).
   - SVG output is inserted directly into the DOM container with zoom and copy controls.
2. **PlantUML**:
   - Options evaluated:
     - Option 1: Rely on external `dot` (Graphviz) binary. Fails if user does not have Graphviz installed.
     - Option 2: Smetana (pure Java layout engine built into PlantUML). Works 100% headless in-process with no C library dependencies.
     - Option 3: Delegate to JetBrains PlantUML plugin if present; otherwise fall back to bundled PlantUML core with Smetana.
3. **Packaging / Size Impact**:
   - Bundling PlantUML Smetana + Mermaid dist easily satisfies the < 15 MB size budget (R-NFR-6).
   - Rendering runs off the EDT with an LRU cache keyed by SHA-256 of diagram source code and a 3-second render timeout.

## Decision

- **Mermaid**: Bundled official distribution served locally via `ResourceProvider`, initialized in JCEF with light/dark theme detection.
- **PlantUML**: Bundled in-process PlantUML generator using Smetana engine as default fallback so diagrams render out-of-the-box offline without requiring Graphviz.
- **Error Handling**: Syntactically invalid diagrams will render a clearly styled error box displaying the error message and the original code fence source.
