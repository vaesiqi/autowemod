package com.wemod.automation.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wemod.automation.model.Script
import com.wemod.automation.ui.components.PermissionStatus
import com.wemod.automation.ui.viewmodel.MainViewModel
import com.wemod.automation.utils.PermissionUtils
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onNavigateToEditor: (String) -> Unit,
    onRunScript: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context))
    val coroutineScope = rememberCoroutineScope()
    
    // 从ViewModel观察状态
    val scripts by viewModel.scripts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // 本地状态
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var scriptToDelete by remember { mutableStateOf<String?>(null) }
    
    // 自动刷新权限状态
    LaunchedEffect(Unit) {
        while (true) {
            accessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
            overlayEnabled = PermissionUtils.canDrawOverlays(context)
            kotlinx.coroutines.delay(2000)
        }
    }
    
    // 处理错误消息
    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            // 可以显示Snackbar或Toast
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog && scriptToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                scriptToDelete = null
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个脚本吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scriptToDelete?.let { viewModel.deleteScript(it) }
                        showDeleteDialog = false
                        scriptToDelete = null
                    }
                ) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scriptToDelete = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🤖",
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "游戏自动化框架",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "创建和运行自动化脚本",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // 刷新按钮
            IconButton(
                onClick = { viewModel.loadScripts() },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 权限状态
        PermissionStatus(
            accessibilityEnabled = accessibilityEnabled,
            overlayEnabled = overlayEnabled,
            onCheckPermissions = {
                coroutineScope.launch {
                    accessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
                    overlayEnabled = PermissionUtils.canDrawOverlays(context)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 脚本管理区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的脚本 (${scripts.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = {
                    val newScriptId = viewModel.createNewScript()
                    if (newScriptId.isNotEmpty()) {
                        onNavigateToEditor(newScriptId)
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("新建脚本")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 脚本列表
        if (isLoading && scripts.isEmpty()) {
            // 加载中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (scripts.isEmpty()) {
            // 空状态
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "空",
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "还没有脚本",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "点击上方按钮创建第一个脚本",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 创建示例脚本按钮
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.createSampleScript()
                        }
                    }
                ) {
                    Text("创建示例脚本")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(scripts) { script ->
                    ScriptCard(
                        script = script,
                        onEdit = { onNavigateToEditor(script.id) },
                        onRun = { onRunScript(script.id) },
                        onDelete = {
                            scriptToDelete = script.id
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
        
        // 底部提示
        if (scripts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "共 ${scripts.size} 个脚本",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ScriptCard(
    script: Script,
    onEdit: () -> Unit,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 脚本标题和信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = script.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = "${script.actions.size} 个动作 | 更新于 ${formatTime(script.modifiedAt)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // 动作预览（只显示前3个）
            if (script.actions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    script.actions.take(3).forEachIndexed { index, action ->
                        Text(
                            text = "${index + 1}. ${action.name}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                    if (script.actions.size > 3) {
                        Text(
                            text = "... 还有 ${script.actions.size - 3} 个动作",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "暂无动作",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onRun,
                    modifier = Modifier.weight(1f),
                    enabled = script.actions.isNotEmpty()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "运行", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("运行")
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    // 如果是今天
    val today = java.util.Calendar.getInstance().apply {
        time = java.util.Date(now)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        timestamp >= today -> "今天"
        timestamp >= today - 24 * 60 * 60 * 1000 -> "昨天"
        else -> {
            val formatter = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
            formatter.format(date)
        }
    }
}

// ViewModel Factory
class MainViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}