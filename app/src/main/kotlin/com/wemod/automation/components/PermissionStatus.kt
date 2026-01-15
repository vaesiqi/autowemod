package com.wemod.automation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp  // 添加这个导入

@Composable
fun PermissionStatus(
    accessibilityEnabled: Boolean = false,
    overlayEnabled: Boolean = false,
    onCheckPermissions: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "权限状态",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 无障碍服务状态
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = if (accessibilityEnabled) "✅" else "❌",
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "无障碍服务: ${if (accessibilityEnabled) "已开启" else "未开启"}",
                    color = if (accessibilityEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            // 悬浮窗权限状态
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = if (overlayEnabled) "✅" else "❌",
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "悬浮窗权限: ${if (overlayEnabled) "已授予" else "未授予"}",
                    color = if (overlayEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            // 检查按钮
            Button(
                onClick = onCheckPermissions,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (accessibilityEnabled && overlayEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else Color(0xFFFF9800)
                )
            ) {
                Text("检查权限状态")
            }
        }
    }
}

@Composable
fun PermissionDialog(
    accessibilityEnabled: Boolean,
    overlayEnabled: Boolean,
    onDismiss: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("权限设置") },
        text = {
            Column {
                PermissionItem(
                    title = "无障碍服务",
                    description = "用于自动化点击和滑动",
                    enabled = accessibilityEnabled,
                    onRequest = onRequestAccessibility
                )
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                PermissionItem(
                    title = "悬浮窗权限",
                    description = "用于显示控制悬浮窗",
                    enabled = overlayEnabled,
                    onRequest = onRequestOverlay
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    enabled: Boolean,
    onRequest: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = if (enabled) "✅ 已开启" else "❌ 未开启",
                color = if (enabled) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontSize = 14.sp  // 使用 sp 单位
            )
        }
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        
        if (!enabled) {
            Button(
                onClick = onRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("去开启")
            }
        }
    }
}