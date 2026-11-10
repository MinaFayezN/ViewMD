package dev.mina.viewmd.diagram

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

object PlantUmlRenderer {

    private val cache = ConcurrentHashMap<String, String>()

    fun renderToSvg(source: String): Result<String> {
        val trimmed = source.trim()
        val pumlSource = if (trimmed.startsWith("@startuml")) {
            trimmed
        } else {
            "@startuml\n$trimmed\n@enduml"
        }

        val cacheKey = sha256(pumlSource)
        val cached = cache[cacheKey]
        if (cached != null) {
            return Result.success(cached)
        }

        return try {
            val reader = SourceStringReader(pumlSource)
            val os = ByteArrayOutputStream()
            val desc = reader.outputImage(os, FileFormatOption(FileFormat.SVG))
            if (desc == null || os.size() == 0) {
                return Result.failure(IllegalStateException("PlantUML produced empty output"))
            }
            val svg = os.toString(StandardCharsets.UTF_8)
            cache[cacheKey] = svg
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
