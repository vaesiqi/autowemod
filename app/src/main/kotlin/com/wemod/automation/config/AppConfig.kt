// 替换 app/src/main/kotlin/com/wemod/automation/config/AppConfig.kt
// ===========================================

package com.wemod.automation.config

import android.content.Context
import android.os.Build
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp  // 添加sp导入

/**
 * 应用配置中心 - 统一管理所有配置项
 */
object AppConfig {
    
    // ===== 应用信息 =====
    const val APP_NAME = "WeMod自动化助手"
    const val APP_PACKAGE = "com.wemod.automation"
    const val APP_VERSION = "1.4.0"
    
    // ===== 系统配置 =====
    object System {
        const val MIN_SDK_VERSION = 26  // Android 8.0
        const val TARGET_SDK_VERSION = 34
        const val COMPILE_SDK_VERSION = 34
        
        // 设备兼容性
        val IS_ANDROID_M_OR_ABOVE: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        
        val IS_ANDROID_O_OR_ABOVE: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        
        val IS_ANDROID_R_OR_ABOVE: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        
        // 构建类型 - 简化处理，不使用BuildConfig
        val IS_DEBUG_BUILD: Boolean = true  // 默认true，AndroidIDE中通常是debug构建
    }
    
    // ===== 权限配置 =====
    object Permissions {
        const val CHECK_INTERVAL = 2000L  // 2秒
        const val AUTO_REFRESH_ENABLED = true
        
        // 权限向导
        const val SHOW_WIZARD_ON_FIRST_LAUNCH = true
        const val SHOW_WIZARD_WHEN_MISSING = true
        const val WIZARD_TIMEOUT = 30000L  // 30秒超时
        
        // 权限状态缓存
        const val CACHE_DURATION = 5000L  // 5秒
    }
    
    // ===== 存储配置 =====
    object Storage {
        // 目录结构
        const val SCRIPTS_DIR = "scripts"
        const val BACKUP_DIR = "backups"
        const val TEMP_DIR = "temp"
        const val LOGS_DIR = "logs"
        
        // 文件扩展名
        const val SCRIPT_FILE_EXTENSION = ".axs"
        const val EXPORT_FILE_EXTENSION = ".json"
        const val BACKUP_FILE_EXTENSION = ".backup"
        
        // 缓存配置
        const val MEMORY_CACHE_ENABLED = true
        const val CACHE_EXPIRY_TIME = 5 * 60 * 1000L  // 5分钟
        const val MAX_CACHE_SIZE = 50  // 最大缓存脚本数
        
        // 备份配置
        const val AUTO_BACKUP_ENABLED = true
        const val BACKUP_INTERVAL = 24 * 60 * 60 * 1000L  // 24小时
        const val MAX_BACKUP_COUNT = 5
        const val BACKUP_ON_SAVE = true
        
        // 性能优化
        const val USE_ASYNC_SAVE = true
        const val BATCH_OPERATION_SIZE = 10
    }
    
    // ===== UI配置 =====
    object UI {
        // 动画
        const val ANIMATION_ENABLED = true
        const val ANIMATION_DURATION = 300
        const val SIDEBAR_ANIMATION_DELAY_STEP = 50
        const val FADE_DURATION = 200
        const val SLIDE_DURATION = 300
        
        // 布局
        val CARD_CORNER_RADIUS = 12.dp
        val BUTTON_CORNER_RADIUS = 8.dp
        val LIST_ITEM_HEIGHT = 62.dp
        val ICON_SIZE_SMALL = 16.dp
        val ICON_SIZE_MEDIUM = 24.dp
        val ICON_SIZE_LARGE = 32.dp
        
        // 权重分配
        const val TOP_SECTION_WEIGHT = 0.2f
        const val BOTTOM_SECTION_WEIGHT = 0.8f
        const val SIDEBAR_WIDTH_DP = 280
        
        // 间距系统
        val SPACING_XS = 4.dp
        val SPACING_SM = 8.dp
        val SPACING_MD = 16.dp
        val SPACING_LG = 24.dp
        val SPACING_XL = 32.dp
        
        // 文本大小
        val TEXT_SIZE_XS = 10.sp
        val TEXT_SIZE_SM = 12.sp
        val TEXT_SIZE_MD = 14.sp
        val TEXT_SIZE_LG = 16.sp
        val TEXT_SIZE_XL = 18.sp
        val TEXT_SIZE_XXL = 22.sp
        
