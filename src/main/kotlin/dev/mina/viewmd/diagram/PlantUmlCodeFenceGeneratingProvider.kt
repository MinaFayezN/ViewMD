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

package dev.mina.viewmd.diagram

import dev.mina.viewmd.util.HtmlUtils
import org.intellij.markdown.ast.ASTNode
import org.intellij.plugins.markdown.extensions.CodeFenceGeneratingProvider

class PlantUmlCodeFenceGeneratingProvider : CodeFenceGeneratingProvider {

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
                val escapedMsg = HtmlUtils.escapeHtml(err.message ?: "Unknown PlantUML rendering error")
                val escapedSrc = HtmlUtils.escapeHtml(raw)
                """
                <div class="viewmd-diagram-error">
                  <div class="viewmd-diagram-error-title">PlantUML rendering failed: $escapedMsg</div>
                  <pre class="viewmd-diagram-error-source"><code>$escapedSrc</code></pre>
                </div>
                """.trimIndent()
            }
        )
    }
}
