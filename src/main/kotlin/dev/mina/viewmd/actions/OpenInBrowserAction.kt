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

class OpenInBrowserAction : AnAction("Open Preview in Browser", "Open full ViewMD preview in the default web browser", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return

        try {
            val title = virtualFile.name
            val rawMarkdown = document.text
            // Convert markdown source into HTML using the platform MarkdownUtil
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
        val isMarkdown = file != null && (file.extension.equals("md", ignoreCase = true) || file.extension.equals("markdown", ignoreCase = true))
        e.presentation.isEnabledAndVisible = isMarkdown
    }
}
