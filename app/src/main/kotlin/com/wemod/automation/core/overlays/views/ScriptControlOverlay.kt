// 文件：app/src/main/kotlin/com/wemod/automation/core/overlays/views/ScriptControlOverlay.kt
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
 * 脚本控制悬浮窗
 * 提供脚本的快速控制功能
 */
class ScriptControlOverlay(private val context: Context) : IOverlayView {
    
    override val overlayType: OverlayType = OverlayType.SCRIPT_CONTROL
    override val overlayTitle: String = "脚本控制"
    
    private lateinit var rootView: LinearLayout
    private lateinit var currentScriptText: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnRecord: Button
    
    // 事件回调
    var onStartClicked: (() -> Unit)? = null
    var onPauseClicked: (() -> Unit)? = null
    var onStopClicked: (() -> Unit)? = null
    var onRecordClicked: (() -> Unit)? = null
    
    private var currentWidth = 280
    private var currentHeight = 180
    private var currentX = 100
    private var currentY = 300
    
    override fun createView(context: Context): View {
        rootView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.overlay_background)
            
            // 标题栏
            val titleBar = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundResource(R.drawable.overlay_title_bg)
                
                val title = TextView(context).apply {
                    text = "🎮 脚本控制"
                    textSize = 12f
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(8, 4, 8, 4)
                }
                
                addView(title, LinearLayout.LayoutParams(0, 40, 1f))
            }
            
            addView(titleBar, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 40
            ))
            
            // 内容区域
            val contentContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 12, 16, 12)
                
                // 当前脚本信息
                currentScriptText = TextView(context).apply {
                    text = "当前: 无脚本"
                    textSize = 11f
                    setTextColor(0xFF333333.toInt())
                    setPadding(0, 0, 0, 8)
                }
                
                addView(currentScriptText)
                
                // 控制按钮行1
                val buttonRow1 = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    
                    btnStart = Button(context).apply {
                        text = "▶️ 开始"
                        textSize = 12f
                        setOnClickListener { onStartClicked?.invoke() }
                    }
                    
                    btnPause = Button(context).apply {
                        text = "⏸️ 暂停"
                        textSize = 12f
                        setOnClickListener { onPauseClicked?.invoke() }
                    }
                    
                    addView(btnStart, LinearLayout.LayoutParams(0, 45, 1f).apply {
                        marginEnd = 4
                    })
                    addView(btnPause, LinearLayout.LayoutParams(0, 45, 1f).apply {
                        marginStart = 4
                    })
                }
                
                addView(buttonRow1, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ))
                
                // 间距
                addView(View(context), LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 8
                ))
                
                // 控制按钮行2
                val buttonRow2 = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    
                    btnStop = Button(context).apply {
                        text = "⏹️ 停止"
                        textSize = 12f
                        setOnClickListener { onStopClicked?.invoke() }
                    }
                    
                    btnRecord = Button(context).apply {
                        text = "🔴 录制"
                        textSize = 12f
                        setOnClickListener { onRecordClicked?.invoke() }
                    }
                    
                    addView(btnStop, LinearLayout.LayoutParams(0, 45, 1f).apply {
                        marginEnd = 4
                    })
                    addView(btnRecord, LinearLayout.LayoutParams(0, 45, 1f).apply {
                        marginStart = 4
                    })
                }
                
                addView(buttonRow2, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ))
            }
            
            addView(contentContainer)
        }
        
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
        Log.d("ScriptControlOverlay", "脚本控制悬浮窗创建完成")
    }
    
    override fun onViewDestroyed() {
        Log.d("ScriptControlOverlay", "脚本控制悬浮窗销毁")
    }
    
    override fun updateView(data: Map<String, Any>) {
        // 更新当前脚本
        data["currentScript"]?.let {
            currentScriptText.text = "当前: $it"
        }
        
        // 更新按钮状态
        data["isRunning"]?.let {
            val isRunning = it as? Boolean ?: false
            btnStart.isEnabled = !isRunning
            btnPause.isEnabled = isRunning
            btnStop.isEnabled = isRunning
        }
    }
    
    override fun getCurrentSize(): Pair<Int, Int> {
        return Pair(currentWidth, currentHeight)
    }
    
    override fun getCurrentPosition(): Pair<Int, Int> {
        return Pair(currentX, currentY)
    }
}