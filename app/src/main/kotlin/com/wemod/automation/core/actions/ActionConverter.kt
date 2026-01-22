// ===========================================
// 文件：app/src/main/kotlin/com/wemod/automation/core/actions/ActionConverter.kt
// 完全修复版：修复 ActionParam 到基本类型的转换问题，添加详细调试
// ===========================================

package com.wemod.automation.core.actions

import android.content.Context
import android.util.Log
import com.wemod.automation.model.ActionData
import com.wemod.automation.model.ActionParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 动作转换器
 * 核心职责：
 * 1. ActionData → ActionConfig 转换（修复类型转换）
 * 2. 动作执行调度
 * 3. 错误处理和日志记录
 */
object ActionConverter {
    
    private const val TAG = "ActionConverter"
    
    // 协程作用域用于异步执行
    private val converterScope = CoroutineScope(Dispatchers.IO)
    
    // ===== 核心修复：类型转换函数 =====
    
    /**
     * 将 ActionParam 转换为对应的基本类型
     * 🎯 关键修复：确保返回的是基本类型（String、Int、Long等），不是包装类型
     */
    private fun convertActionParamToAny(param: ActionParam): Any {
        return when (param) {
            is ActionParam.StringParam -> param.value
            is ActionParam.IntParam -> param.value
            is ActionParam.LongParam -> param.value
            is ActionParam.BooleanParam -> param.value
            is ActionParam.FloatParam -> param.value
            is ActionParam.DoubleParam -> param.value
            else -> {
                // 备用方案：尝试获取值并确保是基本类型
                val value = param.getValue()
                Log.w(TAG, "⚠️ 未知的 ActionParam 类型: ${param::class.simpleName}, 值: $value")
                
                // 尝试识别类型
                when (value) {
                    is String, is Int, is Long, is Boolean, is Float, is Double -> {
                        value  // 已经是基本类型
                    }
                    is Number -> {
                        // 数字类型转换
                        if (value.toDouble() % 1 == 0.0) {
                            value.toInt()  // 整数
                        } else {
                            value.toDouble()  // 浮点数
                        }
                    }
                    else -> value.toString()  // 其他类型转为字符串
                }
            }
        }
    }
    
    /**
     * 将 ActionData 转换为 ActionConfig
     * 🎯 修复：正确转换 ActionParam 到对应的基本类型
     */
    fun toActionConfig(actionData: ActionData): ActionConfig {
        Log.d(TAG, "🔄 开始转换 ActionData -> ActionConfig")
        Log.d(TAG, "动作: ${actionData.name} [${actionData.type}]")
        Log.d(TAG, "原始参数数量: ${actionData.params.size}")
        
        val convertedParams = mutableMapOf<String, Any>()
        
        // 🎯 关键修复：逐一转换每个参数
        actionData.params.forEach { (key, param) ->
            try {
                val convertedValue = convertActionParamToAny(param)
                convertedParams[key] = convertedValue
                Log.d(TAG, "  参数转换: $key → $convertedValue (${convertedValue::class.simpleName})")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 参数转换失败: $key", e)
                // 使用默认值
                val defaultValue = when (key) {
                    "x", "y", "startX", "startY", "endX", "endY" -> 500
                    "duration" -> 1000L
                    else -> "unknown"
                }
                convertedParams[key] = defaultValue
            }
        }
        
        Log.d(TAG, "✅ ActionConfig 转换完成")
        Log.d(TAG, "  类型: ${actionData.type}")
        Log.d(TAG, "  参数数量: ${convertedParams.size}")
        
        // 返回配置
        return ActionConfig(
            type = actionData.type,
            params = convertedParams
        )
    }
    
