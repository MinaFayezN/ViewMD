package dev.mina.viewmd.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import dev.mina.viewmd.css.CssResolver
import dev.mina.viewmd.css.CssStatus
import javax.swing.JLabel

class ViewMdConfigurable(private val project: Project) : BoundSearchableConfigurable("ViewMD", "dev.mina.viewmd.settings") {

    private val settings = ViewMdSettings.instance
    private val diagnosticsLabel = JLabel()

    override fun apply() {
        super.apply()
        settings.fireSettingsChanged()
    }

    override fun createPanel(): DialogPanel {
        return panel {
            group("Layout & Typography") {
                row("Max Content Width (px):") {
                    intTextField(range = 400..3840)
                        .bindIntText(settings.state::contentWidthPx)
                }
                row {
                    checkBox("Full width (ignore max content width)")
                        .bindSelected(settings.state::isFullWidth)
                }
                row("Font Size (px):") {
                    intTextField(range = 10..40)
                        .bindIntText(settings.state::fontSizePx)
                }
            }

            group("Tables") {
                row("Minimum column width (characters):") {
                    intTextField(range = 4..50)
                        .bindIntText(settings.state::colMinChars)
                }
                row("First column minimum width (characters):") {
                    intTextField(range = 4..60)
                        .bindIntText(settings.state::colFirstMinChars)
                }
            }

            group("Front Matter") {
                row {
                    checkBox("Hide front matter card completely")
                        .bindSelected(settings.state::hideFrontMatterCard)
                }
                row {
                    checkBox("Expand front matter card by default")
                        .bindSelected(settings.state::expandFrontMatterCard)
                }
            }

            group("Custom CSS Diagnostics") {
                row("Stylesheet Path:") {
                    val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("css")
                        .withTitle("Select Custom CSS File")
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = descriptor,
                        project = project
                    ).bindText(settings.state::customCssPath)
                    .onChanged { tf ->
                        updateDiagnostics(tf.text)
                    }
                }
                row("Status:") {
                    cell(diagnosticsLabel)
                }
            }
        }.also {
            updateDiagnostics(settings.state.customCssPath)
        }
    }

    private fun updateDiagnostics(pathText: String) {
        val status = CssResolver.resolve(pathText, project)
        when (status) {
            is CssStatus.Valid -> {
                diagnosticsLabel.text = "Found & readable (${status.sizeBytes} bytes) -> ${status.path}"
            }
            is CssStatus.Empty -> {
                diagnosticsLabel.text = "File is empty (0 bytes) -> ${status.path}"
            }
            is CssStatus.NotFound -> {
                diagnosticsLabel.text = "Not found -> ${status.path}"
            }
            is CssStatus.NotReadable -> {
                diagnosticsLabel.text = "Not readable (${status.reason}) -> ${status.path}"
            }
            is CssStatus.NoneConfigured -> {
                diagnosticsLabel.text = "No custom CSS specified (using built-in ViewMD theme)"
            }
        }
    }
}
