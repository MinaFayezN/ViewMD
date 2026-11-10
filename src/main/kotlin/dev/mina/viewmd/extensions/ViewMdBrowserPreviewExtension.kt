package dev.mina.viewmd.extensions

import com.intellij.openapi.Disposable
import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel
import org.intellij.plugins.markdown.ui.preview.ResourceProvider

class ViewMdBrowserPreviewExtension : MarkdownBrowserPreviewExtension {

    override val scripts: List<String> = listOf("web/postprocess.js")
    override val styles: List<String> = listOf("web/viewmd.css")

    override val resourceProvider: ResourceProvider = object : ResourceProvider {
        override fun canProvide(resourceName: String): Boolean {
            return resourceName == "web/viewmd.css" || resourceName == "web/postprocess.js"
        }

        override fun loadResource(resourceName: String): ResourceProvider.Resource? {
            return ResourceProvider.loadInternalResource(
                ViewMdBrowserPreviewExtension::class.java,
                resourceName,
                null
            )
        }
    }

    override fun dispose() {
    }

    class Provider : MarkdownBrowserPreviewExtension.Provider {
        override fun createBrowserExtension(panel: MarkdownHtmlPanel): MarkdownBrowserPreviewExtension {
            return ViewMdBrowserPreviewExtension()
        }
    }
}
