// 完整修复版：兼容 ActionData 和新的 ScriptRunner
// ===========================================

package com.wemod.automation.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wemod.automation.core.ScriptRunner
import com.wemod.automation.core.accessibility.EnhancedAccessibilityManager
import com.wemod.automation.data.repository.ScriptRepository
import com.wemod.automation.model.Script
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 脚本运行控制ViewModel
 * 专门负责脚本的执行、暂停、恢复、停止
 * 修复：完全使用 ActionData 模型和新的 ScriptRunner
 */
class RunViewModel(private val context: Context) : ViewModel() {
    
    private val TAG = "RunViewModel"
    
    private val scriptRepository = ScriptRepository(context)
    
    // 🎯 关键修复：使用新的 ScriptRunner（需要 Context）
    private val scriptRunner: ScriptRunner by lazy {
        ScriptRunner(context)
    }
    
    // 运行状态
    private val _isScriptRunning = MutableStateFlow(false)
    val isScriptRunning: StateFlow<Boolean> = _isScriptRunning.asStateFlow()
    
    // 暂停状态
    private val _isScriptPaused = MutableStateFlow(false)
    val isScriptPaused: StateFlow<Boolean> = _isScriptPaused.asStateFlow()
    
    // 当前脚本进度
    private val _currentScriptProgress = MutableStateFlow(0)
    val currentScriptProgress: StateFlow<Int> = _currentScriptProgress.asStateFlow()
    
    // 当前脚本名称
    private val _currentScriptName = MutableStateFlow("")
    val currentScriptName: StateFlow<String> = _currentScriptName.asStateFlow()
    
    // 当前脚本对象
    private val _currentScript = MutableStateFlow<Script?>(null)
    val currentScript: StateFlow<Script?> = _currentScript.asStateFlow()
    
    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 运行状态文本
    private val _runStatusText = MutableStateFlow("空闲")
    val runStatusText: StateFlow<String> = _runStatusText.asStateFlow()
    
    // 执行统计
    private val _executionStats = MutableStateFlow<ExecutionStats?>(null)
    val executionStats: StateFlow<ExecutionStats?> = _executionStats.asStateFlow()
    
    // 预估剩余时间
    private val _estimatedRemainingTime = MutableStateFlow(0L)
    val estimatedRemainingTime: StateFlow<Long> = _estimatedRemainingTime.asStateFlow()
    
    init {
        Log.d(TAG, "RunViewModel初始化")
        setupScriptRunnerListener()
    }
    
    /**
     * 设置脚本运行器监听
     */
    private fun setupScriptRunnerListener() {
        viewModelScope.launch {
            // 🎯 监听进度百分比
            launch {
                scriptRunner.getProgressPercentageFlow().collect { percentage ->
                    Log.d(TAG, "进度百分比更新: $percentage%")
                    _currentScriptProgress.value = percentage
                    
                    // 更新预估剩余时间
                    updateEstimatedRemainingTime()
                }
            }
            
            // 监听引擎状态
            launch {
                scriptRunner.getEngineState().collect { engineState ->
                    Log.d(TAG, "引擎状态变化: $engineState")
                    
                    // 更新运行状态
                    _isScriptRunning.value = scriptRunner.isRunning()
                    
                    // 更新暂停状态
                    _isScriptPaused.value = scriptRunner.isPaused()
                    
                    // 更新当前脚本信息
                    updateCurrentScriptInfo()
                    
                    // 更新状态文本
                    updateStatusText(engineState)
                    
                    // 更新执行统计
                    updateExecutionStats()
                    
                    // 如果状态变为空闲或错误，清理当前脚本
                    if (engineState == com.wemod.automation.core.ActionEngine.EngineState.IDLE ||
                        engineState == com.wemod.automation.core.ActionEngine.EngineState.ERROR) {
                        cleanupOnCompletion()
                    }
                }
            }
        }
    }
    
