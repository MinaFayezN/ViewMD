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

package dev.mina.viewmd.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.APP)
@State(
    name = "ViewMdSettings",
    storages = [Storage("viewmd.xml")]
)
class ViewMdSettings : PersistentStateComponent<ViewMdSettings.State> {

    data class State(
        var contentWidthPx: Int = 980,
        var isFullWidth: Boolean = false,
        var fontFamily: String = "",
        var fontSizePx: Int = 15,
        var colMinChars: Int = 12,
        var colFirstMinChars: Int = 14,
        var hideFrontMatterCard: Boolean = false,
        var expandFrontMatterCard: Boolean = false,
        var customCssPath: String = ""
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
        fireSettingsChanged()
    }

    fun fireSettingsChanged() {
        ApplicationManager.getApplication().messageBus
            .syncPublisher(ViewMdSettingsListener.TOPIC)
            .onSettingsChanged(myState)
    }

    companion object {
        val instance: ViewMdSettings
            get() = ApplicationManager.getApplication().getService(ViewMdSettings::class.java)
    }
}