        // 透明度
        const val DISABLED_ALPHA = 0.38f
        const val HOVER_ALPHA = 0.08f
        const val FOCUS_ALPHA = 0.12f
        const val SELECTED_ALPHA = 0.16f
    }
    
    // ===== 引擎配置 =====
    object Engine {
        // 协程配置
        const val DEFAULT_DISPATCHER = "IO"
        const val ENGINE_SCOPE_NAME = "ActionEngine"
        const val MAX_CONCURRENT_ACTIONS = 1  // 单线程执行
        
        // 执行配置
        const val DEFAULT_DELAY_AFTER_ACTION = 1000L
        const val MAX_EXECUTION_TIME = 10 * 60 * 1000L  // 10分钟
        const val MAX_LOOP_COUNT = 100
        const val LOOP_INTERVAL = 1000L  // 循环间隔
        
        // 容错配置
        const val RETRY_COUNT = 3
        const val RETRY_DELAY = 1000L
        const val CONTINUE_ON_ERROR = false  // 出错是否继续
        
        // 高级功能开关
        const val CONDITIONAL_EXECUTION_ENABLED = true
        const val LOOP_SUPPORT_ENABLED = true
        const val VARIABLE_SUPPORT_ENABLED = true
        
        // 调试配置
        const val LOG_LEVEL = "INFO"  // DEBUG, INFO, WARN, ERROR
        const val LOG_EXECUTION_DETAILS = true
        const val PROFILE_PERFORMANCE = false
    }
    
    // ===== 悬浮窗配置 =====
    object Overlay {
        // 尺寸配置
        const val DEFAULT_WIDTH = 300
        const val DEFAULT_HEIGHT = 400
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 2.0f
        const val EDGE_SIZE = 20f  // 边缘识别区域
        
        // 位置配置
        const val DEFAULT_X = 100
        const val DEFAULT_Y = 100
        const val SNAP_TO_EDGE = true
        const val SNAP_THRESHOLD = 50  // 距离边缘多少像素自动吸附
        
        // 交互配置
        const val DRAG_ENABLED = true
        const val RESIZE_ENABLED = true
        const val KEEP_RATIO = true  // 保持宽高比
        const val DOUBLE_CLICK_TO_RESET = true
        
        // 视觉配置
        const val SHOW_BORDER = true
        const val BORDER_WIDTH = 2f
        const val SHOW_SHADOW = true
        const val OPACITY = 0.9f
    }
    
    // ===== 无障碍服务配置 =====
    object Accessibility {
        // 服务配置
        const val SERVICE_NAME = "WeMod自动化服务"
        const val SERVICE_DESCRIPTION = "提供自动化点击、滑动等功能"
        const val SERVICE_ID = "com.wemod.automation/.core.AccessibilityService"
        
        // 性能配置
        const val GESTURE_TIMEOUT = 5000L
        const val CLICK_DURATION = 50L
        const val SWIPE_DURATION = 500L
        
        // 安全配置
        const val SAFE_MODE = true
        const val PREVENT_INTERCEPTION = true
        const val LOG_EVENTS = false  // 是否记录无障碍事件
    }
    
    // ===== 高级功能配置 =====
    object Advanced {
        // 图像识别（迭代3）
        const val IMAGE_RECOGNITION_ENABLED = false
        const val SCREENSHOT_QUALITY = 80
        const val MATCH_THRESHOLD = 0.8f
        const val MAX_RECOGNITION_TIME = 5000L
        
        // 条件判断（迭代4）
        const val CONDITION_MAX_DEPTH = 10
        const val CONDITION_TIMEOUT = 5000L
        const val SUPPORT_NESTED_CONDITIONS = true
        
        // 循环控制（迭代4）
        const val LOOP_MAX_ITERATIONS = 1000
        const val LOOP_DETECTION_ENABLED = true
        const val INFINITE_LOOP_PROTECTION = true
        
        // 变量系统（迭代4）
        const val MAX_VARIABLES = 50
        const val VARIABLE_NAME_MAX_LENGTH = 20
        const val SUPPORT_GLOBAL_VARIABLES = true
        const val SUPPORT_LOCAL_VARIABLES = true
    }
    
    // ===== 性能配置 =====
    object Performance {
        const val GC_INTERVAL = 60 * 1000L  // 1分钟
        const val MEMORY_WARNING_THRESHOLD = 0.8f  // 80%内存使用警告
        const val CPU_WARNING_THRESHOLD = 0.7f  // 70% CPU使用警告
        const val LOG_PERFORMANCE_METRICS = false
        const val PROFILE_STARTUP_TIME = true
        const val MONITOR_FRAME_RATE = false
    }
    
