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
