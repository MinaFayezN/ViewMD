# Contributing to ViewMD

Thank you for your interest in contributing to **ViewMD**!

ViewMD is an open-source project distributed under the **GNU General Public License v3.0 (GPLv3)**. All contributions must be licensed under the GPLv3.

---

## Core Engineering Rules

### 1. Automated Regression Testing Mandate
> [!IMPORTANT]
> **Every bug fix, user feedback item, security hardening, or UI enhancement must include a dedicated automated regression test covering that exact scenario.**
>
> - **Kotlin layer changes**: Add JUnit 4 unit tests in `src/test/kotlin/dev/mina/viewmd/` or platform integration tests (`BasePlatformTestCase`).
> - **Web/DOM/CSS changes**: Add Playwright end-to-end tests in `web-test/tests/`.
> - **Layout/Responsiveness changes**: Run the visual layout torture test (`npm run test:torture` in `web-test/`).
>
> Pull requests without corresponding automated tests will not be accepted.

### 2. GPLv3 Header Requirement
Every Kotlin source file, test file, CSS stylesheet, and JavaScript file must maintain the official GPLv3 copyright header:

```kotlin
/*
 * ViewMD — Enhanced Markdown preview for Android Studio and IntelliJ-based IDEs
 * Copyright (C) 2026 Mina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
```

### 3. Zero CDN & Zero Personal Machine Paths
- ViewMD is **100% offline**: never introduce remote CDN links, external web fonts, or unbundled network calls.
- Never commit absolute personal machine paths (e.g. `/home/<user>/...`). Use relative paths or `~` expansion.

---

## Development Workflow

1. Fork the repository and create a feature branch (`git checkout -b feature/my-feature`).
2. Make your changes and add corresponding automated tests.
3. Verify all test suites pass locally:
   ```bash
   # 1. Run Kotlin tests (68 unit & platform tests)
   ./gradlew test

   # 2. Run Playwright E2E browser tests (29 tests)
   cd web-test && npx playwright test

   # 3. Run visual multi-viewport layout tests (6 viewports)
   npm run test:torture

   # 4. Verify clean build of distribution ZIP
   cd .. && ./gradlew clean buildPlugin
   ```
4. Commit your changes with a clear commit message and submit a pull request.
