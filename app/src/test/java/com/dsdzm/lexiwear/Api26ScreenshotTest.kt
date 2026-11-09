package com.dsdzm.lexiwear

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.dsdzm.lexiwear.data.LexiRepository
import com.dsdzm.lexiwear.ui.LexiTheme
import com.dsdzm.lexiwear.ui.screens.HomeScreen
import com.dsdzm.lexiwear.ui.screens.SearchScreen
import com.dsdzm.lexiwear.ui.screens.VersionsScreen
import com.dsdzm.lexiwear.ui.screens.WordScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * Android 8.0（API 26）兼容性验证：在最低支持系统上真实渲染四个主界面。
 * 只要能出图且无异常，就说明 Compose / Wear M3 在 API 26 上可用。
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [26], qualifiers = "w320dp-h320dp-xxhdpi")
class Api26ScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val repo: LexiRepository = FakeRepo()

    private fun shot(name: String) {
        compose.waitForIdle()
        Thread.sleep(1200)
        compose.waitForIdle()
        val view = compose.activity.window.decorView
        val w = if (view.width > 0) view.width else 960
        val h = if (view.height > 0) view.height else 960
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bmp))
        val dir = File("build/screenshots/api26").apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test
    fun home() {
        compose.setContent {
            LexiTheme {
                HomeScreen(repo, onSearch = {}, onTextbooks = {}, onWord = {}, onAbout = {})
            }
        }
        shot("01-home")
    }

    @Test
    fun word() {
        compose.setContent {
            LexiTheme {
                WordScreen(repo, "responsible", onBack = {}, onWord = {})
            }
        }
        shot("02-word")
    }

    @Test
    fun versions() {
        compose.setContent {
            LexiTheme {
                VersionsScreen(repo, onBack = {}, onOpen = {})
            }
        }
        shot("03-versions")
    }

    @Test
    fun search() {
        compose.setContent {
            LexiTheme {
                SearchScreen(repo, onBack = {}, onWord = {})
            }
        }
        shot("04-search")
    }
}
