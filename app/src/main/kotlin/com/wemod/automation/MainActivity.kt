package com.wemod.automation

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.wemod.automation.ui.components.PermissionStatus
import com.wemod.automation.ui.components.PermissionDialog
import com.wemod.automation.ui.theme.WeModTheme
import com.wemod.automation.utils.PermissionUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    WeModTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen()
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // 状态管理
    var showPermissionDialog by remember { mutableStateOf(false) }
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(false) }
    var overlayRunning by remember { mutableStateOf(false) }
    var refreshCounter by remember { mutableStateOf(0) }
    
    // 状态检查函数
    fun checkAllStatus() {
        accessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
        overlayEnabled = PermissionUtils.canDrawOverlays(context)
        overlayRunning = try {
            com.wemod.automation.core.OverlayService.isRunning
        } catch (e: Exception) {
            false
        }
    }
    
    // 自动刷新状态
    LaunchedEffect(Unit) {
        while (true) {
            checkAllStatus()
            delay(1000)
        }
    }
    
    // 当应用回到前台时强制刷新
    DisposableEffect(Unit) {
        val activity = context as? androidx.activity.ComponentActivity
        val lifecycle = activity?.lifecycle
        
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshCounter++
                checkAllStatus()
            }
        }
        
        lifecycle?.addObserver(observer)
        
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 标题
        Text(
            text = "🤖",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "WeMod",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "游戏自动化框架",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // 权限状态卡片
        PermissionStatus(
            accessibilityEnabled = accessibilityEnabled,
            overlayEnabled = overlayEnabled,
            onCheckPermissions = {
                showPermissionDialog = true
                coroutineScope.launch {
                    checkAllStatus()
                }
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 功能按钮
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 权限设置按钮
            Button(
                onClick = { showPermissionDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("权限设置")
            }
            
            // 测试点击按钮
            Button(
                onClick = {
                    if (accessibilityEnabled) {
                        testClick(context)
                    } else {
                        showToast(context, "请先开启无障碍服务")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = accessibilityEnabled
            ) {
                Text("测试点击 (500, 500)")
            }
            
            // 测试滑动按钮
            Button(
                onClick = {
                    if (accessibilityEnabled) {
                        testSwipe(context)
                    } else {
                        showToast(context, "请先开启无障碍服务")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = accessibilityEnabled
            ) {
                Text("测试滑动 (300→700, 500)")
            }
            
            // 悬浮窗按钮
            OutlinedButton(
                onClick = {
                    if (overlayEnabled) {
                        toggleOverlay(context, overlayRunning) { newStatus ->
                            overlayRunning = newStatus
                            refreshCounter++
                        }
                    } else {
                        showToast(context, "请先开启悬浮窗权限")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = overlayEnabled
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 状态指示灯
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (overlayRunning) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = CircleShape
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (overlayRunning) "关闭悬浮窗" else "显示悬浮窗"
                    )
                }
            }
            
            // 手动刷新按钮
            Button(
                onClick = {
                    refreshCounter++
                    checkAllStatus()
                    showToast(context, "已刷新状态")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("手动刷新状态")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 状态显示区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📊 当前状态",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 无障碍状态
                StatusItem(
                    label = "无障碍服务",
                    enabled = accessibilityEnabled
                )
                
                // 悬浮窗权限状态
                StatusItem(
                    label = "悬浮窗权限",
                    enabled = overlayEnabled
                )
                
                // 悬浮窗运行状态
                StatusItem(
                    label = "悬浮窗运行",
                    enabled = overlayRunning
                )
            }
        }
    }
    
    // 权限对话框
    if (showPermissionDialog) {
        PermissionDialog(
            accessibilityEnabled = accessibilityEnabled,
            overlayEnabled = overlayEnabled,
            onDismiss = { showPermissionDialog = false },
            onRequestAccessibility = { PermissionUtils.openAccessibilitySettings(context) },
            onRequestOverlay = { PermissionUtils.openOverlayPermissionSettings(context) }
        )
    }
}

@Composable
fun StatusItem(label: String, enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (enabled) Color(0xFF4CAF50) else Color(0xFFF44336),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (enabled) "正常" else "未启用",
                fontSize = 12.sp,
                color = if (enabled) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

// 测试功能
private fun testClick(context: Context) {
    if (com.wemod.automation.core.AccessibilityManager.isRunning()) {
        val success = com.wemod.automation.core.AccessibilityManager.click(500, 500)
        val message = if (success) "点击测试已发送" else "点击测试失败"
        showToast(context, message)
    } else {
        showToast(context, "无障碍服务未运行，请先开启")
    }
}

private fun testSwipe(context: Context) {
    if (com.wemod.automation.core.AccessibilityManager.isRunning()) {
        val success = com.wemod.automation.core.AccessibilityManager.swipe(300, 500, 700, 500, 500)
        val message = if (success) "滑动测试已发送" else "滑动测试失败"
        showToast(context, message)
    } else {
        showToast(context, "无障碍服务未运行，请先开启")
    }
}

// 悬浮窗控制函数
private fun toggleOverlay(context: Context, isRunning: Boolean, onStatusChanged: (Boolean) -> Unit) {
    try {
        if (isRunning) {
            com.wemod.automation.core.OverlayService.stop(context)
            showToast(context, "悬浮窗已关闭")
            onStatusChanged(false)
        } else {
            com.wemod.automation.core.OverlayService.start(context)
            showToast(context, "悬浮窗已启动")
            onStatusChanged(true)
        }
    } catch (e: Exception) {
        showToast(context, "操作失败: ${e.message}")
    }
}

// 工具函数
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    WeModTheme {
        HomeScreen()
    }
}