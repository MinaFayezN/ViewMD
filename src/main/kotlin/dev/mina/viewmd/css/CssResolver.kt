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

package dev.mina.viewmd.css

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

sealed class CssStatus {
    data class Valid(val path: Path, val virtualFile: VirtualFile?, val sizeBytes: Long) : CssStatus()
    data class Empty(val path: Path) : CssStatus()
    data class NotFound(val path: Path) : CssStatus()
    data class NotReadable(val path: Path, val reason: String) : CssStatus()
    object NoneConfigured : CssStatus()
}

object CssResolver {

    fun resolve(rawPath: String?, project: Project?): CssStatus {
        val trimmed = rawPath?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return CssStatus.NoneConfigured
        }

        val expanded = expandUserHome(trimmed)
        val path = if (File(expanded).isAbsolute) {
            Path.of(expanded)
        } else {
            val base = project?.basePath
            if (base != null) Path.of(base, expanded) else Path.of(expanded)
        }.normalize().toAbsolutePath()

        if (!Files.exists(path)) {
            return CssStatus.NotFound(path)
        }

        if (Files.isDirectory(path)) {
            return CssStatus.NotReadable(path, "Target path is a directory, not a CSS file")
        }

        if (!Files.isReadable(path)) {
            return CssStatus.NotReadable(path, "File permissions prevent reading")
        }

        val size = try {
            Files.size(path)
        } catch (e: Exception) {
            return CssStatus.NotReadable(path, e.message ?: "Could not determine file size")
        }

        if (size == 0L) {
            return CssStatus.Empty(path)
        }

        val virtualFile = try {
            VfsUtil.findFile(path, true)
        } catch (e: Throwable) {
            null
        }

        return CssStatus.Valid(path, virtualFile, size)
    }

    private fun expandUserHome(path: String): String {
        if (path.startsWith("~" + File.separator) || path == "~") {
            val userHome = System.getProperty("user.home")
            return userHome + path.substring(1)
        }
        return path
    }
}
