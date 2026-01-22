package com.wemod.automation.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.wemod.automation.ui.components.sidebar.*
import com.wemod.automation.ui.components.sidebar.SidebarConfigs

/**
 * 侧边栏抽屉组件（兼容旧版本）
 * 使用新的组件化架构
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarDrawer(
    drawerState: DrawerState,
    accessibilityEnabled: Boolean,
    overlayEnabled: Boolean,
    overlayRunning: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onToggleOverlay: () -> Unit,
    onTestClick: () -> Unit,
    onTestSwipe: () -> Unit,
    onOpenSettings: () -> Unit,
    content: @Composable () -> Unit
) {
    // 使用默认配置生成菜单
    val sections = remember(
        accessibilityEnabled,
        overlayEnabled,
        overlayRunning
    ) {
        SidebarConfigs.defaultConfig(
            accessibilityEnabled = accessibilityEnabled,
            overlayEnabled = overlayEnabled,
            overlayRunning = overlayRunning,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            onOpenOverlaySettings = onOpenOverlaySettings,
            onToggleOverlay = onToggleOverlay,
            onTestClick = onTestClick,
            onTestSwipe = onTestSwipe,
            onOpenSettings = onOpenSettings
        )
    }
    
    // 使用新的组件化侧边栏
    com.wemod.automation.ui.components.sidebar.SidebarDrawer(
        drawerState = drawerState,
        sections = sections,
        content = content
    )
}