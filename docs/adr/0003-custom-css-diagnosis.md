---
summary: "Root cause of 'custom CSS not found' in Android Studio and ViewMD resolution strategy"
triggers: [custom CSS failure, Android Studio CSS bug, CssResolver]
---

# ADR 0003: Root Cause Analysis of Custom CSS Failure in Android Studio

## Context

Users reported that Android Studio's bundled Markdown preview failed with "custom CSS not found" even when the CSS file existed on disk.

## Spike Analysis (Spike A2)

Decompilation of `org.intellij.plugins.markdown.settings.MarkdownSettingsConfigurable` and `MarkdownSettingsUtil` uncovered the root cause:

1. **Path Resolution & Validation Flaw**:
   - The stock implementation invokes:
     ```kotlin
     Path.of(pathText)
     Files.notExists(path)
     MarkdownSettingsUtil.belongsToTheProject(project, path)
     ```
   - In `MarkdownSettingsUtil.belongsToTheProject`:
     ```kotlin
     val vf = VfsUtil.findFile(path, true) ?: return false
     return ProjectFileIndex.getInstance(project).isInProjectOrExcluded(vf)
     ```
2. **Failure Vectors**:
   - **Tilde paths (`~/...`)**: `Path.of("~/style.css")` is treated as a relative path to the IDE process working directory, NOT expanded to `$HOME`.
   - **External / Global paths**: `belongsToTheProject` checks `ProjectFileIndex.isInProjectOrExcluded(vf)`. If a user specifies a shared team stylesheet in `~/.config/...` or outside the immediate project root, the stock preview rejects the file with `markdown.settings.stylesheet.path.outside.project.error`.
   - **Unrefreshed VFS**: If a file was created or touched outside the IDE, `VfsUtil.findFile(path, true)` can return `null` if the virtual file system has not performed a synchronous refresh.

## Decision

ViewMD's custom CSS subsystem (`dev.mina.viewmd.css`):
1. Expands `~` to `System.getProperty("user.home")`.
2. Resolves relative paths first against `project.basePath`, falling back to absolute paths.
3. Automatically triggers a VFS refresh (`VfsUtil.markDirtyAndRefresh`) if `findFileByNioFile` returns null on an existing NIO `Path`.
4. Supports both project-local and global stylesheets with clear diagnostics in the UI:
   - Full absolute resolved path
   - Status badge: `Found & Readable (X bytes)`, `File Empty`, `Not Found`, or `Permission Denied`
5. Monitors file changes via `VirtualFileListener` / `BulkFileListener` for instant live reload.
