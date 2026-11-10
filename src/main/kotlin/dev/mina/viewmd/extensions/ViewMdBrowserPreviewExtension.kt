package dev.mina.viewmd.extensions

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import dev.mina.viewmd.css.CssResolver
import dev.mina.viewmd.css.CssStatus
import dev.mina.viewmd.css.CssWatcher
import dev.mina.viewmd.settings.ViewMdSettings
import dev.mina.viewmd.settings.ViewMdSettingsListener
import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel
import org.intellij.plugins.markdown.ui.preview.ResourceProvider
import java.nio.file.Files

class ViewMdBrowserPreviewExtension(
    private val panel: MarkdownHtmlPanel
) : MarkdownBrowserPreviewExtension, Disposable {

    private val logger = Logger.getInstance(ViewMdBrowserPreviewExtension::class.java)

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
    private val cssWatcher = CssWatcher(panel.project, this) { path ->
        try {
            val content = Files.readString(path)
            sendCustomCss(content)
        } catch (e: Exception) {
            logger.warn("Failed reading changed custom CSS file: $path", e)
        }
    }

    init {
        // Theme changes
        connection.subscribe(LafManagerListener.TOPIC, LafManagerListener {
            val isDark = isCurrentThemeDark()
            notifyThemeChanged(isDark)
        })

        // Live settings synchronization
        connection.subscribe(ViewMdSettingsListener.TOPIC, ViewMdSettingsListener { state ->
            applySettings(state)
        })

        // Initial settings push on creation
        ApplicationManager.getApplication().invokeLater({
            applySettings(ViewMdSettings.instance.state)
            val isDark = isCurrentThemeDark()
            notifyThemeChanged(isDark)
        }, { Disposer.isDisposed(this) })
    }

    private fun isCurrentThemeDark(): Boolean {
        val theme = LafManager.getInstance().currentUIThemeLookAndFeel
        return theme?.isDark ?: false
    }

    private fun notifyThemeChanged(isDark: Boolean) {
        val json = """{"type":"THEME_CHANGED","isDark":$isDark}"""
        sendToBrowser("THEME_CHANGED", json)
    }

    private fun applySettings(state: ViewMdSettings.State) {
        val maxWidth = if (state.isFullWidth) "none" else "${state.contentWidthPx}px"
        val customCssContent = resolveCustomCss(state.customCssPath)

        val json = buildString {
            append("{")
            append("\"type\":\"SETTINGS_CHANGED\",")
            append("\"maxWidth\":\"").append(escapeJson(maxWidth)).append("\",")
            append("\"fontSizePx\":").append(state.fontSizePx).append(",")
            append("\"fontFamily\":\"").append(escapeJson(state.fontFamily)).append("\",")
            append("\"colMinChars\":").append(state.colMinChars).append(",")
            append("\"colFirstMinChars\":").append(state.colFirstMinChars).append(",")
            append("\"hideFrontMatterCard\":").append(state.hideFrontMatterCard).append(",")
            append("\"expandFrontMatterCard\":").append(state.expandFrontMatterCard).append(",")
            append("\"customCss\":\"").append(escapeJson(customCssContent)).append("\"")
            append("}")
        }

        sendToBrowser("SETTINGS_CHANGED", json)
    }

    private fun resolveCustomCss(cssPath: String): String {
        val status = CssResolver.resolve(cssPath, panel.project)
        return when (status) {
            is CssStatus.Valid -> {
                cssWatcher.setWatchedPath(status.path)
                try {
                    Files.readString(status.path)
                } catch (e: Exception) {
                    ""
                }
            }
            else -> {
                cssWatcher.setWatchedPath(null)
                ""
            }
        }
    }

    private fun sendCustomCss(cssContent: String) {
        val json = """{"type":"CUSTOM_CSS_CHANGED","css":"${escapeJson(cssContent)}"}"""
        sendToBrowser("CUSTOM_CSS_CHANGED", json)
    }

    private fun sendToBrowser(eventType: String, jsonPayload: String) {
        // Try invoking runJavaScript via reflection on JCEF panel if supported
        try {
            val method = panel.javaClass.methods.firstOrNull {
                it.name == "runJavaScript" && it.parameterCount in 1..3
            }
            if (method != null) {
                val js = "window.postMessage($jsonPayload, '*');"
                if (method.parameterCount == 1) {
                    method.invoke(panel, js)
                } else if (method.parameterCount == 3) {
                    method.invoke(panel, js, "viewmd", 0)
                }
            }
        } catch (e: Throwable) {
            logger.debug("Reflection runJavaScript call omitted", e)
        }

        // Also broadcast via BrowserPipe
        try {
            panel.browserPipe?.send("viewmd:event", jsonPayload)
        } catch (e: Throwable) {
            logger.debug("BrowserPipe send failed", e)
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
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
