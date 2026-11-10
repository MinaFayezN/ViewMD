package dev.mina.viewmd.css

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CssResolverTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testNoneConfigured() {
        assertEquals(CssStatus.NoneConfigured, CssResolver.resolve("", null))
        assertEquals(CssStatus.NoneConfigured, CssResolver.resolve("   ", null))
        assertEquals(CssStatus.NoneConfigured, CssResolver.resolve(null, null))
    }

    @Test
    fun testValidAbsoluteCssFile() {
        val cssFile = tempFolder.newFile("style.css")
        cssFile.writeText("body { color: red; }")

        val status = CssResolver.resolve(cssFile.absolutePath, null)
        assertTrue(status is CssStatus.Valid)
        val valid = status as CssStatus.Valid
        assertEquals(cssFile.toPath().toAbsolutePath(), valid.path)
        assertEquals(cssFile.length(), valid.sizeBytes)
    }

    @Test
    fun testEmptyCssFile() {
        val emptyFile = tempFolder.newFile("empty.css")

        val status = CssResolver.resolve(emptyFile.absolutePath, null)
        assertTrue(status is CssStatus.Empty)
        val empty = status as CssStatus.Empty
        assertEquals(emptyFile.toPath().toAbsolutePath(), empty.path)
    }

    @Test
    fun testNotFoundCssFile() {
        val nonExistent = File(tempFolder.root, "does-not-exist.css")

        val status = CssResolver.resolve(nonExistent.absolutePath, null)
        assertTrue(status is CssStatus.NotFound)
    }

    @Test
    fun testDirectoryGivenAsCssFile() {
        val dir = tempFolder.newFolder("styles")

        val status = CssResolver.resolve(dir.absolutePath, null)
        assertTrue(status is CssStatus.NotReadable)
    }
}
