---
summary: "Toolchain, JDK compatibility, target IDE versions and bytecode targets"
triggers: [toolchain setup, JDK version, bytecode target, IDE compatibility]
---

# ADR 0001: Toolchain, Runtime Environment, and Bytecode Level

## Context

ViewMD must build reliably on the developer workstation and target both IntelliJ IDEA (2026.2+) and Android Studio (2026.1+ / 2026.2+). The build host runs OpenJDK 27 (`~/.jdks/openjdk-27.0.1.0.3`) as the project SDK, but IntelliJ Platform IDEs execute plugins inside their bundled JetBrains Runtime (JBR). Compiling against an incompatible JVM target or using untested Gradle/Kotlin combinations risks runtime `UnsupportedClassVersionError` or Gradle plugin failures.

## Verified Environmental Facts

| Fact | Value / Path | Verification Command / Source |
|---|---|---|
| Project SDK (Build JDK) | OpenJDK 27.0.1.0.3 (`~/.jdks/openjdk-27.0.1.0.3`) | `./gradlew buildPlugin` with `JAVA_HOME` set |
| Default Host Java | OpenJDK 25.0.5.0.3 (`/usr/bin/java`) | `java -version` |
| Gradle Wrapper | 9.8.2 | `./gradlew --version` |
| Kotlin JVM Plugin | 2.4.0 | `settings.gradle.kts` |
| IntelliJ Platform Gradle Plugin | 2.20.0 | `settings.gradle.kts` |
| Android Studio Install | `~/.local/share/JetBrains/Toolbox/apps/android-studio` | Discovered from Toolbox |
| Android Studio Version & Build | `AI-262.10968.63.2622.16840690` (selector: `AndroidStudio2026.2.2`) | `product-info.json` |
| Android Studio JBR | OpenJDK 25.0.4 (`build 25.0.4+-16339643-b508.27`) | `jbr/bin/java -version` |
| IntelliJ IDEA Install | `~/.local/share/JetBrains/Toolbox/apps/intellij-idea` | Discovered from Toolbox |
| IntelliJ IDEA Version & Build | `2026.3` (Build `263.7307.200`) | `product-info.json` |
| IntelliJ IDEA JBR | OpenJDK 25.0.4.1 (`build 25.0.4.1+1-b635.79`) | `jbr/bin/java -version` |

## Decision

1. **Gradle Build JDK**: Use JDK 27 (`~/.jdks/openjdk-27.0.1.0.3`) via Gradle JVM options / toolchain convention. Gradle 9.8.2 and Kotlin 2.4.0 support JDK 27.
2. **Bytecode Level**: Compile with `jvmTarget = JvmTarget.JVM_25` and Java compile option `-release 25`. Both Android Studio 2026.2 and IntelliJ IDEA 2026.3 bundle JBR 25. Compiling with target 27 would crash on these JBRs.
3. **Primary Verification Target**: Android Studio 2026.2.2 / compile against IntelliJ IDEA `2026.2.4` baseline.

## Consequences

- Plugin bytecode is fully compatible with JBR 25 in both target IDEs.
- Builds must always specify or inherit `jvmTarget = 25` and Java release 25.
