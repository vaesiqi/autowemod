package com.wemod.automation.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 基础无障碍服务
 */
class AccessibilityService : AccessibilityService() {
    
    companion object {
        const val TAG = "WeModAccessibility"
    }
    
    override fun onCreate() {
        super.onCreate()
        AccessibilityManager.setService(this)
        Log.d(TAG, "无障碍服务已创建")
    }
    
    override fun onDestroy() {
        AccessibilityManager.clearService()
        Log.d(TAG, "无障碍服务已销毁")
        super.onDestroy()
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "无障碍服务已连接")
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                       AccessibilityEvent.TYPE_VIEW_FOCUSED or
                       AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                       AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
            
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                   AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            
            notificationTimeout = 100
            packageNames = null
        }
        
        this.serviceInfo = info
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            Log.d(TAG, "接收到事件: ${event.eventType}")
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "无障碍服务被中断")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "无障碍服务启动命令")
        return START_STICKY
    }
    
    /**
     * 执行点击操作
     */
    fun performClick(x: Int, y: Int): Boolean {
        return try {
            GlobalScope.launch(Dispatchers.IO) {
                val gesture = GestureDescription.Builder()
                    .addStroke(
                        GestureDescription.StrokeDescription(
                            Path().apply { moveTo(x.toFloat(), y.toFloat()) },
                            0,
                            50
                        )
                    )
                    .build()
                
                dispatchGesture(gesture, null, null)
            }
            Log.d(TAG, "执行点击: ($x, $y)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "点击失败", e)
            false
        }
    }
    
    /**
     * 执行滑动操作
     */
    fun performSwipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Int = 500): Boolean {
        return try {
            GlobalScope.launch(Dispatchers.IO) {
                val gesture = GestureDescription.Builder()
                    .addStroke(
                        GestureDescription.StrokeDescription(
                            Path().apply {
                                moveTo(startX.toFloat(), startY.toFloat())
                                lineTo(endX.toFloat(), endY.toFloat())
                            },
                            0,
                            duration.toLong()
                        )
                    )
                    .build()
                
                dispatchGesture(gesture, null, null)
            }
            Log.d(TAG, "执行滑动: ($startX, $startY) -> ($endX, $endY)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "滑动失败", e)
            false
        }
    }
    
    /**
     * 查找节点并点击
     */
    fun clickNodeByText(text: String? = null, description: String? = null): Boolean {
        return try {
            val rootNode = rootInActiveWindow ?: return false
            val node = findNode(rootNode, text, description)
            node?.let {
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "点击节点: ${text ?: description}")
                return true
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "点击节点失败", e)
            false
        }
    }
    
    /**
     * 递归查找节点
     */
    private fun findNode(
        node: AccessibilityNodeInfo,
        text: String?,
        description: String?
    ): AccessibilityNodeInfo? {
        if ((text != null && node.text?.contains(text) == true) ||
            (description != null && node.contentDescription?.contains(description) == true)) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNode(child, text, description)
            if (found != null) {
                return found
            }
            child.recycle()
        }
        
        return null
    }
}