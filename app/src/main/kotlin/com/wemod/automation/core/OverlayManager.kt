package com.wemod.automation.core

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

/**
 * 悬浮窗管理器
 * 提供悬浮窗的集中管理功能
 */
object OverlayManager {
    
    /**
     * 检查悬浮窗权限
     */
    fun checkPermission(context: Context): Boolean {
        return OverlayService.hasPermission(context)
    }
    
    /**
     * 请求悬浮窗权限
     */
    fun requestPermission(context: Context) {
        OverlayService.start(context)
    }
    
    /**
     * 切换悬浮窗状态
     */
    fun toggleOverlay(context: Context) {
        if (OverlayService.isRunning) {
            stopOverlay(context)
        } else {
            startOverlay(context)
        }
    }
    
    /**
     * 启动悬浮窗
     */
    fun startOverlay(context: Context) {
        if (!checkPermission(context)) {
            Toast.makeText(context, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            return
        }
        
        OverlayService.start(context)
    }
    
    /**
     * 停止悬浮窗
     */
    fun stopOverlay(context: Context) {
        OverlayService.stop(context)
    }
    
    /**
     * 获取悬浮窗状态
     */
    fun getStatus(): String {
        return if (OverlayService.isRunning) "运行中" else "已停止"
    }
}