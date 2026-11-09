package com.dsdzm.lexiwear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.MotionScheme

/** Material 3 Expressive 调色板：深空底 + 薄荷主色 + 琥珀强调 */
object Ink {
    val Bg = Color(0xFF070B0F)
    val Surface = Color(0xFF101A22)
    val SurfaceHigh = Color(0xFF17252F)
    val Mint = Color(0xFF6FE3C4)
    val MintDeep = Color(0xFF06231C)
    val Amber = Color(0xFFFFC46B)
    val Text = Color(0xFFE9F2F3)
    val Muted = Color(0xFF91A6B3)
    val Line = Color(0xFF22333F)
    val Rose = Color(0xFFFF8E8E)
}

@Composable
fun LexiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
