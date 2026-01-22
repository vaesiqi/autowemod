// 文件：app/src/main/kotlin/com/wemod/automation/core/overlays/views/MiniControlOverlay.kt
package com.wemod.automation.core.overlays.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.wemod.automation.R
import com.wemod.automation.core.overlays.IOverlayView
import com.wemod.automation.core.overlays.OverlayType

/**
 * 迷你控制面板
 * 简化版控制面板，类似原来的 OverlayService
 */
class MiniControlOverlay(private val context: Context) : IOverlayView {
    
    override val overlayType: OverlayType = OverlayType.MINI_CONTROL
    override val overlayTitle: String = "迷你控制"
    
    private lateinit var rootView: RelativeLayout
    private lateinit var btnMain: Button
    private lateinit var btnQuickActions: LinearLayout
    
    // 拖拽相关
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    private var currentWidth = 180
    private var currentHeight = 120
    private var currentX = 100
    private var currentY = 100
    
    private var isQuickActionsVisible = false
    
    override fun createView(context: Context): View {
        rootView = RelativeLayout(context).apply {
            // 使用现有的背景资源，如果没有就使用代码设置
            try {
                setBackgroundResource(R.drawable.overlay_background_rounded)
            } catch (e: Exception) {
                // 如果资源不存在，使用代码设置背景
                setBackgroundColor(0xCC4A9EFF.toInt())
            }
            
            // 设置最小尺寸
            minimumWidth = currentWidth
            minimumHeight = currentHeight
            
            // 主按钮
            btnMain = Button(context).apply {
                text = "⚙️"
                textSize = 18f
                
                try {
                    setBackgroundResource(R.drawable.overlay_button_bg)
                } catch (e: Exception) {
                    // 备用背景
                    setBackgroundColor(0xFF4A9EFF.toInt())
                }
                
                setOnLongClickListener { view ->
                    // 长按显示快捷操作
                    toggleQuickActions()
                    true
                }
                
                setOnClickListener {
                    // 单击回到主应用或显示更多选项
                }
            }
            
            val btnParams = RelativeLayout.LayoutParams(80, 80)
            btnParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            addView(btnMain, btnParams)
            
            // 快捷操作面板（默认隐藏）
            btnQuickActions = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                
                try {
                    setBackgroundResource(R.drawable.overlay_panel_bg)
                } catch (e: Exception) {
                    setBackgroundColor(0xE6FFFFFF.toInt())
                }
                
                visibility = View.GONE
                
                // 运行按钮
                val btnRun = Button(context).apply {
                    text = "▶️ 运行"
                    textSize = 10f
                    setOnClickListener {
                        // 运行脚本
                    }
                }
                
                addView(btnRun, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 35
                ))
                
                // 录制按钮
                val btnRecord = Button(context).apply {
                    text = "🔴 录制"
                    textSize = 10f
                    setOnClickListener {
                        // 录制动作
                    }
                }
                
                addView(btnRecord, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 35
                ))
                
                // 设置按钮
                val btnSettings = Button(context).apply {
                    text = "⚙️ 设置"
                    textSize = 10f
                    setOnClickListener {
                        // 打开设置
                    }
                }
                
                addView(btnSettings, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 35
                ))
            }
            
            val quickActionsParams = RelativeLayout.LayoutParams(120, 120)
            quickActionsParams.addRule(RelativeLayout.BELOW, btnMain.id)
            quickActionsParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            quickActionsParams.topMargin = 10
            addView(btnQuickActions, quickActionsParams)
        }
        
        // 设置拖拽
        setupDrag()
        
        return rootView
    }
    
    override fun getLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                   WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            
            gravity = Gravity.TOP or Gravity.START
            width = currentWidth
            height = currentHeight
            x = currentX
            y = currentY
        }
    }
    
    override fun onViewCreated(view: View) {
        Log.d("MiniControlOverlay", "迷你控制面板创建完成")
    }
    
    override fun onViewDestroyed() {
        Log.d("MiniControlOverlay", "迷你控制面板销毁")
    }
    
    override fun updateView(data: Map<String, Any>) {
        // 可以更新按钮状态、显示通知等
        data.forEach { (key, value) ->
            Log.d("MiniControlOverlay", "更新数据: $key = $value")
        }
    }
    
    override fun getCurrentSize(): Pair<Int, Int> {
        return Pair(currentWidth, currentHeight)
    }
    
    override fun getCurrentPosition(): Pair<Int, Int> {
        return Pair(currentX, currentY)
    }
    
    /**
     * 切换快捷操作显示
     */
    private fun toggleQuickActions() {
        isQuickActionsVisible = !isQuickActionsVisible
        
        if (isQuickActionsVisible) {
            btnQuickActions.visibility = View.VISIBLE
            currentHeight = 250
        } else {
            btnQuickActions.visibility = View.GONE
            currentHeight = 120
        }
        
        // 实际应用中应该通知容器更新布局参数
        Log.d("MiniControlOverlay", "快捷操作面板: ${if (isQuickActionsVisible) "显示" else "隐藏"}")
    }
    
    /**
     * 设置拖拽功能
     */
    private fun setupDrag() {
        rootView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    initialX = currentX
                    initialY = currentY
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                
                android.view.MotionEvent.ACTION_MOVE -> {
                    val newX = initialX + (event.rawX - initialTouchX).toInt()
                    val newY = initialY + (event.rawY - initialTouchY).toInt()
                    
                    currentX = newX
                    currentY = newY
                    
                    // 在实际实现中，这里应该通过回调通知容器更新位置
                    Log.d("MiniControlOverlay", "拖拽到位置: ($currentX, $currentY)")
                    true
                }
                
                android.view.MotionEvent.ACTION_UP -> {
                    Log.d("MiniControlOverlay", "拖拽结束，最终位置: ($currentX, $currentY)")
                    true
                }
                
                else -> false
            }
        }
    }
}