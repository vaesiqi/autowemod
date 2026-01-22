// 文件：app/src/main/kotlin/com/wemod/automation/ui/viewmodel/PermissionViewModel.kt
package com.wemod.automation.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wemod.automation.utils.PermissionUtils
import com.wemod.automation.utils.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 权限状态ViewModel
 * 负责管理和监听权限状态变化
 */
class PermissionViewModel(private val context: Context) : ViewModel() {
    
    private val TAG = "PermissionViewModel"
    
    // 权限状态
    private val _accessibilityEnabled = MutableStateFlow(false)
    val accessibilityEnabled: StateFlow<Boolean> = _accessibilityEnabled.asStateFlow()
    
    private val _overlayEnabled = MutableStateFlow(false)
    val overlayEnabled: StateFlow<Boolean> = _overlayEnabled.asStateFlow()
    
    // 悬浮窗运行状态
    private val _overlayRunning = MutableStateFlow(false)
    val overlayRunning: StateFlow<Boolean> = _overlayRunning.asStateFlow()
    
    // 权限向导状态
    private val _shouldShowPermissionWizard = MutableStateFlow(false)
    val shouldShowPermissionWizard: StateFlow<Boolean> = _shouldShowPermissionWizard.asStateFlow()
    
    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 自动刷新控制
    private var isAutoRefreshRunning = true
    
    init {
        // 启动时检查权限
        viewModelScope.launch {
            Log.d(TAG, "PermissionViewModel初始化")
            refreshPermissions()
            
            // 检查是否需要显示权限向导
            val shouldShow = PreferencesManager.shouldShowPermissionWizard(context)
            _shouldShowPermissionWizard.value = shouldShow
            Log.d(TAG, "权限向导显示状态: $shouldShow")
            
            // 启动自动刷新（每2秒）
            startAutoRefresh()
        }
    }
    
    /**
     * 刷新所有权限状态
     */
    fun refreshPermissions() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "刷新权限状态...")
                
                val newAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
                val newOverlay = PermissionUtils.canDrawOverlays(context)
                val newOverlayRunning = try {
                    com.wemod.automation.core.overlays.OverlayContainerService.isRunning
                } catch (e: Exception) {
                    Log.w(TAG, "获取悬浮窗运行状态失败", e)
                    false
                }
                
                Log.d(TAG, "权限状态 - 无障碍: $newAccessibility, 悬浮窗: $newOverlay, 运行中: $newOverlayRunning")
                
                // 只有在状态变化时才更新，避免不必要的重组
                if (newAccessibility != _accessibilityEnabled.value ||
                    newOverlay != _overlayEnabled.value ||
                    newOverlayRunning != _overlayRunning.value) {
                    
                    _accessibilityEnabled.value = newAccessibility
                    _overlayEnabled.value = newOverlay
                    _overlayRunning.value = newOverlayRunning
                    
                    Log.d(TAG, "权限状态已更新")
                }
                
                _errorMessage.value = null
                
            } catch (e: Exception) {
                _errorMessage.value = "刷新权限状态失败: ${e.message}"
                Log.e(TAG, "刷新权限状态失败", e)
            }
        }
    }
    
    /**
     * 启动自动权限状态刷新
     */
    private fun startAutoRefresh() {
        viewModelScope.launch {
            isAutoRefreshRunning = true
            while (isAutoRefreshRunning) {
                delay(2000) // 每2秒刷新一次
                
                try {
                    val newAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
                    val newOverlay = PermissionUtils.canDrawOverlays(context)
                    val newOverlayRunning = try {
                        com.wemod.automation.core.overlays.OverlayContainerService.isRunning
                    } catch (e: Exception) {
                        false
                    }
                    
                    // 只有在状态变化时才更新
                    if (newAccessibility != _accessibilityEnabled.value ||
                        newOverlay != _overlayEnabled.value ||
                        newOverlayRunning != _overlayRunning.value) {
                        
                        _accessibilityEnabled.value = newAccessibility
                        _overlayEnabled.value = newOverlay
                        _overlayRunning.value = newOverlayRunning
                        
                        Log.d(TAG, "自动刷新权限状态")
                    }
                } catch (e: Exception) {
                    // 忽略刷新错误，继续循环
                    Log.w(TAG, "自动刷新权限状态失败", e)
                }
            }
        }
    }
    
    /**
     * 停止自动刷新
     */
    fun stopAutoRefresh() {
        isAutoRefreshRunning = false
        Log.d(TAG, "停止自动刷新")
    }
    
    /**
     * 标记权限向导已显示
     */
    fun markPermissionWizardShown() {
        viewModelScope.launch {
            PreferencesManager.markPermissionWizardShown(context)
            _shouldShowPermissionWizard.value = false
            Log.d(TAG, "权限向导已标记为已显示")
        }
    }
    
    /**
     * 检查是否所有权限都已开启
     */
    val allPermissionsGranted: Boolean
        get() = _accessibilityEnabled.value && _overlayEnabled.value
    
    /**
     * 检查是否缺少权限
     */
    val missingPermissions: List<String>
        get() {
            val missing = mutableListOf<String>()
            if (!_accessibilityEnabled.value) missing.add("无障碍服务")
            if (!_overlayEnabled.value) missing.add("悬浮窗权限")
            return missing
        }
    
    /**
     * 检查是否缺少权限
     */
    fun hasMissingPermissions(): Boolean {
        return !_accessibilityEnabled.value || !_overlayEnabled.value
    }
    
    /**
     * 获取权限状态摘要
     */
    fun getPermissionSummary(): String {
        return buildString {
            append("无障碍: ")
            append(if (_accessibilityEnabled.value) "✓" else "✗")
            append(" | 悬浮窗: ")
            append(if (_overlayEnabled.value) "✓" else "✗")
            if (_overlayEnabled.value) {
                append(" (")
                append(if (_overlayRunning.value) "显示中" else "已停止")
                append(")")
            }
        }
    }
    
    /**
     * 获取权限状态详细描述
     */
    fun getPermissionStatusDetail(): String {
        return when {
            _accessibilityEnabled.value && _overlayEnabled.value -> "所有权限已开启"
            !_accessibilityEnabled.value && !_overlayEnabled.value -> "缺少无障碍服务和悬浮窗权限"
            !_accessibilityEnabled.value -> "缺少无障碍服务权限"
            !_overlayEnabled.value -> "缺少悬浮窗权限"
            else -> "权限状态未知"
        }
    }
    
    /**
     * 强制刷新并等待完成
     */
    suspend fun refreshPermissionsAndWait() {
        refreshPermissions()
        // 等待一小段时间让状态更新
        delay(100)
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
        stopAutoRefresh()
        Log.d(TAG, "PermissionViewModel已清理")
        super.onCleared()
    }
}