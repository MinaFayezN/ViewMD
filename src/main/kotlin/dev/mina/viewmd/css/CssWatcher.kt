package dev.mina.viewmd.css

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.nio.file.Path

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
            val eventPath = event.file?.toNioPath()
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
