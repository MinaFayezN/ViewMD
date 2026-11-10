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
