// 新建或修改 WeModApplication.kt
package com.wemod.automation

import android.app.Application
import com.wemod.automation.core.actions.ActionRegistry

class WeModApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化动作注册表
        ActionRegistry.registerBuiltInActions()
        android.util.Log.d("WeModApplication", "动作注册表初始化完成")
    }
}