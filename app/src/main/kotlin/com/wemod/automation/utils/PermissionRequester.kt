package com.wemod.automation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * 权限申请管理器
 * 提供主动申请权限的功能
 */
object PermissionRequester {
    
    private const val TAG = "PermissionRequester"
    
    /**
     * 请求悬浮窗权限
     * 注意：SYSTEM_ALERT_WINDOW是特殊权限，需要跳转到设置页面
     */
    fun requestOverlayPermission(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Log.d(TAG, "已跳转悬浮窗权限设置")
                } else {
                    Log.d(TAG, "悬浮窗权限已授予")
                }
            } else {
                Log.d(TAG, "Android 6.0以下不需要悬浮窗权限")
            }
        } catch (e: Exception) {
            Log.e(TAG, "请求悬浮窗权限失败", e)
            // 备用方案：打开应用设置
            openAppSettings(context)
        }
    }
    
    /**
     * 请求无障碍服务权限
     * 注意：无障碍服务需要用户手动在设置中开启
     */
    fun requestAccessibilityPermission(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "已跳转无障碍服务设置")
        } catch (e: Exception) {
            Log.e(TAG, "请求无障碍权限失败", e)
            openAppSettings(context)
        }
    }
    
    /**
     * 批量请求权限
     * 先请求悬浮窗，再请求无障碍
     */
    fun requestAllPermissions(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                // 先请求悬浮窗权限
                requestOverlayPermission(context)
            } else {
                // 悬浮窗已授权，请求无障碍
                if (!PermissionUtils.isAccessibilityServiceEnabled(context)) {
                    requestAccessibilityPermission(context)
                } else {
                    Log.d(TAG, "所有权限已授予")
                }
            }
        } else {
            // Android 6.0以下，直接请求无障碍
            if (!PermissionUtils.isAccessibilityServiceEnabled(context)) {
                requestAccessibilityPermission(context)
            }
        }
    }
    
    /**
     * 检查是否需要请求权限
     */
    fun needToRequestPermissions(context: Context): Boolean {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
        val hasAccessibility = PermissionUtils.isAccessibilityServiceEnabled(context)
        
        return !hasOverlay || !hasAccessibility
    }
    
    /**
     * 获取缺失的权限列表
     */
    fun getMissingPermissions(context: Context): List<String> {
        val missingPermissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                missingPermissions.add("悬浮窗权限")
            }
        }
        
        if (!PermissionUtils.isAccessibilityServiceEnabled(context)) {
            missingPermissions.add("无障碍服务")
        }
        
        return missingPermissions
    }
    
    /**
     * 打开应用设置页面（备用）
     */
    private fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "打开应用设置失败", e)
        }
    }
    
    /**
     * 智能权限请求：根据当前缺失的权限决定请求顺序
     */
    fun smartRequestPermissions(context: Context) {
        val missing = getMissingPermissions(context)
        
        when {
            missing.contains("悬浮窗权限") && missing.contains("无障碍服务") -> {
                // 两者都缺失，先请求悬浮窗
                requestOverlayPermission(context)
            }
            missing.contains("悬浮窗权限") -> {
                // 只缺悬浮窗
                requestOverlayPermission(context)
            }
            missing.contains("无障碍服务") -> {
                // 只缺无障碍
                requestAccessibilityPermission(context)
            }
            else -> {
                Log.d(TAG, "所有权限已授予")
            }
        }
    }
}