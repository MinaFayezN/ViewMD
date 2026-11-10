package dev.mina.viewmd.diagram

import com.intellij.openapi.diagnostic.Logger
import org.intellij.markdown.ast.ASTNode
import org.intellij.plugins.markdown.extensions.CodeFenceGeneratingProvider

class PlantUmlCodeFenceGeneratingProvider : CodeFenceGeneratingProvider {

    private val logger = Logger.getInstance(PlantUmlCodeFenceGeneratingProvider::class.java)

    override fun isApplicable(language: String): Boolean {
        val lang = language.lowercase().trim()
        return lang == "plantuml" || lang == "puml"
    }

    override fun generateHtml(language: String, raw: String, node: ASTNode): String {
        return PlantUmlRenderer.renderToSvg(raw).fold(
            onSuccess = { svg ->
                """<div class="viewmd-diagram-container viewmd-plantuml-container">$svg</div>"""
            },
            onFailure = { err ->
                val escapedMsg = escapeHtml(err.message ?: "Unknown PlantUML rendering error")
                val escapedSrc = escapeHtml(raw)
                """
                <div class="viewmd-diagram-error">
                  <div class="viewmd-diagram-error-title">PlantUML rendering failed: $escapedMsg</div>
                  <pre class="viewmd-diagram-error-source"><code>$escapedSrc</code></pre>
                </div>
                """.trimIndent()
            }
        )
    }

    private fun escapeHtml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
