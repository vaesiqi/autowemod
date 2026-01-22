// 完全替换 app/src/main/kotlin/com/wemod/automation/core/errors/AppError.kt
// ===========================================

package com.wemod.automation.core.errors

/**
 * 统一错误处理系统（最终简化版）
 */
sealed class AppError(
    open val code: String,
    override val message: String,
    open val severity: ErrorSeverity = ErrorSeverity.ERROR
) : Throwable(message)
    
// ===== 权限错误 (1xx) =====
data class PermissionDenied(
    val permission: String,
    override val code: String = "PERM_001",
    override val message: String = "缺少权限: $permission",
    override val severity: ErrorSeverity = ErrorSeverity.WARNING
) : AppError(code, message, severity)

data class AccessibilityNotEnabled(
    override val code: String = "PERM_002",
    override val message: String = "无障碍服务未开启",
    override val severity: ErrorSeverity = ErrorSeverity.ERROR
) : AppError(code, message, severity)

// ===== 脚本错误 (2xx) =====
data class ScriptNotFound(
    val scriptId: String,
    override val code: String = "SCRIPT_001",
    override val message: String = "脚本不存在: $scriptId",
    override val severity: ErrorSeverity = ErrorSeverity.ERROR
) : AppError(code, message, severity)

data class ScriptExecutionFailed(
    val scriptId: String,
    val actionIndex: Int,
    val actionName: String,
    override val code: String = "SCRIPT_002",
    override val message: String = "脚本执行失败: $scriptId (动作$actionIndex: $actionName)",
    override val severity: ErrorSeverity = ErrorSeverity.ERROR
) : AppError(code, message, severity)

// ===== 动作错误 (3xx) =====
data class InvalidAction(
    val actionType: String,
    override val code: String = "ACTION_001",
    override val message: String = "无效的动作类型: $actionType",
    override val severity: ErrorSeverity = ErrorSeverity.ERROR
) : AppError(code, message, severity)

// ===== 存储错误 (4xx) =====
data class StorageError(
    val operation: String,
    val path: String? = null,
    override val code: String = "STORAGE_001",
    override val message: String = "存储操作失败: $operation${path?.let { " ($it)" } ?: ""}",
    override val severity: ErrorSeverity = ErrorSeverity.ERROR
) : AppError(code, message, severity)

// ===== 引擎错误 (5xx) =====
data class EngineError(
    val state: String,
    val operation: String? = null,
    override val code: String = "ENGINE_001",
    override val message: String = "引擎错误 [$state]${operation?.let { " - $operation" } ?: ""}",
    override val severity: ErrorSeverity = ErrorSeverity.ERROR
) : AppError(code, message, severity)

// ===== 验证错误 (6xx) =====
data class ValidationError(
    val field: String,
    val reason: String,
    override val code: String = "VALID_001",
    override val message: String = "验证失败 [$field]: $reason",
    override val severity: ErrorSeverity = ErrorSeverity.WARNING
) : AppError(code, message, severity)

// ===== 未知错误 (999) =====
data class UnknownError(
    val detail: String? = null,
    override val code: String = "UNKNOWN_001",
    override val message: String = "未知错误${detail?.let { ": $detail" } ?: ""}",
    override val severity: ErrorSeverity = ErrorSeverity.CRITICAL
) : AppError(code, message, severity)

/**
 * 错误严重级别
 */
enum class ErrorSeverity {
    INFO,       // 信息性消息
    WARNING,    // 警告
    ERROR,      // 错误
    CRITICAL    // 严重错误
}

/**
 * 错误处理器接口
 */
interface ErrorHandler {
    fun handleError(error: AppError)
    fun logError(error: AppError)
}

/**
 * 默认错误处理器
 */
object DefaultErrorHandler : ErrorHandler {
    override fun handleError(error: AppError) {
        logError(error)
        
        when (error.severity) {
            ErrorSeverity.CRITICAL -> {
                android.util.Log.wtf("AppError", "CRITICAL: ${error.message}")
            }
            else -> {
                // 其他错误已记录
            }
        }
    }
    
    override fun logError(error: AppError) {
        val logMessage = "[${error.code}] ${error.message}"
        when (error.severity) {
            ErrorSeverity.INFO -> android.util.Log.i("AppError", logMessage)
            ErrorSeverity.WARNING -> android.util.Log.w("AppError", logMessage)
            ErrorSeverity.ERROR -> android.util.Log.e("AppError", logMessage)
            ErrorSeverity.CRITICAL -> android.util.Log.wtf("AppError", logMessage)
        }
    }
}

/**
 * 错误工具函数
 */
object ErrorUtils {
    /**
     * 从Throwable转换为AppError
     */
    fun fromThrowable(e: Throwable): AppError {
        return when (e) {
            is AppError -> e
            is SecurityException -> PermissionDenied("security", message = e.message ?: "安全异常")
            is IllegalArgumentException -> ValidationError("illegal_argument", "参数错误", message = e.message ?: "参数错误")
            is IllegalStateException -> EngineError("illegal_state", message = e.message ?: "状态错误")
            else -> UnknownError(e.message, message = e.message ?: "未知异常")
        }
    }
    
    /**
     * 运行代码并捕获错误
     */
    inline fun <T> runCatching(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Throwable) {
            val appError = fromThrowable(e)
            DefaultErrorHandler.logError(appError)
            Result.failure(appError)
        }
    }
    
    /**
     * 判断错误是否可恢复
     */
    fun isRecoverable(error: AppError): Boolean {
        return when (error.severity) {
            ErrorSeverity.CRITICAL -> false
            ErrorSeverity.ERROR -> false
            else -> true
        }
    }
}