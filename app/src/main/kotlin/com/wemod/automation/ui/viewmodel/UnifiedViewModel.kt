// 文件：app/src/main/kotlin/com/wemod/automation/ui/viewmodel/UnifiedViewModel.kt
package com.wemod.automation.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wemod.automation.model.Script
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 统一ViewModel - 协调所有ViewModel
 * 为MainScreen提供统一的数据接口
 */
class UnifiedViewModel(context: Context) : ViewModel() {
    
    // 子ViewModel
    private val mainViewModel = MainViewModel(context)
    private val permissionViewModel = PermissionViewModel(context)
    private val runViewModel = RunViewModel(context)
    
    // 公开子ViewModel的状态（委托模式）
    
    // 脚本相关
    val scripts: StateFlow<List<Script>> = mainViewModel.scripts
    val isLoading: StateFlow<Boolean> = mainViewModel.isLoading
    val selectedScript: StateFlow<Script?> = mainViewModel.selectedScript
    val scriptCount: Int
        get() = mainViewModel.scriptCount
    val hasScripts: Boolean
        get() = mainViewModel.hasScripts
    
    // 权限相关
    val accessibilityEnabled: StateFlow<Boolean> = permissionViewModel.accessibilityEnabled
    val overlayEnabled: StateFlow<Boolean> = permissionViewModel.overlayEnabled
    val overlayRunning: StateFlow<Boolean> = permissionViewModel.overlayRunning
    val shouldShowPermissionWizard: StateFlow<Boolean> = permissionViewModel.shouldShowPermissionWizard
    val allPermissionsGranted: Boolean
        get() = permissionViewModel.allPermissionsGranted
    val missingPermissions: List<String>
        get() = permissionViewModel.missingPermissions
    val permissionErrorMessage: StateFlow<String?> = permissionViewModel.errorMessage
    
    // 运行相关
    val isScriptRunning: StateFlow<Boolean> = runViewModel.isScriptRunning
    val isScriptPaused: StateFlow<Boolean> = runViewModel.isScriptPaused
    val currentScriptProgress: StateFlow<Int> = runViewModel.currentScriptProgress
    val currentScriptName: StateFlow<String> = runViewModel.currentScriptName
    val currentScript: StateFlow<Script?> = runViewModel.currentScript
    val runStatusText: StateFlow<String> = runViewModel.runStatusText
    val isAnyScriptRunningOrPaused: Boolean
        get() = runViewModel.isAnyScriptRunningOrPaused
    val runErrorMessage: StateFlow<String?> = runViewModel.errorMessage
    
    // 错误消息（合并所有错误）- 使用显式类型并转换为 StateFlow
    val anyErrorMessage: StateFlow<String?> = combine(
        mainViewModel.errorMessage,
        permissionViewModel.errorMessage,
        runViewModel.errorMessage
    ) { mainError: String?, permissionError: String?, runError: String? ->
        mainError ?: permissionError ?: runError
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    init {
        // 在这里初始化一些跨ViewModel的逻辑
        viewModelScope.launch {
            // 当权限变化时，如果无障碍服务关闭但脚本在运行，需要停止脚本
            permissionViewModel.accessibilityEnabled.collect { enabled ->
                if (!enabled && (isScriptRunning.value || isScriptPaused.value)) {
                    stopScript()
                }
            }
        }
    }
    
    // === 代理方法 ===
    
    // 脚本管理
    fun loadScripts() = mainViewModel.loadScripts()
    fun createNewScript(): String = mainViewModel.createNewScript()
    fun createSampleScript() = mainViewModel.createSampleScript()
    fun deleteScript(scriptId: String) = mainViewModel.deleteScript(scriptId)
    fun selectScript(scriptId: String) = mainViewModel.selectScript(scriptId)
    fun updateScript(script: Script): Boolean = mainViewModel.updateScript(script)
    fun getScript(scriptId: String): Script? = mainViewModel.getScript(scriptId)
    
    // 权限管理
    fun refreshPermissions() = permissionViewModel.refreshPermissions()
    fun markPermissionWizardShown() = permissionViewModel.markPermissionWizardShown()
    
    // 运行控制
    fun runScript(scriptId: String): Boolean {
        return runViewModel.runScript(scriptId, permissionViewModel.accessibilityEnabled.value)
    }
    
    fun runTestScript(): Boolean {
        return runViewModel.runTestScript(permissionViewModel.accessibilityEnabled.value)
    }
    
    fun pauseScript(): Boolean = runViewModel.pauseScript()
    fun resumeScript(): Boolean = runViewModel.resumeScript()
    fun stopScript(): Boolean = runViewModel.stopScript()
    fun getRunSummary(): String = runViewModel.getRunSummary()
    fun getRunStatusDetail(): String = runViewModel.getRunStatusDetail()
    fun getCurrentScriptId(): String? = runViewModel.getCurrentScriptId()
    
    // 错误处理
    fun clearAllErrors() {
        mainViewModel.clearError()
        permissionViewModel.clearError()
        runViewModel.clearError()
    }
    
    // 清理资源
    override fun onCleared() {
        // 子ViewModel会自己清理（如果有需要的话）
        super.onCleared()
    }
}

/**
 * UnifiedViewModel的工厂类
 */
class UnifiedViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UnifiedViewModel::class.java)) {
            return UnifiedViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}