// ===========================================
// 修改文件：app/src/main/kotlin/com/wemod/automation/core/ScriptRunner.kt
// 重构：支持新的 ActionEngine 和 ActionData
// ===========================================

package com.wemod.automation.core

import android.content.Context
import android.util.Log
import com.wemod.automation.model.ActionData
import com.wemod.automation.model.Script
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 脚本运行器 - 高层API封装（重构版）
 * 提供简单的接口来运行和控制脚本
 */
class ScriptRunner(private val context: Context) {
    
    private val TAG = "ScriptRunner"
    
    // 底层引擎实例
    private val actionEngine: ActionEngine = ActionEngine.getInstance(context)
    
    // 当前运行的脚本
    private var currentScript: Script? = null
    
    init {
        Log.d(TAG, "ScriptRunner初始化完成")
    }
    
    /**
     * 运行脚本
     * @param script 要运行的脚本
     * @param loopCount 循环次数
     * @return 是否成功启动
     */
    fun runScript(script: Script, loopCount: Int = 1): Boolean {
        Log.d(TAG, "准备运行脚本: ${script.name}")
        
        try {
            // 检查无障碍服务
            if (!com.wemod.automation.core.accessibility.EnhancedAccessibilityManager.isRunning()) {
                Log.w(TAG, "无障碍服务未开启，无法运行脚本")
                return false
            }
            
            // 检查脚本是否为空
            if (script.actions.isEmpty()) {
                Log.w(TAG, "脚本为空，无法运行")
                return false
            }
            
            // 检查是否有脚本正在运行
            if (isRunning() || isPaused()) {
                Log.w(TAG, "已有脚本在运行中，无法运行新脚本")
                return false
            }
            
            // 保存当前脚本
            currentScript = script
            
            // 调用底层引擎执行脚本
            actionEngine.executeScript(script, loopCount)
            
            Log.i(TAG, "脚本已开始执行: ${script.name}")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "运行脚本失败: ${script.name}", e)
            currentScript = null
            return false
        }
    }
    
    /**
     * 运行测试脚本（用于功能测试）
     */
    fun runTestScript(): Boolean {
        Log.d(TAG, "运行测试脚本")
        
        try {
            // 检查无障碍服务
            if (!com.wemod.automation.core.accessibility.EnhancedAccessibilityManager.isRunning()) {
                Log.w(TAG, "无障碍服务未开启，无法运行测试脚本")
                return false
            }
            
            // 创建测试脚本
            val testScript = Script(
                id = "test_script_${System.currentTimeMillis()}",
                name = "测试脚本",
                actions = mutableListOf(
                    ActionData.createClick(name = "测试点击"),
                    ActionData.createWait(name = "等待1秒", duration = 1000L),
                    ActionData.createSwipe(name = "测试滑动", duration = 500L)
                )
            )
            
            return runScript(testScript, 1)
            
        } catch (e: Exception) {
            Log.e(TAG, "运行测试脚本失败", e)
            return false
        }
    }
    
    /**
     * 暂停当前运行的脚本
     */
    fun pause(): Boolean {
        Log.d(TAG, "暂停脚本")
        
        return try {
            actionEngine.pause()
            true
        } catch (e: Exception) {
            Log.e(TAG, "暂停脚本失败", e)
            false
        }
    }
    
    /**
     * 恢复暂停的脚本
     */
    fun resume(): Boolean {
        Log.d(TAG, "恢复脚本")
        
        return try {
            actionEngine.resume()
            true
        } catch (e: Exception) {
            Log.e(TAG, "恢复脚本失败", e)
            false
        }
    }
    
    /**
     * 停止当前运行的脚本
     */
    fun stop(): Boolean {
        Log.d(TAG, "停止脚本")
        
        return try {
            actionEngine.stop()
            currentScript = null
            true
        } catch (e: Exception) {
            Log.e(TAG, "停止脚本失败", e)
            false
        }
    }
    
    /**
     * 获取引擎状态流
     */
    fun getEngineState(): StateFlow<ActionEngine.EngineState> {
        return actionEngine.engineState
    }
    
    /**
     * 获取当前进度流
     * @return Flow<Pair<当前动作索引, 总动作数>>
     */
    fun getProgressFlow(): Flow<Pair<Int, Int>> {
        return actionEngine.getProgressFlow()
    }
    
    /**
     * 获取进度百分比流
     * @return Flow<Int> 0-100的百分比
     */
    fun getProgressPercentageFlow(): Flow<Int> {
        return actionEngine.getProgressPercentageFlow()
    }
    
    /**
     * 获取当前进度（兼容旧API）
     * @return Pair(当前动作索引, 总动作数)
     */
    fun getProgress(): Pair<Int, Int> {
        val current = actionEngine.currentProgress.value
        val total = actionEngine.totalActions.value
        Log.d(TAG, "获取进度: $current/$total")
        return Pair(current, total)
    }
    
    /**
     * 获取当前脚本名称
     */
    fun getCurrentScriptName(): String {
        val name = currentScript?.name ?: actionEngine.getCurrentScriptName()
        Log.d(TAG, "获取当前脚本名称: $name")
        return name
    }
    
    /**
     * 获取当前执行的脚本
     */
    fun getCurrentScript(): Script? {
        return currentScript
    }
    
    /**
     * 检查是否有脚本正在运行
     */
    fun isRunning(): Boolean {
        val state = actionEngine.engineState.value
        val isRunning = state == ActionEngine.EngineState.RUNNING
        Log.d(TAG, "检查运行状态: $state -> $isRunning")
        return isRunning
    }
    
    /**
     * 检查脚本是否暂停
     */
    fun isPaused(): Boolean {
        val isPaused = actionEngine.engineState.value == ActionEngine.EngineState.PAUSED
        Log.d(TAG, "检查暂停状态: $isPaused")
        return isPaused
    }
    
    /**
     * 检查脚本是否空闲
     */
    fun isIdle(): Boolean {
        val isIdle = actionEngine.engineState.value == ActionEngine.EngineState.IDLE
        Log.d(TAG, "检查空闲状态: $isIdle")
        return isIdle
    }
    
    /**
     * 检查脚本是否错误
     */
    fun isError(): Boolean {
        val isError = actionEngine.engineState.value == ActionEngine.EngineState.ERROR
        Log.d(TAG, "检查错误状态: $isError")
        return isError
    }
    
    /**
     * 获取状态文本描述
     */
    fun getStatus(): String {
        val state = actionEngine.engineState.value
        val scriptName = getCurrentScriptName()
        
        return when (state) {
            ActionEngine.EngineState.IDLE -> "空闲"
            ActionEngine.EngineState.RUNNING -> "运行中: $scriptName"
            ActionEngine.EngineState.PAUSED -> "已暂停: $scriptName"
            ActionEngine.EngineState.STOPPED -> "已停止"
            ActionEngine.EngineState.ERROR -> "错误状态"
        }
    }
    
    /**
     * 获取引擎状态文本
     */
    fun getEngineStateText(): String {
        return actionEngine.engineState.value.toString()
    }
    
    /**
     * 获取详细的执行信息
     */
    fun getExecutionInfo(): String {
        return actionEngine.getCurrentExecutionInfo()
    }
    
    /**
     * 获取进度百分比
     */
    fun getProgressPercentage(): Int {
        return actionEngine.getProgressPercentage()
    }
    
    /**
     * 重置运行器状态
     */
    fun reset() {
        Log.d(TAG, "重置ScriptRunner")
        currentScript = null
        // 注意：不重置底层引擎，因为它可能有其他用途
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        Log.d(TAG, "清理ScriptRunner资源")
        stop()
        reset()
        // 注意：不清理ActionEngine实例，因为它可能被其他地方使用
    }
    
    /**
     * 获取执行统计信息
     */
    fun getExecutionStats(): ActionEngine.ExecutionStats {
        return actionEngine.getExecutionStats()
    }
    
    /**
     * 预估剩余时间
     */
    fun estimateRemainingTime(): Long {
        return actionEngine.estimateRemainingTime()
    }
}