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

package dev.mina.viewmd.actions

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ExportHtmlActionTest : BasePlatformTestCase() {

    private val action = ExportHtmlAction()

    fun testActionVisibleForMdFile() {
        val psiFile = myFixture.configureByText("sample.md", "# Hello")
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.VIRTUAL_FILE, psiFile.virtualFile)
            .build()
        val event = TestActionEvent.createTestEvent(action, dataContext)

        action.update(event)
        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testActionVisibleForMarkdownExtension() {
        val psiFile = myFixture.configureByText("document.markdown", "# Hello")
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.VIRTUAL_FILE, psiFile.virtualFile)
            .build()
        val event = TestActionEvent.createTestEvent(action, dataContext)

        action.update(event)
        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testActionHiddenForTxtFile() {
        val psiFile = myFixture.configureByText("notes.txt", "Plain text")
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.VIRTUAL_FILE, psiFile.virtualFile)
            .build()
        val event = TestActionEvent.createTestEvent(action, dataContext)

        action.update(event)
        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testActionHiddenWhenNoFile() {
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .build()
        val event = TestActionEvent.createTestEvent(action, dataContext)

        action.update(event)
        assertFalse(event.presentation.isEnabledAndVisible)
    }
}
