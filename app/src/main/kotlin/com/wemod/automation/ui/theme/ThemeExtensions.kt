package com.wemod.automation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 暗色主题扩展函数
 */

// 获取当前主题的背景颜色
@Composable
fun backgroundSurface(): Color {
    return if (isSystemInDarkTheme()) SurfaceDark else Color.White
}

// 获取卡片背景颜色
@Composable
fun cardBackground(): Color {
    return if (isSystemInDarkTheme()) CardBackgroundDark else Color.White
}

// 获取高亮卡片背景颜色
@Composable
fun elevatedCardBackground(): Color {
    return if (isSystemInDarkTheme()) CardElevatedDark else Color(0xFFF8F9FA)
}

// 获取禁用状态的颜色
@Composable
fun disabledColor(): Color {
    return if (isSystemInDarkTheme()) 
        OnSurfaceDark.copy(alpha = DisabledAlpha)
    else 
        Color.Black.copy(alpha = DisabledAlpha)
}

// 获取悬停状态的颜色
@Composable
fun hoverColor(): Color {
    return if (isSystemInDarkTheme())
        OnSurfaceDark.copy(alpha = HoverAlpha)
    else
        Color.Black.copy(alpha = HoverAlpha)
}

// 获取脚本状态颜色
fun getScriptStatusColor(isRunning: Boolean, isPaused: Boolean): Color {
    return when {
        isRunning -> ScriptRunningColor
        isPaused -> ScriptPausedColor
        else -> ScriptIdleColor
    }
}

// 获取动作类型颜色
fun getActionTypeColor(actionType: String): Color {
    return when (actionType) {
        "click" -> ClickActionColor
        "swipe" -> SwipeActionColor
        "wait" -> WaitActionColor
        else -> Gray60
    }
}

/**
 * 自定义Surface - 带有暗色主题支持
 */
@Composable
fun WeModSurface(
    modifier: Modifier = Modifier,
    color: Color = if (isSystemInDarkTheme()) SurfaceDark else Color.White,
    contentColor: Color = if (isSystemInDarkTheme()) OnSurfaceDark else Color.Black,
    tonalElevation: Dp = 0.dp,
    shape: Shape = WeModShapes.medium,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shape = shape,
        content = content
    )
}

/**
 * 自定义Card - 带有暗色主题支持
 */
@Composable
fun WeModCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val backgroundColor = if (elevated) elevatedCardBackground() else cardBackground()
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = if (isSystemInDarkTheme()) OnSurfaceDark else Color.Black
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (elevated) 4.dp else 1.dp
        ),
        shape = WeModShapes.medium,
        onClick = onClick ?: {}
    ) {
        content()
    }
}

/**
 * 状态指示器颜色
 */
@Composable
fun statusColor(enabled: Boolean): Color {
    return if (enabled) SuccessColor else ErrorColor
}

/**
 * 获取渐变背景
 */
@Composable
fun gradientBackground(): List<Color> {
    return if (isSystemInDarkTheme()) {
        listOf(
            Color(0xFF1A1A1A),
            Color(0xFF252525),
            Color(0xFF2D2D2D)
        )
    } else {
        listOf(
            Color(0xFFF8F9FA),
            Color(0xFFE9ECEF),
            Color(0xFFDEE2E6)
        )
    }
}