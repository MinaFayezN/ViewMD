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
            val clean = resourceName.removePrefix("/")
            return clean == "web/viewmd.css" ||
                   clean == "web/postprocess.js" ||
                   clean == "web/vendor/mermaid.min.js"
        }

        override fun loadResource(resourceName: String): ResourceProvider.Resource? {
            val normalized = if (resourceName.startsWith("/")) resourceName else "/$resourceName"
            return ResourceProvider.loadInternalResource(
                ViewMdBrowserPreviewExtension::class.java,
                normalized,
                null
            )
        }
    }

    private val cssWatcher = CssWatcher(panel.project, this) { path ->
        reloadCustomCss(path)
    }

    @Volatile
    private var isDisposed = false

    init {
        val busConnection = ApplicationManager.getApplication().messageBus.connect(this)

        // Listen for IDE Theme changes (Light/Dark mode)
        busConnection.subscribe(LafManagerListener.TOPIC, LafManagerListener {
            if (isDisposed) return@LafManagerListener
            val isDark = LafManager.getInstance().currentUIThemeLookAndFeel?.isDark ?: false
            sendThemeEvent(isDark)
        })

        // Listen for ViewMD settings adjustments (Width, Fonts, Front Matter Card visibility)
        busConnection.subscribe(ViewMdSettingsListener.TOPIC, ViewMdSettingsListener { settings ->
            if (isDisposed) return@ViewMdSettingsListener
            applySettings(settings)
        })

        // Initialize custom CSS watching and initial settings
        applySettings(ViewMdSettings.instance.state)
    }

    private fun sendThemeEvent(isDark: Boolean) {
        val json = """{"type":"THEME_CHANGED","isDark":$isDark}"""
        sendToBrowser("THEME_CHANGED", json)
    }

    private fun applySettings(settings: ViewMdSettings.State) {
        val widthStr = if (settings.isFullWidth) "100%" else "${settings.contentWidthPx}px"
        val fontFamStr = settings.fontFamily.replace("\"", "\\\"")

        // Handle custom CSS resolution
        val status = CssResolver.resolve(settings.customCssPath, panel.project)
        val customCssContent = if (status is CssStatus.Valid) {
            cssWatcher.setWatchedPath(status.path)
            try {
                Files.readString(status.path)
            } catch (e: Exception) {
                ""
            }
        } else {
            cssWatcher.setWatchedPath(null)
            ""
        }

        val json = """{
            "type":"SETTINGS_CHANGED",
            "maxWidth":"$widthStr",
            "fontFamily":"$fontFamStr",
            "fontSizePx":${settings.fontSizePx},
            "colMinChars":${settings.colMinChars},
            "colFirstMinChars":${settings.colFirstMinChars},
            "hideFrontMatterCard":${settings.hideFrontMatterCard},
            "expandFrontMatterCard":${settings.expandFrontMatterCard},
            "customCss":"${escapeJson(customCssContent)}"
        }""".trimIndent().replace("\n", "")

        sendToBrowser("SETTINGS_CHANGED", json)
    }

    private fun reloadCustomCss(path: java.nio.file.Path) {
        val cssContent = try {
            Files.readString(path)
        } catch (e: Exception) {
            ""
        }
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
        isDisposed = true
    }

    class Provider : MarkdownBrowserPreviewExtension.Provider {
        override fun createBrowserExtension(panel: MarkdownHtmlPanel): MarkdownBrowserPreviewExtension {
            val extension = ViewMdBrowserPreviewExtension(panel)
            Disposer.register(panel, extension)
            return extension
        }
    }
}
