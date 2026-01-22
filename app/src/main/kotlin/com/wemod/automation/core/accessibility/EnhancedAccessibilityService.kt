// 完全替换：app/src/main/kotlin/com/wemod/automation/core/accessibility/EnhancedAccessibilityService.kt
// ===========================================

package com.wemod.automation.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.wemod.automation.core.accessibility.node.NodeFinder
import com.wemod.automation.core.accessibility.node.NodeOperator
import com.wemod.automation.core.accessibility.types.ScrollDirection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 增强型无障碍服务 - 支持所有可能的自动化操作
 */
class EnhancedAccessibilityService : AccessibilityService() {
    
    companion object {
        const val TAG = "EnhancedAccessibility"
        private var instance: EnhancedAccessibilityService? = null
        
        fun getInstance(): EnhancedAccessibilityService? = instance
        
        fun isServiceRunning(): Boolean = instance != null
        
        // 🎯 新增：获取服务ID（用于权限检查）
        fun getServiceId(context: Context): String {
            return "${context.packageName}/${EnhancedAccessibilityService::class.java.name}"
        }
    }
    
    // 协程作用域
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 状态管理
    private val _serviceState = MutableStateFlow(ServiceState.IDLE)
    val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()
    
    // 事件通道
    private val eventChannel = Channel<AccessibilityEvent>(capacity = Channel.UNLIMITED)
    
    // 节点操作器
    private lateinit var nodeOperator: NodeOperator
    private lateinit var nodeFinder: NodeFinder
    
    // 手势队列
    private val gestureQueue = mutableListOf<GestureTask>()
    private var isExecutingGesture = false
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        EnhancedAccessibilityManager.setService(this)  // 🎯 新增：设置管理器引用
        Log.d(TAG, "增强型无障碍服务已创建")
        
        // 初始化组件
        nodeOperator = NodeOperator(this)
        nodeFinder = NodeFinder()
        
