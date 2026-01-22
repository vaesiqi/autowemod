// ===========================================
// 文件：app/src/main/kotlin/com/wemod/automation/ui/viewmodel/MainViewModel.kt
// 修复版：完全兼容 ActionData 模型
// ===========================================

package com.wemod.automation.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wemod.automation.data.repository.ScriptRepository
import com.wemod.automation.model.ActionData
import com.wemod.automation.model.Script
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主ViewModel - 脚本管理
 * 专门负责脚本的CRUD操作
 * 修复：完全使用 ActionData 模型
 */
class MainViewModel(private val context: Context) : ViewModel() {
    
    private val TAG = "MainViewModel"
    
    private val scriptRepository = ScriptRepository(context)
    
    // 脚本列表状态
    private val _scripts = MutableStateFlow<List<Script>>(emptyList())
    val scripts: StateFlow<List<Script>> = _scripts.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 当前选中的脚本
    private val _selectedScript = MutableStateFlow<Script?>(null)
    val selectedScript: StateFlow<Script?> = _selectedScript.asStateFlow()
    
    // 脚本统计
    private val _scriptStats = MutableStateFlow<ScriptStats?>(null)
    val scriptStats: StateFlow<ScriptStats?> = _scriptStats.asStateFlow()
    
    init {
        Log.d(TAG, "MainViewModel初始化")
        loadScripts()
    }
    
    /**
     * 加载所有脚本
     */
    fun loadScripts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                Log.d(TAG, "开始加载脚本...")
                val loadedScripts = scriptRepository.getAllScripts()
                _scripts.value = loadedScripts
                
                Log.d(TAG, "加载完成，共 ${loadedScripts.size} 个脚本")
                