    /**
     * 更新当前脚本信息
     */
    private fun updateCurrentScriptInfo() {
        val scriptName = scriptRunner.getCurrentScriptName()
        if (scriptName != _currentScriptName.value) {
            _currentScriptName.value = scriptName
            Log.d(TAG, "脚本名称更新: $scriptName")
        }
        
        val script = scriptRunner.getCurrentScript()
        if (script != null && script.id != _currentScript.value?.id) {
            _currentScript.value = script
            Log.d(TAG, "脚本对象更新: ${script.name}")
        }
    }
    
    /**
     * 更新状态文本
     */
    private fun updateStatusText(engineState: com.wemod.automation.core.ActionEngine.EngineState) {
        val newStatus = when (engineState) {
            com.wemod.automation.core.ActionEngine.EngineState.IDLE -> "空闲"
            com.wemod.automation.core.ActionEngine.EngineState.RUNNING -> "运行中"
            com.wemod.automation.core.ActionEngine.EngineState.PAUSED -> "已暂停"
            com.wemod.automation.core.ActionEngine.EngineState.STOPPED -> "已停止"
            com.wemod.automation.core.ActionEngine.EngineState.ERROR -> "错误"
        }
        
        if (newStatus != _runStatusText.value) {
            _runStatusText.value = newStatus
            Log.d(TAG, "状态文本更新: $newStatus")
        }
    }
    
    /**
     * 运行脚本
     */
    fun runScript(scriptId: String, accessibilityEnabled: Boolean): Boolean {
        Log.d(TAG, "运行脚本: $scriptId")
        _errorMessage.value = null
        
        // 检查是否有脚本在运行或暂停
        if (_isScriptRunning.value || _isScriptPaused.value) {
            _errorMessage.value = "已有脚本在运行中，请先停止或恢复当前脚本"
            return false
        }
        
        // 检查无障碍服务
        if (!accessibilityEnabled) {
            _errorMessage.value = "请先开启无障碍服务"
            return false
        }
        
        // 检查无障碍管理器是否就绪
        if (!EnhancedAccessibilityManager.isRunning()) {
            _errorMessage.value = "无障碍服务未就绪，请检查服务状态"
            return false
        }
        
        val script = scriptRepository.loadScript(scriptId)
        if (script == null) {
            _errorMessage.value = "脚本不存在"
            return false
        }
        
        if (script.actions.isEmpty()) {
            _errorMessage.value = "脚本为空，无法运行"
            return false
        }
        
        // 验证脚本动作
        val invalidActions = script.actions.filterNot { it.isValid() }
        if (invalidActions.isNotEmpty()) {
            _errorMessage.value = "脚本中有 ${invalidActions.size} 个无效动作"
            return false
        }
        
        // 保存当前脚本
        _currentScript.value = script
        Log.d(TAG, "开始运行脚本: ${script.name}, 动作数: ${script.actions.size}")
        
        // 重置预估时间
        _estimatedRemainingTime.value = 0L
        
        return try {
            val success = scriptRunner.runScript(script)
            if (!success) {
                _errorMessage.value = "启动脚本失败，请检查无障碍服务是否开启"
                _currentScript.value = null
                Log.w(TAG, "脚本启动失败")
            } else {
                Log.d(TAG, "✅ 脚本启动成功")
                
                // 初始预估时间
                updateEstimatedRemainingTime()
            }
            success
        } catch (e: Exception) {
            val errorMsg = "运行脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            _currentScript.value = null
            Log.e(TAG, errorMsg, e)
            false
        }
    }
    
    /**
     * 运行测试脚本
     */
    fun runTestScript(accessibilityEnabled: Boolean): Boolean {
        Log.d(TAG, "运行测试脚本")
        _errorMessage.value = null
        
        if (!accessibilityEnabled) {
            _errorMessage.value = "请先开启无障碍服务"
            return false
        }
        
        if (!EnhancedAccessibilityManager.isRunning()) {
            _errorMessage.value = "无障碍服务未就绪"
            return false
        }
        
        return try {
            val success = scriptRunner.runTestScript()
            if (!success) {
                _errorMessage.value = "测试脚本运行失败，请检查无障碍服务"
                Log.w(TAG, "测试脚本运行失败")
            } else {
                Log.d(TAG, "✅ 测试脚本启动成功")
                
                // 重置预估时间
                _estimatedRemainingTime.value = 0L
            }
            success
        } catch (e: Exception) {
            val errorMsg = "运行测试脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            false
        }
    }
    
