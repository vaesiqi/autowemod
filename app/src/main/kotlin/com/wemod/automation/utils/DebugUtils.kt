// ===========================================
// 文件：app/src/main/kotlin/com/wemod/automation/utils/DebugUtils.kt
// 统一调试工具类 - 支持脚本、动作、执行流程的调试
// ===========================================

package com.wemod.automation.utils

import android.util.Log
import com.wemod.automation.model.ActionData
import com.wemod.automation.model.Script

/**
 * 统一调试工具类
 */
object DebugUtils {
    
    // 日志标签
    const val TAG = "WeModDebug"
    
    // 调试开关
    var isDebugEnabled = true
    
    // ===== 基础调试函数 =====
    
    /**
     * 记录调试信息
     */
    fun d(tag: String, message: String) {
        if (isDebugEnabled) {
            Log.d(tag, "🔍 $message")
        }
    }
    
    /**
     * 记录执行跟踪
     */
    fun trace(stage: String, data: Map<String, Any?> = emptyMap()) {
        if (isDebugEnabled) {
            val dataStr = if (data.isNotEmpty()) {
                data.entries.joinToString(", ") { (k, v) -> "$k=$v" }
            } else ""
            
            Log.d(TAG, "🚀 [$stage] ${if (dataStr.isNotEmpty()) "($dataStr)" else ""}")
        }
    }
    
    /**
     * 记录错误信息
     */
    fun e(tag: String, message: String, exception: Exception? = null) {
        Log.e(tag, "❌ $message", exception)
    }
    
    /**
     * 记录警告信息
     */
    fun w(tag: String, message: String) {
        Log.w(tag, "⚠️ $message")
    }
    
    /**
     * 记录成功信息
     */
    fun s(tag: String, message: String) {
        Log.d(tag, "✅ $message")
    }
    
    // ===== 脚本调试函数 =====
    
    /**
     * 调试脚本信息
     */
    fun debugScript(script: Script, prefix: String = "") {
        if (!isDebugEnabled) return
        
        Log.d(TAG, "${prefix}📄 脚本调试信息:")
        Log.d(TAG, "${prefix}  名称: ${script.name}")
        Log.d(TAG, "${prefix}  ID: ${script.id}")
        Log.d(TAG, "${prefix}  创建时间: ${formatTime(script.createdAt)}")
        Log.d(TAG, "${prefix}  修改时间: ${formatTime(script.modifiedAt)}")
        Log.d(TAG, "${prefix}  动作数量: ${script.actions.size}")
        Log.d(TAG, "${prefix}  启用的动作: ${script.actions.count { it.enabled }}")
        
        // 动作类型统计
        val typeStats = script.actions.groupingBy { it.type }.eachCount()
        if (typeStats.isNotEmpty()) {
            Log.d(TAG, "${prefix}  动作类型分布:")
            typeStats.forEach { (type, count) ->
                Log.d(TAG, "${prefix}    $type: $count")
            }
        }
        
        // 详细调试每个动作
        if (script.actions.isNotEmpty()) {
            Log.d(TAG, "${prefix}  动作详情:")
            script.actions.forEachIndexed { index, action ->
                debugActionData(action, "${prefix}    [${index + 1}] ")
            }
        }
    }
    
    /**
     * 调试动作数据
     */
    fun debugActionData(action: ActionData, prefix: String = "") {
        if (!isDebugEnabled) return
        
        Log.d(TAG, "${prefix}${if (action.enabled) "▶️" else "⏸️"} ${action.name} [${action.type}]")
        Log.d(TAG, "${prefix}  ID: ${action.id}")
        Log.d(TAG, "${prefix}  启用: ${action.enabled}")
        Log.d(TAG, "${prefix}  延迟: ${action.delayAfter}ms")
        Log.d(TAG, "${prefix}  参数数量: ${action.params.size}")
        
        // 参数详情
        if (action.params.isNotEmpty()) {
            action.params.forEach { (key, param) ->
                val value = param.getValue()
                val typeName = param::class.simpleName ?: "Unknown"
                Log.d(TAG, "${prefix}  参数 $key: $value ($typeName)")
            }
        }
    }
    
