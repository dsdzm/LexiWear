package com.dsdzm.lexiwear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.Text
import com.dsdzm.lexiwear.R

@Composable
fun SectionCard(
    title: String? = null,
    modifier: Modifier = Modifier,
    accent: Color = Ink.Mint,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Ink.Surface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (title != null) {
            Text(
                title,
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.height(6.dp))
        }
        content()
    }
}

@Composable
fun Badge(text: String, color: Color = Ink.Mint) {
    Text(
        text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
            Icon(
                painterResource(R.drawable.ic_back),
                contentDescription = "返回",
                tint = Ink.Muted,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(title, color = Ink.Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
