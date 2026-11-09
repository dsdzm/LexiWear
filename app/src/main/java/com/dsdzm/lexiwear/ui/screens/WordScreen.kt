package com.dsdzm.lexiwear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.dsdzm.lexiwear.data.LexiRepository
import com.dsdzm.lexiwear.data.WordEntry
import com.dsdzm.lexiwear.ui.Badge
import com.dsdzm.lexiwear.ui.Ink
import com.dsdzm.lexiwear.ui.SectionCard
import com.dsdzm.lexiwear.ui.TopBar

@Composable
fun WordScreen(
    repo: LexiRepository,
    word: String,
    onBack: () -> Unit,
    onWord: (String) -> Unit,
) {
    var entry by remember(word) { mutableStateOf<WordEntry?>(null) }
    var loading by remember(word) { mutableStateOf(true) }

    LaunchedEffect(word) {
        loading = true
        entry = repo.word(word)
        loading = false
        if (entry != null) repo.addRecent(entry!!.word)
    }

    AppScaffold(
        timeText = { TimeText() },
        containerColor = Ink.Bg,
        contentColor = Ink.Text,
    ) {
        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 24.dp),
            autoCentering = null,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item { TopBar("词典", onBack) }

            when {
                loading -> item {
                    Text("查询中…", color = Ink.Muted, fontSize = 11.sp)
                }

                entry == null -> {
                    item {
                        Text(word, color = Ink.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    item {
                        Text(
                            "词库暂未收录该词条。\n可尝试搜索词根或改用教材词表浏览。",
                            color = Ink.Muted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        )
                    }
                }

                else -> {
                    val e = entry!!
                    item {
                        Column {
                            Text(
                                e.word,
                                color = Ink.Text,
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                            )
                            val phones = buildList {
                                if (e.uk.isNotBlank()) add("英 /${e.uk}/")
                                if (e.us.isNotBlank()) add("美 /${e.us}/")
                            }
                            if (phones.isNotEmpty()) {
                                Text(
                                    phones.joinToString("   "),
                                    color = Ink.Muted,
                                    fontSize = 10.sp,
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (e.pos.isNotBlank()) Badge(e.pos, Ink.Amber)
                                if (e.occurrences.isNotEmpty()) Badge("教材词", Ink.Mint)
                            }
                        }
                    }

                    if (e.senses.isNotEmpty()) {
                        item {
                            ListHeader { Text("释义", color = Ink.Mint, fontSize = 10.sp) }
                        }
                        items(e.senses) { sense ->
                            SectionCard {
                                Text(
                                    sense,
                                    color = Ink.Text,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                )
                            }
                        }
                    }

                    if (e.examples.isNotEmpty()) {
                        item {
                            ListHeader { Text("用法 · 例句", color = Ink.Mint, fontSize = 10.sp) }
                        }
                        items(e.examples) { ex ->
                            SectionCard {
                                Text(
                                    ex.en,
                                    color = Ink.Text,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                )
                                if (ex.zh.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        ex.zh,
                                        color = Ink.Muted,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                    )
                                }
                            }
                        }
                    }

                    if (e.collocations.isNotEmpty()) {
                        item {
                            ListHeader { Text("教材搭配", color = Ink.Amber, fontSize = 10.sp) }
                        }
                        items(e.collocations) { c ->
                            SectionCard(accent = Ink.Amber) {
                                Text(c.phrase, color = Ink.Text, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                if (c.meaning.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(c.meaning, color = Ink.Muted, fontSize = 9.5.sp, lineHeight = 13.sp)
                                }
                            }
                        }
                    }

                    if (e.forms.isNotEmpty()) {
                        item {
                            ListHeader { Text("词形变化", color = Ink.Muted, fontSize = 10.sp) }
                        }
                        item {
                            SectionCard {
                                Text(
                                    e.forms.joinToString("   ·   ") { "${it.name} ${it.value}" },
                                    color = Ink.Text,
                                    fontSize = 10.5.sp,
                                    lineHeight = 15.sp,
                                )
                            }
                        }
                    }

                    val rel = e.rel.distinctBy { it.word.lowercase() }
                    if (rel.isNotEmpty()) {
                        item {
                            ListHeader { Text("派生词 / 同根词", color = Ink.Mint, fontSize = 10.sp) }
                        }
                        items(rel) { r ->
                            Card(
                                onClick = { onWord(r.word) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Ink.Surface,
                                    contentColor = Ink.Text,
                                ),
                                contentPadding = PaddingValues(horizontal = 13.dp, vertical = 8.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                r.word,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            if (r.pos.isNotBlank()) {
                                                Spacer(Modifier.width(6.dp))
                                                Badge(r.pos, Ink.Amber)
                                            }
                                        }
                                        if (r.tran.isNotBlank()) {
                                            Text(
                                                r.tran,
                                                color = Ink.Muted,
                                                fontSize = 9.5.sp,
                                                lineHeight = 13.sp,
                                                maxLines = 2,
                                            )
                                        }
                                    }
                                    Text("›", color = Ink.Muted, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    if (e.occurrences.isNotEmpty()) {
                        item {
                            ListHeader { Text("教材出现位置", color = Ink.Muted, fontSize = 10.sp) }
                        }
                        item {
                            SectionCard {
                                e.occurrences.forEachIndexed { i, o ->
                                    if (i > 0) Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${o.version} · ${o.book} · ${o.unit}",
                                        color = Ink.Muted,
                                        fontSize = 9.5.sp,
                                        lineHeight = 13.sp,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "释义与例句由词典数据自动匹配，仅供学习参考；教材义项以课本为准。",
                            color = Ink.Muted,
                            fontSize = 8.5.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
