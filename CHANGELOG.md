# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-11-10

### Added
- Horizontal scrolling container (`.viewmd-table-wrap`) for wide tables preventing column text squeezing.
- Sticky table headers and zebra rows with configurable minimum column widths.
- Interactive table hover toolbar with "Copy Markdown" and "Copy TSV" buttons.
- Code block hover copy button with clipboard integration.
- YAML front matter parser supporting `summary`, `triggers` chips, `index: false` tag, and error diagnostics.
- Bundled offline Mermaid diagram renderer supporting flowcharts, sequence diagrams, and class diagrams.
- Bundled in-process PlantUML renderer powered by Smetana with SHA-256 caching.
- Custom CSS diagnostics panel resolving absolute, home tilde (`~`), and project-relative paths with live reload.
- Real-time IDE theme synchronization (`LafManagerListener`) between light and dark modes.
- "Open in Browser" and "Export as HTML..." actions generating standalone HTML pages with inlined assets.
