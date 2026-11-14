# ViewMD

> A prettier, smarter Markdown preview for Android Studio and IntelliJ IDEA: responsive tables that scroll horizontally without squeezing text, YAML front matter presented as cards, and 100% offline Mermaid & PlantUML diagrams.

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-green.svg)](CHANGELOG.md)

---

## Why ViewMD?

The stock Markdown preview in Android Studio and IntelliJ IDEA suffers from major pain points:
1. **Squeezed Tables**: Wide tables wrap text one letter per line across narrow columns, making them impossible to read.
2. **Missing Custom CSS**: Custom CSS in Android Studio frequently fails with "file not found" due to unresolved tilde (`~`) paths or project root boundaries.
3. **Raw Front Matter**: YAML headers (`summary:`, `triggers:`, `index:`) display as raw code blocks at the top of documents.
4. **Diagram Setup Friction**: Mermaid and PlantUML require manual extension downloads or external Graphviz installations.

ViewMD solves all of these with **zero remote requests**, **zero CDN dependencies**, and **instant responsive layouts**.

---

## Features

- **Responsive Tables**: Wide tables scroll horizontally inside an overflow container with sticky headers, zebra rows, and readable column widths.
- **Table Hover Toolbar**: Hover over any table to quickly copy it as clean Markdown or TSV for Google Sheets and Excel.
- **YAML Front Matter Cards**: Front matter is automatically parsed and styled into a collapsed, clean metadata card with tag chips.
- **100% Offline Diagrams**: Bundled client-side Mermaid renderer and in-process PlantUML (powered by the Smetana engine).
- **Bulletproof Custom CSS**: Full diagnostics in the settings dialog resolving `~`, relative paths, and absolute paths with live reload.
- **Dynamic Theme Sync**: Matches light, dark, and high-contrast IDE color schemes dynamically.
- **Standalone Export**: One-click "Open Preview in Browser" and "Export as HTML" with all styles and scripts self-contained.

---

## Compatibility

| IDE | Minimum Version | Tested Baseline |
|---|---|---|
| **Android Studio** | 2026.2+ (Ladybug / current) | AI-262.10968.63.2622.16840690 |
| **IntelliJ IDEA** | 2026.2+ | 2026.2.4 / 2026.3 |

---

## Installation

### From JetBrains Marketplace
Search for **ViewMD** under `Settings > Plugins > Marketplace` and click **Install**.

### Manual Installation (From Distribution ZIP)
1. Download `ViewMD-1.0.0.zip` from [Releases](https://github.com/mina/ViewMD/releases).
2. Open Android Studio / IntelliJ IDEA.
3. Go to `Settings > Plugins > ⚙ (gear icon) > Install Plugin from Disk...`.
4. Select `ViewMD-1.0.0.zip` and restart the IDE if prompted.

### From Source
```bash
git clone https://github.com/mina/ViewMD.git
cd ViewMD
./gradlew buildPlugin
```
The compiled archive is generated at `build/distributions/ViewMD-1.0.0.zip`.

---

## Settings

Navigate to `Settings > Tools > ViewMD`:
- **Max Content Width**: Configure default preview width (default: 980 px) or enable Full Width mode.
- **Table Column Min-Width**: Customize character width constraints per cell (default: 12ch).
- **First Column Min-Width**: Customize the first column width (default: 14ch).
- **Front Matter Controls**: Choose to hide cards completely or expand them by default.
- **Custom CSS Diagnostics**: Inspect stylesheet path status in real-time (`Valid`, `Empty`, `Not Found`, `Not Readable`).

---

## Documentation

- [User Guide](docs/USER_GUIDE.md)
- [Architecture & ADRs](docs/ARCHITECTURE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Developer Guide](docs/DEVELOPING.md)
- [Security Policy](docs/SECURITY.md)
- [Credits & Third-Party Licenses](docs/CREDITS.md)
- [Contributing Guidelines](CONTRIBUTING.md)

---

## License

ViewMD is distributed under the [GNU General Public License v3.0 (GPLv3)](LICENSE).
Anyone using or distributing derivative works must also make their code available under the GPLv3.
