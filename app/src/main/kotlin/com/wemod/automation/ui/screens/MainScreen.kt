package com.wemod.automation.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wemod.automation.model.Script
import com.wemod.automation.ui.components.*
import com.wemod.automation.ui.viewmodel.UnifiedViewModel
import com.wemod.automation.ui.viewmodel.UnifiedViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCheckPermissions: () -> Unit = {},
    onToggleOverlay: () -> Unit = {},
    onTestClick: () -> Unit = {},
    onTestSwipe: () -> Unit = {},
    onNavigateToEditor: (String) -> Unit
) {
    val context = LocalContext.current
    
    // 正确的ViewModel获取方式
    val viewModel: UnifiedViewModel = viewModel(
        factory = UnifiedViewModelFactory(context)
    )
    
    val coroutineScope = rememberCoroutineScope()
    
    // 侧边栏状态
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    
    // 从UnifiedViewModel观察状态
    val scripts: List<Script> by viewModel.scripts.collectAsState()
    val isLoading: Boolean by viewModel.isLoading.collectAsState()
    val errorMessage: String? by viewModel.anyErrorMessage.collectAsState()
    
    // 权限状态
    val accessibilityEnabled: Boolean by viewModel.accessibilityEnabled.collectAsState()
    val overlayEnabled: Boolean by viewModel.overlayEnabled.collectAsState()
    val overlayRunning: Boolean by viewModel.overlayRunning.collectAsState()
    val shouldShowPermissionWizard: Boolean by viewModel.shouldShowPermissionWizard.collectAsState()
    
    // 脚本运行状态
    val isScriptRunning: Boolean by viewModel.isScriptRunning.collectAsState()
    val isScriptPaused: Boolean by viewModel.isScriptPaused.collectAsState()
    val currentScriptName: String by viewModel.currentScriptName.collectAsState()
    val currentScriptProgress: Int by viewModel.currentScriptProgress.collectAsState()
    
    // 本地状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var scriptToDelete by remember { mutableStateOf<String?>(null) }
    var showPermissionWizard by remember { mutableStateOf(false) }
    
    // 状态指示器显示控制
    var showRunningIndicator by remember { mutableStateOf(false) }
    var scriptFinishedTime by remember { mutableLongStateOf(0L) }
    
    // 检查是否需要显示权限向导
    LaunchedEffect(shouldShowPermissionWizard) {
        if (shouldShowPermissionWizard) {
            showPermissionWizard = true
            viewModel.markPermissionWizardShown()
        }
    }
    
    // 监听脚本运行状态变化
    LaunchedEffect(isScriptRunning, isScriptPaused) {
        if (isScriptRunning || isScriptPaused) {
            // 脚本开始运行或暂停，显示指示器
            showRunningIndicator = true
            scriptFinishedTime = 0L
        } else if (showRunningIndicator) {
            // 脚本完成运行（且不是暂停状态），记录完成时间
            scriptFinishedTime = System.currentTimeMillis()
            
            // 3秒后自动隐藏
            delay(3000)
            if (System.currentTimeMillis() - scriptFinishedTime >= 3000) {
                showRunningIndicator = false
            }
        }
    }
    
    // 侧边栏控制函数
    val openDrawer: () -> Unit = {
        coroutineScope.launch {
            drawerState.open()
        }
    }
    
    val closeDrawer: () -> Unit = {
        coroutineScope.launch {
            drawerState.close()
        }
    }
    
    // 权限相关回调 - 用户手动触发权限向导
    val onManualCheckPermissions: () -> Unit = {
        showPermissionWizard = true
    }
    
    // 测试功能（使用ViewModel中的方法）
    val onViewModelTestClick: () -> Unit = {
        if (accessibilityEnabled) {
            val success = viewModel.runTestScript()
            if (!success) {
                Toast.makeText(context, "点击测试失败，请检查无障碍服务", Toast.LENGTH_SHORT).show()
            }
        } else {
            onCheckPermissions()
        }
    }
    
    val onViewModelTestSwipe: () -> Unit = {
        // 滑动测试目前还是使用原来的方法，因为ScriptRunner中没有专门的滑动测试
        if (accessibilityEnabled) {
            onTestSwipe()
        } else {
            onCheckPermissions()
        }
    }
    
    // 检查是否有脚本正在运行或暂停
    val isAnyScriptRunningOrPaused = viewModel.isAnyScriptRunningOrPaused
    
    // 使用侧边栏包装主内容
SidebarDrawer(
    drawerState = drawerState,
    accessibilityEnabled = accessibilityEnabled,
    overlayEnabled = overlayEnabled,
    overlayRunning = overlayRunning,
    onOpenAccessibilitySettings = {
        closeDrawer()
        // 跳转到无障碍设置
        com.wemod.automation.utils.PermissionUtils.openAccessibilitySettings(context)
    },
    onOpenOverlaySettings = {
        closeDrawer()
        // 跳转到悬浮窗权限设置
        com.wemod.automation.utils.PermissionUtils.openOverlayPermissionSettings(context)
    },
    onToggleOverlay = {
        closeDrawer()
        onToggleOverlay()  // 原来的切换悬浮窗显示/隐藏
    },
    onTestClick = {
        closeDrawer()
        onViewModelTestClick()
    },
    onTestSwipe = {
        closeDrawer()
        onViewModelTestSwipe()
    },
    onOpenSettings = {
        closeDrawer()
        onCheckPermissions()
    }
) {
    // ... 主内容 ..
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // === 顶部固定区域（约40%高度）===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 1. 标题区域（添加菜单按钮）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 菜单按钮
                    IconButton(
                        onClick = openDrawer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "打开侧边栏",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "游戏自动化框架",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "创建和运行自动化脚本",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    // 刷新按钮
                    IconButton(
                        onClick = { viewModel.loadScripts() },
                        enabled = !isLoading,
                        modifier = Modifier.size(40.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 2. 快捷权限请求提示（如果缺少权限）
                if (!accessibilityEnabled || !overlayEnabled) {
                    QuickPermissionRequest(
                        accessibilityEnabled = accessibilityEnabled,
                        overlayEnabled = overlayEnabled,
                        onRequest = {
                            closeDrawer()
                            onCheckPermissions()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 3. 脚本运行状态指示器
                if (showRunningIndicator) {
                    RunningStatusIndicator(
                        scriptName = currentScriptName,
                        progress = currentScriptProgress,
                        isPaused = isScriptPaused,
                        onPauseResume = {
                            if (isScriptPaused) {
                                viewModel.resumeScript()
                            } else {
                                viewModel.pauseScript()
                            }
                        },
                        onStop = { 
                            viewModel.stopScript()
                            showRunningIndicator = false
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 4. 状态卡片（一行两个）- 简化版，点击打开侧边栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusCard(
                        title = "无障碍服务",
                        enabled = accessibilityEnabled,
                        description = if (accessibilityEnabled) "已开启" else "未开启",
                        actionText = "设置",
                        onClick = {
                            openDrawer()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatusCard(
                        title = "悬浮窗",
                        enabled = overlayEnabled,
                        description = if (overlayEnabled) {
                            if (overlayRunning) "显示中" else "已授权"
                        } else {
                            "未授权"
                        },
                        actionText = "控制",
                        onClick = {
                            openDrawer()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 5. 快捷操作提示卡片
                if (!accessibilityEnabled || !overlayEnabled) {
                    Surface(
                        onClick = openDrawer,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "设置",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "权限设置与功能测试",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "点击这里或菜单按钮进行详细设置",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "进入",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 6. 添加一个运行状态提示（如果有脚本在运行）
                if (isAnyScriptRunningOrPaused) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "提示",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isScriptPaused) 
                                    "脚本已暂停，可在上方控制" 
                                else 
                                    "脚本运行中，其他脚本暂停操作",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // 分隔线
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            // === 底部脚本列表区域（约60%高度，可滚动）===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
                // 脚本管理标题和新建按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "我的脚本 (${scripts.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // 新建脚本按钮
                    FilledTonalButton(
                        onClick = {
                            val newScriptId = viewModel.createNewScript()
                            if (newScriptId.isNotEmpty()) {
                                onNavigateToEditor(newScriptId)
                                Toast.makeText(
                                    context, 
                                    "脚本创建成功！", 
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = "新建", 
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("新建", fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 脚本列表（可滚动）
                if (isLoading && scripts.isEmpty()) {
                    // 加载中
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "加载脚本中...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                } else if (scripts.isEmpty()) {
                    // 空状态
                    EmptyScriptsView(
                        onCreateSample = {
                            coroutineScope.launch {
                                viewModel.createSampleScript()
                            }
                        },
                        onCreateNew = {
                            val newScriptId = viewModel.createNewScript()
                            if (newScriptId.isNotEmpty()) {
                                onNavigateToEditor(newScriptId)
                            }
                        }
                    )
                } else {
                    ScriptsListView(
                        scripts = scripts,
                        isAnyScriptRunning = isAnyScriptRunningOrPaused,
                        onEdit = onNavigateToEditor,
                        onRun = { scriptId -> 
                            // 关键修复：检查是否有脚本正在运行或暂停
                            if (isAnyScriptRunningOrPaused) {
                                Toast.makeText(
                                    context,
                                    "请先完成、停止或恢复当前脚本",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@ScriptsListView
                            }
                            
                            val success = viewModel.runScript(scriptId)
                            if (!success) {
                                Toast.makeText(
                                    context,
                                    "运行失败，请检查无障碍服务",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onDelete = { scriptId ->
                            scriptToDelete = scriptId
                            showDeleteDialog = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog && scriptToDelete != null) {
        DeleteConfirmationDialog(
            scriptId = scriptToDelete!!,
            onConfirm = {
                viewModel.deleteScript(it)
                showDeleteDialog = false
                scriptToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                scriptToDelete = null
            }
        )
    }
    
    // 权限向导对话框
    if (showPermissionWizard) {
        PermissionWizard(
            showDialog = showPermissionWizard,
            onDismiss = { 
                showPermissionWizard = false
                viewModel.markPermissionWizardShown()
                viewModel.refreshPermissions()
            },
            onAllGranted = { 
                showPermissionWizard = false
                viewModel.markPermissionWizardShown()
                Toast.makeText(context, "🎉 所有权限已开启！", Toast.LENGTH_SHORT).show()
                viewModel.refreshPermissions()
            }
        )
    }
    
    // 处理错误消息
    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearAllErrors()
        }
    }
}