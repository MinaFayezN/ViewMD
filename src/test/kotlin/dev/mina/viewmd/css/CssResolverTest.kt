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

package dev.mina.viewmd.css

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

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

    @Test
    fun testTildeAloneResolvesToHomeDirectoryAndFailsAsDirectory() {
        val status = CssResolver.resolve("~", null)
        assertTrue(status is CssStatus.NotReadable)
        val notReadable = status as CssStatus.NotReadable
        assertTrue(notReadable.reason.contains("directory"))
    }

    @Test
    fun testPathNormalization() {
        val cssFile = tempFolder.newFile("normalized.css")
        cssFile.writeText("p { margin: 0; }")

        val unnormalized = tempFolder.root.absolutePath + "/./" + cssFile.name
        val status = CssResolver.resolve(unnormalized, null)
        assertTrue(status is CssStatus.Valid)
        val valid = status as CssStatus.Valid
        assertEquals(cssFile.toPath().toAbsolutePath(), valid.path)
    }

    @Test
    fun testSymlinkToCssFile() {
        val targetFile = tempFolder.newFile("target.css")
        targetFile.writeText("h1 { font-size: 20px; }")

        val symlinkPath = tempFolder.root.toPath().resolve("link.css")
        try {
            Files.createSymbolicLink(symlinkPath, targetFile.toPath())
            val status = CssResolver.resolve(symlinkPath.toString(), null)
            assertTrue(status is CssStatus.Valid)
        } catch (_: UnsupportedOperationException) {
            // Symlinks may not be supported on some file systems; safely pass
        }
    }

    @Test
    fun testRejectsNonCssExtension() {
        val txtFile = tempFolder.newFile("style.txt")
        txtFile.writeText("body { color: red; }")

        val status = CssResolver.resolve(txtFile.absolutePath, null)
        assertTrue(status is CssStatus.NotReadable)
        val notReadable = status as CssStatus.NotReadable
        assertTrue(notReadable.reason.contains(".css extension"))
    }

    @Test
    fun testRejectsSymlinkTargetingNonCss() {
        val targetTxt = tempFolder.newFile("secret.txt")
        targetTxt.writeText("secret content")

        val symlinkPath = tempFolder.root.toPath().resolve("sneaky.css")
        try {
            Files.createSymbolicLink(symlinkPath, targetTxt.toPath())
            val status = CssResolver.resolve(symlinkPath.toString(), null)
            assertTrue(status is CssStatus.NotReadable)
            val notReadable = status as CssStatus.NotReadable
            assertTrue(notReadable.reason.contains(".css extension"))
        } catch (_: UnsupportedOperationException) {
            // Symlinks may not be supported; pass cleanly
        }
    }

    @Test
    fun testRejectsOversizedCssFile() {
        val largeFile = tempFolder.newFile("huge.css")
        // Create sparse file or write dummy bytes over 5MB
        val channel = java.io.RandomAccessFile(largeFile, "rw").channel
        channel.position(CssResolver.MAX_CSS_SIZE_BYTES + 1024L)
        channel.write(java.nio.ByteBuffer.wrap(byteArrayOf(1)))
        channel.close()

        val status = CssResolver.resolve(largeFile.absolutePath, null)
        assertTrue(status is CssStatus.NotReadable)
        val notReadable = status as CssStatus.NotReadable
        assertTrue(notReadable.reason.contains("5MB limit"))
    }
}
