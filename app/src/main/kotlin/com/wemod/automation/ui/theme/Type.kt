package com.wemod.automation.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 排版常量
 * 注意：在AndroidIDE中，我们可以使用系统默认字体
 */
object TypographyTokens {
    // 使用系统默认字体
    val TitleFontFamily = FontFamily.Default
    val BodyFontFamily = FontFamily.Default
    val LabelFontFamily = FontFamily.Default
    val MonospaceFontFamily = FontFamily.Monospace
    
    // 字体权重
    val ExtraLight = FontWeight(200)
    val Light = FontWeight(300)
    val Regular = FontWeight(400)
    val Medium = FontWeight(500)
    val SemiBold = FontWeight(600)
    val Bold = FontWeight(700)
    val ExtraBold = FontWeight(800)
    val Black = FontWeight(900)
}

/**
 * 文本样式快捷方式 - 修改为常量，不在@Composable中调用MaterialTheme
 */
object TextStyles {
    // 应用标题
    val AppTitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
    
    // 卡片标题
    val CardTitle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
    
    // 卡片描述
    val CardDescription = TextStyle(
        fontSize = 12.sp
    )
    
    // 状态文本
    val StatusEnabled = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
    
    val StatusDisabled = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
    
    // 脚本名称
    val ScriptName = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
    
    // 脚本信息
    val ScriptInfo = TextStyle(
        fontSize = 10.sp
    )
    
    // 按钮文本
    val ButtonPrimary = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
    
    val ButtonSecondary = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
    
    // 警告/错误文本
    val WarningText = TextStyle(
        fontSize = 12.sp
    )
    
    val ErrorText = TextStyle(
        fontSize = 12.sp
    )
    
    val SuccessText = TextStyle(
        fontSize = 12.sp
    )
}