    /**
     * 暂停脚本
     */
    fun pauseScript(): Boolean {
        Log.d(TAG, "暂停脚本")
        _errorMessage.value = null
        
        if (!_isScriptRunning.value) {
            _errorMessage.value = "没有脚本在运行，无法暂停"
            return false
        }
        
        return try {
            val success = scriptRunner.pause()
            if (success) {
                Log.d(TAG, "✅ 脚本暂停成功")
            } else {
                _errorMessage.value = "暂停脚本失败"
                Log.w(TAG, "暂停脚本失败")
            }
            success
        } catch (e: Exception) {
            val errorMsg = "暂停脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            false
        }
    }
    
    /**
     * 恢复脚本
     */
    fun resumeScript(): Boolean {
        Log.d(TAG, "恢复脚本")
        _errorMessage.value = null
        
        if (!_isScriptPaused.value) {
            _errorMessage.value = "没有暂停的脚本，无法恢复"
            return false
        }
        
        return try {
            val success = scriptRunner.resume()
            if (success) {
                Log.d(TAG, "✅ 脚本恢复成功")
            } else {
                _errorMessage.value = "恢复脚本失败"
                Log.w(TAG, "恢复脚本失败")
            }
            success
        } catch (e: Exception) {
            val errorMsg = "恢复脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            false
        }
    }
    
    /**
     * 停止脚本
     */
    fun stopScript(): Boolean {
        Log.d(TAG, "停止脚本")
        _errorMessage.value = null
        
        if (!_isScriptRunning.value && !_isScriptPaused.value) {
            _errorMessage.value = "没有运行中的脚本，无法停止"
            return false
        }
        
        return try {
            val success = scriptRunner.stop()
            if (success) {
                Log.d(TAG, "✅ 脚本停止成功")
                cleanupOnStop()
            } else {
                _errorMessage.value = "停止脚本失败"
                Log.w(TAG, "停止脚本失败")
            }
            success
        } catch (e: Exception) {
            val errorMsg = "停止脚本失败: ${e.message}"
            _errorMessage.value = errorMsg
            Log.e(TAG, errorMsg, e)
            false
        }
    }
    
    /**
     * 停止时清理
     */
    private fun cleanupOnStop() {
        _currentScript.value = null
        _currentScriptName.value = ""
        _currentScriptProgress.value = 0
        _estimatedRemainingTime.value = 0L
        _executionStats.value = null
    }
    
    /**
     * 完成时清理
     */
    private fun cleanupOnCompletion() {
        // 只在真正完成时清理（不是暂停状态）
        if (!_isScriptPaused.value) {
            _currentScript.value = null
            _currentScriptName.value = ""
            _estimatedRemainingTime.value = 0L
            Log.d(TAG, "脚本执行完成，清理状态")
        }
    }
    
    /**
     * 更新预估剩余时间
     */
    private fun updateEstimatedRemainingTime() {
        val remainingTime = scriptRunner.estimateRemainingTime()
        if (remainingTime != _estimatedRemainingTime.value) {
            _estimatedRemainingTime.value = remainingTime
            Log.d(TAG, "预估剩余时间更新: ${remainingTime}ms")
        }
    }
    
    /**
     * 更新执行统计
     */
    private fun updateExecutionStats() {
        val stats = scriptRunner.getExecutionStats()
        _executionStats.value = ExecutionStats(
            startTime = stats.startTime,
            totalTime = stats.totalTime,
            successCount = stats.successCount,
            failureCount = stats.failureCount,
            totalActions = stats.totalActions,
            successRate = stats.successRate,
            currentState = stats.currentState
        )
    }
    
