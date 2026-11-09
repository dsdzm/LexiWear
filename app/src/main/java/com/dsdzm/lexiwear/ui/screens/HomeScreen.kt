package com.dsdzm.lexiwear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextButtonDefaults
import androidx.wear.compose.material3.TimeText
import com.dsdzm.lexiwear.R
import com.dsdzm.lexiwear.data.LexiRepository
import com.dsdzm.lexiwear.data.Meta
import com.dsdzm.lexiwear.ui.Badge
import com.dsdzm.lexiwear.ui.Ink

@Composable
fun HomeScreen(
    repo: LexiRepository,
    onSearch: () -> Unit,
    onTextbooks: () -> Unit,
    onWord: (String) -> Unit,
    onAbout: () -> Unit,
) {
    var meta by remember { mutableStateOf<Meta?>(null) }
    var wotd by remember { mutableStateOf<String?>(null) }
    var wotdBrief by remember { mutableStateOf("") }
    var recent by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        meta = repo.meta()
        recent = repo.recent()
        val w = repo.wordOfTheDay()
        wotd = w
        if (w != null) {
            wotdBrief = repo.word(w)?.senses?.firstOrNull().orEmpty()
        }
    }

    AppScaffold(
        timeText = { TimeText() },
        containerColor = Ink.Bg,
        contentColor = Ink.Text,
    ) {
        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            item {
                Column {
                    Text(
                        "袖典",
                        color = Ink.Text,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp,
                    )
                    Text(
                        "高中英语 · 离线词典",
                        color = Ink.Muted,
                        fontSize = 10.sp,
                    )
                }
            }

            item {
                Button(
                    onClick = onSearch,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink.Mint,
                        contentColor = Ink.MintDeep,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = Ink.MintDeep,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("查单词", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Card(
                    onClick = onTextbooks,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Ink.Surface,
                        contentColor = Ink.Text,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.ic_book),
                            contentDescription = null,
                            tint = Ink.Mint,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("教材词表", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (meta != null) "${meta!!.versions} 个版本 · ${meta!!.books} 册 · ${meta!!.units} 个单元"
                                else "按版本 / 年级 / 单元浏览",
                                color = Ink.Muted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            wotd?.let { word ->
                item {
                    Card(
                        onClick = { onWord(word) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Ink.SurfaceHigh,
                            contentColor = Ink.Text,
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("每日一词", color = Ink.Mint, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(6.dp))
                                    Badge("离线", Ink.Amber)
                                }
                                Text(word, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                if (wotdBrief.isNotBlank()) {
                                    Text(
                                        wotdBrief,
                                        color = Ink.Muted,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    onClick = {
                        // 随机词通过回调打开；由界面层触发协程加载
                        onWord("__random__")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = Ink.Surface,
                        contentColor = Ink.Text,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.ic_dice),
                            contentDescription = null,
                            tint = Ink.Amber,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("随便看看", fontSize = 12.sp, color = Ink.Muted)
                    }
                }
            }

            if (recent.isNotEmpty()) {
                item {
                    ListHeader {
                        Text("最近查询", color = Ink.Muted, fontSize = 10.sp)
                    }
                }
                items(recent) { w ->
                    Card(
                        onClick = { onWord(w) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = CardDefaults.cardColors(
                            containerColor = Ink.Surface,
                            contentColor = Ink.Text,
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(w, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                TextButton(
                    onClick = onAbout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextButtonDefaults.textButtonColors(contentColor = Ink.Muted),
                ) {
                    Text("关于 · 数据来源", fontSize = 10.sp)
                }
            }
        }
    }
}
