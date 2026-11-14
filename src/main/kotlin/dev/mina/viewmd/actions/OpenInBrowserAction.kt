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

import com.intellij.ide.BrowserUtil
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.Messages
import dev.mina.viewmd.export.HtmlExporter
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil

/**
 * Action to open the full ViewMD Markdown preview in the user's default external web browser.
 */
class OpenInBrowserAction : AnAction("Open Preview in Browser", "Open full ViewMD preview in the default web browser", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return

        try {
            val title = virtualFile.name
            val rawMarkdown = document.text
            val renderedBody = MarkdownUtil.generateMarkdownHtml(virtualFile, rawMarkdown, project)
            val isDark = LafManager.getInstance().currentUIThemeLookAndFeel?.isDark ?: false
            val tempPath = HtmlExporter.exportToTempFile(
                title = title,
                bodyHtml = renderedBody,
                project = project,
                isDark = isDark
            )
            BrowserUtil.browse(tempPath.toFile())
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Failed to export and open in browser: ${ex.message}", "ViewMD")
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val ext = file?.extension
        val isMarkdown = ext != null && (ext.equals("md", ignoreCase = true) || ext.equals("markdown", ignoreCase = true))
        e.presentation.isEnabledAndVisible = isMarkdown
    }
}
