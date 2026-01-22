// ===========================================
// 文件：app/src/main/kotlin/com/wemod/automation/core/ActionEngine.kt
// 重构：支持 ActionData 和插件化动作系统
// ===========================================

package com.wemod.automation.core

import android.content.Context
import android.util.Log
import com.wemod.automation.core.actions.ActionConverter
import com.wemod.automation.model.ActionData
import com.wemod.automation.model.Script
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 动作执行引擎 - 重构版
 * 特性：
 * 1. 支持 ActionData 数据模型
 * 2. 集成 ActionConverter 执行插件化动作
 * 3. 完整的暂停/恢复/停止控制
 * 4. 实时进度反馈
 */
class ActionEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "ActionEngine"
        
        // 单例实例（带Context）
        private var instance: ActionEngine? = null
        
        /**
         * 获取或创建 ActionEngine 实例
         * @param context 应用上下文
         */
        fun getInstance(context: Context): ActionEngine {
            return instance ?: synchronized(this) {
                instance ?: ActionEngine(context.applicationContext).also { instance = it }
            }
        }
        
        /**
         * 销毁实例（用于测试或资源清理）
         */
        fun destroyInstance() {
            instance?.cleanup()
            instance = null
        }
    }
    
    // 协程作用域
    private val engineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentExecutionJob: Job? = null
    
    // 引擎状态
    enum class EngineState {
        IDLE,       // 空闲
        RUNNING,    // 运行中
        PAUSED,     // 已暂停
        STOPPED,    // 已停止
        ERROR       // 错误
    }
    
    private val _engineState = MutableStateFlow(EngineState.IDLE)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()
    
    // 当前执行进度
    private val _currentProgress = MutableStateFlow(0)
    val currentProgress: StateFlow<Int> = _currentProgress.asStateFlow()
    
    private val _totalActions = MutableStateFlow(0)
    val totalActions: StateFlow<Int> = _totalActions.asStateFlow()
    
    // 当前执行的脚本和动作
    private var currentScript: Script? = null
    private var currentActionIndex: Int = 0
    private var currentActionList: List<ActionData> = emptyList()
    
    // 暂停控制
    private val pauseLock = java.lang.Object()
    private var isPaused = false
    
    // 统计信息
    private var startTime: Long = 0
    private var successCount: Int = 0
    private var failureCount: Int = 0
    
    init {
        Log.d(TAG, "ActionEngine 初始化完成")
    }
    
    /**
     * 执行脚本（使用 ActionData）
     * @param script 要执行的脚本
     * @param loopCount 循环次数（0表示无限循环）
     */
    fun executeScript(script: Script, loopCount: Int = 1) {
        Log.d(TAG, "=== executeScript 开始 ===")
        Log.d(TAG, "脚本: ${script.name}, 动作数: ${script.actions.size}, 循环: $loopCount")
        Log.d(TAG, "当前引擎状态: ${_engineState.value}")
        
        // 检查是否已在运行
        if (_engineState.value == EngineState.RUNNING || _engineState.value == EngineState.PAUSED) {
            Log.w(TAG, "引擎已在运行中，停止当前执行")
            stop()
            // 短暂延迟确保状态重置
            Thread.sleep(100)
        }
        
        // 重置状态并设置脚本信息
        resetState()
        currentScript = script
        currentActionList = script.actions
        _totalActions.value = script.actions.size
        
        Log.d(TAG, "动作列表大小: ${script.actions.size}")
        Log.d(TAG, "重置后引擎状态: ${_engineState.value}")
        
        // 验证脚本
        val validationResults = ActionConverter.validateActionDataList(script.actions)
        val invalidActions = validationResults.filter { !it.isValid }
        
        if (invalidActions.isNotEmpty()) {
            Log.e(TAG, "脚本验证失败，有 ${invalidActions.size} 个无效动作")
            _engineState.value = EngineState.ERROR
            return
        }
        
        // 开始执行
        startTime = System.currentTimeMillis()
        successCount = 0
        failureCount = 0
        
        currentExecutionJob = engineScope.launch {
            Log.d(TAG, "协程启动，开始执行脚本")
            
            try {
                Log.i(TAG, "开始执行脚本: ${script.name}")
                
                // 设置状态为 RUNNING
                _engineState.value = EngineState.RUNNING
                Log.d(TAG, "引擎状态设置为 RUNNING")
                
                // 执行循环
                var loopsRemaining = loopCount
                var loopIndex = 1
                
                while (loopsRemaining > 0 || loopCount == 0) {
                    Log.d(TAG, "开始第 $loopIndex 轮循环 (剩余: ${if (loopCount == 0) "∞" else loopsRemaining})")
                    
                    // 执行脚本中的所有动作
                    executeActions(script.actions)
                    
                    // 检查是否被停止
                    if (_engineState.value != EngineState.RUNNING && _engineState.value != EngineState.PAUSED) {
                        Log.d(TAG, "循环被中断，当前状态: ${_engineState.value}")
                        break
                    }
                    
                    loopsRemaining--
                    loopIndex++
                    
                    // 如果不是最后一轮，等待循环间隔
                    if (loopsRemaining > 0 || loopCount == 0) {
                        Log.d(TAG, "等待循环间隔...")
                        delay(1000) // 循环间隔
                    }
                }
                
                // 执行完成
                val totalTime = System.currentTimeMillis() - startTime
                Log.i(TAG, "脚本执行完成: ${script.name}, 耗时: ${totalTime}ms, 成功: $successCount, 失败: $failureCount")
                
                if (failureCount == 0) {
                    _engineState.value = EngineState.IDLE
                    Log.d(TAG, "执行完成，状态设置为 IDLE")
                } else {
                    _engineState.value = EngineState.ERROR
                    Log.w(TAG, "执行完成但有失败动作，状态设置为 ERROR")
                }
                
            } catch (e: CancellationException) {
                Log.i(TAG, "执行被取消: ${script.name}")
                _engineState.value = EngineState.STOPPED
            } catch (e: Exception) {
                Log.e(TAG, "执行脚本时出错: ${script.name}", e)
                _engineState.value = EngineState.ERROR
            } finally {
                val totalTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "协程执行结束，总耗时: ${totalTime}ms, 最终状态: ${_engineState.value}")
            }
        }
        
        Log.d(TAG, "executeScript 函数结束")
    }
    
    /**
     * 执行动作列表 - 修复版：确保进度实时更新
     */
    private suspend fun executeActions(actions: List<ActionData>) {
        Log.d(TAG, "=== executeActions 开始 ===")
        Log.d(TAG, "动作总数: ${actions.size}")
        
        // 🎯 关键修复：在循环开始前重置进度为0
        currentActionIndex = 0
        _currentProgress.value = 0  // 确保初始进度为0
        Log.d(TAG, "重置动作索引为0，初始进度为0")
        
        for ((index, action) in actions.withIndex()) {
            // 🎯 关键修复：在循环开始时更新进度
            currentActionIndex = index
            _currentProgress.value = index  // 设置当前要执行的动作索引
            
            Log.d(TAG, "--- 处理动作 [$index/${actions.size}] ---")
            Log.d(TAG, "当前动作索引: $currentActionIndex")
            Log.d(TAG, "当前进度: ${_currentProgress.value}/${_totalActions.value}")
            Log.d(TAG, "动作名称: ${action.name}")
            Log.d(TAG, "动作类型: ${action.type}")
            Log.d(TAG, "动作是否启用: ${action.enabled}")
            
            // 检查是否被暂停
            checkPauseState()
            
            // 检查是否被停止
            if (_engineState.value != EngineState.RUNNING) {
                Log.w(TAG, "⚠️ 执行被中断！当前状态: ${_engineState.value}")
                Log.w(TAG, "期望状态: RUNNING, 实际状态: ${_engineState.value}")
                return
            }
            
            // 只执行启用的动作
            if (action.enabled) {
                Log.d(TAG, "✅ 执行动作 [$index/${actions.size}]: ${action.name} [${action.type}]")
                val success = executeSingleAction(action)
                
                if (success) {
                    successCount++
                    Log.d(TAG, "✅ 动作执行成功")
                } else {
                    failureCount++
                    Log.w(TAG, "❌ 动作执行失败")
                    
                    // 如果配置了失败后停止，则中断执行
                    // 这里可以添加配置选项
                }
            } else {
                Log.d(TAG, "⏭️ 跳过禁用的动作: ${action.name}")
            }
            
            // 🎯 关键修复：动作完成后也更新进度
            _currentProgress.value = index + 1
            Log.d(TAG, "动作完成，进度更新为: ${_currentProgress.value}/${_totalActions.value}")
        }
        
        // 所有动作执行完成
        Log.d(TAG, "✅ 所有动作执行完成")
        _currentProgress.value = actions.size
        Log.d(TAG, "最终进度: ${_currentProgress.value}/${actions.size}")
    }
    
    /**
     * 执行单个动作（使用 ActionConverter）
     */
    private suspend fun executeSingleAction(action: ActionData): Boolean {
        return try {
            // 🎯 关键修改：使用 ActionConverter 执行动作
            val success = ActionConverter.executeActionData(context, action)
            
            if (!success) {
                Log.w(TAG, "❌ 动作执行失败: ${action.name}")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "执行动作失败: ${action.name}", e)
            false
        }
    }
    
    /**
     * 暂停执行
     */
    fun pause() {
        Log.d(TAG, "调用pause()，当前状态: ${_engineState.value}")
        if (_engineState.value == EngineState.RUNNING) {
            isPaused = true
            _engineState.value = EngineState.PAUSED
            Log.i(TAG, "引擎已暂停，新状态: ${_engineState.value}")
        } else {
            Log.w(TAG, "无法暂停，当前状态不是RUNNING: ${_engineState.value}")
        }
    }
    
    /**
     * 恢复执行
     */
    fun resume() {
        Log.d(TAG, "调用resume()，当前状态: ${_engineState.value}")
        if (_engineState.value == EngineState.PAUSED) {
            synchronized(pauseLock) {
                isPaused = false
                pauseLock.notifyAll()
            }
            _engineState.value = EngineState.RUNNING
            Log.i(TAG, "引擎已恢复，新状态: ${_engineState.value}")
        } else {
            Log.w(TAG, "无法恢复，当前状态不是PAUSED: ${_engineState.value}")
        }
    }
    
    /**
     * 停止执行
     */
    fun stop() {
        Log.d(TAG, "调用stop()，当前状态: ${_engineState.value}")
        currentExecutionJob?.cancel("用户停止")
        resetState()
        Log.i(TAG, "引擎已停止，状态重置")
    }
    
    /**
     * 检查暂停状态（阻塞直到恢复）
     */
    private suspend fun checkPauseState() {
        if (isPaused) {
            Log.d(TAG, "检测到暂停状态，等待恢复...")
            while (isPaused) {
                delay(100)
                // 检查状态是否变化
                if (_engineState.value != EngineState.PAUSED) {
                    Log.d(TAG, "暂停状态检查：外部状态已变为 ${_engineState.value}")
                    isPaused = false
                }
            }
            Log.d(TAG, "暂停结束，继续执行")
        }
    }
    
    /**
     * 重置引擎状态
     */
    private fun resetState() {
        Log.d(TAG, "重置引擎状态")
        currentScript = null
        currentActionList = emptyList()
        currentActionIndex = 0
        _currentProgress.value = 0
        _totalActions.value = 0
        isPaused = false
        _engineState.value = EngineState.IDLE  // 明确设置为IDLE
        Log.d(TAG, "引擎状态已重置为IDLE")
    }
    
    /**
     * 获取当前执行信息
     */
    fun getCurrentExecutionInfo(): String {
        val state = _engineState.value
        return when (state) {
            EngineState.IDLE -> "空闲"
            EngineState.RUNNING -> {
                val scriptName = currentScript?.name ?: "未知脚本"
                val progress = if (_totalActions.value > 0) {
                    "${currentActionIndex + 1}/${_totalActions.value}"
                } else {
                    "0/0"
                }
                "运行中: $scriptName ($progress)"
            }
            EngineState.PAUSED -> {
                val scriptName = currentScript?.name ?: "未知脚本"
                "已暂停: $scriptName"
            }
            EngineState.STOPPED -> "已停止"
            EngineState.ERROR -> "错误状态"
        }
    }
    
    /**
     * 获取当前执行的脚本名称
     */
    fun getCurrentScriptName(): String {
        return currentScript?.name ?: "无"
    }
    
    /**
     * 获取当前执行的脚本
     */
    fun getCurrentScript(): Script? {
        return currentScript
    }
    
    /**
     * 获取当前进度百分比 (0-100)
     */
    fun getProgressPercentage(): Int {
        val total = _totalActions.value
        val current = _currentProgress.value
        return if (total > 0) {
            (current * 100 / total).coerceIn(0, 100)
        } else {
            0
        }
    }
    
    /**
     * 获取当前动作索引
     */
    fun getCurrentActionIndex(): Int {
        return currentActionIndex
    }
    
    /**
     * 获取当前执行的动作
     */
    fun getCurrentAction(): ActionData? {
        return if (currentActionIndex < currentActionList.size) {
            currentActionList[currentActionIndex]
        } else {
            null
        }
    }
    
    /**
     * 获取执行统计信息
     */
    fun getExecutionStats(): ExecutionStats {
        val totalTime = if (startTime > 0) System.currentTimeMillis() - startTime else 0L
        val totalActions = successCount + failureCount
        val successRate = if (totalActions > 0) (successCount * 100 / totalActions) else 0
        
        return ExecutionStats(
            startTime = startTime,
            totalTime = totalTime,
            successCount = successCount,
            failureCount = failureCount,
            totalActions = totalActions,
            successRate = successRate,
            currentState = _engineState.value
        )
    }
    
    /**
     * 获取实时进度流（供外部监听）
     */
    fun getProgressFlow(): kotlinx.coroutines.flow.Flow<Pair<Int, Int>> {
        return kotlinx.coroutines.flow.combine(
            currentProgress,
            totalActions
        ) { current, total ->
            Pair(current, total)
        }
    }
    
    /**
     * 获取进度百分比流（供UI直接使用）
     */
    fun getProgressPercentageFlow(): kotlinx.coroutines.flow.Flow<Int> {
        return kotlinx.coroutines.flow.combine(
            currentProgress,
            totalActions
        ) { current, total ->
            if (total > 0) {
                (current * 100 / total).coerceIn(0, 100)
            } else {
                0
            }
        }
    }
    
    /**
     * 获取状态流
     */
    fun getStateFlow(): StateFlow<EngineState> {
        return engineState
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        Log.d(TAG, "清理引擎资源")
        stop()
        engineScope.cancel()
        instance = null
        Log.i(TAG, "引擎资源已清理")
    }
    
    /**
     * 执行统计信息
     */
    data class ExecutionStats(
        val startTime: Long,
        val totalTime: Long,
        val successCount: Int,
        val failureCount: Int,
        val totalActions: Int,
        val successRate: Int,
        val currentState: EngineState
    )
    
    /**
     * 预估剩余时间
     */
    fun estimateRemainingTime(): Long {
        if (_totalActions.value == 0 || _currentProgress.value == 0) {
            return 0L
        }
        
        val elapsedTime = System.currentTimeMillis() - startTime
        val progress = _currentProgress.value.toFloat() / _totalActions.value.toFloat()
        
        return if (progress > 0) {
            (elapsedTime / progress - elapsedTime).toLong()
        } else {
            0L
        }
    }
}