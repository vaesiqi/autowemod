// 文件：app/src/main/kotlin/com/wemod/automation/ui/components/RunningStatusIndicator.kt
package com.wemod.automation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember  // 🎯 添加这行！
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RunningStatusIndicator(
    scriptName: String,
    progress: Int,
    isPaused: Boolean = false,
    onPauseResume: () -> Unit = {},
    onStop: () -> Unit
) {
    // 🎯 修复：使用正确的remember函数
    val displayProgress = androidx.compose.runtime.remember(progress) {
        when {
            progress < 0 -> 0
            progress > 100 -> 100
            else -> progress
        }
    }
    
    // 🎯 修复：简化进度计算
    val progressValue = androidx.compose.runtime.remember(displayProgress) {
        val value = displayProgress.toFloat() / 100f
        // 即使进度为0也显示微小进度（1%），避免完全空白
        if (value <= 0f) 0.01f else value.coerceAtMost(1f)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaused) 
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 第一行：状态信息和进度
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态图标
                if (isPaused) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "已暂停",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 脚本信息和进度
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPaused) "⏸️ 已暂停: $scriptName" 
                               else "▶️ 正在运行: $scriptName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isPaused) 
                            MaterialTheme.colorScheme.secondary 
                            else MaterialTheme.colorScheme.primary
                    )
                    
                    // 🎯 关键修复：始终显示进度条
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isPaused) 
                            MaterialTheme.colorScheme.secondary 
                            else MaterialTheme.colorScheme.primary,
                        trackColor = if (isPaused)
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
                
                // 进度百分比 - 始终显示
                Text(
                    text = "$displayProgress%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaused) 
                        MaterialTheme.colorScheme.secondary 
                        else MaterialTheme.colorScheme.primary
                )
            }
            
            // 第二行：控制按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 暂停/继续按钮
                Button(
                    onClick = onPauseResume,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) 
                            MaterialTheme.colorScheme.secondary 
                            else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow 
                                          else Icons.Default.Pause,
                            contentDescription = if (isPaused) "继续" else "暂停",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isPaused) "继续" else "暂停",
                            fontSize = 13.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 停止按钮
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "停止",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("停止", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}