    /**
     * 批量调试脚本列表
     */
    fun debugScriptList(scripts: List<Script>) {
        if (!isDebugEnabled) return
        
        Log.d(TAG, "📋 脚本列表调试 (${scripts.size}个):")
        
        if (scripts.isEmpty()) {
            Log.d(TAG, "  列表为空")
            return
        }
        
        scripts.forEachIndexed { index, script ->
            Log.d(TAG, "  [${index + 1}] ${script.name} - ${script.actions.size}个动作")
        }
        
        // 统计信息
        val totalActions = scripts.sumOf { it.actions.size }
        val enabledActions = scripts.sumOf { script -> 
            script.actions.count { it.enabled }
        }
        
        Log.d(TAG, "📊 统计信息:")
        Log.d(TAG, "  总脚本数: ${scripts.size}")
        Log.d(TAG, "  总动作数: $totalActions")
        Log.d(TAG, "  启用动作: $enabledActions")
        Log.d(TAG, "  禁用动作: ${totalActions - enabledActions}")
    }
    
    /**
     * 调试动作执行流程
     */
    fun debugActionExecution(
        action: ActionData,
        stage: String,
        success: Boolean? = null,
        extraInfo: Map<String, Any?> = emptyMap()
    ) {
        if (!isDebugEnabled) return
        
        val statusIcon = when (success) {
            true -> "✅"
            false -> "❌"
            null -> "🔧"
        }
        
        Log.d(TAG, "$statusIcon 动作执行 [$stage]:")
        Log.d(TAG, "  名称: ${action.name}")
        Log.d(TAG, "  类型: ${action.type}")
        Log.d(TAG, "  动作ID: ${action.id}")
        
        if (success != null) {
            Log.d(TAG, "  结果: ${if (success) "成功" else "失败"}")
        }
        
        if (extraInfo.isNotEmpty()) {
            Log.d(TAG, "  额外信息:")
            extraInfo.forEach { (key, value) ->
                Log.d(TAG, "    $key: $value")
            }
        }
    }
    
    // ===== 执行流程跟踪 =====
    
    /**
     * 执行流程追踪
     */
    fun traceExecution(stage: String, data: Map<String, Any?> = emptyMap()) {
        trace(stage, data)
    }
    
    /**
     * 开始执行阶段
     */
    fun startStage(stage: String) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "▶️ 开始: $stage")
        Log.d(TAG, "═══════════════════════════════════════")
    }
    
    /**
     * 结束执行阶段
     */
    fun endStage(stage: String, success: Boolean = true) {
        val icon = if (success) "✅" else "❌"
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "$icon 结束: $stage")
        Log.d(TAG, "═══════════════════════════════════════")
    }
    
    // ===== 辅助函数 =====
    
    /**
     * 格式化时间戳
     */
    private fun formatTime(timestamp: Long): String {
        return try {
            val date = java.util.Date(timestamp)
            val formatter = java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            timestamp.toString()
        }
    }
    
    /**
     * 打印分隔线
     */
    fun separator(title: String = "") {
        if (!isDebugEnabled) return
        
        val line = "=".repeat(40)
        if (title.isNotEmpty()) {
            Log.d(TAG, "\n$line")
            Log.d(TAG, "  $title")
            Log.d(TAG, "$line\n")
        } else {
            Log.d(TAG, line)
        }
    }
    
    /**
     * 调试内存信息
     */
    fun debugMemoryInfo(tag: String) {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory() / (1024 * 1024)  // MB
        val freeMemory = runtime.freeMemory() / (1024 * 1024)    // MB
        val usedMemory = totalMemory - freeMemory
        
        Log.d(tag, "💾 内存信息:")
        Log.d(tag, "  总内存: ${totalMemory}MB")
        Log.d(tag, "  已使用: ${usedMemory}MB")
        Log.d(tag, "  空闲内存: ${freeMemory}MB")
        Log.d(tag, "  使用率: ${String.format("%.1f", usedMemory.toFloat() / totalMemory * 100)}%")
    }
}