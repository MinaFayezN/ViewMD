package dev.mina.viewmd.export

import com.intellij.openapi.project.Project
import dev.mina.viewmd.css.CssResolver
import dev.mina.viewmd.css.CssStatus
import dev.mina.viewmd.settings.ViewMdSettings
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object HtmlExporter {

    fun generateStandaloneHtml(
        title: String,
        bodyHtml: String,
        project: Project?,
        isDark: Boolean = false
    ): String {
        val builtInCss = loadResourceText("web/viewmd.css")
        val postprocessJs = loadResourceText("web/postprocess.js")
        val mermaidJs = loadResourceText("web/vendor/mermaid.min.js")

        val customCssText = resolveCustomCss(project)

        val themeAttr = if (isDark) "data-theme=\"dark\"" else "data-theme=\"light\""
        val themeOverride = if (isDark) {
            """
            :root {
              color-scheme: dark !important;
              --viewmd-fg: #e6edf3;
              --viewmd-bg: #0d1117;
              --viewmd-soft: #151b23;
              --viewmd-line: #3d444d;
              --viewmd-code: #262c36;
              --viewmd-link: #4493f8;
            }
            html, body { background: #0d1117 !important; color: #e6edf3 !important; }
            """.trimIndent()
        } else {
            """
            html, body { background: #ffffff !important; color: #1f2328 !important; }
            """.trimIndent()
        }

        return """
        <!doctype html>
        <html lang="en" $themeAttr>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>${escapeHtml(title)}</title>
          <style>
          $builtInCss
          $themeOverride
          $customCssText
          </style>
          <script>
          $mermaidJs
          </script>
        </head>
        <body>
          $bodyHtml
          <script>
          $postprocessJs
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    fun exportToTempFile(title: String, bodyHtml: String, project: Project?, isDark: Boolean = false): Path {
        val html = generateStandaloneHtml(title, bodyHtml, project, isDark)
        val tempFile = Files.createTempFile("viewmd-export-", ".html")
        Files.writeString(tempFile, html, StandardCharsets.UTF_8)
        return tempFile
    }

    private fun resolveCustomCss(project: Project?): String {
        val path = try {
            ViewMdSettings.instance.state.customCssPath
        } catch (e: Throwable) {
            ""
        }
        val status = CssResolver.resolve(path, project)
        if (status is CssStatus.Valid) {
            return try {
                Files.readString(status.path, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                ""
            }
        }
        return ""
    }

    private fun loadResourceText(resourcePath: String): String {
        val normalized = if (resourcePath.startsWith("/")) resourcePath else "/$resourcePath"
        val stream = HtmlExporter::class.java.getResourceAsStream(normalized)
            ?: HtmlExporter::class.java.classLoader.getResourceAsStream(resourcePath)
        return stream?.use {
            String(it.readAllBytes(), StandardCharsets.UTF_8)
        } ?: ""
    }

    private fun escapeHtml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
