## Description

Please describe the changes in this pull request and link any relevant issues:
Fixes #

## Type of Change

- [ ] Bug fix (non-breaking change fixing an issue)
- [ ] New feature (non-breaking change adding functionality)
- [ ] Documentation update
- [ ] Performance / Security improvement
- [ ] CI / Tooling configuration

## Pre-Merge Verification Checklist

- [ ] I have read the [CONTRIBUTING.md](CONTRIBUTING.md) guide.
- [ ] `./gradlew test` passes with all Kotlin unit and platform tests green.
- [ ] `cd web-test && npm test` passes with all Playwright browser tests green.
- [ ] No external CDN, tracking, or network calls are introduced (strict local offline guarantee).
- [ ] Changes adhere to the strict Content Security Policy (`default-src 'none'`).
- [ ] Documentation and `CHANGELOG.md` have been updated if user-facing behavior changed.
- [ ] Sensitive paths, machine-specific paths, or proprietary enterprise tokens are absent.
