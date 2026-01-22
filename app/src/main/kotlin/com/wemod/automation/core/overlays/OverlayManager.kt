// 新文件：app/src/main/kotlin/com/wemod/automation/core/overlays/OverlayManager.kt
package com.wemod.automation.core.overlays

import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager

/**
 * 悬浮窗管理器
 * 负责管理所有悬浮窗的生命周期
 */
class OverlayManager(private val context: Context) {
    
    companion object {
        private const val TAG = "OverlayManager"
        
        // 单例实例
        private var instance: OverlayManager? = null
        
        fun getInstance(context: Context): OverlayManager {
            return instance ?: synchronized(this) {
                instance ?: OverlayManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // WindowManager
    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    
    // 已注册的悬浮窗工厂
    private val overlayFactories = mutableMapOf<OverlayType, OverlayFactory>()
    
    // 活动的悬浮窗
    private val activeOverlays = mutableMapOf<String, OverlayInstance>()
    
    // 悬浮窗位置记忆
    private val overlayPositions = mutableMapOf<String, Pair<Int, Int>>()
    
    /**
     * 注册悬浮窗工厂
     */
    fun registerFactory(type: OverlayType, factory: OverlayFactory) {
        overlayFactories[type] = factory
        Log.d(TAG, "注册悬浮窗工厂: $type")
    }
    
    /**
     * 创建悬浮窗
     */
    fun createOverlay(
        type: OverlayType,
        instanceId: String = generateInstanceId(type),
        initialX: Int = 100,
        initialY: Int = 100,
        initialWidth: Int? = null,
        initialHeight: Int? = null,
        customData: Map<String, Any> = emptyMap()
    ): Boolean {
        Log.d(TAG, "创建悬浮窗: $type, 实例ID: $instanceId")
        
        try {
            // 检查是否已存在
            if (activeOverlays.containsKey(instanceId)) {
                Log.w(TAG, "悬浮窗实例已存在: $instanceId")
                return false
            }
            
            // 获取工厂
            val factory = overlayFactories[type]
            if (factory == null) {
                Log.e(TAG, "未注册的悬浮窗类型: $type")
                return false
            }
            
            // 创建悬浮窗实例
            val overlay = factory.createOverlay(context)
            
            // 创建视图
            val view = overlay.createView(context)
            
            // 获取布局参数
            val layoutParams = overlay.getLayoutParams().apply {
                x = initialX
                y = initialY
                
                // 设置初始尺寸
                initialWidth?.let { width = it }
                initialHeight?.let { height = it }
            }
            
            // 添加到窗口管理器
            windowManager.addView(view, layoutParams)
            
            // 回调视图创建
            overlay.onViewCreated(view)
            
            // 保存实例
            val instance = OverlayInstance(
                id = instanceId,
                type = type,
                overlay = overlay,
                view = view,
                layoutParams = layoutParams
            )
            
            activeOverlays[instanceId] = instance
            
            // 应用自定义数据
            if (customData.isNotEmpty()) {
                overlay.updateView(customData)
            }
            
            Log.d(TAG, "✅ 悬浮窗创建成功: $instanceId ($type)")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "创建悬浮窗失败: $type", e)
            return false
        }
    }
    
    /**
     * 销毁悬浮窗
     */
    fun destroyOverlay(instanceId: String): Boolean {
        Log.d(TAG, "销毁悬浮窗: $instanceId")
        
        return try {
            val instance = activeOverlays[instanceId] ?: return false
            
            // 回调销毁
            instance.overlay.onViewDestroyed()
            
            // 保存位置记忆
            overlayPositions["${instance.type}_${instanceId}"] = 
                Pair(instance.layoutParams.x, instance.layoutParams.y)
            
            // 从窗口管理器移除
            windowManager.removeView(instance.view)
            
            // 移除实例
            activeOverlays.remove(instanceId)
            
            Log.d(TAG, "✅ 悬浮窗销毁成功: $instanceId")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "销毁悬浮窗失败: $instanceId", e)
            false
        }
    }
    
    /**
     * 获取悬浮窗实例
     */
    fun getOverlay(instanceId: String): OverlayInstance? {
        return activeOverlays[instanceId]
    }
    
    /**
     * 更新悬浮窗内容
     */
    fun updateOverlay(instanceId: String, data: Map<String, Any>): Boolean {
        return try {
            val instance = activeOverlays[instanceId] ?: return false
            instance.overlay.updateView(data)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 移动悬浮窗
     */
    fun moveOverlay(instanceId: String, x: Int, y: Int): Boolean {
        return try {
            val instance = activeOverlays[instanceId] ?: return false
            instance.layoutParams.x = x
            instance.layoutParams.y = y
            windowManager.updateViewLayout(instance.view, instance.layoutParams)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 调整悬浮窗尺寸
     */
    fun resizeOverlay(instanceId: String, width: Int, height: Int): Boolean {
        return try {
            val instance = activeOverlays[instanceId] ?: return false
            instance.layoutParams.width = width
            instance.layoutParams.height = height
            windowManager.updateViewLayout(instance.view, instance.layoutParams)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取所有活动的悬浮窗
     */
    fun getAllActiveOverlays(): List<OverlayInstance> {
        return activeOverlays.values.toList()
    }
    
    /**
     * 获取指定类型的悬浮窗
     */
    fun getOverlaysByType(type: OverlayType): List<OverlayInstance> {
        return activeOverlays.values.filter { it.type == type }
    }
    
    /**
     * 检查是否存在指定类型的悬浮窗
     */
    fun hasOverlayType(type: OverlayType): Boolean {
        return activeOverlays.values.any { it.type == type }
    }
    
    /**
     * 销毁所有悬浮窗
     */
    fun destroyAllOverlays() {
        Log.d(TAG, "销毁所有悬浮窗")
        
        // 复制列表避免并发修改
        val instanceIds = activeOverlays.keys.toList()
        instanceIds.forEach { instanceId ->
            destroyOverlay(instanceId)
        }
    }
    
    /**
     * 生成实例ID
     */
    private fun generateInstanceId(type: OverlayType): String {
        val timestamp = System.currentTimeMillis()
        return "${type.name.lowercase()}_$timestamp"
    }
}

/**
 * 悬浮窗工厂接口
 */
interface OverlayFactory {
    fun createOverlay(context: Context): IOverlayView
}

/**
 * 悬浮窗实例
 */
data class OverlayInstance(
    val id: String,
    val type: OverlayType,
    val overlay: IOverlayView,
    val view: View,
    val layoutParams: WindowManager.LayoutParams
)