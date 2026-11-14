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

package dev.mina.viewmd.export

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class HtmlExporterTest {

    @Test
    fun testGenerateStandaloneHtmlContainsBaselineAssets() {
        val html = HtmlExporter.generateStandaloneHtml(
            title = "Test Export",
            bodyHtml = "<h1>Export Title</h1><p>Sample body</p>",
            project = null,
            isDark = false
        )

        assertTrue(html.contains("<!doctype html>"))
        assertTrue(html.contains("Test Export"))
        assertTrue(html.contains("viewmd-table-wrap"))
        assertTrue(html.contains("wrapTables"))
        assertTrue(html.contains("Export Title"))
        // Since no mermaid diagram in bodyHtml, 4.9MB mermaid.min.js should NOT be embedded
        assertFalse(html.contains("mermaid.min.js") || html.length > 500_000)
    }

    @Test
    fun testGenerateStandaloneHtmlIncludesMermaidWhenRequestedOrDetected() {
        val html = HtmlExporter.generateStandaloneHtml(
            title = "Test Mermaid Export",
            bodyHtml = "<pre class=\"mermaid\">graph TD; A-->B;</pre>",
            project = null,
            isDark = false
        )

        assertTrue(html.contains("graph TD; A-->B;"))
        assertTrue(html.length > 1_000_000) // Contains bundled mermaid
    }

    @Test
    fun testExportToTempFileCreatesReadableFile() {
        val tempPath = HtmlExporter.exportToTempFile(
            title = "Temp Doc",
            bodyHtml = "<p>Temp Content</p>",
            project = null
        )

        assertTrue(Files.exists(tempPath))
        assertTrue(Files.size(tempPath) > 0)
        val content = Files.readString(tempPath)
        assertTrue(content.contains("Temp Content"))

        Files.deleteIfExists(tempPath)
    }

    @Test
    fun testGenerateStandaloneHtmlContainsThemeToggleAndSanitizesBody() {
        val html = HtmlExporter.generateStandaloneHtml(
            title = "Theme Test",
            bodyHtml = "<body><h1>Nested Body Title</h1></body>",
            project = null,
            isDark = true
        )

        assertTrue(html.contains("data-theme=\"dark\""))
        assertTrue(html.contains("viewmd-theme-toggle"))
        assertTrue(html.contains("Nested Body Title"))
        // Check body unnesting: only one body open tag in the entire document
        val bodyCount = Regex("<body[^>]*>", RegexOption.IGNORE_CASE).findAll(html).count()
        assertEquals(1, bodyCount)
    }
}
