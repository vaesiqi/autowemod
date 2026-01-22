// 替换 app/src/main/kotlin/com/wemod/automation/core/overlays/OverlayContainerService.kt
package com.wemod.automation.core.overlays

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.wemod.automation.core.overlays.views.*

/**
 * 悬浮窗容器服务
 * 替代旧的 OverlayService，提供动态悬浮窗管理
 */
class OverlayContainerService : Service() {
    
    companion object {
        private const val TAG = "OverlayContainer"
        
        // 服务状态
        var isRunning = false
            private set
        
        // 实例访问改为公开
        var instance: OverlayContainerService? = null
            private set
        
        /**
         * 启动服务
         */
        fun start(context: Context) {
            if (isRunning) {
                Log.d(TAG, "服务已在运行中")
                return
            }
            
            val intent = Intent(context, OverlayContainerService::class.java)
            context.startService(intent)
            Log.d(TAG, "启动悬浮窗容器服务")
        }
        
        /**
         * 停止服务
         */
        fun stop(context: Context) {
            if (!isRunning) {
                Log.d(TAG, "服务未运行")
                return
            }
            
            instance?.stopSelf()
            Log.d(TAG, "停止悬浮窗容器服务")
        }
    }
    
    private lateinit var overlayManager: OverlayManager
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        isRunning = true
        Log.d(TAG, "悬浮窗容器服务已创建")
        
        // 初始化管理器
        overlayManager = OverlayManager.getInstance(this)
        
        // 注册所有悬浮窗工厂
        registerAllFactories()
        
        // 根据配置创建初始悬浮窗
        createInitialOverlays()
    }
    
    override fun onDestroy() {
        // 销毁所有悬浮窗
        overlayManager.destroyAllOverlays()
        
        instance = null
        isRunning = false
        Log.d(TAG, "悬浮窗容器服务已销毁")
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "服务启动命令")
        return START_NOT_STICKY
    }
    
    /**
     * 注册所有悬浮窗工厂
     */
    private fun registerAllFactories() {
        Log.d(TAG, "注册悬浮窗工厂...")
        
        // 运行指示器
        overlayManager.registerFactory(OverlayType.RUNNING_INDICATOR, object : OverlayFactory {
            override fun createOverlay(context: Context): IOverlayView {
                return RunningIndicatorOverlay(context)
            }
        })
        
        // 脚本编辑器（暂时注释掉，先实现核心功能）
        /*
        overlayManager.registerFactory(OverlayType.SCRIPT_EDITOR, object : OverlayFactory {
            override fun createOverlay(context: Context): IOverlayView {
                return ScriptEditorOverlay(context)
            }
        })
        */
        
        // 游戏攻略（暂时注释掉）
        /*
        overlayManager.registerFactory(OverlayType.GAME_GUIDE, object : OverlayFactory {
            override fun createOverlay(context: Context): IOverlayView {
                return GameGuideOverlay(context)
            }
        })
        */
        
        // 脚本控制
        overlayManager.registerFactory(OverlayType.SCRIPT_CONTROL, object : OverlayFactory {
            override fun createOverlay(context: Context): IOverlayView {
                return ScriptControlOverlay(context)
            }
        })
        
        // 迷你控制面板
        overlayManager.registerFactory(OverlayType.MINI_CONTROL, object : OverlayFactory {
            override fun createOverlay(context: Context): IOverlayView {
                return MiniControlOverlay(context)
            }
        })
        
        Log.d(TAG, "✅ 所有悬浮窗工厂注册完成")
    }
    
    /**
     * 创建初始悬浮窗
     */
    private fun createInitialOverlays() {
        Log.d(TAG, "创建初始悬浮窗...")
        
        // 默认创建迷你控制面板
        overlayManager.createOverlay(
            type = OverlayType.MINI_CONTROL,
            instanceId = "mini_control_main",
            initialX = 100,
            initialY = 100,
            initialWidth = 180,
            initialHeight = 120
        )
        
        Log.d(TAG, "✅ 初始悬浮窗创建完成")
    }
    
    /**
     * 外部API：创建悬浮窗
     */
    fun createOverlay(
        type: OverlayType,
        instanceId: String? = null,
        x: Int = 100,
        y: Int = 100,
        width: Int? = null,
        height: Int? = null,
        data: Map<String, Any> = emptyMap()
    ): String? {
        val id = instanceId ?: "${type.name.lowercase()}_${System.currentTimeMillis()}"
        
        val success = overlayManager.createOverlay(
            type = type,
            instanceId = id,
            initialX = x,
            initialY = y,
            initialWidth = width,
            initialHeight = height,
            customData = data
        )
        
        return if (success) id else null
    }
    
    /**
     * 外部API：销毁悬浮窗
     */
    fun destroyOverlay(instanceId: String): Boolean {
        return overlayManager.destroyOverlay(instanceId)
    }
    
    /**
     * 外部API：更新悬浮窗
     */
    fun updateOverlay(instanceId: String, data: Map<String, Any>): Boolean {
        return overlayManager.updateOverlay(instanceId, data)
    }
    
    /**
     * 外部API：移动悬浮窗
     */
    fun moveOverlay(instanceId: String, x: Int, y: Int): Boolean {
        return overlayManager.moveOverlay(instanceId, x, y)
    }
    
    /**
     * 外部API：获取所有悬浮窗
     */
    fun getAllOverlays(): List<OverlayInstance> {
        return overlayManager.getAllActiveOverlays()
    }
}