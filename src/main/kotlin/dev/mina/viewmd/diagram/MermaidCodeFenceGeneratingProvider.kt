package dev.mina.viewmd.diagram

import org.intellij.markdown.ast.ASTNode
import org.intellij.plugins.markdown.extensions.CodeFenceGeneratingProvider

class MermaidCodeFenceGeneratingProvider : CodeFenceGeneratingProvider {

    override fun isApplicable(language: String): Boolean {
        return language.lowercase().trim() == "mermaid"
    }

    override fun generateHtml(language: String, raw: String, node: ASTNode): String {
        val escaped = escapeHtml(raw.trim())
        return """<div class="viewmd-mermaid-block"><pre class="mermaid">$escaped</pre></div>"""
    }

    private fun escapeHtml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