    /**
     * 获取运行状态摘要
     */
    fun getRunSummary(): String {
        return if (_isScriptRunning.value || _isScriptPaused.value) {
            "${_runStatusText.value}: ${_currentScriptName.value} (${_currentScriptProgress.value}%)"
        } else {
            _runStatusText.value
        }
    }
    
    /**
     * 检查是否有脚本正在运行或暂停
     */
    val isAnyScriptRunningOrPaused: Boolean
        get() = _isScriptRunning.value || _isScriptPaused.value
    
    /**
     * 获取当前运行状态详细描述
     */
    fun getRunStatusDetail(): String {
        val status = when {
            _isScriptRunning.value -> "运行中"
            _isScriptPaused.value -> "已暂停"
            else -> "空闲"
        }
        
        return if (_currentScript.value != null) {
            "$status: ${_currentScript.value?.name} (${_currentScriptProgress.value}%)"
        } else {
            status
        }
    }
    
    /**
     * 获取当前脚本ID
     */
    fun getCurrentScriptId(): String? {
        return _currentScript.value?.id
    }
    
    /**
     * 获取进度信息
     */
    fun getProgressInfo(): ProgressInfo {
        val current = _currentScriptProgress.value
        val total = 100  // 百分比
        
        return ProgressInfo(
            current = current,
            total = total,
            percentage = current,
            formatted = "$current%"
        )
    }
    
    /**
     * 格式化剩余时间
     */
    fun formatRemainingTime(): String {
        val seconds = _estimatedRemainingTime.value / 1000
        val minutes = seconds / 60
        
        return if (minutes > 0) {
            "${minutes}分${seconds % 60}秒"
        } else if (seconds > 0) {
            "${seconds}秒"
        } else {
            "计算中..."
        }
    }
    
    /**
     * 强制停止所有脚本（紧急停止）
     */
    fun forceStopAllScripts(): Boolean {
        Log.w(TAG, "强制停止所有脚本")
        
        return try {
            scriptRunner.stop()
            scriptRunner.cleanup()
            
            // 重置所有状态
            _isScriptRunning.value = false
            _isScriptPaused.value = false
            _currentScriptProgress.value = 0
            _currentScriptName.value = ""
            _currentScript.value = null
            _estimatedRemainingTime.value = 0L
            _executionStats.value = null
            _runStatusText.value = "已强制停止"
            
            Log.d(TAG, "✅ 所有脚本已强制停止")
            true
        } catch (e: Exception) {
            Log.e(TAG, "强制停止失败", e)
            false
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
        Log.d(TAG, "清除错误消息")
    }
    
    /**
     * 清理资源
     */
    override fun onCleared() {
        Log.d(TAG, "RunViewModel清理中...")
        
        // 停止所有正在运行的脚本
        if (_isScriptRunning.value || _isScriptPaused.value) {
            Log.d(TAG, "清理时发现运行中的脚本，尝试停止")
            try {
                scriptRunner.stop()
            } catch (e: Exception) {
                Log.e(TAG, "清理时停止脚本失败", e)
            }
        }
        
        // 清理 ScriptRunner 资源
        try {
            scriptRunner.cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "清理 ScriptRunner 失败", e)
        }
        
        Log.d(TAG, "RunViewModel已清理")
        super.onCleared()
    }
    
    // ===== 数据类 =====
    
    /**
     * 执行统计信息
     */
    data class ExecutionStats(
        val startTime: Long,
        val totalTime: Long,
        val successCount: Int,
        val failureCount: Int,
        val totalActions: Int,
        val successRate: Int,
        val currentState: com.wemod.automation.core.ActionEngine.EngineState
    ) {
        fun getFormattedTime(): String {
            val seconds = totalTime / 1000
            val minutes = seconds / 60
            
            return if (minutes > 0) {
                "${minutes}分${seconds % 60}秒"
            } else {
                "${seconds}秒"
            }
        }
    }
    
    /**
     * 进度信息
     */
    data class ProgressInfo(
        val current: Int,
        val total: Int,
        val percentage: Int,
        val formatted: String
    )
}
