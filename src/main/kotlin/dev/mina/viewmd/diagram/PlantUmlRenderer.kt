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

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.LinkedHashMap

object PlantUmlRenderer {

    init {
        System.setProperty("PLANTUML_SECURITY_PROFILE", "SANDBOX")
    }

    private const val MAX_CACHE_SIZE = 200

    private val FORBIDDEN_DIRECTIVES = Regex("(?im)^\\s*!(include|includesub|import|load_path|dirpath|define_from_file)\\b")

    private val lock = Any()
    private val cache = object : LinkedHashMap<String, String>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    fun renderToSvg(source: String): Result<String> {
        val trimmed = source.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("PlantUML source is empty"))
        }

        val pumlSource = if (trimmed.startsWith("@startuml")) {
            trimmed
        } else {
            "@startuml\n$trimmed\n@enduml"
        }

        if (FORBIDDEN_DIRECTIVES.containsMatchIn(pumlSource)) {
            return Result.failure(SecurityException("PlantUML inclusion directives (!include/!import) are disabled for security"))
        }

        val cacheKey = sha256(pumlSource)
        synchronized(lock) {
            val cached = cache[cacheKey]
            if (cached != null) {
                return Result.success(cached)
            }
        }

        return try {
            val reader = SourceStringReader(pumlSource)
            val os = ByteArrayOutputStream()
            val desc = reader.outputImage(os, FileFormatOption(FileFormat.SVG))
            if (desc == null || os.size() == 0) {
                return Result.failure(IllegalStateException("PlantUML produced empty output"))
            }
            val svg = os.toString(StandardCharsets.UTF_8)
            synchronized(lock) {
                cache[cacheKey] = svg
            }
            Result.success(svg)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
