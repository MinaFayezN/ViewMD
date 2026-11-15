# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-11-15

### Added
- **Responsive Table Scrolling**: Horizontal overflow scroll container (`.viewmd-table-wrap`) preventing column text squeezing across multi-column tables.
- **Sticky Table Headers & Zebra Striping**: Column headers stay pinned at top of viewport during vertical scrolling; alternating row striping with configurable cell minimum width constraints (`colMinChars`, `colFirstMinChars`).
- **Table Action Toolbar**: Hover toolbar providing instant 1-click **Copy Markdown** (GFM) and **Copy TSV** (for spreadsheets) with transient clipboard feedback.
- **YAML Front Matter Cards**: Collapsible metadata cards with trigger badge chips, unindexed document warning tags, and error resilience.
- **100% Offline Mermaid Diagrams**: Bundled offline Mermaid.js v10.9 renderer supporting flowcharts, sequence diagrams, and class diagrams with strict CSP isolation.
- **In-Process PlantUML Renderer**: Pure Java Smetana engine with thread-safe synchronized LRU cache (bounded at 200 SVGs), requiring zero external Graphviz installation.
- **7-Stage Sanitization Pipeline**: Order-independent DOM sanitization stripping IDE command runner links, headless images, empty `src` attributes, and phantom `#` anchors during export and external preview.
- **Standalone Alert Badges**: Automatic replacement of internal relative `alertIcons/*.png` with standalone accessible emoji badges (`ℹ️`, `💡`, `❗`, `⚠️`, `🛑`).
- **Official Stitch Vector Icons**: Bundled 40×40 SVG icons (`pluginIcon.svg` and `pluginIcon_dark.svg`) matching JetBrains Marketplace design guidelines.
- **Custom CSS Engine & Diagnostics**: Settings panel resolving absolute, home tilde (`~`), and project-relative paths with real-time status reporting and live hot-reload via Virtual File System events.
- **Live IDE Theme Synchronization**: Seamless real-time theme adaptation matching Light, Dark, Darcula, and High Contrast schemes via `LafManagerListener`.
- **Standalone Document Export**: One-click "Open Preview in Browser" and "Export as HTML..." generating self-contained single-file HTML pages with inlined styles and scripts.
- **Automated Regression Suite**: 70 Kotlin unit & platform tests, 29 Playwright E2E browser tests, and 6-viewport visual torture tests guaranteeing zero layout blow-out.
- **License**: Released under the GNU General Public License v3.0 (GPLv3).
