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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors

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

    @Test
    fun testAutoWrapsStartUml() {
        val puml = "component Comp1"
        val result = PlantUmlRenderer.renderToSvg(puml)
        assertTrue(result.isSuccess)
        val svg = result.getOrThrow()
        assertTrue(svg.contains("<svg"))
        assertTrue(svg.contains("Comp1"))
    }

    @Test
    fun testPreservesExplicitStartUml() {
        val puml = "@startuml\ncomponent CompExplicit\n@enduml"
        val result = PlantUmlRenderer.renderToSvg(puml)
        assertTrue(result.isSuccess)
        val svg = result.getOrThrow()
        assertTrue(svg.contains("<svg"))
        assertTrue(svg.contains("CompExplicit"))
    }

    @Test
    fun testEmptyInputReturnsFailure() {
        val result = PlantUmlRenderer.renderToSvg("")
        assertTrue(result.isFailure)
    }

    @Test
    fun testWhitespaceOnlyInputReturnsFailure() {
        val result = PlantUmlRenderer.renderToSvg("   \n\t  ")
        assertTrue(result.isFailure)
    }

    @Test
    fun testCacheEvictionAt201() {
        val initialDiagram = "class InitialClassForEvictionTest"
        val initialSvg = PlantUmlRenderer.renderToSvg(initialDiagram).getOrThrow()

        // Insert 205 distinct diagrams into the cache (MAX_CACHE_SIZE = 200)
        for (i in 1..205) {
            val puml = "class EvictionNode$i"
            val res = PlantUmlRenderer.renderToSvg(puml)
            assertTrue(res.isSuccess)
        }

        // When re-rendering initialDiagram, it should have been evicted and recreated
        val reRenderedSvg = PlantUmlRenderer.renderToSvg(initialDiagram).getOrThrow()
        assertNotSame(initialSvg, reRenderedSvg)
    }

    @Test
    fun testConcurrentRenders() {
        val threadPool = Executors.newFixedThreadPool(8)
        val puml = "class ConcurrentA\nclass ConcurrentB\nConcurrentA --> ConcurrentB"

        val tasks = (1..20).map {
            Callable {
                PlantUmlRenderer.renderToSvg(puml).getOrThrow()
            }
        }

        val results = threadPool.invokeAll(tasks)
        threadPool.shutdown()

        val first = results.first().get()
        for (f in results) {
            val svg = f.get()
            assertTrue(svg.contains("<svg"))
            assertEquals(first, svg)
        }
    }
}
