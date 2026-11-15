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

    fun testFindAnchorOrImageGenerators() {
        val cl = org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil::class.java.classLoader
        try {
            val runnerClass = Class.forName("org.intellij.plugins.markdown.extensions.jcef.commandRunner.CommandRunnerExtension", true, cl)
            val constructor = runnerClass.constructors.first()
            // Try instantiating with dummy panel or finding what getHtmlForLineRunner returns
            val m = runnerClass.getDeclaredMethod("getHtmlForLineRunner", Boolean::class.javaPrimitiveType, String::class.java)
            m.isAccessible = true
            // CommandRunnerExtension needs a MarkdownHtmlPanel. Let's see if we can instantiate it or inspect bytecode/decompiled info
            println("METHOD_FOUND: $m")
        } catch (e: Throwable) {
            println("RUNNER_CLASS error: $e")
        }
    }
}
