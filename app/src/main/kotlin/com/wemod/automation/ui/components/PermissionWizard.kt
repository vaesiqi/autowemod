package com.wemod.automation.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wemod.automation.utils.PermissionRequester
import com.wemod.automation.utils.PermissionUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 权限申请向导对话框 - 完整版（修复按钮显示问题）
 */
@Composable
fun PermissionWizard(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAllGranted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // 向导状态
    var currentStep by remember { mutableStateOf(1) }
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var autoCheck by remember { mutableStateOf(true) }
    
    // 当对话框显示时，检查当前权限状态
    LaunchedEffect(showDialog) {
        if (showDialog) {
            accessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
            overlayEnabled = PermissionUtils.canDrawOverlays(context)
            
            // 根据当前权限状态决定初始步骤
            when {
                !overlayEnabled -> currentStep = 1
                !accessibilityEnabled -> currentStep = 2
                else -> {
                    onAllGranted()
                    return@LaunchedEffect
                }
            }
        }
    }
    
    // 自动检查权限状态
    LaunchedEffect(currentStep, showDialog, autoCheck) {
        if (showDialog && autoCheck) {
            while (showDialog && autoCheck) {
                val newOverlay = PermissionUtils.canDrawOverlays(context)
                val newAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
                
                if (newOverlay != overlayEnabled || newAccessibility != accessibilityEnabled) {
                    overlayEnabled = newOverlay
                    accessibilityEnabled = newAccessibility
                    
                    when (currentStep) {
                        1 -> if (overlayEnabled) {
                            currentStep = 2
                            delay(500)
                        }
                        2 -> if (accessibilityEnabled) {
                            onAllGranted()
                            autoCheck = false
                        }
                    }
                }
                delay(1500)
            }
        }
    }
    
    if (showDialog) {
        Dialog(
            onDismissRequest = {
                if (!isChecking) {
                    onDismiss()
                }
            }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题区域
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "权限",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Text(
                        text = "权限设置向导",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    
                    Text(
                        text = "请按顺序开启以下权限以使用完整功能",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // 步骤指示器
                    StepIndicator(currentStep = currentStep)
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // 当前步骤内容
                    when (currentStep) {
                        1 -> OverlayPermissionStep(
                            isGranted = overlayEnabled,
                            onRequest = { PermissionRequester.requestOverlayPermission(context) }
                        )
                        2 -> AccessibilityPermissionStep(
                            isGranted = accessibilityEnabled,
                            onRequest = { PermissionRequester.requestAccessibilityPermission(context) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 底部按钮 - 修复显示问题
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 上一步按钮
                        if (currentStep == 2) {
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .height(48.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "上一步",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("上一步", fontSize = 14.sp)
                                }
                            }
                        } else {
                            // 占位空间
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        // 下一步/完成按钮
                        when (currentStep) {
                            1 -> Button(
                                onClick = {
                                    if (overlayEnabled) {
                                        currentStep = 2
                                    } else {
                                        PermissionRequester.requestOverlayPermission(context)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (overlayEnabled) MaterialTheme.colorScheme.primary 
                                                   else Color(0xFFFF9800)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (overlayEnabled) {
                                        Text("下一步", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    } else {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "设置",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("去开启", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            
                            2 -> Button(
                                onClick = {
                                    if (accessibilityEnabled) {
                                        onAllGranted()
                                    } else {
                                        PermissionRequester.requestAccessibilityPermission(context)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (accessibilityEnabled) Color(0xFF4CAF50) 
                                                   else Color(0xFFFF9800)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (accessibilityEnabled) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "完成",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("完成", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    } else {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "设置",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("去开启", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                    
                    // 跳过/稍后按钮
                    if (currentStep == 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("稍后设置", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ... 保持 StepIndicator, StepCircle, StepConnector, OverlayPermissionStep, 
// AccessibilityPermissionStep, InstructionItem, SuccessCard 函数不变 ...
// 只需要修改上面的按钮部分
/**
 * 步骤指示器
 */
@Composable
private fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 步骤1：悬浮窗权限
        StepCircle(
            stepNumber = 1,
            label = "悬浮窗",
            isActive = currentStep == 1,
            isCompleted = currentStep > 1
        )
        
        StepConnector(isActive = currentStep >= 2)
        
        // 步骤2：无障碍服务
        StepCircle(
            stepNumber = 2,
            label = "无障碍",
            isActive = currentStep == 2,
            isCompleted = currentStep > 2
        )
    }
}

@Composable
private fun StepCircle(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = when {
                        isCompleted -> Color(0xFF4CAF50)
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                )
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "已完成",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive || isCompleted) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepConnector(isActive: Boolean) {
    Box(
        modifier = Modifier
            .height(2.dp)
            .width(40.dp)
            .background(
                color = if (isActive) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.surfaceVariant
            )
    )
}

/**
 * 悬浮窗权限步骤
 */
@Composable
private fun OverlayPermissionStep(
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 图标
        Icon(
            Icons.Default.Apps,
            contentDescription = "悬浮窗",
            modifier = Modifier.size(72.dp),
            tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 标题
        Text(
            text = if (isGranted) "✅ 悬浮窗权限已开启" else "1. 悬浮窗权限",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 描述
        Text(
            text = if (isGranted) {
                "很好！悬浮窗权限已成功开启\n现在可以在其他应用上层显示控制面板了"
            } else {
                "允许应用在其他应用上层显示控制面板\n这是实现便捷操作的关键权限"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (!isGranted) {
            // 操作指南卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "操作指南",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    InstructionItem(
                        number = 1,
                        text = "点击下方\"去开启\"按钮"
                    )
                    
                    InstructionItem(
                        number = 2,
                        text = "在设置页面中找到本应用"
                    )
                    
                    InstructionItem(
                        number = 3,
                        text = "开启\"允许显示在其他应用上层\"开关"
                    )
                    
                    InstructionItem(
                        number = 4,
                        text = "返回应用继续下一步"
                    )
                }
            }
        } else {
            // 成功状态卡片
            SuccessCard(
                title = "权限已开启",
                message = "悬浮窗权限设置完成"
            )
        }
    }
}

/**
 * 无障碍服务步骤
 */
@Composable
private fun AccessibilityPermissionStep(
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 图标
        Icon(
            Icons.Default.Accessibility,
            contentDescription = "无障碍",
            modifier = Modifier.size(72.dp),
            tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 标题
        Text(
            text = if (isGranted) "✅ 无障碍服务已开启" else "2. 无障碍服务",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 描述
        Text(
            text = if (isGranted) {
                "完美！无障碍服务已成功开启\n现在可以使用自动化点击和滑动功能了"
            } else {
                "允许应用模拟点击和滑动操作\n这是实现自动化功能的核心服务"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (!isGranted) {
            // 操作指南卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "操作指南",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    InstructionItem(
                        number = 1,
                        text = "点击下方\"去开启\"按钮"
                    )
                    
                    InstructionItem(
                        number = 2,
                        text = "在无障碍设置中找到\"WeMod自动化服务\""
                    )
                    
                    InstructionItem(
                        number = 3,
                        text = "开启服务开关"
                    )
                    
                    InstructionItem(
                        number = 4,
                        text = "返回应用完成设置"
                    )
                }
            }
        } else {
            // 成功状态卡片
            SuccessCard(
                title = "服务已开启",
                message = "无障碍服务设置完成"
            )
        }
    }
}

/**
 * 操作步骤项
 */
@Composable
private fun InstructionItem(number: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 成功状态卡片
 */
@Composable
private fun SuccessCard(title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "成功",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                )
            }
        }
    }
}


