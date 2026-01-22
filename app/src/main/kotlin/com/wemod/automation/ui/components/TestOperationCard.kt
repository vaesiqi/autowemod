package com.wemod.automation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp  // ← 添加这行
@Composable
fun TestOperationCard(
    accessibilityEnabled: Boolean,
    onTestClick: () -> Unit,
    onTestSwipe: () -> Unit,
    onCheckPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "功能测试",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 测试点击按钮
                Button(
                    onClick = {
                        if (accessibilityEnabled) {
                            onTestClick()
                        } else {
                            onCheckPermissions()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.TouchApp,
                            contentDescription = "点击测试",
                            modifier = Modifier.size(18.dp)
                        )
                        Text("测试点击", fontSize = 13.sp)
                    }
                }
                
                // 测试滑动按钮
                Button(
                    onClick = {
                        if (accessibilityEnabled) {
                            onTestSwipe()
                        } else {
                            onCheckPermissions()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "滑动测试",
                            modifier = Modifier.size(18.dp)
                        )
                        Text("测试滑动", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}