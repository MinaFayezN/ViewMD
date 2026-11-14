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

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.nio.file.Path

/**
 * Listens to VFS events and triggers a reload when the user's custom CSS file is modified.
 */
class CssWatcher(
    private val project: Project?,
    private val parentDisposable: Disposable,
    private val onCssChanged: (Path) -> Unit
) {

    @Volatile
    private var watchedPath: Path? = null

    init {
        VirtualFileManager.getInstance().addAsyncFileListener(
            { events -> processEvents(events) },
            parentDisposable
        )
    }

    fun setWatchedPath(path: Path?) {
        watchedPath = path
    }

    private fun processEvents(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        val current = watchedPath ?: return null
        val matched = events.any { event ->
            val eventPath = try {
                event.file?.toNioPath()
            } catch (_: Throwable) {
                null
            }
            eventPath != null && eventPath == current
        }

        if (!matched) return null

        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                onCssChanged(current)
            }
        }
    }
}
