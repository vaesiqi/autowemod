package com.wemod.automation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wemod.automation.model.Script

@Composable
fun ScriptsListView(
    scripts: List<Script>,
    isAnyScriptRunning: Boolean = false,  // ← 新增参数：运行状态
    onEdit: (String) -> Unit,
    onRun: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(scripts) { script ->
            ScriptCard(
                script = script,
                isAnyScriptRunning = isAnyScriptRunning,  // ← 传递运行状态
                onEdit = { onEdit(script.id) },
                onRun = { onRun(script.id) },
                onDelete = { onDelete(script.id) }
            )
        }
        
        // 底部留白和信息
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "共 ${scripts.size} 个脚本",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                // 如果有脚本运行中，显示提示
                if (isAnyScriptRunning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "有脚本运行中，其他脚本暂停运行",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}