                // 如果没有脚本，创建一个示例
                if (loadedScripts.isEmpty()) {
                    Log.d(TAG, "脚本列表为空，创建示例脚本")
                    createSampleScript()
                } else {
                    // 计算统计信息
                    updateScriptStats()
                }
                
            } catch (e: Exception) {
                val errorMsg = "加载脚本失败: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 创建新脚本
     */
    fun createNewScript(): String {
        Log.d(TAG, "创建新脚本")
        
        return try {
            val newScript = Script.createEmpty()
            val saveResult = scriptRepository.saveScript(newScript)
            
            if (saveResult) {
                Log.d(TAG, "✅ 新脚本创建成功: ${newScript.name}")
                loadScripts() // 重新加载列表
                newScript.id
            } else {
                throw Exception("保存脚本失败")
            }
        } catch (e: Exception) {
            val errorMsg = "创建脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            ""
        }
    }
    
    /**
     * 创建带有默认动作的新脚本
     */
    fun createNewScriptWithActions(name: String = "新脚本"): String {
        Log.d(TAG, "创建带动作的新脚本: $name")
        
        return try {
            val newScript = Script(
                name = name,
                actions = mutableListOf(
                    ActionData.createClick(name = "点击动作", x = 500, y = 500),
                    ActionData.createWait(name = "等待动作", duration = 1000L),
                    ActionData.createSwipe(name = "滑动动作", duration = 500L)
                )
            )
            
            val saveResult = scriptRepository.saveScript(newScript)
            
            if (saveResult) {
                Log.d(TAG, "✅ 带动作的新脚本创建成功: ${newScript.name}")
                loadScripts()
                newScript.id
            } else {
                throw Exception("保存带动作的脚本失败")
            }
        } catch (e: Exception) {
            val errorMsg = "创建带动作脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            ""
        }
    }
    
    /**
     * 创建示例脚本
     */
    fun createSampleScript() {
        viewModelScope.launch {
            Log.d(TAG, "创建示例脚本")
            
            try {
                val sampleScript = createSampleScriptData()
                val saveResult = scriptRepository.saveScript(sampleScript)
                
                if (saveResult) {
                    Log.d(TAG, "✅ 示例脚本创建成功")
                    loadScripts()
                } else {
                    _errorMessage.value = "保存示例脚本失败"
                }
            } catch (e: Exception) {
                val errorMsg = "创建示例脚本失败: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }
    
    /**
     * 创建示例脚本数据
     */
    private fun createSampleScriptData(): Script {
        return Script(
            name = "示例脚本",
            actions = mutableListOf(
                ActionData.createClick(
                    name = "点击屏幕中心",
                    x = 500,
                    y = 500,
                    delayAfter = 1000L
                ),
                ActionData.createWait(
                    name = "等待1秒",
                    duration = 1000L,
                    delayAfter = 500L
                ),
                ActionData.createSwipe(
                    name = "从左到右滑动",
                    startX = 200,
                    startY = 500,
                    endX = 800,
                    endY = 500,
                    duration = 500L,
                    delayAfter = 1000L
                ),
                ActionData.createClick(
                    name = "点击右上角",
                    x = 900,
                    y = 100,
                    delayAfter = 800L
                ),
                ActionData.createWait(
                    name = "等待2秒",
                    duration = 2000L,
                    delayAfter = 0L
                )
            )
        )
    }
    
    /**
     * 删除脚本
     */
    fun deleteScript(scriptId: String) {
        viewModelScope.launch {
            Log.d(TAG, "删除脚本: $scriptId")
            _errorMessage.value = null
            
            try {
                if (scriptRepository.deleteScript(scriptId)) {
                    Log.d(TAG, "✅ 脚本删除成功")
                    loadScripts()
                    
                    // 如果删除的是当前选中的脚本，清空选择
                    if (_selectedScript.value?.id == scriptId) {
                        _selectedScript.value = null
                    }
                    
                    // 更新统计
                    updateScriptStats()
                } else {
                    throw Exception("删除失败")
                }
            } catch (e: Exception) {
                val errorMsg = "删除脚本失败: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }
    
    /**
     * 选择脚本
     */
    fun selectScript(scriptId: String) {
        viewModelScope.launch {
            Log.d(TAG, "选择脚本: $scriptId")
            
            try {
                val script = scriptRepository.loadScript(scriptId)
                if (script != null) {
                    _selectedScript.value = script
                    Log.d(TAG, "✅ 脚本选择成功: ${script.name}")
                } else {
                    _errorMessage.value = "脚本不存在: $scriptId"
                }
            } catch (e: Exception) {
                val errorMsg = "选择脚本失败: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }
    
    /**
     * 更新脚本
     */
    fun updateScript(script: Script): Boolean {
        Log.d(TAG, "更新脚本: ${script.name}")
        
        return try {
            val success = scriptRepository.saveScript(script)
            if (success) {
                Log.d(TAG, "✅ 脚本更新成功")
                loadScripts()
                _selectedScript.value = script
                updateScriptStats()
            } else {
                Log.w(TAG, "❌ 脚本更新失败")
            }
            success
        } catch (e: Exception) {
            val errorMsg = "更新脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            false
        }
    }
    
    /**
     * 导出脚本为JSON字符串
     */
    fun exportScript(scriptId: String): String? {
        Log.d(TAG, "导出脚本: $scriptId")
        
        return try {
            val script = scriptRepository.loadScript(scriptId)
            script?.let {
                val json = scriptRepository.exportScript(it)
                Log.d(TAG, "✅ 脚本导出成功")
                json
            }
        } catch (e: Exception) {
            val errorMsg = "导出脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            null
        }
    }
    
    /**
     * 从JSON字符串导入脚本
     */
    fun importScript(jsonString: String): Boolean {
        Log.d(TAG, "导入脚本")
        
        return try {
            val script = scriptRepository.importScript(jsonString)
            script?.let {
                val saveResult = scriptRepository.saveScript(it)
                if (saveResult) {
                    Log.d(TAG, "✅ 脚本导入成功: ${script.name}")
                    loadScripts()
                    updateScriptStats()
                    true
                } else {
                    false
                }
            } ?: run {
                _errorMessage.value = "导入失败：无效的JSON格式"
                false
            }
        } catch (e: Exception) {
            val errorMsg = "导入脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            false
        }
    }
    
    /**
     * 获取脚本
     */
    fun getScript(scriptId: String): Script? {
        Log.d(TAG, "获取脚本: $scriptId")
        
        return try {
            scriptRepository.loadScript(scriptId)
        } catch (e: Exception) {
            Log.e(TAG, "获取脚本失败: $scriptId", e)
            null
        }
    }
    
    /**
     * 复制脚本
     */
    fun duplicateScript(scriptId: String): String? {
        Log.d(TAG, "复制脚本: $scriptId")
        
        return try {
            val originalScript = scriptRepository.loadScript(scriptId)
            if (originalScript == null) {
                _errorMessage.value = "要复制的脚本不存在"
                return null
            }
            
            // 创建副本
            val duplicateScript = originalScript.copy(
                id = Script.generateId(),
                name = "${originalScript.name} - 副本",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis()
            )
            
            val saveResult = scriptRepository.saveScript(duplicateScript)
            if (saveResult) {
                Log.d(TAG, "✅ 脚本复制成功: ${duplicateScript.name}")
                loadScripts()
                duplicateScript.id
            } else {
                _errorMessage.value = "保存副本失败"
                null
            }
        } catch (e: Exception) {
            val errorMsg = "复制脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            null
        }
    }
    
    /**
     * 重命名脚本
     */
    fun renameScript(scriptId: String, newName: String): Boolean {
        Log.d(TAG, "重命名脚本: $scriptId -> $newName")
        
        return try {
            val script = scriptRepository.loadScript(scriptId)
            if (script == null) {
                _errorMessage.value = "脚本不存在"
                return false
            }
            
            val updatedScript = script.copy(name = newName, modifiedAt = System.currentTimeMillis())
            val saveResult = scriptRepository.saveScript(updatedScript)
            
            if (saveResult) {
                Log.d(TAG, "✅ 脚本重命名成功")
                loadScripts()
                if (_selectedScript.value?.id == scriptId) {
                    _selectedScript.value = updatedScript
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            val errorMsg = "重命名脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            false
        }
    }
    
    /**
     * 获取脚本数量
     */
    val scriptCount: Int
        get() = _scripts.value.size
    
    /**
     * 检查是否有脚本
     */
    val hasScripts: Boolean
        get() = _scripts.value.isNotEmpty()
    
    /**
     * 获取所有脚本名称列表
     */
    val scriptNames: List<String>
        get() = _scripts.value.map { it.name }
    
    /**
     * 获取脚本动作统计
     */
    fun getScriptActionStats(scriptId: String): Map<String, Int>? {
        val script = _scripts.value.find { it.id == scriptId }
        return script?.getActionTypeCounts()
    }
    
    /**
     * 更新脚本统计信息
     */
    private fun updateScriptStats() {
        val allScripts = _scripts.value
        if (allScripts.isEmpty()) {
            _scriptStats.value = null
            return
        }
        
        val totalActions = allScripts.sumOf { it.actions.size }
        val enabledActions = allScripts.sumOf { script ->
            script.actions.count { it.enabled }
        }
        val disabledActions = totalActions - enabledActions
        
        // 计算预估总执行时间
        val estimatedTotalTime = allScripts.sumOf { script ->
            script.actions.sumOf { it.delayAfter }
        }
        
        // 动作类型分布
        val actionTypeCounts = mutableMapOf<String, Int>()
        allScripts.forEach { script ->
            script.actions.forEach { action ->
                actionTypeCounts[action.type] = actionTypeCounts.getOrDefault(action.type, 0) + 1
            }
        }
        
        _scriptStats.value = ScriptStats(
            totalScripts = allScripts.size,
            totalActions = totalActions,
            enabledActions = enabledActions,
            disabledActions = disabledActions,
            estimatedTotalTime = estimatedTotalTime,
            actionTypeDistribution = actionTypeCounts,
            averageActionsPerScript = if (allScripts.isNotEmpty()) totalActions / allScripts.size else 0
        )
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
        Log.d(TAG, "清除错误消息")
    }
    
    /**
     * 脚本统计信息
     */
    data class ScriptStats(
        val totalScripts: Int,
        val totalActions: Int,
        val enabledActions: Int,
        val disabledActions: Int,
        val estimatedTotalTime: Long,
        val actionTypeDistribution: Map<String, Int>,
        val averageActionsPerScript: Int
    )
    
    /**
     * 生成脚本ID（扩展函数）
     */
    private fun Script.Companion.generateId(): String {
        return java.util.UUID.randomUUID().toString()
    }
    
    /**
     * 获取脚本文件路径（用于调试）
     */
    fun getScriptFilePath(scriptId: String): String? {
        return try {
            scriptRepository.getScriptFilePath(scriptId)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 清理资源
     */
    override fun onCleared() {
        Log.d(TAG, "MainViewModel已清理")
        super.onCleared()
    }
}

// ===== 扩展函数（可选） =====

/**
 * 扩展函数：快速获取脚本
 */
fun MainViewModel.getScriptOrNull(scriptId: String): Script? {
    return getScript(scriptId)
}

/**
 * 扩展函数：检查脚本是否存在
 */
fun MainViewModel.scriptExists(scriptId: String): Boolean {
    return getScript(scriptId) != null
}