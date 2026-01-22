package com.wemod.automation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 快捷权限请求组件 - 修复按钮显示问题
 */
@Composable
fun QuickPermissionRequest(
    accessibilityEnabled: Boolean,
    overlayEnabled: Boolean,
    onRequest: () -> Unit
) {
    val missingPermissions = mutableListOf<String>()
    if (!overlayEnabled) missingPermissions.add("悬浮窗")
    if (!accessibilityEnabled) missingPermissions.add("无障碍服务")
    
    val missingCount = missingPermissions.size
    
    if (missingCount > 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "缺少${missingCount}个权限",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "缺少: ${missingPermissions.joinToString("、")}",
                        fontSize = 12.sp,
                        color = Color(0xFFFF9800).copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "点击开启权限以使用完整功能",
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800).copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 修复按钮：设置最小宽度和合适的内边距
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 100.dp, max = 140.dp),  // 设置宽度范围
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Security, 
                            contentDescription = "权限", 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "开启权限", 
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}