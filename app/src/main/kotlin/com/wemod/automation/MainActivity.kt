package com.wemod.automation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.setContent
import com.wemod.automation.ui.components.PermissionStatus
import com.wemod.automation.ui.components.PermissionDialog
import com.wemod.automation.ui.theme.WeModTheme
import com.wemod.automation.utils.PermissionUtils
import com.wemod.automation.core.AccessibilityManager  // 添加这个导入
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
    
    // 自动刷新权限状态
    LaunchedEffect(Unit) {
        while (true) {
            accessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
            overlayEnabled = PermissionUtils.canDrawOverlays(context)
            delay(2000)
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
                // 立即更新状态
                coroutineScope.launch {
                    accessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
                    overlayEnabled = PermissionUtils.canDrawOverlays(context)
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
            
            // 测试点击按钮（需要无障碍权限）
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
            
            // 测试滑动按钮（需要无障碍权限）
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
            
            // 悬浮窗按钮（需要悬浮窗权限）
            OutlinedButton(
                onClick = {
                    showToast(context, "悬浮窗功能开发中...")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = overlayEnabled
            ) {
                Text("显示悬浮窗")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 提示文本
        Text(
            text = if (accessibilityEnabled && overlayEnabled) 
                "✅ 所有权限已开启，可以开始使用了！"
            else "⚠️ 请先开启所有必要权限",
            fontSize = 14.sp,
            color = if (accessibilityEnabled && overlayEnabled) 
                Color(0xFF4CAF50) 
            else Color(0xFFFF9800),
            textAlign = TextAlign.Center
        )
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

// 测试功能
private fun testClick(context: Context) {
    if (AccessibilityManager.isRunning()) {
        showToast(context, "无障碍服务正在运行，可以执行点击")
        // 暂时注释掉实际点击，先确保编译通过
        // val success = AccessibilityManager.click(500, 500)
        // val message = if (success) "点击测试已发送" else "点击测试失败"
        // showToast(context, message)
    } else {
        showToast(context, "无障碍服务未运行，请先开启")
    }
}

private fun testSwipe(context: Context) {
    if (AccessibilityManager.isRunning()) {
        showToast(context, "无障碍服务正在运行，可以执行滑动")
        //暂时注释掉实际滑动
        val success = AccessibilityManager.swipe(300, 500, 700, 500, 500)
        val message = if (success) "滑动测试已发送" else "滑动测试失败"
        showToast(context, message)
    } else {
        showToast(context, "无障碍服务未运行，请先开启")
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