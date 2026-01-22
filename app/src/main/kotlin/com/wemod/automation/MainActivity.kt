// 在文件顶部添加导入
package com.wemod.automation

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.wemod.automation.core.errors.AppError
import com.wemod.automation.di.initializeKoin
import com.wemod.automation.ui.components.PermissionWizard
import com.wemod.automation.ui.screens.MainScreen
import com.wemod.automation.ui.theme.WeModTheme
import com.wemod.automation.utils.PermissionUtils
import com.wemod.automation.utils.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import android.util.Log

// 新增：Koin ViewModel委托导入
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.wemod.automation.ui.viewmodel.UnifiedViewModel

class MainActivity : ComponentActivity() {
    
    // 使用Koin获取ViewModel（简化方式）
    private val viewModel: UnifiedViewModel by viewModel()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化Koin（第一次运行）
        try {
            initializeKoin(this)
            Log.d("MainActivity", "Koin初始化成功")
        } catch (e: Exception) {
            Log.w("MainActivity", "Koin可能已初始化: ${e.message}")
        }
        
    // 初始化动作注册表
    com.wemod.automation.core.actions.ActionRegistry.registerBuiltInActions()
    
    

        // 设置透明状态栏和导航栏（原有代码不变）
        window.apply {
            statusBarColor = android.graphics.Color.TRANSPARENT
            navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decorView.systemUiVisibility = decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
        
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    val view = LocalView.current
    val context = LocalContext.current
    
    // 设置系统栏颜色（原有代码不变）
    SideEffect {
        val window = (context as? ComponentActivity)?.window ?: return@SideEffect
        
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            view.systemUiVisibility = view.systemUiVisibility and
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            view.systemUiVisibility = view.systemUiVisibility and
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    
    // 强制使用暗色主题
    WeModTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UnifiedMainScreen()
        }
    }
}

/**
 * 统一的主界面 - 使用Koin获取ViewModel
 */
@Composable
fun UnifiedMainScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    
    // 获取ViewModel（两种方式兼容）
    // 方式1：使用传统viewModel()函数（需要传入factory）
    // val viewModel: UnifiedViewModel = viewModel(
    //     factory = UnifiedViewModelFactory(context)
    // )
    
    // 方式2：使用Koin（推荐）
    // 注意：在@Composable中直接使用Koin需要额外设置
    // 暂时使用传统方式保持兼容
    
    val viewModel: UnifiedViewModel = remember {
        com.wemod.automation.ui.viewmodel.UnifiedViewModel(context)
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    // 权限向导状态
    var showPermissionWizard by remember { mutableStateOf(false) }
    
    // 首次启动检查
    LaunchedEffect(Unit) {
        delay(500)
        
        val shouldShowWizard = PreferencesManager.shouldShowPermissionWizard(context)
        Log.d("MainActivity", "应该显示权限向导: $shouldShowWizard")
        
        if (shouldShowWizard) {
            showPermissionWizard = true
            PreferencesManager.markPermissionWizardShown(context)
            Log.d("MainActivity", "已标记权限向导为已显示")
        }
    }
    
    // 脚本编辑器导航
    val onNavigateToEditor: (String) -> Unit = { scriptId ->
        Toast.makeText(context, "编辑器开发中 (脚本ID: $scriptId)", Toast.LENGTH_SHORT).show()
    }
    
    // 悬浮窗控制
    val onToggleOverlay: () -> Unit = {
        toggleOverlay(context)
    }
    
  
    
    // 权限相关回调
    val onCheckPermissions: () -> Unit = {
        Log.d("MainActivity", "用户手动触发权限向导")
        showPermissionWizard = true
    }
    
    // 错误处理
    LaunchedEffect(Unit) {
        // 监听错误（未来集成错误处理系统）
    }
    
    // 主界面容器
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // 调用MainScreen
        MainScreen(
            onCheckPermissions = onCheckPermissions,
            onToggleOverlay = onToggleOverlay,
            onNavigateToEditor = onNavigateToEditor
        )
    }
    
    // 权限向导对话框
    if (showPermissionWizard) {
        PermissionWizard(
            showDialog = showPermissionWizard,
            onDismiss = { 
                Log.d("MainActivity", "用户关闭权限向导")
                showPermissionWizard = false
                PreferencesManager.markPermissionWizardShown(context)
            },
            onAllGranted = { 
                Log.d("MainActivity", "权限向导完成，所有权限已开启")
                showPermissionWizard = false
                PreferencesManager.markPermissionWizardShown(context)
                Toast.makeText(context, "🎉 所有权限已开启！", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    // 确保系统栏适配
    DisposableEffect(Unit) {
        val window = (context as? ComponentActivity)?.window
        
        onDispose {
            window?.let {
                WindowCompat.setDecorFitsSystemWindows(it, true)
            }
        }
    }
}


/**
 * 切换悬浮窗
 */
// 在 toggleOverlay 函数中：
private fun toggleOverlay(context: Context) {
    Log.d("MainActivity", "切换悬浮窗")
    
    try {
        // 使用新的容器服务
        if (com.wemod.automation.core.overlays.OverlayContainerService.isRunning) {
            Log.d("MainActivity", "停止悬浮窗服务")
            com.wemod.automation.core.overlays.OverlayContainerService.stop(context)
            Toast.makeText(context, "悬浮窗已关闭", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("MainActivity", "启动悬浮窗服务")
            com.wemod.automation.core.overlays.OverlayContainerService.start(context)
            Toast.makeText(context, "悬浮窗已启动", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        val errorMsg = "操作失败: ${e.message}"
        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        Log.e("MainActivity", errorMsg, e)
    }
}

@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    WeModTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "游戏自动化框架",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "重构版本 - 配置中心已集成",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Koin依赖注入",
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
                                                color = Color(0xFF4CAF50),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "已集成",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}