    /**
     * 同步执行 ActionData
     * 直接在当前协程中执行
     */
    suspend fun executeActionData(context: Context, actionData: ActionData): Boolean {
        Log.d(TAG, "=== 开始执行动作 ===")
        Log.d(TAG, "动作: ${actionData.name} [${actionData.type}]")
        Log.d(TAG, "ID: ${actionData.id}, 启用: ${actionData.enabled}")
        
        // 检查动作是否启用
        if (!actionData.enabled) {
            Log.d(TAG, "⏭️ 跳过禁用的动作: ${actionData.name}")
            return true  // 禁用的动作视为成功跳过
        }
        
        // 获取动作实例
        val action = ActionRegistry.getAction(actionData.type)
        if (action == null) {
            Log.e(TAG, "❌ 未找到动作类型: ${actionData.type}")
            return false
        }
        
        // 🎯 转换配置（使用修复后的函数）
        Log.d(TAG, "🔄 转换 ActionData -> ActionConfig...")
        val config = toActionConfig(actionData)
        
        // 验证配置参数
        Log.d(TAG, "配置参数详情:")
        config.params.forEach { (key, value) ->
            Log.d(TAG, "  $key: $value (类型: ${value::class.simpleName})")
        }
        
        // 验证动作
        val validationResult = action.validate(config)
        
        if (!validationResult.isValid) {
            Log.e(TAG, "❌ 动作验证失败: ${validationResult.errors.joinToString()}")
            return false
        }
        
        if (validationResult.warnings.isNotEmpty()) {
            Log.w(TAG, "⚠️ 动作验证警告: ${validationResult.warnings.joinToString()}")
        }
        
        // 执行动作
        return try {
            Log.d(TAG, "▶️ 执行动作: ${actionData.name}")
            val success = action.execute(context, config)
            
            if (success) {
                Log.d(TAG, "✅ 动作执行成功: ${actionData.name}")
            } else {
                Log.w(TAG, "⚠️ 动作执行失败: ${actionData.name}")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ 动作执行异常: ${actionData.name}", e)
            Log.e(TAG, "异常详情: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            Log.d(TAG, "=== 动作执行结束 ===")
        }
    }
    
    /**
     * 异步执行 ActionData（不等待结果）
     */
    fun executeActionDataAsync(context: Context, actionData: ActionData) {
        converterScope.launch {
            executeActionData(context, actionData)
        }
    }
    
    /**
     * 批量执行 ActionData 列表
     */
    suspend fun executeActionDataList(context: Context, actions: List<ActionData>): List<Boolean> {
        Log.d(TAG, "批量执行 ${actions.size} 个动作")
        
        val results = mutableListOf<Boolean>()
        for ((index, action) in actions.withIndex()) {
            Log.d(TAG, "执行第 ${index + 1}/${actions.size} 个动作: ${action.name}")
            val result = executeActionData(context, action)
            results.add(result)
            
            // 如果动作失败且配置了失败后停止，则中断
            if (!result) {
                Log.w(TAG, "动作执行失败，停止批量执行")
                break
            }
        }
        
        val successCount = results.count { it }
        val totalCount = results.size
        Log.d(TAG, "批量执行完成: 成功 $successCount/$totalCount")
        
        return results
    }
    
    /**
     * 验证 ActionData 列表
     */
    fun validateActionDataList(actions: List<ActionData>): List<ValidationResult> {
        val results = mutableListOf<ValidationResult>()
        
        for (actionData in actions) {
            val action = ActionRegistry.getAction(actionData.type)
            if (action == null) {
                // 如果动作类型未注册，记录警告但不一定失败
                Log.w(TAG, "未注册的动作类型: ${actionData.type}")
                results.add(ValidationResult.withWarnings("未注册的动作类型: ${actionData.type}"))
            } else {
                val config = toActionConfig(actionData)
                results.add(action.validate(config))
            }
        }
        
        return results
    }
    
    /**
     * 获取动作执行时间的预估
     */
    fun estimateExecutionTime(actions: List<ActionData>): Long {
        // 预估时间 = 所有动作的 delayAfter 之和 + 基础执行时间
        var totalTime = 0L
        
        actions.forEach { action ->
            if (action.enabled) {
                // 基础执行时间（不同类型动作不同）
                val baseTime = when (action.type) {
                    "click" -> 100L
                    "swipe" -> {
                        val duration = action.getParam<Any>("duration", 500L)
                        when (duration) {
                            is Long -> duration
                            is Int -> duration.toLong()
                            else -> 500L
                        }
                    }
                    "wait" -> {
                        val duration = action.getParam<Any>("duration", 1000L)
                        when (duration) {
                            is Long -> duration
                            is Int -> duration.toLong()
                            else -> 1000L
                        }
                    }
                    else -> 50L
                }
                
                totalTime += baseTime + action.delayAfter
            }
        }
        
        Log.d(TAG, "预估执行时间: ${totalTime}ms (${actions.size}个动作)")
        return totalTime
    }
    
    /**
     * 批量创建 ActionData（用于测试和示例）
     */
    fun createSampleActionDataList(): List<ActionData> {
        return listOf(
            ActionData.createClick(name = "点击屏幕中心"),
            ActionData.createWait(name = "等待1秒", duration = 1000L),
            ActionData.createSwipe(name = "水平滑动", duration = 500L),
            ActionData.createClick(name = "点击右上角", x = 900, y = 100),
            ActionData.createWait(name = "等待2秒", duration = 2000L)
        )
    }
    
    /**
     * 将 ActionData 转换为可读的描述
     */
    fun toReadableDescription(actionData: ActionData): String {
        return when (actionData.type) {
            "click" -> {
                val x = actionData.getIntParam("x")
                val y = actionData.getIntParam("y")
                "${actionData.name}: 点击位置($x, $y)"
            }
            "swipe" -> {
                val startX = actionData.getIntParam("startX")
                val startY = actionData.getIntParam("startY")
                val endX = actionData.getIntParam("endX")
                val endY = actionData.getIntParam("endY")
                "${actionData.name}: 从($startX,$startY)滑动到($endX,$endY)"
            }
            "wait" -> {
                val duration = actionData.getLongParam("duration")
                "${actionData.name}: 等待${duration}ms"
            }
            else -> "${actionData.name}: [${actionData.type}]"
        }
    }
    
    /**
     * 统计动作列表信息
     */
    data class ActionListStats(
        val totalCount: Int,
        val enabledCount: Int,
        val disabledCount: Int,
        val estimatedTime: Long,
        val typeDistribution: Map<String, Int>
    )
    
    fun analyzeActionList(actions: List<ActionData>): ActionListStats {
        val enabledCount = actions.count { it.enabled }
        val disabledCount = actions.count { !it.enabled }
        val estimatedTime = estimateExecutionTime(actions)
        
        // 类型分布
        val typeDistribution = actions.groupingBy { it.type }.eachCount()
        
        return ActionListStats(
            totalCount = actions.size,
            enabledCount = enabledCount,
            disabledCount = disabledCount,
            estimatedTime = estimatedTime,
            typeDistribution = typeDistribution
        )
    }
    
    /**
     * 动作执行结果
     */
    data class ExecutionResult(
        val actionId: String,
        val actionName: String,
        val actionType: String,
        val success: Boolean,
        val startTime: Long,
        val endTime: Long,
        val errorMessage: String? = null
    ) {
        val duration: Long get() = endTime - startTime
    }
    
    /**
     * 带跟踪的执行（返回详细信息）
     */
    suspend fun executeWithTracking(
        context: Context,
        actionData: ActionData
    ): ExecutionResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            val success = executeActionData(context, actionData)
            val endTime = System.currentTimeMillis()
            
            ExecutionResult(
                actionId = actionData.id,
                actionName = actionData.name,
                actionType = actionData.type,
                success = success,
                startTime = startTime,
                endTime = endTime
            )
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            ExecutionResult(
                actionId = actionData.id,
                actionName = actionData.name,
                actionType = actionData.type,
                success = false,
                startTime = startTime,
                endTime = endTime,
                errorMessage = e.message
            )
        }
    }
    
