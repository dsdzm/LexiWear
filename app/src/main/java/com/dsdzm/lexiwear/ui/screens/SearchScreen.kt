package com.dsdzm.lexiwear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.dsdzm.lexiwear.R
import com.dsdzm.lexiwear.data.IndexEntry
import com.dsdzm.lexiwear.data.LexiRepository
import com.dsdzm.lexiwear.ui.Ink
import com.dsdzm.lexiwear.ui.TopBar
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    repo: LexiRepository,
    onBack: () -> Unit,
    onWord: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<IndexEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            searched = false
            return@LaunchedEffect
        }
        loading = true
        delay(120)
        results = repo.search(query)
        loading = false
        searched = true
    }

    AppScaffold(
        timeText = { TimeText() },
        containerColor = Ink.Bg,
        contentColor = Ink.Text,
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
            Spacer(Modifier.height(22.dp))
            TopBar("词典搜索", onBack)
            Spacer(Modifier.height(6.dp))

            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(color = Ink.Text, fontSize = 14.sp),
                cursorBrush = SolidColor(Ink.Mint),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                decorationBox = { inner ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Ink.SurfaceHigh)
                            .padding(horizontal = 13.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            androidx.compose.ui.res.painterResource(R.drawable.ic_search),
                            contentDescription = null,
                            tint = Ink.Muted,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text("输入单词或中文释义", color = Ink.Muted, fontSize = 13.sp)
                            }
                            inner()
                        }
                    }
                },
            )

            Spacer(Modifier.height(8.dp))

            ScalingLazyColumn(
                state = rememberScalingLazyListState(),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                when {
                    query.isBlank() -> item {
                        Text(
                            "支持英文单词、词组与中文释义检索。\n例如：responsible / 负责",
                            color = Ink.Muted,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                    }

                    loading && results.isEmpty() -> item {
                        Text("查找中…", color = Ink.Muted, fontSize = 11.sp)
                    }

                    searched && results.isEmpty() -> item {
                        Text("没有找到“$query”", color = Ink.Muted, fontSize = 11.sp)
                    }

                    else -> {
                        item {
                            Text(
                                "找到 ${results.size} 条",
                                color = Ink.Muted,
                                fontSize = 9.sp,
                            )
                        }
                        items(results) { e ->
                            Card(
                                onClick = {
                                    focus.clearFocus()
                                    onWord(e.word)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Ink.Surface,
                                    contentColor = Ink.Text,
                                ),
                                contentPadding = PaddingValues(horizontal = 13.dp, vertical = 9.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            e.word,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (e.brief.isNotBlank()) {
                                            Text(
                                                e.brief,
                                                color = Ink.Muted,
                                                fontSize = 9.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
