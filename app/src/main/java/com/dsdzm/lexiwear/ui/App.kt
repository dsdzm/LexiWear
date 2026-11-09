package com.dsdzm.lexiwear.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dsdzm.lexiwear.data.AssetLexiRepository
import com.dsdzm.lexiwear.data.LexiRepository
import kotlinx.coroutines.launch
import com.dsdzm.lexiwear.ui.screens.AboutScreen
import com.dsdzm.lexiwear.ui.screens.BooksScreen
import com.dsdzm.lexiwear.ui.screens.HomeScreen
import com.dsdzm.lexiwear.ui.screens.SearchScreen
import com.dsdzm.lexiwear.ui.screens.UnitWordsScreen
import com.dsdzm.lexiwear.ui.screens.UnitsScreen
import com.dsdzm.lexiwear.ui.screens.VersionsScreen
import com.dsdzm.lexiwear.ui.screens.WordScreen

sealed interface Screen {
    data object Home : Screen
    data object Search : Screen
    data class Word(val word: String) : Screen
    data object Versions : Screen
    data class Books(val versionId: String, val versionName: String) : Screen
    data class Units(val bookId: String, val bookName: String) : Screen
    data class UnitWords(val bookId: String, val unitIndex: Int, val title: String) : Screen
    data object About : Screen
}

@Composable
fun LexiApp() {
    val context = LocalContext.current
    val repo = remember { AssetLexiRepository(context.applicationContext) }
    val stack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val scope = rememberCoroutineScope()

    fun push(screen: Screen) {
        if (stack.lastOrNull() != screen) stack.add(screen)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun openWord(word: String) {
        if (word == "__random__") {
            scope.launch {
                repo.randomWord()?.let { push(Screen.Word(it)) }
            }
        } else {
            push(Screen.Word(word))
        }
    }

    BackHandler(enabled = stack.size > 1) { pop() }

    Box(Modifier.fillMaxSize().background(Ink.Bg)) {
        AnimatedContent(
            targetState = stack.last(),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen",
        ) { screen ->
            when (screen) {
                Screen.Home -> HomeScreen(
                    repo = repo,
                    onSearch = { push(Screen.Search) },
                    onTextbooks = { push(Screen.Versions) },
                    onWord = { openWord(it) },
                    onAbout = { push(Screen.About) },
                )

                Screen.Search -> SearchScreen(
                    repo = repo,
                    onBack = { pop() },
                    onWord = { openWord(it) },
                )

                is Screen.Word -> WordScreen(
                    repo = repo,
                    word = screen.word,
                    onBack = { pop() },
                    onWord = { openWord(it) },
                )

                Screen.Versions -> VersionsScreen(
                    repo = repo,
                    onBack = { pop() },
                    onOpen = { push(Screen.Books(it.id, it.name)) },
                )

                is Screen.Books -> BooksScreen(
                    repo = repo,
                    versionId = screen.versionId,
                    versionName = screen.versionName,
                    onBack = { pop() },
                    onOpen = { push(Screen.Units(it.id, it.name)) },
                )

                is Screen.Units -> UnitsScreen(
                    repo = repo,
                    bookId = screen.bookId,
                    bookName = screen.bookName,
                    onBack = { pop() },
                    onOpen = { index, name ->
                        push(Screen.UnitWords(screen.bookId, index, name))
                    },
                )

                is Screen.UnitWords -> UnitWordsScreen(
                    repo = repo,
                    bookId = screen.bookId,
                    unitIndex = screen.unitIndex,
                    title = screen.title,
                    onBack = { pop() },
                    onWord = { openWord(it) },
                )

                Screen.About -> AboutScreen(repo = repo, onBack = { pop() })
            }
        }
    }
}
