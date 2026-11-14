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

package dev.mina.viewmd.frontmatter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FrontMatterParserTest {

    @Test
    fun testValidFrontMatter() {
        val markdown = """
            ---
            summary: "My fixture doc"
            triggers: [test1, "test 2"]
            index: false
            custom: "val"
            ---
            # Body Title
            Some text.
        """.trimIndent()

        val (result, body) = FrontMatterParser.parse(markdown)
        assertNotNull(result)
        assertTrue(result!!.isValid)
        assertEquals("My fixture doc", result.summary)
        assertEquals(listOf("test1", "test 2"), result.triggers)
        assertFalse(result.indexed)
        assertEquals("val", result.otherFields["custom"])
        assertTrue(body.startsWith("# Body Title"))
    }

    @Test
    fun testTriggersAsCommaSeparatedString() {
        val markdown = """
            ---
            summary: Doc
            triggers: apple, banana, cherry
            ---
            Body
        """.trimIndent()

        val (result, _) = FrontMatterParser.parse(markdown)
        assertNotNull(result)
        assertTrue(result!!.isValid)
        assertEquals(listOf("apple", "banana", "cherry"), result.triggers)
    }

    @Test
    fun testIndexExplicitAndDefault() {
        // Default true
        val mdDefault = "---\nsummary: S\n---\nBody"
        val (resDef, _) = FrontMatterParser.parse(mdDefault)
        assertTrue(resDef!!.indexed)

        // String "false"
        val mdFalseStr = "---\nsummary: S\nindex: false\n---\nBody"
        val (resFalse, _) = FrontMatterParser.parse(mdFalseStr)
        assertFalse(resFalse!!.indexed)

        // Boolean true
        val mdTrue = "---\nsummary: S\nindex: true\n---\nBody"
        val (resTrue, _) = FrontMatterParser.parse(mdTrue)
        assertTrue(resTrue!!.indexed)
    }

    @Test
    fun testNestedYamlInOtherFields() {
        val markdown = """
            ---
            summary: Nested Doc
            metadata:
              author: Mina
              version: 1
            ---
            Body
        """.trimIndent()

        val (result, _) = FrontMatterParser.parse(markdown)
        assertNotNull(result)
        assertTrue(result!!.isValid)
        val meta = result.otherFields["metadata"]
        assertTrue(meta is Map<*, *>)
        assertEquals("Mina", (meta as Map<*, *>)["author"])
    }

    @Test
    fun testWindowsLineEndings() {
        val markdown = "---\r\nsummary: CRLF Doc\r\ntriggers: [a, b]\r\n---\r\n# CRLF Body"
        val (result, body) = FrontMatterParser.parse(markdown)
        assertNotNull(result)
        assertTrue(result!!.isValid)
        assertEquals("CRLF Doc", result.summary)
        assertEquals(listOf("a", "b"), result.triggers)
        assertTrue(body.startsWith("# CRLF Body"))
    }

    @Test
    fun testFrontMatterWithoutBody() {
        val markdown = "---\nsummary: Only FM\n---"
        val (result, body) = FrontMatterParser.parse(markdown)
        assertNotNull(result)
        assertTrue(result!!.isValid)
        assertEquals("Only FM", result.summary)
        assertEquals("", body)
    }

    @Test
    fun testNonMapYamlProducesError() {
        val markdown = "---\n- item 1\n- item 2\n---\nBody"
        val (result, _) = FrontMatterParser.parse(markdown)
        assertNotNull(result)
        assertFalse(result!!.isValid)
        assertTrue(result.errorMessage!!.contains("mapping"))
    }

    @Test
    fun testUnclosedFrontMatter() {
        val markdown = """
            ---
            summary: "Missing end
            # Body Title
        """.trimIndent()

        val (result, body) = FrontMatterParser.parse(markdown)
        assertNotNull(result)
        assertFalse(result!!.isValid)
        assertTrue(result.errorMessage!!.contains("Unclosed"))
    }

    @Test
    fun testInvalidYamlSyntax() {
        val markdown = """
            ---
            summary: unquoted: colon: error: [
            ---
            # Body Title
        """.trimIndent()

        val (result, body) = FrontMatterParser.parse(markdown)
        assertNotNull(result)
        assertFalse(result!!.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(body.startsWith("# Body Title"))
    }

    @Test
    fun testNoFrontMatter() {
        val markdown = "# Normal Markdown\nContent."
        val (result, body) = FrontMatterParser.parse(markdown)
        assertNull(result)
        assertEquals(markdown, body)
    }
}
