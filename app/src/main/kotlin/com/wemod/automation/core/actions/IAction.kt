// ===========================================
// 文件：app/src/main/kotlin/com/wemod/automation/core/actions/IAction.kt
// 最终版：消除未检查的强制转换警告，更安全的类型处理
// ===========================================

package com.wemod.automation.core.actions

import android.content.Context
import androidx.compose.runtime.Composable
import com.wemod.automation.model.ActionParam

/**
 * 插件化动作接口
 * 所有动作类型都应该实现此接口
 */
interface IAction {
    
    /**
     * 动作类型标识符
     */
    val type: String
    
    /**
     * 动作显示名称
     */
    val displayName: String
    
    /**
     * 动作描述
     */
    val description: String
    
    /**
     * 执行动作
     * @param context 上下文
     * @param config 动作配置
     * @return 执行是否成功
     */
    suspend fun execute(context: Context, config: ActionConfig): Boolean
    
    /**
     * 验证动作配置
     * @param config 动作配置
     * @return 验证结果
     */
    fun validate(config: ActionConfig): ValidationResult
    
    /**
     * 获取配置UI
     * 用于编辑器中的参数配置
     */
    @Composable
    fun ConfigUI(
        config: ActionConfig,
        onConfigChanged: (ActionConfig) -> Unit
    )
}

/**
 * 动作配置数据类
 */
data class ActionConfig(
    val type: String,
    val params: Map<String, Any> = emptyMap()
) {
    
    /**
     * 获取参数值（带默认值）- 优化版，消除强制转换警告
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getParam(key: String, defaultValue: T): T {
        val value = params[key] ?: return defaultValue
        
        // 使用 reified 的替代方案：通过 when 表达式进行类型安全的转换
        return when (defaultValue) {
            is Int -> getIntValue(value, defaultValue) as T
            is Long -> getLongValue(value, defaultValue) as T
            is String -> getStringValue(value, defaultValue) as T
            is Boolean -> getBooleanValue(value, defaultValue) as T
            is Float -> getFloatValue(value, defaultValue) as T
            is Double -> getDoubleValue(value, defaultValue) as T
            else -> defaultValue
        }
    }
    
    // 辅助函数：类型安全的获取值
    private fun getIntValue(value: Any, defaultValue: Int): Int {
        return when (value) {
            is Int -> value
            is ActionParam.IntParam -> value.value
            is Number -> value.toInt()
            else -> try {
                value.toString().toInt()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    private fun getLongValue(value: Any, defaultValue: Long): Long {
        return when (value) {
            is Long -> value
            is ActionParam.LongParam -> value.value
            is Number -> value.toLong()
            else -> try {
                value.toString().toLong()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    private fun getStringValue(value: Any, defaultValue: String): String {
        return when (value) {
            is String -> value
            is ActionParam.StringParam -> value.value
            else -> value.toString()
        }
    }
    
    private fun getBooleanValue(value: Any, defaultValue: Boolean): Boolean {
        return when (value) {
            is Boolean -> value
            is ActionParam.BooleanParam -> value.value
            else -> try {
                value.toString().toBoolean()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    private fun getFloatValue(value: Any, defaultValue: Float): Float {
        return when (value) {
            is Float -> value
            is ActionParam.FloatParam -> value.value
            is Number -> value.toFloat()
            else -> try {
                value.toString().toFloat()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    private fun getDoubleValue(value: Any, defaultValue: Double): Double {
        return when (value) {
            is Double -> value
            is ActionParam.DoubleParam -> value.value
            is Number -> value.toDouble()
            else -> try {
                value.toString().toDouble()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    /**
     * 获取字符串参数（支持 ActionParam）
     */
    fun getString(key: String, defaultValue: String = ""): String {
        val value = params[key] ?: return defaultValue
        
        return when (value) {
            is String -> value
            is ActionParam.StringParam -> value.value
            else -> value.toString()
        }
    }
    
    /**
     * 获取整数参数（支持 ActionParam）
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        val value = params[key] ?: return defaultValue
        
        return when (value) {
            is Int -> value
            is ActionParam.IntParam -> value.value
            is Number -> value.toInt()
            else -> try {
                value.toString().toInt()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    /**
     * 获取长整数参数（支持 ActionParam）
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        val value = params[key] ?: return defaultValue
        
        return when (value) {
            is Long -> value
            is ActionParam.LongParam -> value.value
            is Number -> value.toLong()
            else -> try {
                value.toString().toLong()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    /**
     * 获取布尔参数（支持 ActionParam）
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val value = params[key] ?: return defaultValue
        
        return when (value) {
            is Boolean -> value
            is ActionParam.BooleanParam -> value.value
            else -> try {
                value.toString().toBoolean()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    /**
     * 获取浮点数参数（支持 ActionParam）
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        val value = params[key] ?: return defaultValue
        
        return when (value) {
            is Float -> value
            is ActionParam.FloatParam -> value.value
            is Number -> value.toFloat()
            else -> try {
                value.toString().toFloat()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
    
    /**
     * 获取双精度参数（支持 ActionParam）
     */
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        val value = params[key] ?: return defaultValue
        
        return when (value) {
            is Double -> value
            is ActionParam.DoubleParam -> value.value
            is Number -> value.toDouble()
            else -> try {
                value.toString().toDouble()
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
}

/**
 * 验证结果
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        val VALID = ValidationResult(true)
        
        fun invalid(vararg errors: String): ValidationResult {
            return ValidationResult(false, errors.toList())
        }
        
        fun withWarnings(vararg warnings: String): ValidationResult {
            return ValidationResult(true, warnings = warnings.toList())
        }
    }
}

/**
 * 执行结果
 */
data class ExecutionResult(
    val success: Boolean,
    val message: String = "",
    val data: Map<String, Any> = emptyMap()
) {
    companion object {
        val SUCCESS = ExecutionResult(true, "执行成功")
        
        fun success(message: String = "执行成功", data: Map<String, Any> = emptyMap()): ExecutionResult {
            return ExecutionResult(true, message, data)
        }
        
        fun failure(message: String = "执行失败", data: Map<String, Any> = emptyMap()): ExecutionResult {
            return ExecutionResult(false, message, data)
        }
    }
}