    // ===== 功能开关 =====
    object FeatureFlags {
        // 核心功能
        const val SCRIPT_ENGINE = true
        const val PERMISSION_SYSTEM = true
        const val OVERLAY_SYSTEM = true
        const val ACCESSIBILITY_SERVICE = true
        
        // 实验性功能
        const val PLUGIN_SYSTEM = false
        const val CLOUD_SYNC = false
        const val AI_ASSISTANT = false
        const val VOICE_CONTROL = false
        
        // 开发中功能
        const val VISUAL_EDITOR = false      // 迭代2
        const val IMAGE_RECOGNITION = false  // 迭代3
        const val CONDITION_LOGIC = true     // 迭代4（部分实现）
        const val VARIABLE_SYSTEM = true     // 迭代4（部分实现）
        
        // 用户可配置的功能
        var enableAdvancedDebugging = false
        var enablePerformanceLogging = false
        var enableDeveloperOptions = false
    }
    
    // ===== 调试配置 =====
    object Debug {
        const val ENABLE_STRICT_MODE = false
        const val LOG_LIFECYCLE_EVENTS = false
        const val DUMP_STATE_ON_ERROR = true
        const val SHOW_LAYOUT_BOUNDS = false
        const val LOG_VIEW_MODEL_STATE = false
        
        // 开发工具
        const val ENABLE_DEVELOPER_OPTIONS = true
        const val SHOW_INTERNAL_STATE = false
        const val ALLOW_RESET_PREFERENCES = true
        
        // 日志配置
        const val LOG_TO_FILE = false
        const val LOG_FILE_MAX_SIZE = 1024 * 1024  // 1MB
        const val MAX_LOG_FILES = 5
    }
    
    // ===== 版本兼容 =====
    object Compatibility {
        // 向后兼容
        const val SUPPORT_LEGACY_SCRIPTS = true
        const val MIGRATION_ENABLED = true
        const val AUTO_MIGRATE_DATA = true
        
        // 数据格式版本
        const val SCRIPT_FORMAT_VERSION = 1
        const val PROJECT_FORMAT_VERSION = 1
        const val ACTION_FORMAT_VERSION = 1
        
        // 迁移策略
        const val CREATE_BACKUP_BEFORE_MIGRATION = true
        const val VALIDATE_AFTER_MIGRATION = true
    }
    
    // ===== 网络配置 =====
    object Network {
        const val SYNC_ENABLED = false
        const val SYNC_INTERVAL = 30 * 60 * 1000L  // 30分钟
        const val CONNECTION_TIMEOUT = 10000L
        const val READ_TIMEOUT = 30000L
        const val USE_COMPRESSION = true
        const val ENABLE_ENCRYPTION = true
    }
    
    // ===== 工具函数 =====
    
    /**
     * 获取应用版本名称
     */
    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: APP_VERSION
        } catch (e: Exception) {
            APP_VERSION
        }
    }
    
    /**
     * 获取应用版本代码
     */
    fun getAppVersionCode(context: Context): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1L
        }
    }
    
    /**
     * 检查是否开发环境
     */
    val isDevelopmentEnvironment: Boolean
        get() = true  // 在AndroidIDE中通常是开发环境
    
    /**
     * 检查是否应该启用高级功能
     */
    val shouldEnableAdvancedFeatures: Boolean
        get() = isDevelopmentEnvironment || FeatureFlags.CONDITION_LOGIC
    
    /**
     * 获取当前Android API级别
     */
    val androidApiLevel: Int
        get() = Build.VERSION.SDK_INT
    
    /**
     * 格式化文件大小
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.2f %s".format(size, units[unitIndex])
    }
    
    /**
     * 获取配置摘要（用于调试）
     */
    fun getConfigSummary(): Map<String, Any> {
        return mapOf(
            "app" to mapOf(
                "name" to APP_NAME,
                "version" to APP_VERSION,
                "package" to APP_PACKAGE
            ),
            "system" to mapOf(
                "minSdk" to System.MIN_SDK_VERSION,
                "targetSdk" to System.TARGET_SDK_VERSION,
                "isDebug" to System.IS_DEBUG_BUILD
            ),
            "features" to mapOf(
                "scriptEngine" to FeatureFlags.SCRIPT_ENGINE,
                "visualEditor" to FeatureFlags.VISUAL_EDITOR,
                "imageRecognition" to FeatureFlags.IMAGE_RECOGNITION,
                "conditionLogic" to FeatureFlags.CONDITION_LOGIC
            )
        )
    }
}