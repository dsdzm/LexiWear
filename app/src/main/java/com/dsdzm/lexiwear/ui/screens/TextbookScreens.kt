package com.dsdzm.lexiwear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.dsdzm.lexiwear.data.LexiRepository
import com.dsdzm.lexiwear.data.TbBook
import com.dsdzm.lexiwear.data.TbBookBrief
import com.dsdzm.lexiwear.data.TbVersion
import com.dsdzm.lexiwear.ui.Badge
import com.dsdzm.lexiwear.ui.Ink
import com.dsdzm.lexiwear.ui.TopBar

@Composable
private fun ListCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null,
) {
    Card(
        onClick = onClick,
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
                    title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    color = Ink.Muted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (badge != null) {
                Spacer(Modifier.width(6.dp))
                Badge(badge, Ink.Amber)
            }
            Text("›", color = Ink.Muted, fontSize = 16.sp)
        }
    }
}

@Composable
fun VersionsScreen(
    repo: LexiRepository,
    onBack: () -> Unit,
    onOpen: (TbVersion) -> Unit,
) {
    var versions by remember { mutableStateOf<List<TbVersion>>(emptyList()) }
    LaunchedEffect(Unit) { versions = repo.versions() }

    AppScaffold(
        timeText = { TimeText() },
        containerColor = Ink.Bg,
        contentColor = Ink.Text,
    ) {
        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item { TopBar("教材词表", onBack) }
            item {
                Column {
                    Text("现行最新版", color = Ink.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${versions.size} 个版本 · 按版本 / 年级 / 单元",
                        color = Ink.Muted,
                        fontSize = 9.5.sp,
                    )
                }
            }
            items(versions) { v ->
                val units = v.books.sumOf { it.units }
                ListCard(
                    title = v.name,
                    subtitle = "${v.group} · ${v.books.size} 册 · $units 单元",
                    onClick = { onOpen(v) },
                )
            }
        }
    }
}

@Composable
fun BooksScreen(
    repo: LexiRepository,
    versionId: String,
    versionName: String,
    onBack: () -> Unit,
    onOpen: (TbBookBrief) -> Unit,
) {
    var version by remember(versionId) { mutableStateOf<TbVersion?>(null) }
    LaunchedEffect(versionId) {
        version = repo.versions().firstOrNull { it.id == versionId }
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
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item { TopBar("选择册次", onBack) }
            item {
                Column {
                    Text(
                        versionName,
                        color = Ink.Text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "必修 / 选择性必修 · 按年级编排",
                        color = Ink.Muted,
                        fontSize = 9.sp,
                    )
                }
            }
            items(version?.books ?: emptyList()) { b ->
                ListCard(
                    title = b.name,
                    subtitle = "${b.units} 个单元",
                    badge = b.grade,
                    onClick = { onOpen(b) },
                )
            }
        }
    }
}

@Composable
fun UnitsScreen(
    repo: LexiRepository,
    bookId: String,
    bookName: String,
    onBack: () -> Unit,
    onOpen: (Int, String) -> Unit,
) {
    var book by remember(bookId) { mutableStateOf<TbBook?>(null) }
    LaunchedEffect(bookId) { book = repo.book(bookId) }

    AppScaffold(
        timeText = { TimeText() },
        containerColor = Ink.Bg,
        contentColor = Ink.Text,
    ) {
        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item { TopBar("选择单元", onBack) }
            item {
                Column {
                    Text(
                        bookName,
                        color = Ink.Text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        book?.let { "${it.units.size} 个单元 · 共 ${it.units.sumOf { u -> u.words.size }} 词" }
                            ?: "载入中…",
                        color = Ink.Muted,
                        fontSize = 9.sp,
                    )
                }
            }
            itemsIndexed(book?.units ?: emptyList()) { index, u ->
                ListCard(
                    title = u.name,
                    subtitle = "${u.words.size} 个词条",
                    onClick = { onOpen(index, u.name) },
                )
            }
        }
    }
}

@Composable
fun UnitWordsScreen(
    repo: LexiRepository,
    bookId: String,
    unitIndex: Int,
    title: String,
    onBack: () -> Unit,
    onWord: (String) -> Unit,
) {
    var book by remember(bookId) { mutableStateOf<TbBook?>(null) }
    LaunchedEffect(bookId) { book = repo.book(bookId) }
    val unit = book?.units?.getOrNull(unitIndex)

    AppScaffold(
        timeText = { TimeText() },
        containerColor = Ink.Bg,
        contentColor = Ink.Text,
    ) {
        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            item { TopBar("单元词表", onBack) }
            item {
                Column {
                    Text(
                        title,
                        color = Ink.Text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        book?.let { "${it.version} · ${it.name} · ${unit?.words?.size ?: 0} 词" }
                            ?: "载入中…",
                        color = Ink.Muted,
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                    )
                }
            }
            items(unit?.words ?: emptyList()) { w ->
                Card(
                    onClick = { onWord(w.word) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Ink.Surface,
                        contentColor = Ink.Text,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(w.word, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            if (w.meaning.isNotBlank()) {
                                Text(
                                    w.meaning,
                                    color = Ink.Muted,
                                    fontSize = 8.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (w.pos.isNotBlank()) Badge(w.pos, Ink.Amber)
                    }
                }
            }
            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    "词条顺序与教材词汇表一致",
                    color = Ink.Muted,
                    fontSize = 8.5.sp,
                )
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}
