package dev.mina.viewmd.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.concurrent.atomic.AtomicBoolean

class ViewMdSettingsSyncTest : BasePlatformTestCase() {

    fun testSettingsListenerFiresOnApply() {
        val settings = ViewMdSettings.instance
        val fired = AtomicBoolean(false)

        ApplicationManager.getApplication().messageBus.connect(testRootDisposable).subscribe(
            ViewMdSettingsListener.TOPIC,
            ViewMdSettingsListener {
                fired.set(true)
            }
        )

        settings.fireSettingsChanged()
        assertTrue(fired.get())
    }
}
