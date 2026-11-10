package dev.mina.viewmd.settings

import com.intellij.util.messages.Topic

fun interface ViewMdSettingsListener {
    fun onSettingsChanged(settings: ViewMdSettings.State)

    companion object {
        val TOPIC = Topic.create("ViewMdSettingsChanged", ViewMdSettingsListener::class.java)
    }
}