        _serviceState.value = ServiceState.CONNECTING
        startEventProcessing()
    }
    
    override fun onDestroy() {
        EnhancedAccessibilityManager.clearService()  // 🎯 新增：清除管理器引用
        instance = null
        serviceScope.cancel()
        eventChannel.close()
        Log.d(TAG, "增强型无障碍服务已销毁")
        super.onDestroy()
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "无障碍服务已连接")
        
        // 配置服务信息（最大化权限）
        val info = AccessibilityServiceInfo().apply {
            // 监听所有事件
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                       AccessibilityEvent.TYPE_VIEW_LONG_CLICKED or
                       AccessibilityEvent.TYPE_VIEW_FOCUSED or
                       AccessibilityEvent.TYPE_VIEW_SCROLLED or
                       AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                       AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                       AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                       AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
            
            // 启用所有反馈类型
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            
            // 启用所有标志（最大化功能）
            flags = AccessibilityServiceInfo.DEFAULT or
                   AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                   AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                   AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY or
                   AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                   AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
            
            // 设置通知超时
            notificationTimeout = 100
            
            // 设置包名（为空表示监听所有应用）
            packageNames = null
        }
        
        this.serviceInfo = info
        _serviceState.value = ServiceState.CONNECTED
        Log.d(TAG, "无障碍服务配置完成，已启用全部功能")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            // 发送到事件通道异步处理
            serviceScope.launch {
                eventChannel.send(it)
            }
        }
    }
    
    override fun onInterrupt() {
        Log.w(TAG, "无障碍服务被中断")
        _serviceState.value = ServiceState.INTERRUPTED
    }
    
    // ===== 核心功能 API =====
    
    /**
     * 执行点击（增强版）
     */
    fun performClick(x: Int, y: Int, duration: Long = 50): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeGesture { builder ->
                builder.addStroke(
                    GestureDescription.StrokeDescription(
                        Path().apply { moveTo(x.toFloat(), y.toFloat()) },
                        0,
                        duration
                    )
                )
            }
        } else {
            false
        }
    }
    
    /**
     * 执行长按
     */
    fun performLongClick(x: Int, y: Int, duration: Long = 1000): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeGesture { builder ->
                builder.addStroke(
                    GestureDescription.StrokeDescription(
                        Path().apply { moveTo(x.toFloat(), y.toFloat()) },
                        0,
                        duration
                    )
                )
            }
        } else {
            false
        }
    }
    
    /**
     * 执行滑动（增强版）
     */
    fun performSwipe(
        startX: Int, startY: Int,
        endX: Int, endY: Int,
        duration: Long = 500
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeGesture { builder ->
                builder.addStroke(
                    GestureDescription.StrokeDescription(
                        Path().apply {
                            moveTo(startX.toFloat(), startY.toFloat())
                            lineTo(endX.toFloat(), endY.toFloat())
                        },
                        0,
                        duration
                    )
                )
            }
        } else {
            false
        }
    }
    
    /**
     * 执行多点触控手势
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun performMultiTouch(gestures: List<Pair<Path, Long>>): Boolean {
        return executeGesture { builder ->
            gestures.forEach { (path, duration) ->
                builder.addStroke(
                    GestureDescription.StrokeDescription(path, 0, duration)
                )
            }
        }
    }
    
    /**
     * 查找并点击节点
     */
    fun findAndClick(
        text: String? = null,
        description: String? = null,
        className: String? = null,
        clickable: Boolean = true
    ): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val node = nodeFinder.findNode(rootNode, text, description, className, clickable)
        return node?.let {
            nodeOperator.clickNode(it)
            true
        } ?: false
    }
    
    /**
     * 查找并输入文本
     */
    fun findAndInput(
        text: String,
        targetText: String? = null,
        targetDescription: String? = null
    ): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val node = nodeFinder.findNode(rootNode, targetText, targetDescription)
        return node?.let {
            nodeOperator.inputText(it, text)
            true
        } ?: false
    }
    
    /**
     * 滚动查找
     */
    fun scrollToFind(
        text: String,
        maxScrolls: Int = 10,
        scrollDirection: ScrollDirection = ScrollDirection.DOWN
    ): Boolean {
        return nodeOperator.scrollToFind(rootInActiveWindow, text, maxScrolls, scrollDirection)
    }
    
    /**
     * 执行全局手势（返回、主页、最近任务）
     */
    fun performGlobalAction(action: GlobalAction): Boolean {
        return when (action) {
            GlobalAction.BACK -> performGlobalAction(GLOBAL_ACTION_BACK)
            GlobalAction.HOME -> performGlobalAction(GLOBAL_ACTION_HOME)
            GlobalAction.RECENTS -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            GlobalAction.NOTIFICATIONS -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            GlobalAction.QUICK_SETTINGS -> performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            GlobalAction.POWER_DIALOG -> performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        }
    }
    
    /**
     * 获取当前窗口信息
     */
    fun getCurrentWindowInfo(): WindowInfo? {
        val rootNode = rootInActiveWindow ?: return null
        
        return WindowInfo(
            packageName = rootNode.packageName?.toString(),
            className = rootNode.className?.toString(),
            windowId = rootNode.windowId,
            childCount = rootNode.childCount
        )
    }
    
    /**
     * 获取屏幕上的所有节点信息（调试用）
     */
    fun dumpNodeHierarchy(): String {
        return nodeFinder.dumpHierarchy(rootInActiveWindow)
    }
    
    // ===== 私有辅助方法 =====
    
    private fun startEventProcessing() {
        serviceScope.launch {
            for (event in eventChannel) {
                processAccessibilityEvent(event)
            }
        }
    }
    
    private fun processAccessibilityEvent(event: AccessibilityEvent) {
        // 可以在这里实现事件监听和响应逻辑
        // 例如：自动点击特定按钮、记录用户操作等
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                Log.v(TAG, "视图点击事件: ${event.packageName}")
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.v(TAG, "窗口状态变化: ${event.packageName}")
            }
            // 其他事件处理...
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.N)
    private fun executeGesture(buildAction: (GestureDescription.Builder) -> Unit): Boolean {
        return try {
            val builder = GestureDescription.Builder()
            buildAction(builder)
            val gesture = builder.build()
            
            dispatchGesture(gesture, null, null)
            true
        } catch (e: Exception) {
            Log.e(TAG, "执行手势失败", e)
            false
        }
    }
    
    private fun queueGesture(task: GestureTask) {
        gestureQueue.add(task)
        if (!isExecutingGesture) {
            executeNextGesture()
        }
    }
    
    private fun executeNextGesture() {
        if (gestureQueue.isEmpty()) {
            isExecutingGesture = false
            return
        }
        
        isExecutingGesture = true
        val task = gestureQueue.removeAt(0)
        
        serviceScope.launch {
            val success = task.execute()
            if (success) {
                delay(task.delayAfter)
            }
            executeNextGesture()
        }
    }
    
    // ===== 数据类 =====
    
    data class WindowInfo(
        val packageName: String?,
        val className: String?,
        val windowId: Int,
        val childCount: Int
    )
    
    data class GestureTask(
        val execute: suspend () -> Boolean,
        val delayAfter: Long = 0
    )
    
    enum class ServiceState {
        IDLE,
        CONNECTING,
        CONNECTED,
        INTERRUPTED,
        DISCONNECTED
    }
    
    enum class GlobalAction {
        BACK,
        HOME,
        RECENTS,
        NOTIFICATIONS,
        QUICK_SETTINGS,
        POWER_DIALOG
    }
}