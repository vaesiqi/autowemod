package com.wemod.automation

import android.app.Application
import com.wemod.automation.ui.viewmodel.MainViewModel

class WeModApplication : Application() {
    
    // 在这里可以初始化全局依赖，如数据库、网络等
    override fun onCreate() {
        super.onCreate()
        // 可以在这里初始化全局组件
    }
}