package com.example.notecast.presentation.screen.record



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

/**
 * Small shared row used in RecordingScreen's header menu.
 * Placed in the same package as RecordingScreen to avoid import/package mismatch.
 */
@Composable
fun MenuCardRow(
    title: String,
    icon: ImageVector? = null,
    iconTint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                icon?.let {
                    Icon(imageVector = it, contentDescription = null, tint = iconTint, modifier = Modifier.size(iconSize))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(text = title, color = iconTint, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}