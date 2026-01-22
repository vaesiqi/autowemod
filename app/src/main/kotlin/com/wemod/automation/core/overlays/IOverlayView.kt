// 新文件：app/src/main/kotlin/com/wemod/automation/core/overlays/IOverlayView.kt
package com.wemod.automation.core.overlays

import android.content.Context
import android.view.View
import android.view.WindowManager

/**
 * 悬浮窗视图接口
 * 所有悬浮窗视图都必须实现此接口
 */
interface IOverlayView {
    
    /**
     * 悬浮窗类型标识
     */
    val overlayType: OverlayType
    
    /**
     * 悬浮窗标题
     */
    val overlayTitle: String
    
    /**
     * 创建视图
     */
    fun createView(context: Context): View
    
    /**
     * 获取布局参数
     */
    fun getLayoutParams(): WindowManager.LayoutParams
    
    /**
     * 视图创建完成回调
     */
    fun onViewCreated(view: View)
    
    /**
     * 视图销毁回调
     */
    fun onViewDestroyed()
    
    /**
     * 更新视图内容
     */
    fun updateView(data: Map<String, Any> = emptyMap())
    
    /**
     * 获取当前尺寸
     */
    fun getCurrentSize(): Pair<Int, Int>
    
    /**
     * 获取当前位置
     */
    fun getCurrentPosition(): Pair<Int, Int>
}

/**
 * 悬浮窗类型枚举
 */
enum class OverlayType {
    RUNNING_INDICATOR,    // 运行指示器
    SCRIPT_EDITOR,        // 脚本编辑器
    GAME_GUIDE,           // 游戏攻略
    SCRIPT_CONTROL,       // 脚本控制
    MINI_CONTROL          // 迷你控制面板
}