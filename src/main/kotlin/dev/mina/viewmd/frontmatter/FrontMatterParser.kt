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

package dev.mina.viewmd.frontmatter

import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings

data class FrontMatterResult(
    val summary: String? = null,
    val triggers: List<String> = emptyList(),
    val indexed: Boolean = true,
    val otherFields: Map<String, Any?> = emptyMap(),
    val rawText: String = "",
    val errorMessage: String? = null,
    val errorLine: Int? = null,
    val isValid: Boolean = true
)

object FrontMatterParser {

    private val LOAD_SETTINGS = LoadSettings.builder()
        .setLabel("ViewMD FrontMatter")
        .build()

    fun parse(markdown: String): Pair<FrontMatterResult?, String> {
        if (!markdown.startsWith("---")) {
            return Pair(null, markdown)
        }

        val secondDelimIndex = findClosingDelimiter(markdown)
        if (secondDelimIndex == -1) {
            val result = FrontMatterResult(
                rawText = markdown,
                errorMessage = "Unclosed front matter block (missing closing '---')",
                errorLine = 1,
                isValid = false
            )
            return Pair(result, markdown)
        }

        val frontMatterRaw = markdown.substring(3, secondDelimIndex).trim()
        val remainingBody = markdown.substring(secondDelimIndex + 3).trimStart('\r', '\n')

        try {
            val load = Load(LOAD_SETTINGS)
            val parsedObj = load.loadFromString(frontMatterRaw)

            if (parsedObj !is Map<*, *>) {
                val result = FrontMatterResult(
                    rawText = frontMatterRaw,
                    errorMessage = "Front matter must be a YAML mapping (key-value pairs)",
                    isValid = false
                )
                return Pair(result, remainingBody)
            }

            var summary: String? = null
            var triggers = emptyList<String>()
            var indexed = true
            val others = mutableMapOf<String, Any?>()

            for ((k, v) in parsedObj) {
                val key = k?.toString() ?: continue
                when (key.lowercase()) {
                    "summary" -> summary = v?.toString()
                    "triggers" -> {
                        triggers = when (v) {
                            is List<*> -> v.mapNotNull { it?.toString() }
                            is String -> v.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                            else -> emptyList()
                        }
                    }
                    "index" -> {
                        indexed = when (v) {
                            is Boolean -> v
                            is String -> v.lowercase() != "false"
                            else -> true
                        }
                    }
                    else -> others[key] = v
                }
            }

            val result = FrontMatterResult(
                summary = summary,
                triggers = triggers,
                indexed = indexed,
                otherFields = others,
                rawText = frontMatterRaw,
                isValid = true
            )
            return Pair(result, remainingBody)
        } catch (ex: Exception) {
            val result = FrontMatterResult(
                rawText = frontMatterRaw,
                errorMessage = ex.message?.lines()?.firstOrNull() ?: "YAML syntax error",
                isValid = false
            )
            return Pair(result, remainingBody)
        }
    }

    private fun findClosingDelimiter(text: String): Int {
        val lines = text.split("\n")
        var currentOffset = 0
        for (i in lines.indices) {
            val line = lines[i].trimEnd('\r')
            if (i > 0 && line.trim() == "---") {
                return currentOffset
            }
            currentOffset += lines[i].length + 1
        }
        return -1
    }
}
