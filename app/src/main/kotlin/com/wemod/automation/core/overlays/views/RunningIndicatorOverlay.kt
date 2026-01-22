// 新文件：app/src/main/kotlin/com/wemod/automation/core/overlays/views/RunningIndicatorOverlay.kt
package com.wemod.automation.core.overlays.views

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.wemod.automation.R
import com.wemod.automation.core.overlays.IOverlayView
import com.wemod.automation.core.overlays.OverlayType
import android.util.Log

/**
 * 运行指示器悬浮窗
 * 显示脚本运行状态和进度
 */
class RunningIndicatorOverlay(private val context: Context) : IOverlayView {
    
    override val overlayType: OverlayType = OverlayType.RUNNING_INDICATOR
    override val overlayTitle: String = "运行状态"
    
    private lateinit var rootView: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var scriptNameText: TextView
    private lateinit var progressText: TextView
    private lateinit var statusText: TextView
    
    // 当前尺寸和位置
    private var currentWidth = 300
    private var currentHeight = 120
    private var currentX = 100
    private var currentY = 100
    
    override fun createView(context: Context): View {
        // 创建根布局
        rootView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.overlay_background)
            
            // 标题栏
            val titleBar = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundResource(R.drawable.overlay_title_bg)
                
                // 标题
                val title = TextView(context).apply {
                    text = "▶️ 脚本运行中"
                    textSize = 12f
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(8, 4, 8, 4)
                }
                
                // 关闭按钮
                val closeBtn = ImageButton(context).apply {
                    setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                    setBackgroundResource(android.R.color.transparent)
                    setOnClickListener {
                        // 由容器管理销毁
                    }
                }
                
                addView(title, LinearLayout.LayoutParams(0, 40, 1f))
                addView(closeBtn, LinearLayout.LayoutParams(40, 40))
            }
            
            // 内容区域
            val content = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 8, 12, 8)
                
                // 脚本名称
                scriptNameText = TextView(context).apply {
                    text = "脚本: 加载中..."
                    textSize = 11f
                    setTextColor(0xFF333333.toInt())
                }
                
                // 进度条
                progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = 0
                }
                
                // 进度文本
                progressText = TextView(context).apply {
                    text = "进度: 0%"
                    textSize = 10f
                    setTextColor(0xFF666666.toInt())
                }
                
                // 状态文本
                statusText = TextView(context).apply {
                    text = "状态: 空闲"
                    textSize = 10f
                    setTextColor(0xFF666666.toInt())
                }
                
                addView(scriptNameText)
                addView(progressBar, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 20
                ))
                addView(progressText)
                addView(statusText)
            }
            
            addView(titleBar, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 40
            ))
            addView(content)
        }
        
        // 设置拖拽和缩放
        setupDragAndResize()
        
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
        Log.d("RunningIndicatorOverlay", "视图创建完成")
    }
    
    override fun onViewDestroyed() {
        Log.d("RunningIndicatorOverlay", "视图销毁")
    }
    
    override fun updateView(data: Map<String, Any>) {
        // 更新脚本名称
        data["scriptName"]?.let { 
            scriptNameText.text = "脚本: $it"
        }
        
        // 更新进度
        data["progress"]?.let {
            val progress = (it as? Number)?.toInt() ?: 0
            progressBar.progress = progress
            progressText.text = "进度: ${progress}%"
        }
        
        // 更新状态
        data["status"]?.let {
            val status = it.toString()
            statusText.text = "状态: $status"
            
            // 根据状态改变颜色
            when {
                status.contains("运行") -> statusText.setTextColor(0xFF4CAF50.toInt())
                status.contains("暂停") -> statusText.setTextColor(0xFFFF9800.toInt())
                status.contains("停止") -> statusText.setTextColor(0xFFF44336.toInt())
                else -> statusText.setTextColor(0xFF666666.toInt())
            }
        }
        
        // 更新其他数据
        data["estimatedTime"]?.let {
            // 可以显示预估剩余时间
        }
    }
    
    override fun getCurrentSize(): Pair<Int, Int> {
        return Pair(currentWidth, currentHeight)
    }
    
    override fun getCurrentPosition(): Pair<Int, Int> {
        return Pair(currentX, currentY)
    }
    
    /**
     * 设置拖拽和缩放
     */
    private fun setupDragAndResize() {
        // 简化实现，实际应该实现拖拽和缩放逻辑
        // 这里可以使用旧版 OverlayService 的拖拽缩放逻辑
    }
}