package com.dsdzm.lexiwear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dsdzm.lexiwear.ui.LexiApp
import com.dsdzm.lexiwear.ui.LexiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LexiTheme {
                LexiApp()
            }
        }
    }
}
