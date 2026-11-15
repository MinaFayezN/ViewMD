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

    @Test
    fun testGenerateStandaloneHtmlEscapesTitleHtml() {
        val html = HtmlExporter.generateStandaloneHtml(
            title = "<script>alert('xss')</script>",
            bodyHtml = "<p>Clean content</p>",
            project = null,
            isDark = false
        )

        assertTrue(html.contains("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"))
        assertFalse(html.contains("<title><script>"))
    }

    @Test
    fun testGenerateStandaloneHtmlStripsIdeRunnerArtifactsAndSanitizesAlertIcons() {
        val bodyWithIdeArtifacts = """
            <pre class="code-fence"><a class='run-icon code-block' href='#' role='button' data-command='Run:echo 123'><img src='commandRunner/run.png'></a><div class="code-fence-highlighter-copy-button"><img class="code-fence-highlighter-copy-button-icon"></div><code class="language-bash">echo 123</code></pre>
            <blockquote class="markdown-alert markdown-alert-note"><p class="markdown-alert-title"><img class="markdown-alert-icon" src="alertIcons/note.png" alt="">Note</p><p>Alert note text</p></blockquote>
        """.trimIndent()

        val html = HtmlExporter.generateStandaloneHtml(
            title = "IDE Artifacts Cleanup Test",
            bodyHtml = bodyWithIdeArtifacts,
            project = null,
            isDark = false
        )

        // 1. Broken commandRunner run anchor must be completely stripped
        assertFalse(html.contains("commandRunner/run.png"))
        assertFalse(html.contains("data-command="))
        assertFalse(html.contains("class='run-icon"))

        // 2. Broken duplicate copy button from IDE must be stripped from content
        val contentBody = html.substringAfter("<div class=\"viewmd-content\">").substringBefore("</div>")
        assertFalse(contentBody.contains("code-fence-highlighter-copy-button"))

        // 3. Relative alertIcons/note.png must be replaced with standalone icon badge
        assertFalse(contentBody.contains("alertIcons/note.png"))
        assertTrue(contentBody.contains("viewmd-alert-icon"))
        assertTrue(contentBody.contains("ℹ️"))
    }

    @Test
    fun testGenerateStandaloneHtmlHardensHeadlessImagesAndInvertedAlertAttributes() {
        val mixedContent = """
            <pre class="code-fence"><a href="#" role="button"><img class="code-fence-highlighter-copy-button-icon"></a><code>val x = 1</code></pre>
            <blockquote class="markdown-alert"><p><img src="alertIcons/tip.png" class="markdown-alert-icon" alt="Tip">Tip title</p></blockquote>
            <p><img src="" alt="empty src"><img class="headless-img" alt="no src"></p>
            <p><a href="#">   </a></p>
        """.trimIndent()

        val html = HtmlExporter.generateStandaloneHtml(
            title = "Hardening Test",
            bodyHtml = mixedContent,
            project = null,
            isDark = false
        )

        val contentBody = html.substringAfter("<div class=\"viewmd-content\">").substringBefore("</div>")

        // Inverted alert icon attributes are cleanly converted
        assertFalse("Must not contain relative alertIcons", contentBody.contains("alertIcons/tip.png"))
        assertTrue("Must contain sanitized badge", contentBody.contains("viewmd-alert-icon"))
        assertTrue("Must contain tip emoji", contentBody.contains("💡"))

        // Headless and empty images are removed
        assertFalse("Must not contain headless copy button icon", contentBody.contains("code-fence-highlighter-copy-button-icon"))
        assertFalse("Must not contain headless-img", contentBody.contains("headless-img"))

        // Phantom empty anchors are purged
        assertFalse("Must not contain phantom # anchor jumps", contentBody.contains("href=\"#\""))
        assertFalse("Must not contain phantom # anchor jumps (single quote)", contentBody.contains("href='#'"))
    }
}
