# ViewMD

> A prettier, smarter Markdown preview for IntelliJ IDEA and Android Studio: tables that scroll horizontally without squeezing text, YAML front matter presented as cards, and 100% offline Mermaid & PlantUML diagrams.

[![Build](https://github.com/mina/ViewMD/workflows/Build/badge.svg)](https://github.com/mina/ViewMD/actions)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](CHANGELOG.md)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg)](LICENSE)

---

## Why ViewMD?

The stock Markdown preview in IntelliJ IDEA and Android Studio suffers from major pain points:
1. **Squeezed Tables**: Tables wrap text one letter per line across narrow columns when wide tables are viewed.
2. **Missing Custom CSS**: Custom CSS in Android Studio frequently fails with "file not found" due to unresolved tilde (`~`) paths or project root boundaries.
3. **Raw Front Matter**: YAML headers (`summary:`, `triggers:`) display as raw code blocks at the top of documents.
4. **Diagram Setup Friction**: Mermaid and PlantUML require manual extension downloads or external Graphviz installations.

ViewMD solves all of these with zero remote requests, zero CDN dependencies, and instant responsive layouts.

## Features

- **Responsive Tables**: Wide tables scroll horizontally inside an overflow container with sticky headers, zebra rows, and minimum column widths.
- **Table Toolbar**: Hover over any table to quickly copy it as raw Markdown or TSV for spreadsheets.
- **YAML Front Matter Cards**: Front matter is automatically parsed and styled into a collapsed metadata card.
- **100% Offline Diagrams**: Bundled Mermaid and in-process PlantUML (with built-in Smetana engine).
- **Bulletproof Custom CSS**: Full diagnostics in the settings dialog resolving `~`, relative paths, and absolute paths with live reload.
- **Live Theme Switching**: Matches light, dark, and high-contrast IDE color schemes dynamically.
- **Export**: One-click "Open in Browser" and "Export as HTML" with inlined styles and scripts.

## Installation

### From JetBrains Marketplace
Search for **ViewMD** under `Settings > Plugins > Marketplace`.

### From Source
```bash
git clone https://github.com/mina/ViewMD.git
cd ViewMD
./gradlew buildPlugin
```
Install the generated zip archive located at `build/distributions/ViewMD-1.0.0.zip` via `Settings > Plugins > ⚙ > Install Plugin from Disk...`.

## Settings

Navigate to `Settings > Tools > ViewMD`:
- **Max Content Width**: Configure default preview width (default: 980 px) or enable Full Width mode.
- **Table Column Min-Width**: Customize character width constraints per cell.
- **Custom CSS Resolver**: Inspect resolved path status (`Valid`, `Empty`, `Not Found`, `Not Readable`).

## Documentation

- [User Guide](docs/USER_GUIDE.md)
- [Architecture & ADRs](docs/ARCHITECTURE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Developer Guide](docs/DEVELOPING.md)
- [Security Policy](docs/SECURITY.md)
- [Credits & Third-Party Licenses](docs/CREDITS.md)

## License

ViewMD is distributed under the [Apache License 2.0](LICENSE).
