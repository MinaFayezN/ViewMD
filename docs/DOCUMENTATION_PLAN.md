# Documentation plan

The template `README.md` is placeholder text (GitHub setup steps, template structure). Replace it. Every command in
every doc must be run once before the doc is considered done. Use relative links and keep tables readable at 80 columns
in the source where possible (the plugin exists because wide tables are painful).

## Deliverables

| File | Audience | Content |
|---|---|---|
| `README.md` | Everyone | One-paragraph pitch; screenshot (before/after table); feature list; install (Marketplace, from disk, from source); quick start; supported IDEs and versions; settings overview; custom CSS in 3 lines; link map to the docs below; license; status badge |
| `docs/USER_GUIDE.md` | Users | Opening a doc, split modes, tables (scroll, toolbar), front matter card, diagrams (Mermaid, PlantUML with examples), custom CSS with a worked example, export, keyboard shortcuts, settings reference table, troubleshooting |
| `docs/TROUBLESHOOTING.md` | Users | Symptom-first table: preview blank, JCEF unavailable, CSS not applied (use the diagnostics panel), diagram error, slow on big files, Android Studio specifics; how to collect logs |
| `docs/DEVELOPING.md` | Contributors | Prerequisites (JDK 27, Gradle wrapper), clone/build/run/test commands, project layout, running against IntelliJ IDEA and Android Studio, debugging JCEF (remote devtools), updating bundled Mermaid/KaTeX/PlantUML, regenerating screenshots |
| `docs/ARCHITECTURE.md` | Contributors | Final architecture after Phase 0 with diagrams (use Mermaid; they must render in ViewMD itself) |
| `docs/adr/NNNN-*.md` | Contributors | One short ADR per decision: context, options, decision, consequences, verification evidence |
| `docs/SECURITY.md` | Users, reviewers | Threat model, sanitiser and CSP, network policy, how to report a vulnerability |
| `CONTRIBUTING.md` | Contributors | Branching, commit style, tests required, doc updates required, review checklist |
| `CHANGELOG.md` | Everyone | Keep a Changelog format, wired to the changelog Gradle plugin already in the build; fill 1.0.0 |
| `docs/RELEASING.md` | Maintainer | Versioning, signing secrets, Marketplace upload, verification matrix, tagging |
| `docs/CREDITS.md` | Everyone | Third-party licenses: Mermaid, KaTeX, PlantUML, highlighter, Markdown parser; license text where required |
| `LICENSE` | Everyone | Ask the owner which license (`DECISION`); default proposal Apache-2.0 |
| `plugin.xml` | Marketplace | Real name, vendor (dev.mina), 3-paragraph description with feature bullets, change notes from the changelog |

## README skeleton (write real content, not this outline)

```markdown
# ViewMD

> A prettier, smarter Markdown preview for IntelliJ IDEA and Android Studio: tables that scroll, front matter cards,
> offline Mermaid and PlantUML.

[badges: build, version, downloads]

![Before and after](docs/images/before-after.png)

## Why
## Features
## Install
## Quick start
## Settings
## Custom CSS
## Screenshots
## Compatibility
## Documentation
## Contributing
## License
```

## Style rules for all docs

- Tables for facts and options, bullets for steps, prose only for reasoning.
- Every doc starts with a one-line statement of who it is for and when to read it.
- Add YAML front matter (`summary:` quoted when it contains `: `, `triggers:` 1-4 phrases on one line) to every doc under
  `docs/`, so ViewMD's own front matter card is exercised by its own documentation.
- Screenshots live in `docs/images/`, named `<feature>-<theme>-<width>.png`, with alt text.
- No placeholder text, no "TODO" in a released doc; unknowns go in "Known limitations".
- Spell-check and link-check docs before each gate (a small script in `scripts/`, run in CI).
