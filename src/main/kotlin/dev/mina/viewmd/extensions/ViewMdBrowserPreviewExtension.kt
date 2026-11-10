package dev.mina.viewmd.extensions

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel
import org.intellij.plugins.markdown.ui.preview.ResourceProvider

class ViewMdBrowserPreviewExtension(
    private val panel: MarkdownHtmlPanel
) : MarkdownBrowserPreviewExtension, Disposable {

    override val scripts: List<String> = listOf("web/vendor/mermaid.min.js", "web/postprocess.js")
    override val styles: List<String> = listOf("web/viewmd.css")

    override val resourceProvider: ResourceProvider = object : ResourceProvider {
        override fun canProvide(resourceName: String): Boolean {
            return resourceName == "web/viewmd.css" ||
                   resourceName == "web/postprocess.js" ||
                   resourceName == "web/vendor/mermaid.min.js"
        }

        override fun loadResource(resourceName: String): ResourceProvider.Resource? {
            return ResourceProvider.loadInternalResource(
                ViewMdBrowserPreviewExtension::class.java,
                resourceName,
                null
            )
        }
    }

    private val connection = ApplicationManager.getApplication().messageBus.connect(this)

    init {
        // R-LAY-3: Live theme switching without reopening the file
        connection.subscribe(LafManagerListener.TOPIC, LafManagerListener {
            val isDark = isCurrentThemeDark()
            notifyThemeChanged(isDark)
        })
    }

    private fun isCurrentThemeDark(): Boolean {
        val theme = LafManager.getInstance().currentUIThemeLookAndFeel
        return theme?.isDark ?: false
    }

    private fun notifyThemeChanged(isDark: Boolean) {
        val pipe = panel.browserPipe ?: return
        val json = """{"type":"THEME_CHANGED","isDark":$isDark}"""
        pipe.send("viewmd:event", json)
    }

    override fun dispose() {
        // Automatically cleaned up by Disposer
    }

    class Provider : MarkdownBrowserPreviewExtension.Provider {
        override fun createBrowserExtension(panel: MarkdownHtmlPanel): MarkdownBrowserPreviewExtension {
            val extension = ViewMdBrowserPreviewExtension(panel)
            Disposer.register(panel, extension)
            return extension
        }
    }
}
