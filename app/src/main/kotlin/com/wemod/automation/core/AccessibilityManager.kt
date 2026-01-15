package com.wemod.automation.core

import android.util.Log

/**
 * 无障碍服务管理器
 * 提供全局访问无障碍服务的接口
 */
object AccessibilityManager {
    
    private const val TAG = "AccessibilityManager"
    private var serviceInstance: AccessibilityService? = null
    
    /**
     * 设置无障碍服务实例
     * 由 AccessibilityService 在 onCreate 时调用
     */
    fun setService(service: AccessibilityService) {
        serviceInstance = service
        Log.d(TAG, "无障碍服务实例已设置")
    }
    
    /**
     * 清除无障碍服务实例
     * 由 AccessibilityService 在 onDestroy 时调用
     */
    fun clearService() {
        serviceInstance = null
        Log.d(TAG, "无障碍服务实例已清除")
    }
    
    /**
     * 检查无障碍服务是否正在运行
     */
    fun isRunning(): Boolean = serviceInstance != null
    
    /**
     * 执行点击操作
     */
    fun click(x: Int, y: Int): Boolean {
        return serviceInstance?.performClick(x, y) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法执行点击")
            false
        }
    }
    
    /**
     * 执行滑动操作
     */
    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Int = 500): Boolean {
        return serviceInstance?.performSwipe(startX, startY, endX, endY, duration) ?: run {
            Log.w(TAG, "无障碍服务未运行，无法执行滑动")
            false
        }
    }
}