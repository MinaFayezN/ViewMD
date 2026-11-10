package dev.mina.viewmd.diagram

import org.junit.Assert.*
import org.junit.Test

class PlantUmlRendererTest {

    @Test
    fun testValidSequenceDiagramRendersSvgOffline() {
        val puml = """
            Alice -> Bob: Authentication Request
            Bob --> Alice: Authentication Response
        """.trimIndent()

        val result = PlantUmlRenderer.renderToSvg(puml)
        assertTrue(result.isSuccess)
        val svg = result.getOrThrow()
        assertTrue(svg.contains("<svg"))
        assertTrue(svg.contains("Alice"))
        assertTrue(svg.contains("Bob"))
    }

    @Test
    fun testCacheHit() {
        val puml = "class Foo\nclass Bar\nFoo --> Bar"
        val first = PlantUmlRenderer.renderToSvg(puml).getOrThrow()
        val second = PlantUmlRenderer.renderToSvg(puml).getOrThrow()
        assertSame(first, second) // Identity check on cached instance
    }
}
