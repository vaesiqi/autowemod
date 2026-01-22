package com.wemod.automation.ui.components

import androidx.compose.foundation.background  // 添加这行
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight  // 添加这行
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp  // 添加这行

/**
 * 状态卡片组件
 * 用于显示权限状态、服务状态等信息
 * 
 * @param title 卡片标题（如"无障碍服务"）
 * @param enabled 是否已启用
 * @param description 描述文字
 * @param actionText 操作按钮文字（如"去开启"、"已显示"）
 * @param onClick 点击事件
 * @param modifier 修饰符
 */
@Composable
fun StatusCard(
    title: String,
    enabled: Boolean,
    description: String,
    actionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ... 函数体保持不变 ...
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // 状态指示器和标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (enabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 描述文字
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 操作按钮区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = if (enabled) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            Color(0xFFFF9800).copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionText,
                    fontSize = 13.sp,
                    color = if (enabled) MaterialTheme.colorScheme.primary else Color(0xFFFF9800),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}