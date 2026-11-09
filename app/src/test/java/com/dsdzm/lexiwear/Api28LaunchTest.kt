package com.dsdzm.lexiwear

import android.os.Looper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * 真实入口验证：在 API 28（Android 9）上完整启动 MainActivity，
 * 走真实的 AssetLexiRepository（不替换为假数据），确保启动路径不抛异常。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w320dp-h320dp-xxhdpi")
class Api28LaunchTest {

    @Test
    fun launchesWithoutCrash() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity.window.decorView)

        repeat(5) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(200)
        }
        shadowOf(Looper.getMainLooper()).idle()

        val decor = activity.window.decorView as android.view.ViewGroup
        assertTrue("decorView 应有子视图", decor.childCount > 0)
        controller.pause().stop().destroy()
    }
}
