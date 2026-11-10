# Contributing to ViewMD

Thank you for contributing to ViewMD!

## Development Workflow

1. Create a feature branch off `main`:
   ```bash
   git checkout -b feat/my-improvement
   ```
2. Follow our commit conventions:
   - `feat(...)`: New features
   - `fix(...)`: Bug fixes
   - `docs(...)`: Documentation updates
   - `test(...)`: Adding or updating tests
3. Ensure all tests and verification suites pass:
   ```bash
   ./gradlew test
   ./gradlew buildPlugin
   cd web-test && node test-runner.js
   ```
4. Verify that table formatting and layout constraints remain intact across 400px, 900px, and 1600px widths.
