---
summary: "Release process and verification checklist for ViewMD maintainers"
triggers: [release checklist, publishing plugin, marketplace upload]
---

# ViewMD Release Guide

This guide covers the verification and publishing steps for creating official releases.

## Pre-Release Checklist

1. **Verify Environment**:
   ```bash
   ./gradlew test
   ./gradlew buildPlugin
   cd web-test && node test-runner.js
   ```
2. **Check Artifact Size**:
   Verify that `build/distributions/ViewMD-*.zip` is under 15 MB.
3. **Verify Documentation**:
   Ensure `CHANGELOG.md` reflects all added features and fixes.
4. **Tagging**:
   ```bash
   git tag 1.0.0-rc.1
   ```
