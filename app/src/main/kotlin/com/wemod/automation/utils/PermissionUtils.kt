package com.wemod.automation.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat

/**
 * 权限检查工具类
 */
object PermissionUtils {
    
    /**
     * 检查无障碍服务是否已启用
     * 这个函数需要准确匹配我们在 AndroidManifest.xml 中声明的服务路径
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val accessibilityManager = ContextCompat.getSystemService(
                context, 
                AccessibilityManager::class.java
            ) ?: return false
            
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )
            
            if (enabledServices.isEmpty()) {
                return false
            }
            
            // 关键：这里要正确匹配服务ID
            // 格式通常是：包名/服务类全名
            val packageName = context.packageName
            val serviceName = "com.wemod.automation.core.AccessibilityService"
            val expectedServiceId = "$packageName/$serviceName"
            
            // 调试日志（可以在Logcat中查看）
            android.util.Log.d("PermissionUtils", "期望的服务ID: $expectedServiceId")
            
            for (service in enabledServices) {
                val serviceId = service.id
                android.util.Log.d("PermissionUtils", "找到的服务: $serviceId")
                
                // 检查是否匹配
                if (serviceId.contains(packageName) && 
                    serviceId.contains("AccessibilityService")) {
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查悬浮窗权限是否已授予
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            // Android 6.0 以下默认有权限
            true
        }
    }
    
    /**
     * 打开无障碍服务设置页面
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 打开悬浮窗权限设置页面
     */
    fun openOverlayPermissionSettings(context: Context) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
            } else {
                Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                    putExtra("package", context.packageName)
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}