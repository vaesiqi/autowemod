package com.wemod.automation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    // 主色调
    primary = PrimaryDark,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimary,
    
    // 次要色调
    secondary = SecondaryDark,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondary,
    
    // 表面颜色
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    // 背景
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    
    // 错误
    error = ErrorColor,
    onError = Color.White,
    errorContainer = ErrorColor.copy(alpha = 0.2f),
    onErrorContainer = ErrorColor,
    
    // 轮廓
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    
    // 其他
    scrim = ScrimDark,
    surfaceTint = PrimaryDark,
    inverseSurface = Gray95,
    inverseOnSurface = Gray20,
    inversePrimary = PrimaryLight,
)

private val LightColorScheme = lightColorScheme(
    // 可以根据需要添加浅色主题
    primary = PrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryLight.copy(alpha = 0.1f),
    onPrimaryContainer = Color.Black,
    
    secondary = SecondaryLight,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryLight.copy(alpha = 0.1f),
    onSecondaryContainer = Color.Black,
    
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    
    background = Color(0xFFF8F9FA),
    onBackground = Color.Black,
    
    error = ErrorColor,
    onError = Color.White,
    
    outline = Color(0xFFDDDDDD),
    outlineVariant = Color(0xFFEEEEEE),
)

/**
 * 自定义排版系统
 */
val WeModTypography = Typography(
    // 显示文本
    displayLarge = TextStyle(
        fontFamily = TypographyTokens.TitleFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    
    // 标题
    titleLarge = TextStyle(
        fontFamily = TypographyTokens.TitleFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TypographyTokens.TitleFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = TypographyTokens.TitleFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    
    // 正文
    bodyLarge = TextStyle(
        fontFamily = TypographyTokens.BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TypographyTokens.BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = TypographyTokens.BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    
    // 标签
    labelLarge = TextStyle(
        fontFamily = TypographyTokens.LabelFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TypographyTokens.LabelFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TypographyTokens.LabelFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    ),
    
    // 特殊
    headlineLarge = TextStyle(
        fontFamily = TypographyTokens.TitleFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = TypographyTokens.TitleFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
)

/**
 * 自定义形状系统
 */
val WeModShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun WeModTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = WeModTypography,
        shapes = WeModShapes,
        content = content
    )
}