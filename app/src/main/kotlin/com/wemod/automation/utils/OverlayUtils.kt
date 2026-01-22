// 更新 app/src/main/kotlin/com/wemod/automation/utils/OverlayUtils.kt
package com.wemod.automation.utils

import android.content.Context
import android.widget.Toast
import com.wemod.automation.core.overlays.OverlayContainerService
import com.wemod.automation.core.overlays.OverlayType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 悬浮窗工具类 - 新版本，使用容器服务
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
            if (OverlayContainerService.isRunning) {
                // 停止服务
                Toast.makeText(context, "正在关闭悬浮窗...", Toast.LENGTH_SHORT).show()
                
                CoroutineScope(Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(100)
                    OverlayContainerService.stop(context)
                }
            } else {
                // 启动服务
                OverlayContainerService.start(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 创建特定类型的悬浮窗
     */
    fun createOverlay(
        context: Context,
        type: OverlayType,
        instanceId: String? = null,
        x: Int = 100,
        y: Int = 100,
        width: Int? = null,
        height: Int? = null,
        data: Map<String, Any> = emptyMap()
    ): String? {
        if (!OverlayContainerService.isRunning) {
            Toast.makeText(context, "请先启动悬浮窗服务", Toast.LENGTH_SHORT).show()
            return null
        }
        
        return OverlayContainerService.instance?.createOverlay(
            type = type,
            instanceId = instanceId,
            x = x,
            y = y,
            width = width,
            height = height,
            data = data
        )
    }
    
    /**
     * 销毁悬浮窗
     */
    fun destroyOverlay(context: Context, instanceId: String): Boolean {
        return OverlayContainerService.instance?.destroyOverlay(instanceId) ?: false
    }
    
    /**
     * 更新悬浮窗内容
     */
    fun updateOverlay(context: Context, instanceId: String, data: Map<String, Any>): Boolean {
        return OverlayContainerService.instance?.updateOverlay(instanceId, data) ?: false
    }
    
    /**
     * 显示运行指示器
     */
    fun showRunningIndicator(
        context: Context,
        scriptName: String = "",
        progress: Int = 0,
        status: String = "空闲"
    ): String? {
        return createOverlay(
            context = context,
            type = OverlayType.RUNNING_INDICATOR,
            instanceId = "running_indicator",
            x = 50,
            y = 50,
            width = 300,
            height = 140,
            data = mapOf(
                "scriptName" to scriptName,
                "progress" to progress,
                "status" to status
            )
        )
    }
    
    /**
     * 显示脚本控制器
     */
    fun showScriptControl(context: Context): String? {
        return createOverlay(
            context = context,
            type = OverlayType.SCRIPT_CONTROL,
            instanceId = "script_control",
            x = 100,
            y = 300,
            width = 280,
            height = 180
        )
    }
    
    /**
     * 检查悬浮窗状态
     */
    fun checkOverlayStatus(): Boolean {
        return OverlayContainerService.isRunning
    }
}