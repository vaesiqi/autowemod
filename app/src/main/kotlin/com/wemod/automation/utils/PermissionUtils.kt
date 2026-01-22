package com.wemod.automation.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import android.util.Log

/**
 * 权限检查工具类
 */
object PermissionUtils {
    
    /**
     * 检查无障碍服务是否已启用
     * 修复版：更健壮的服务检测逻辑
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                ?: return false
            
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )
            
            if (enabledServices.isEmpty()) {
                return false
            }
            
            // 服务ID格式通常是：包名/服务类全名
            val packageName = context.packageName
            val serviceName = "com.wemod.automation.core.AccessibilityService"
            
            // 多个可能的ID格式（适配不同Android版本）
            val possibleServiceIds = listOf(
                "$packageName/$serviceName",  // 标准格式
                "$packageName/.core.AccessibilityService",  // 可能省略包名
                serviceName,  // 可能只有类名
                "com.wemod.automation/.core.AccessibilityService"  // 完整格式
            )
            
            Log.d("PermissionUtils", "查找无障碍服务，包名: $packageName")
            Log.d("PermissionUtils", "可能的服务ID: $possibleServiceIds")
            
            for (service in enabledServices) {
                val serviceId = service.id
                Log.d("PermissionUtils", "找到的服务ID: $serviceId")
                
                // 检查是否匹配任何可能的ID格式
                if (possibleServiceIds.any { serviceId.contains(it) || serviceId.endsWith(it) }) {
                    Log.d("PermissionUtils", "找到匹配的无障碍服务")
                    return true
                }
                
                // 额外检查：服务是否包含我们的包名和AccessibilityService关键字
                if (serviceId.contains(packageName) && 
                    (serviceId.contains("AccessibilityService") || serviceId.contains("accessibility"))) {
                    Log.d("PermissionUtils", "通过包名和关键字匹配到服务")
                    return true
                }
            }
            
            Log.d("PermissionUtils", "未找到匹配的无障碍服务")
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PermissionUtils", "检查无障碍服务时出错: ${e.message}")
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
            // 备用方案：打开应用设置
            openAppSettings(context)
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
            openAppSettings(context)
        }
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
            e.printStackTrace()
        }
    }
}