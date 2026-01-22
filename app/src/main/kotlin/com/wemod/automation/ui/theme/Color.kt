package com.wemod.automation.ui.theme

import androidx.compose.ui.graphics.Color

// 暗灰色调色板 - 完整的灰度阶梯
val Gray05 = Color(0xFF0D0D0D)
val Gray10 = Color(0xFF1A1A1A)
val Gray15 = Color(0xFF252525)  // 添加缺失的定义
val Gray20 = Color(0xFF2D2D2D)
val Gray25 = Color(0xFF333333)
val Gray30 = Color(0xFF404040)
val Gray35 = Color(0xFF4A4A4A)
val Gray40 = Color(0xFF535353)
val Gray45 = Color(0xFF5C5C5C)
val Gray50 = Color(0xFF666666)
val Gray55 = Color(0xFF707070)
val Gray60 = Color(0xFF808080)
val Gray65 = Color(0xFF8A8A8A)
val Gray70 = Color(0xFF999999)
val Gray75 = Color(0xFFA6A6A6)
val Gray80 = Color(0xFFB3B3B3)
val Gray85 = Color(0xFFBFBFBF)
val Gray90 = Color(0xFFCCCCCC)
val Gray95 = Color(0xFFE6E6E6)
val Gray98 = Color(0xFFF5F5F5)

// 主色调 - 科技蓝
val PrimaryDark = Color(0xFF4A9EFF)
val PrimaryLight = Color(0xFF6BB1FF)
val PrimaryContainer = Color(0xFF1A3A5C)
val OnPrimary = Color(0xFFFFFFFF)

// 次要色调 - 活力橙
val SecondaryDark = Color(0xFFFF8A4C)
val SecondaryLight = Color(0xFFFFA66F)
val SecondaryContainer = Color(0xFF5C2D1A)
val OnSecondary = Color(0xFF1A1A1A)

// 成功/错误色调
val SuccessColor = Color(0xFF4CAF50)
val ErrorColor = Color(0xFFF44336)
val WarningColor = Color(0xFFFF9800)
val InfoColor = Color(0xFF2196F3)

// 表面颜色
val SurfaceDark = Gray10
val SurfaceVariantDark = Gray20
val SurfaceContainerDark = Gray30
val OnSurfaceDark = Gray95
val OnSurfaceVariantDark = Gray80

// 背景和轮廓
val BackgroundDark = Gray10
val OutlineDark = Gray40
val OutlineVariantDark = Gray30

// 卡片颜色
val CardBackgroundDark = Gray20.copy(alpha = 0.8f)
val CardElevatedDark = Gray15.copy(alpha = 0.9f)  // 现在Gray15已定义

// 特殊效果
val ScrimDark = Color(0xCC1A1A1A)
val ShadowDark = Color(0x80000000)

// 状态颜色
val DisabledAlpha = 0.38f
val HoverAlpha = 0.08f
val FocusAlpha = 0.12f
val PressedAlpha = 0.12f

// 悬浮窗特定颜色
val OverlaySurface = Color(0xCC2D2D2D)
val OverlayBorder = PrimaryDark.copy(alpha = 0.3f)

// 脚本状态颜色
val ScriptRunningColor = Color(0xFF4CAF50)
val ScriptPausedColor = Color(0xFFFF9800)
val ScriptStoppedColor = Color(0xFFF44336)
val ScriptIdleColor = Gray60

// 动作类型颜色
val ClickActionColor = Color(0xFF2196F3)
val SwipeActionColor = Color(0xFF9C27B0)
val WaitActionColor = Color(0xFFFF9800)