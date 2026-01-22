package com.wemod.automation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp    
@Composable
fun PermissionSettingCard(
    accessibilityEnabled: Boolean,
    overlayEnabled: Boolean,
    onClick: () -> Unit
) {
    val allEnabled = accessibilityEnabled && overlayEnabled
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (allEnabled) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else Color(0xFFFF9800).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "权限设置",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (allEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else Color(0xFFFF9800)
                )
                Text(
                    text = if (allEnabled) 
                        "所有权限已开启 ✓" 
                    else {
                        val missing = listOf(
                            if (!accessibilityEnabled) "无障碍" else null,
                            if (!overlayEnabled) "悬浮窗" else null
                        ).filterNotNull()
                        "需要开启: ${missing.joinToString("、")}"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "权限设置",
                tint = if (allEnabled) 
                    MaterialTheme.colorScheme.primary 
                else Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}