package com.example.notecast.presentation.ui.common_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecast.presentation.theme.BluePurple

@Composable
fun PermissionGridItem(label: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionWrapper(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Row (verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon, contentDescription = null, tint = Color(0xFF673AB7),
                modifier = Modifier.size(30.dp)
            )
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
        }
        Spacer(Modifier.height(5.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE1F5FE).copy(alpha = 0.5f)) // Màu xanh nhạt theo hình
                .padding(8.dp),
            content = content
        )
    }
}

@Composable
fun PermissionSwitchItem(title: String, subtitle: String, checked: Boolean, icon: ImageVector, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, tint = BluePurple, contentDescription = null, modifier = Modifier.size(35.dp).padding(4.dp))
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF673AB7))
        )
    }
}

@Composable
fun SecurityInfoCard(title: String, subtitle: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2E7D32))
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}