    // ===== 调试工具函数 =====
    
    /**
     * 调试：打印 ActionData 的详细信息
     */
    fun debugActionData(actionData: ActionData) {
        Log.d(TAG, "🔍 调试 ActionData:")
        Log.d(TAG, "  名称: ${actionData.name}")
        Log.d(TAG, "  类型: ${actionData.type}")
        Log.d(TAG, "  ID: ${actionData.id}")
        Log.d(TAG, "  启用: ${actionData.enabled}")
        Log.d(TAG, "  延迟: ${actionData.delayAfter}ms")
        Log.d(TAG, "  参数数量: ${actionData.params.size}")
        
        actionData.params.forEach { (key, param) ->
            Log.d(TAG, "  参数 $key: ${param.getValue()} (类型: ${param::class.simpleName})")
        }
    }
    
    /**
     * 调试：打印 ActionConfig 的详细信息
     */
    fun debugActionConfig(config: ActionConfig) {
        Log.d(TAG, "🔍 调试 ActionConfig:")
        Log.d(TAG, "  类型: ${config.type}")
        Log.d(TAG, "  参数数量: ${config.params.size}")
        
        config.params.forEach { (key, value) ->
            Log.d(TAG, "  参数 $key: $value (类型: ${value::class.simpleName})")
        }
    }
    
    /**
     * 批量调试多个动作
     */
    fun debugActionDataList(actions: List<ActionData>) {
        Log.d(TAG, "🔍 调试动作列表 (${actions.size}个):")
        actions.forEachIndexed { index, action ->
            Log.d(TAG, "  [${index + 1}] ${action.name} [${action.type}] - 参数: ${action.params.size}")
        }
    }
}

// ===== 扩展函数 =====

/**
 * 快速执行 ActionData 的扩展函数
 */
suspend fun ActionData.execute(context: Context): Boolean {
    return ActionConverter.executeActionData(context, this)
}

/**
 * 批量执行 ActionData 列表的扩展函数
 */
suspend fun List<ActionData>.executeAll(context: Context): List<Boolean> {
    return ActionConverter.executeActionDataList(context, this)
}

/**
 * 预估执行时间的扩展函数
 */
fun List<ActionData>.estimateTime(): Long {
    return ActionConverter.estimateExecutionTime(this)
}

/**
 * 获取可读描述的扩展函数
 */
fun ActionData.toReadableDescription(): String {
    return ActionConverter.toReadableDescription(this)
}

/**
 * 分析动作列表的扩展函数
 */
fun List<ActionData>.analyze(): ActionConverter.ActionListStats {
    return ActionConverter.analyzeActionList(this)
}

/**
 * 调试 ActionData 的扩展函数
 */
fun ActionData.debug() {
    ActionConverter.debugActionData(this)
}

/**
 * 调试 ActionConfig 的扩展函数
 */
fun ActionConfig.debug() {
    ActionConverter.debugActionConfig(this)
}