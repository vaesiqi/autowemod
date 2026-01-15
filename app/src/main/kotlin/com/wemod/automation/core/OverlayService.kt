package com.wemod.automation.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.Toast
import com.wemod.automation.R

/**
 * 悬浮窗服务 - 简洁等比例缩放版
 */
class OverlayService : Service() {

    companion object {
        var isRunning = false
        
        fun hasPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
        
        fun start(context: Context) {
            if (!hasPermission(context)) {
                Toast.makeText(context, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(context, OverlayService::class.java)
            context.startService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.stopService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    
    // 手势相关变量
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    // 缩放相关变量
    private var initialWidth = 0
    private var initialHeight = 0
    private var initialDistance = 0f
    private var isResizing = false
    private var isMoving = false
    
    // 最小和最大尺寸（基于初始尺寸的比例）
    private val minScale = 0.5f  // 最小缩放到50%
    private val maxScale = 2.0f  // 最大放大到200%
    
    // 初始尺寸（第一次显示时的尺寸）
    private var baseWidth = 0
    private var baseHeight = 0

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        showOverlay()
    }

    override fun onDestroy() {
        hideOverlay()
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            layoutParams = WindowManager.LayoutParams().apply {
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                
                format = PixelFormat.RGBA_8888
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 100
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            overlayView = inflater.inflate(R.layout.overlay_layout, null)
            
            setupClickEvents()
            setupGestureListener()
            
            windowManager?.addView(overlayView, layoutParams)
            
            // 保存初始尺寸（等比例缩放的基础）
            overlayView?.post {
                baseWidth = overlayView?.width ?: 300
                baseHeight = overlayView?.height ?: 400
            }
            
            Toast.makeText(this, 
                "操作说明：\n" +
                "• 拖拽标题栏：移动\n" +
                "• 拖拽边缘：等比例缩放", 
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "显示悬浮窗失败", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }

    private fun hideOverlay() {
        try {
            overlayView?.let {
                windowManager?.removeView(it)
            }
        } catch (e: Exception) {
            // 忽略移除时的错误
        } finally {
            overlayView = null
        }
        isRunning = false
    }

    private fun setupClickEvents() {
        overlayView?.apply {
            // 关闭按钮
            findViewById<Button>(R.id.btnClose)?.setOnClickListener {
                hideOverlay()
                Toast.makeText(this@OverlayService, "悬浮窗已关闭", Toast.LENGTH_SHORT).show()
                stopSelf()
            }
            
            // 测试点击按钮
            findViewById<Button>(R.id.btnTestClick)?.setOnClickListener {
                if (AccessibilityManager.isRunning()) {
                    AccessibilityManager.click(500, 500)
                    Toast.makeText(this@OverlayService, "执行点击", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@OverlayService, "无障碍服务未运行", Toast.LENGTH_SHORT).show()
                }
            }
            
            // 测试滑动按钮
            findViewById<Button>(R.id.btnTestSwipe)?.setOnClickListener {
                if (AccessibilityManager.isRunning()) {
                    AccessibilityManager.swipe(300, 500, 700, 500, 500)
                    Toast.makeText(this@OverlayService, "执行滑动", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@OverlayService, "无障碍服务未运行", Toast.LENGTH_SHORT).show()
                }
            }
            
            // 录制按钮
            findViewById<Button>(R.id.btnRecord)?.setOnClickListener {
                Toast.makeText(this@OverlayService, "录制功能开发中", Toast.LENGTH_SHORT).show()
            }
            
            // 运行脚本按钮
            findViewById<Button>(R.id.btnRunScript)?.setOnClickListener {
                Toast.makeText(this@OverlayService, "运行脚本功能开发中", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupGestureListener() {
        overlayView?.setOnTouchListener { v, event ->
            val x = event.x
            val y = event.y
            val viewWidth = v.width
            val viewHeight = v.height
            
            // 定义边缘识别区域（20像素）
            val edgeSize = 20f
            
            // 检查是否在边缘区域（四个角或四条边）
            val isNearEdge = 
                x < edgeSize || 
                x > viewWidth - edgeSize || 
                y < edgeSize || 
                y > viewHeight - edgeSize
            
            // 检查是否在标题栏区域（用于移动）
            val isInTitleBar = y < 60f // 标题栏高度约40dp + 一些缓冲
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isNearEdge && !isInTitleBar) {
                        // 开始缩放
                        isResizing = true
                        isMoving = false
                        initialWidth = v.width
                        initialHeight = v.height
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    } else if (isInTitleBar) {
                        // 开始移动
                        isMoving = true
                        isResizing = false
                        initialX = layoutParams?.x ?: 0
                        initialY = layoutParams?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    }
                    return@setOnTouchListener true
                }
                
                MotionEvent.ACTION_MOVE -> {
                    if (isResizing) {
                        // 等比例缩放：基于拖拽距离计算缩放比例
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY
                        
                        // 使用对角线距离来计算缩放（保持宽高比）
                        val distance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
                        val scaleFactor = if (deltaX > 0 || deltaY > 0) {
                            1.0f + distance / 500f  // 放大
                        } else {
                            1.0f - distance / 500f  // 缩小
                        }
                        
                        // 限制缩放范围
                        val currentScale = (initialWidth.toFloat() / baseWidth.toFloat())
                        val newScale = (currentScale * scaleFactor).coerceIn(minScale, maxScale)
                        
                        // 计算新尺寸（保持宽高比）
                        val newWidth = (baseWidth * newScale).toInt()
                        val newHeight = (baseHeight * newScale).toInt()
                        
                        // 更新尺寸
                        layoutParams?.width = newWidth
                        layoutParams?.height = newHeight
                        windowManager?.updateViewLayout(overlayView, layoutParams)
                        
                        // 更新初始位置以便连续缩放
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        initialWidth = newWidth
                        initialHeight = newHeight
                        
                    } else if (isMoving) {
                        // 移动悬浮窗
                        val newX = initialX + (event.rawX - initialTouchX).toInt()
                        val newY = initialY + (event.rawY - initialTouchY).toInt()
                        
                        layoutParams?.x = newX
                        layoutParams?.y = newY
                        windowManager?.updateViewLayout(overlayView, layoutParams)
                    }
                    return@setOnTouchListener true
                }
                
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isResizing) {
                        val currentWidth = v.width
                        val currentHeight = v.height
                        val scale = (currentWidth.toFloat() / baseWidth.toFloat() * 100f).toInt()
                        Toast.makeText(this@OverlayService, "缩放: ${scale}%", Toast.LENGTH_SHORT).show()
                    }
                    isResizing = false
                    isMoving = false
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}