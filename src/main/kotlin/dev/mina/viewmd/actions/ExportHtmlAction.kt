package dev.mina.viewmd.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.Messages
import dev.mina.viewmd.export.HtmlExporter
import java.nio.charset.StandardCharsets
import java.nio.file.Files

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
            val html = HtmlExporter.generateStandaloneHtml(virtualFile.name, "<div id=\"content\">" + document.text + "</div>", project)
            Files.writeString(destination, html, StandardCharsets.UTF_8)
            Messages.showInfoMessage(project, "Exported successfully to: $destination", "ViewMD Export")
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Failed to export HTML: ${ex.message}", "ViewMD Export Error")
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val isMarkdown = file != null && (file.extension.equals("md", ignoreCase = true) || file.extension.equals("markdown", ignoreCase = true))
        e.presentation.isEnabledAndVisible = isMarkdown
    }
}
