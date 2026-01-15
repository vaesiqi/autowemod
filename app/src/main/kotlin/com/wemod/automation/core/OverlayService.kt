package com.wemod.automation.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.wemod.automation.R

/**
 * 悬浮窗服务
 * 负责显示和管理悬浮窗
 */
class OverlayService : Service() {

    companion object {
        private const val TAG = "OverlayService"
        var isRunning = false
        
        /**
         * 检查是否有悬浮窗权限
         */
        fun hasPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
        
        /**
         * 启动悬浮窗服务
         */
        fun start(context: Context) {
            if (!hasPermission(context)) {
                Toast.makeText(context, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
                return
            }
            
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * 停止悬浮窗服务
         */
        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.stopService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createOverlayView()
        Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        isRunning = false
        removeOverlayView()
        Toast.makeText(this, "悬浮窗已关闭", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * 创建悬浮窗视图
     */
    private fun createOverlayView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 创建布局参数
        layoutParams = WindowManager.LayoutParams().apply {
            // 设置窗口类型
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            // 设置格式
            format = PixelFormat.RGBA_8888
            
            // 设置标志位
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            
            // 设置位置和大小
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        // 加载布局
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_layout, null)

        // 设置点击事件
        setupClickEvents()

        // 添加视图到窗口
        windowManager?.addView(overlayView, layoutParams)
    }

    /**
     * 移除悬浮窗视图
     */
    private fun removeOverlayView() {
        overlayView?.let { view ->
            windowManager?.removeView(view)
        }
        overlayView = null
    }

    /**
     * 设置点击事件
     */
    private fun setupClickEvents() {
        overlayView?.let { view ->
            // 关闭按钮
            val closeBtn = view.findViewById<Button>(R.id.btnClose)
            closeBtn?.setOnClickListener {
                stopSelf()
            }
            
            // 测试点击按钮
            val testClickBtn = view.findViewById<Button>(R.id.btnTestClick)
            testClickBtn?.setOnClickListener {
                if (AccessibilityManager.isRunning()) {
                    AccessibilityManager.click(500, 500)
                    showToast("执行点击")
                } else {
                    showToast("无障碍服务未运行")
                }
            }
            
            // 拖拽区域
            val dragArea = view.findViewById<View>(R.id.dragArea)
            dragArea?.setOnTouchListener { v, event ->
                // 简单的拖拽实现
                layoutParams?.x = event.rawX.toInt() - v.width / 2
                layoutParams?.y = event.rawY.toInt() - v.height / 2
                windowManager?.updateViewLayout(overlayView, layoutParams)
                true
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}