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

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.Messages
import dev.mina.viewmd.export.HtmlExporter
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * Action to export the currently active Markdown document as a standalone ViewMD HTML file.
 */
class ExportHtmlAction : AnAction("Export as HTML...", "Export current document as a standalone ViewMD HTML file", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return

        val descriptor = FileSaverDescriptor("Export Markdown as HTML", "Choose destination HTML file", "html")
        val saveDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
        val defaultName = virtualFile.nameWithoutExtension + ".html"
        val wrapper = saveDialog.save(virtualFile.parent, defaultName) ?: return

        try {
            val destination = wrapper.file.toPath()
            val rawMarkdown = document.text
            val renderedBody = MarkdownUtil.generateMarkdownHtml(virtualFile, rawMarkdown, project)
            val isDark = LafManager.getInstance().currentUIThemeLookAndFeel?.isDark ?: false
            val html = HtmlExporter.generateStandaloneHtml(
                title = virtualFile.name,
                bodyHtml = renderedBody,
                project = project,
                isDark = isDark
            )
            Files.writeString(destination, html, StandardCharsets.UTF_8)
            Messages.showInfoMessage(project, "Exported successfully to: $destination", "ViewMD Export")
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Failed to export HTML: ${ex.message}", "ViewMD Export Error")
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val ext = file?.extension
        val isMarkdown = ext != null && (ext.equals("md", ignoreCase = true) || ext.equals("markdown", ignoreCase = true))
        e.presentation.isEnabledAndVisible = isMarkdown
    }
}
