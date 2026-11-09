package com.dsdzm.lexiwear.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.dsdzm.lexiwear.data.LexiRepository
import com.dsdzm.lexiwear.data.Meta
import com.dsdzm.lexiwear.ui.Ink
import com.dsdzm.lexiwear.ui.SectionCard
import com.dsdzm.lexiwear.ui.TopBar

@Composable
fun AboutScreen(repo: LexiRepository, onBack: () -> Unit) {
    var meta by remember { mutableStateOf<Meta?>(null) }
    LaunchedEffect(Unit) { meta = repo.meta() }

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
            item { TopBar("关于", onBack) }
            item {
                Column {
                    Text("袖典 LexiWear", color = Ink.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("v1.0.0 · Wear OS 离线词典", color = Ink.Muted, fontSize = 9.5.sp)
                }
            }
            item {
                SectionCard(title = "词库规模") {
                    Text(
                        buildString {
                            meta?.let {
                                append("词条 ${it.entries}\n")
                                append("教材 ${it.versions} 个版本 / ${it.books} 册 / ${it.units} 单元\n")
                                append("数据构建于 ${it.builtAt}")
                            } ?: append("载入中…")
                        },
                        color = Ink.Text,
                        fontSize = 10.5.sp,
                        lineHeight = 16.sp,
                    )
                }
            }
            item {
                SectionCard(title = "数据来源") {
                    Text(
                        "教材单词表：公开整理的现行高中英语教材词表（word.threeher.cn，CC BY 4.0）。\n" +
                            "音标、释义、例句、派生词：有道词典公开接口，构建时离线打包。\n" +
                            "应用不联网，全部数据内置于安装包。",
                        color = Ink.Muted,
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                    )
                }
            }
            item {
                SectionCard(title = "使用提示", accent = Ink.Amber) {
                    Text(
                        "1. 手表输入不便，可用系统语音键盘输入。\n" +
                            "2. 词条释义为通用词典义项，可能与课本该单元语境义略有出入。\n" +
                            "3. 未收录的人名、地名等专有名词会提示“暂未收录”。",
                        color = Ink.Muted,
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                    )
                }
            }
            item {
                ListHeader { Text("版权", color = Ink.Muted, fontSize = 9.sp) }
            }
            item {
                Text(
                    "非官方学习工具。教材名称与词汇表版权归各出版社所有，仅供个人学习使用，不得用于商业发行。",
                    color = Ink.Muted,
                    fontSize = 8.5.sp,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}
