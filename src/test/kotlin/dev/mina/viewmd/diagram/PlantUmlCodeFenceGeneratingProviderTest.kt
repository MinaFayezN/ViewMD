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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantUmlCodeFenceGeneratingProviderTest {

    private val provider = PlantUmlCodeFenceGeneratingProvider()
    private val stubNode = mockk<ASTNode>(relaxed = true)

    @Test
    fun testIsApplicable() {
        assertTrue(provider.isApplicable("plantuml"))
        assertTrue(provider.isApplicable("puml"))
        assertTrue(provider.isApplicable("PlantUML"))
        assertTrue(provider.isApplicable("  PUML  "))
        assertFalse(provider.isApplicable("mermaid"))
        assertFalse(provider.isApplicable(""))
        assertFalse(provider.isApplicable("java"))
    }

    @Test
    fun testGenerateHtmlSuccessContainsSvg() {
        val raw = "Alice -> Bob: hello"
        val html = provider.generateHtml("plantuml", raw, stubNode)

        assertTrue(html.contains("viewmd-plantuml-container"))
        assertTrue(html.contains("<svg"))
        assertTrue(html.contains("Alice"))
        assertTrue(html.contains("Bob"))
    }

    @Test
    fun testGenerateHtmlFailureShowsErrorBlock() {
        // An empty or unrenderable string that triggers failure
        val raw = ""
        val html = provider.generateHtml("plantuml", raw, stubNode)

        assertTrue(html.contains("viewmd-diagram-error"))
        assertTrue(html.contains("PlantUML rendering failed"))
        assertTrue(html.contains("viewmd-diagram-error-source"))
    }
}
