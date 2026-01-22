// 新建文件：app/src/main/kotlin/com/wemod/automation/core/accessibility/EnhancedAccessibilityManager.kt
// ===========================================

package com.wemod.automation.core.accessibility

import android.content.Context
import android.util.Log

/**
 * 增强版无障碍服务管理器
 * 统一管理 EnhancedAccessibilityService
 */
object EnhancedAccessibilityManager {
    
    private const val TAG = "EnhancedAccessibilityManager"
    
    // 服务实例引用
    private var serviceInstance: EnhancedAccessibilityService? = null
    
    /**
     * 设置服务实例
     */
    fun setService(service: EnhancedAccessibilityService) {
        serviceInstance = service
        Log.d(TAG, "增强无障碍服务实例已设置")
    }
    
    /**
     * 清除服务实例
     */
    fun clearService() {
        serviceInstance = null
        Log.d(TAG, "增强无障碍服务实例已清除")
    }
    
    /**
     * 检查服务是否正在运行
     */
    fun isRunning(): Boolean {
        return serviceInstance != null
    }
    
    /**
     * 执行点击操作
     */
    fun click(x: Int, y: Int, duration: Long = 50): Boolean {
        return serviceInstance?.performClick(x, y, duration) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法执行点击")
            false
        }
    }
    
    /**
     * 执行滑动操作
     */
    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 500): Boolean {
        return serviceInstance?.performSwipe(startX, startY, endX, endY, duration) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法执行滑动")
            false
        }
    }
    
    /**
     * 执行长按操作
     */
    fun longClick(x: Int, y: Int, duration: Long = 1000): Boolean {
        return serviceInstance?.performLongClick(x, y, duration) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法执行长按")
            false
        }
    }
    
    /**
     * 查找并点击节点
     */
    fun findAndClick(text: String? = null, description: String? = null): Boolean {
        return serviceInstance?.findAndClick(text, description) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法查找点击节点")
            false
        }
    }
    
    /**
     * 查找并输入文本
     */
    fun findAndInput(text: String, targetText: String? = null): Boolean {
        return serviceInstance?.findAndInput(text, targetText) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法输入文本")
            false
        }
    }
    
    /**
     * 滚动查找文本
     */
    fun scrollToFind(text: String, maxScrolls: Int = 10): Boolean {
        return serviceInstance?.scrollToFind(text, maxScrolls) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法滚动查找")
            false
        }
    }
    
    /**
     * 获取当前窗口信息
     */
    fun getCurrentWindowInfo(): EnhancedAccessibilityService.WindowInfo? {
        return serviceInstance?.getCurrentWindowInfo()
    }
    
    /**
     * 执行全局操作（返回、主页等）
     */
    fun performGlobalAction(action: EnhancedAccessibilityService.GlobalAction): Boolean {
        return serviceInstance?.performGlobalAction(action) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法执行全局操作")
            false
        }
    }
    
    /**
     * 获取服务状态
     */
    fun getServiceState(): EnhancedAccessibilityService.ServiceState? {
        return serviceInstance?.serviceState?.value
    }
    
    /**
     * 获取服务是否已连接
     */
    fun isConnected(): Boolean {
        val state = getServiceState()
        return state == EnhancedAccessibilityService.ServiceState.CONNECTED
    }
}