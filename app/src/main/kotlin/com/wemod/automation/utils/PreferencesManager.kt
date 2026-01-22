// 新建文件：utils/PreferencesManager.kt
package com.wemod.automation.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 首选项管理器
 */
object PreferencesManager {
    private const val PREFS_NAME = "wemod_prefs"
    
    // 首次启动相关
    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_PERMISSION_WIZARD_SHOWN = "permission_wizard_shown"
    private const val KEY_LAST_VERSION = "last_version"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 检查是否是真正的首次启动（安装后第一次运行）
     */
    fun isFirstLaunch(context: Context): Boolean {
        val prefs = getPreferences(context)
        val firstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        
        if (firstLaunch) {
            // 标记为已启动过
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
            return true
        }
        return false
    }
    
    /**
     * 检查权限向导是否已经显示过
     */
    fun isPermissionWizardShown(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_PERMISSION_WIZARD_SHOWN, false)
    }
    
    /**
     * 标记权限向导已显示
     */
    fun markPermissionWizardShown(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_PERMISSION_WIZARD_SHOWN, true)
            .apply()
    }
    
    /**
     * 检查是否需要显示权限向导
     * 规则：首次启动且缺少权限时显示
     */
    fun shouldShowPermissionWizard(context: Context): Boolean {
        val prefs = getPreferences(context)
        val wizardShown = prefs.getBoolean(KEY_PERMISSION_WIZARD_SHOWN, false)
        
        // 如果已经显示过，不再显示
        if (wizardShown) {
            return false
        }
        
        // 检查是否缺少权限
        val hasOverlay = PermissionUtils.canDrawOverlays(context)
        val hasAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
        
        return !hasOverlay || !hasAccessibility
    }
    
    /**
     * 重置首选项（用于测试）
     */
    fun resetPreferences(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}