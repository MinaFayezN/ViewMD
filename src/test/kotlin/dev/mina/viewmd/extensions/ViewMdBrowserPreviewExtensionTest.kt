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

package dev.mina.viewmd.extensions

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.mockk
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel

class ViewMdBrowserPreviewExtensionTest : BasePlatformTestCase() {

    fun testScriptsAndStylesRegistration() {
        val panel = mockk<MarkdownHtmlPanel>(relaxed = true)
        val extension = ViewMdBrowserPreviewExtension(panel)
        Disposer.register(testRootDisposable, extension)

        assertTrue(extension.scripts.contains("web/vendor/mermaid.min.js"))
        assertTrue(extension.scripts.contains("web/postprocess.js"))
        assertEquals(listOf("web/viewmd.css"), extension.styles)
    }

    fun testResourceProviderCanProvideKnownResources() {
        val panel = mockk<MarkdownHtmlPanel>(relaxed = true)
        val extension = ViewMdBrowserPreviewExtension(panel)
        Disposer.register(testRootDisposable, extension)

        val provider = extension.resourceProvider
        assertTrue(provider.canProvide("web/viewmd.css"))
        assertTrue(provider.canProvide("web/postprocess.js"))
        assertTrue(provider.canProvide("web/vendor/mermaid.min.js"))
        assertTrue(provider.canProvide("/web/viewmd.css"))
        assertFalse(provider.canProvide("web/unknown-script.js"))
        assertFalse(provider.canProvide("images/logo.png"))
    }

    fun testResourceProviderLoadsInternalBundledResources() {
        val panel = mockk<MarkdownHtmlPanel>(relaxed = true)
        val extension = ViewMdBrowserPreviewExtension(panel)
        Disposer.register(testRootDisposable, extension)

        val provider = extension.resourceProvider
        val cssResource = provider.loadResource("web/viewmd.css")
        assertNotNull(cssResource)

        val jsResource = provider.loadResource("web/postprocess.js")
        assertNotNull(jsResource)
    }
}
