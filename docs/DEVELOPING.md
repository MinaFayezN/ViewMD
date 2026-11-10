---
summary: "Developer Guide for building, testing, and debugging ViewMD"
triggers: [developer guide, building ViewMD, running tests, gradle commands]
---

# ViewMD Developer Guide

This guide is for contributors working on ViewMD.

## Prerequisites

- JDK 27 (e.g. `~/.jdks/openjdk-27.0.1.0.3` or your local OpenJDK 27 distribution)
- Node.js LTS (for web layer Playwright verification)

## Building the Plugin

```bash
export JAVA_HOME="~/.jdks/openjdk-27.0.1.0.3"
./gradlew buildPlugin
```

## Running Tests

### Kotlin Unit Tests
```bash
./gradlew test
```

### Playwright Web Layout Tests
```bash
cd web-test
node test-runner.js
```

### Performance Benchmark Suite
```bash
cd web-test
node bench_perf.js
```

## Launching the IDE Sandbox

```bash
./gradlew runIde
```
This starts an isolated sandbox of IntelliJ IDEA with ViewMD loaded.
