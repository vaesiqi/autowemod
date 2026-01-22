// 替换 app/src/main/kotlin/com/wemod/automation/di/AppModule.kt
// ===========================================

package com.wemod.automation.di

import android.content.Context
import com.wemod.automation.config.AppConfig
import com.wemod.automation.core.errors.DefaultErrorHandler
import com.wemod.automation.core.errors.ErrorHandler
import com.wemod.automation.data.repository.ScriptRepository
import com.wemod.automation.ui.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin依赖注入模块（简化版）
 */
val appModule: Module = module {
    
    // ===== 单例依赖 =====
    
    // 应用上下文
    single<Context> { androidContext() }
    
    // 配置管理
    single { AppConfig }
    
    // 错误处理器
    single<ErrorHandler> { DefaultErrorHandler }
    
    // 数据仓库
    single {
        val context: Context = get()
        ScriptRepository(context)
    }
    
    // ===== ViewModel =====
    
    // MainViewModel - 脚本管理
    viewModel { (context: Context) ->
        MainViewModel(context)
    }
    
    // PermissionViewModel - 权限状态管理
    viewModel { (context: Context) ->
        PermissionViewModel(context)
    }
    
    // RunViewModel - 脚本运行控制
    viewModel { (context: Context) ->
        RunViewModel(context)
    }
    
    // UnifiedViewModel - 统一协调器
    viewModel { (context: Context) ->
        UnifiedViewModel(context)
    }
}

/**
 * Koin初始化
 */
fun initializeKoin(context: Context) {
    startKoin {
        androidContext(context)
        modules(appModule)
    }
}

/**
 * 简化的ViewModel获取函数（兼容旧代码）
 */
@Deprecated("Use Koin viewModel() delegation instead")
inline fun <reified T : Any> getViewModel(context: Context): T {
    return when (T::class) {
        UnifiedViewModel::class -> UnifiedViewModel(context) as T
        MainViewModel::class -> MainViewModel(context) as T
        PermissionViewModel::class -> PermissionViewModel(context) as T
        RunViewModel::class -> RunViewModel(context) as T
        else -> throw IllegalArgumentException("Unknown ViewModel type: ${T::class}")
    }
}