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

package dev.mina.viewmd.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ViewMdSettingsTest : BasePlatformTestCase() {

    fun testDefaultStateValues() {
        val settings = ViewMdSettings()
        val state = settings.state

        assertEquals(980, state.contentWidthPx)
        assertFalse(state.isFullWidth)
        assertEquals("", state.fontFamily)
        assertEquals(15, state.fontSizePx)
        assertEquals(12, state.colMinChars)
        assertEquals(14, state.colFirstMinChars)
        assertFalse(state.hideFrontMatterCard)
        assertFalse(state.expandFrontMatterCard)
        assertEquals("", state.customCssPath)
    }

    fun testStatePersistenceRoundtrip() {
        val settings = ViewMdSettings()
        val customState = ViewMdSettings.State(
            contentWidthPx = 1200,
            isFullWidth = true,
            fontFamily = "JetBrains Mono",
            fontSizePx = 16,
            colMinChars = 15,
            colFirstMinChars = 20,
            hideFrontMatterCard = true,
            expandFrontMatterCard = true,
            customCssPath = "/path/to/custom.css"
        )

        settings.loadState(customState)

        val retrieved = settings.state
        assertEquals(1200, retrieved.contentWidthPx)
        assertTrue(retrieved.isFullWidth)
        assertEquals("JetBrains Mono", retrieved.fontFamily)
        assertEquals(16, retrieved.fontSizePx)
        assertEquals(15, retrieved.colMinChars)
        assertEquals(20, retrieved.colFirstMinChars)
        assertTrue(retrieved.hideFrontMatterCard)
        assertTrue(retrieved.expandFrontMatterCard)
        assertEquals("/path/to/custom.css", retrieved.customCssPath)
    }
}
