// 新建：app/src/main/kotlin/com/wemod/automation/core/actions/ActionRegistry.kt
// ===========================================

package com.wemod.automation.core.actions

import android.util.Log

/**
 * 动作注册表 - 插件化管理所有动作类型
 */
object ActionRegistry {
    private const val TAG = "ActionRegistry"
    
    // 动作类型注册表
    private val actionFactories = mutableMapOf<String, ActionFactory>()
    private val actionInstances = mutableMapOf<String, IAction>()
    
    /**
     * 动作工厂接口
     */
    interface ActionFactory {
        fun createAction(): IAction
        fun createDefaultConfig(): ActionConfig
    }
    
    /**
     * 注册动作类型
     */
    fun register(type: String, factory: ActionFactory) {
        if (actionFactories.containsKey(type)) {
            Log.w(TAG, "动作类型已注册: $type")
            return
        }
        
        actionFactories[type] = factory
        
        // 创建动作实例
        val action = factory.createAction()
        actionInstances[type] = action
        
        Log.d(TAG, "✅ 注册动作类型: $type - ${action.displayName}")
    }
    
    /**
     * 注销动作类型
     */
    fun unregister(type: String) {
        actionFactories.remove(type)
        actionInstances.remove(type)
        Log.d(TAG, "注销动作类型: $type")
    }
    
    /**
     * 获取动作实例
     */
    fun getAction(type: String): IAction? {
        return actionInstances[type]
    }
    
    /**
     * 获取动作工厂
     */
    fun getFactory(type: String): ActionFactory? {
        return actionFactories[type]
    }
    
    /**
     * 创建动作配置
     */
    fun createConfig(type: String): ActionConfig? {
        return getFactory(type)?.createDefaultConfig()
    }
    
    /**
     * 检查动作类型是否已注册
     */
    fun hasAction(type: String): Boolean {
        return actionFactories.containsKey(type)
    }
    
    /**
     * 获取所有已注册的动作类型
     */
    fun getAllActionTypes(): List<String> {
        return actionFactories.keys.toList()
    }
    
    /**
     * 获取所有动作实例
     */
    fun getAllActions(): List<IAction> {
        return actionInstances.values.toList()
    }
    
    /**
     * 获取动作信息列表
     */
    fun getActionInfos(): List<ActionInfo> {
        return actionInstances.values.map { action ->
            ActionInfo(
                type = action.type,
                displayName = action.displayName,
                description = action.description
            )
        }
    }
    
    /**
     * 清空所有注册的动作
     */
    fun clear() {
        actionFactories.clear()
        actionInstances.clear()
        Log.d(TAG, "动作注册表已清空")
    }
    
    /**
     * 注册内置动作类型
     */
    fun registerBuiltInActions() {
        Log.d(TAG, "开始注册内置动作...")
        
        // 点击动作
        register("click", object : ActionFactory {
            override fun createAction(): IAction = ClickAction()
            override fun createDefaultConfig(): ActionConfig {
                return ActionConfig("click", mapOf(
                    "x" to 500,
                    "y" to 500,
                    "delayAfter" to 1000L
                ))
            }
        })
        
        // 滑动动作
        register("swipe", object : ActionFactory {
            override fun createAction(): IAction = SwipeAction()
            override fun createDefaultConfig(): ActionConfig {
                return ActionConfig("swipe", mapOf(
                    "startX" to 300,
                    "startY" to 500,
                    "endX" to 700,
                    "endY" to 500,
                    "duration" to 500L,
                    "delayAfter" to 1000L
                ))
            }
        })
        
        // 等待动作
        register("wait", object : ActionFactory {
            override fun createAction(): IAction = WaitAction()
            override fun createDefaultConfig(): ActionConfig {
                return ActionConfig("wait", mapOf(
                    "duration" to 1000L,
                    "delayAfter" to 0L
                ))
            }
        })
        
        Log.d(TAG, "✅ 内置动作注册完成，共 ${actionFactories.size} 种动作")
    }
}

/**
 * 动作信息
 */
data class ActionInfo(
    val type: String,
    val displayName: String,
    val description: String
)