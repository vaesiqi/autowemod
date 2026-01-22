// 新文件：app/src/main/kotlin/com/wemod/automation/ui/components/sidebar/SidebarModels.kt
package com.wemod.automation.ui.components.sidebar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 侧边栏菜单项基础接口
 */
sealed interface SidebarItem {
    val id: String
    val title: String
    val description: String
    val enabled: Boolean
    val animationDelay: Int
}

/**
 * 开关菜单项
 */
data class SwitchItem(
    override val id: String,
    override val title: String,
    override val description: String,
    val icon: ImageVector,
    val checked: Boolean,
    override val enabled: Boolean = true,
    override val animationDelay: Int = 0,
    val onToggle: () -> Unit
) : SidebarItem

/**
 * 按钮菜单项
 */
data class ButtonItem(
    override val id: String,
    override val title: String,
    override val description: String,
    val icon: ImageVector,
    override val enabled: Boolean = true,
    override val animationDelay: Int = 0,
    val onClick: () -> Unit
) : SidebarItem

/**
 * 分组菜单项
 */
data class Section(
    val id: String,
    val title: String,
    val items: List<SidebarItem>,
    val animationDelay: Int = 0
)

/**
 * 预定义侧边栏配置
 */
object SidebarConfigs {
    // 默认配置：权限控制 + 功能测试 + 系统设置
    fun defaultConfig(
        accessibilityEnabled: Boolean,
        overlayEnabled: Boolean,
        overlayRunning: Boolean,
        onOpenAccessibilitySettings: () -> Unit,
        onOpenOverlaySettings: () -> Unit,
        onToggleOverlay: () -> Unit,
        onTestClick: () -> Unit,
        onTestSwipe: () -> Unit,
        onOpenSettings: () -> Unit
    ): List<Section> {
        return listOf(
            // 权限控制区域
            Section(
                id = "permissions",
                title = "权限控制",
                animationDelay = 100,
                items = listOf(
                    SwitchItem(
                        id = "accessibility",
                        title = "无障碍服务",
                        description = if (accessibilityEnabled) "已开启 ✓" else "未开启 ✗",
                        icon = Icons.Default.Accessibility,
                        checked = accessibilityEnabled,
                        onToggle = onOpenAccessibilitySettings,
                        animationDelay = 150
                    ),
                    SwitchItem(
                        id = "overlay_permission",
                        title = "悬浮窗权限",
                        description = if (overlayEnabled) "已授权 ✓" else "未授权 ✗",
                        icon = Icons.Default.Window,
                        checked = overlayEnabled,
                        onToggle = onOpenOverlaySettings,
                        animationDelay = 200
                    ),
                    SwitchItem(
                        id = "overlay_display",
                        title = "显示悬浮窗",
                        description = if (overlayRunning) "显示中" else "已隐藏",
                        icon = Icons.Default.Dashboard,
                        checked = overlayRunning,
                        enabled = overlayEnabled,
                        onToggle = onToggleOverlay,
                        animationDelay = 250
                    )
                )
            ),
            
            // 功能测试区域
            Section(
                id = "test_functions",
                title = "功能测试",
                animationDelay = 300,
                items = listOf(
                    ButtonItem(
                        id = "test_click",
                        title = "测试点击",
                        description = "在屏幕中心位置点击",
                        icon = Icons.Default.TouchApp,
                        onClick = onTestClick,
                        animationDelay = 350
                    ),
                    ButtonItem(
                        id = "test_swipe",
                        title = "测试滑动",
                        description = "从左到右滑动",
                        icon = Icons.Default.SwapHoriz,
                        onClick = onTestSwipe,
                        animationDelay = 400
                    )
                )
            ),
            
            // 系统设置区域
            Section(
                id = "system_settings",
                title = "系统设置",
                animationDelay = 450,
                items = listOf(
                    ButtonItem(
                        id = "permission_settings",
                        title = "权限设置",
                        description = "跳转到系统权限设置",
                        icon = Icons.Default.Security,
                        onClick = onOpenSettings,
                        animationDelay = 500
                    ),
                    ButtonItem(
                        id = "app_settings",
                        title = "应用设置",
                        description = "应用偏好设置（开发中）",
                        icon = Icons.Default.SettingsApplications,
                        enabled = false,
                        onClick = {},
                        animationDelay = 550
                    )
                )
            )
        )
    }
    
    // 最小配置：只有权限控制
    fun minimalConfig(
        accessibilityEnabled: Boolean,
        overlayEnabled: Boolean,
        onOpenAccessibilitySettings: () -> Unit,
        onOpenOverlaySettings: () -> Unit
    ): List<Section> {
        return listOf(
            Section(
                id = "permissions",
                title = "权限控制",
                items = listOf(
                    SwitchItem(
                        id = "accessibility",
                        title = "无障碍服务",
                        description = if (accessibilityEnabled) "已开启 ✓" else "未开启 ✗",
                        icon = Icons.Default.Accessibility,
                        checked = accessibilityEnabled,
                        onToggle = onOpenAccessibilitySettings
                    ),
                    SwitchItem(
                        id = "overlay_permission",
                        title = "悬浮窗权限",
                        description = if (overlayEnabled) "已授权 ✓" else "未授权 ✗",
                        icon = Icons.Default.Window,
                        checked = overlayEnabled,
                        onToggle = onOpenOverlaySettings
                    )
                )
            )
        )
    }
}