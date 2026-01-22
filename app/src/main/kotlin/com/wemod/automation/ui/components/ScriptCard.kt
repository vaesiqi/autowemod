package com.wemod.automation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wemod.automation.model.Script
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background

/**
 * 脚本卡片组件 - 方案C：单行紧凑设计
 * 最大化空间利用，一屏显示更多脚本
 * 
 * @param script 脚本对象
 * @param isAnyScriptRunning 是否有脚本正在运行（包括暂停状态）
 * @param onEdit 编辑按钮点击事件
 * @param onRun 运行按钮点击事件
 * @param onDelete 删除按钮点击事件
 */
@Composable
fun ScriptCard(
    script: Script,
    isAnyScriptRunning: Boolean = false,  // ← 新增参数：运行状态
    onEdit: () -> Unit,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),  // 固定高度，严格控制！
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),  // 无阴影，更扁平
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧：脚本信息（占据主要空间）
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // 脚本名称和动作数量
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 动作数量徽章
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = script.actions.size.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // 脚本名称
                    Text(
                        text = script.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                
                // 更新时间
                Text(
                    text = formatTimeCompact(script.modifiedAt),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 右侧：操作按钮（紧凑图标）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // 运行按钮（仅在脚本有动作且没有其他脚本运行时显示）
                if (script.actions.isNotEmpty()) {
                    IconButton(
                        onClick = onRun,
                        modifier = Modifier.size(32.dp),
                        enabled = !isAnyScriptRunning,  // ← 新增：根据运行状态启用/禁用
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isAnyScriptRunning) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)  // 禁用时的颜色
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Icon(
                            Icons.Outlined.PlayArrow,
                            contentDescription = if (isAnyScriptRunning) "有脚本运行中" else "运行",
                            tint = if (isAnyScriptRunning) 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)  // 禁用时的颜色
                                else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                // 编辑按钮
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // 删除按钮
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 紧凑版时间格式化
 */
private fun formatTimeCompact(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        else -> {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            formatter.format(date)
        }
    }
}