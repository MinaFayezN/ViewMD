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
}
