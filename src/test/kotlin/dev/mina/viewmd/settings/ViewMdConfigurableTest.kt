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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.concurrent.atomic.AtomicBoolean

class ViewMdConfigurableTest : BasePlatformTestCase() {

    fun testConfigurableMetadata() {
        val configurable = ViewMdConfigurable(project)
        assertEquals("ViewMD", configurable.displayName)
        assertEquals("dev.mina.viewmd.settings", configurable.id)
    }

    fun testCreatePanelReturnsValidDialogPanel() {
        val configurable = ViewMdConfigurable(project)
        val panel = configurable.createPanel()
        assertNotNull(panel)
    }

    fun testApplyFiresSettingsChanged() {
        val configurable = ViewMdConfigurable(project)
        val fired = AtomicBoolean(false)

        ApplicationManager.getApplication().messageBus.connect(testRootDisposable).subscribe(
            ViewMdSettingsListener.TOPIC,
            ViewMdSettingsListener {
                fired.set(true)
            }
        )

        configurable.apply()
        assertTrue(fired.get())
    }
}
