package dev.mina.viewmd.frontmatter

import org.junit.Assert.*
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
