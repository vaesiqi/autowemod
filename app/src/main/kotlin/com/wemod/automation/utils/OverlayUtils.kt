package com.wemod.automation.utils

import android.content.Context
import android.widget.Toast
import com.wemod.automation.core.OverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 悬浮窗工具类 - 安全版本
 */
object OverlayUtils {
    
    /**
     * 安全地切换悬浮窗状态
     */
    fun safeToggleOverlay(context: Context) {
        if (!PermissionUtils.canDrawOverlays(context)) {
            Toast.makeText(context, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            if (OverlayService.isRunning) {
                // 安全停止：先通知UI，再停止服务
                Toast.makeText(context, "正在关闭悬浮窗...", Toast.LENGTH_SHORT).show()
                
                // 使用协程延迟停止，避免阻塞UI
                CoroutineScope(Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(100) // 给UI更新一点时间
                    OverlayService.stop(context)
                }
            } else {
                // 启动悬浮窗
                OverlayService.start(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 检查悬浮窗状态（带错误处理）
     */
    fun checkOverlayStatus(): Boolean {
        return try {
            OverlayService.isRunning
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 安全地启动悬浮窗
     */
    fun safeStartOverlay(context: Context): Boolean {
        return try {
            OverlayService.start(context)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 安全地停止悬浮窗
     */
    fun safeStopOverlay(context: Context): Boolean {
        return try {
            OverlayService.stop(context)
            true
        } catch (e: Exception) {
            false
        }
    }
}