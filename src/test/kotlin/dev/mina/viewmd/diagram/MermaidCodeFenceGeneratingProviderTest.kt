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

package dev.mina.viewmd.diagram

import io.mockk.mockk
import org.intellij.markdown.ast.ASTNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MermaidCodeFenceGeneratingProviderTest {

    private val provider = MermaidCodeFenceGeneratingProvider()
    private val stubNode = mockk<ASTNode>(relaxed = true)

    @Test
    fun testIsApplicable() {
        assertTrue(provider.isApplicable("mermaid"))
        assertTrue(provider.isApplicable("Mermaid"))
        assertTrue(provider.isApplicable("  MERMAID  "))
        assertFalse(provider.isApplicable("plantuml"))
        assertFalse(provider.isApplicable("puml"))
        assertFalse(provider.isApplicable(""))
        assertFalse(provider.isApplicable("javascript"))
    }

    @Test
    fun testGenerateHtmlOutputStructure() {
        val raw = "graph TD\n  A --> B"
        val html = provider.generateHtml("mermaid", raw, stubNode)

        assertTrue(html.startsWith("<div class=\"viewmd-mermaid-block\"><pre class=\"mermaid\">"))
        assertTrue(html.contains("graph TD"))
        assertTrue(html.contains("A --&gt; B"))
        assertTrue(html.endsWith("</pre></div>"))
    }

    @Test
    fun testGenerateHtmlEscapesMaliciousInput() {
        val raw = "<script>alert('xss')</script>"
        val html = provider.generateHtml("mermaid", raw, stubNode)

        assertFalse(html.contains("<script>"))
        assertTrue(html.contains("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"))
    }
}
