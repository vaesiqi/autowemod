// 完全替换：app/src/main/kotlin/com/wemod/automation/core/actions/BuiltInActions.kt
// ===========================================

package com.wemod.automation.core.actions

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wemod.automation.core.accessibility.EnhancedAccessibilityManager
import com.wemod.automation.core.accessibility.EnhancedAccessibilityService

/**
 * 点击动作实现（更新版 - 使用增强无障碍服务）
 */
class ClickAction : IAction {
    override val type: String = "click"
    override val displayName: String = "点击"
    override val description: String = "在指定位置执行点击操作"
    
    override suspend fun execute(context: Context, config: ActionConfig): Boolean {
        val x = config.getInt("x", 500)
        val y = config.getInt("y", 500)
        val duration = config.getLong("duration", 50L)
        val delayAfter = config.getLong("delayAfter", 1000L)
        
        return try {
            // 🎯 使用增强版无障碍管理器
            val success = EnhancedAccessibilityManager.click(x, y, duration)
            if (!success) {
                throw Exception("点击执行失败，无障碍服务可能未运行")
            }
            
            // 等待动作后延迟
            if (delayAfter > 0) {
                kotlinx.coroutines.delay(delayAfter)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun validate(config: ActionConfig): ValidationResult {
        val x = config.getInt("x")
        val y = config.getInt("y")
        val duration = config.getLong("duration", 50L)
        
        val errors = mutableListOf<String>()
        
        if (x < 0) errors.add("X坐标不能为负数")
        if (y < 0) errors.add("Y坐标不能为负数")
        if (duration <= 0) errors.add("点击时长必须大于0")
        
        return if (errors.isEmpty()) {
            ValidationResult.VALID
        } else {
            ValidationResult.invalid(*errors.toTypedArray())
        }
    }
    
    @Composable
    override fun ConfigUI(config: ActionConfig, onConfigChanged: (ActionConfig) -> Unit) {
        // 简化版UI，实际应该包含坐标输入等
        androidx.compose.material3.Text("点击动作配置 (X: ${config.getInt("x")}, Y: ${config.getInt("y")}, 时长: ${config.getLong("duration", 50L)}ms)")
    }
}

/**
 * 滑动动作实现（更新版 - 使用增强无障碍服务）
 */
class SwipeAction : IAction {
    override val type: String = "swipe"
    override val displayName: String = "滑动"
    override val description: String = "从起点滑动到终点"
    
    override suspend fun execute(context: Context, config: ActionConfig): Boolean {
        val startX = config.getInt("startX", 300)
        val startY = config.getInt("startY", 500)
        val endX = config.getInt("endX", 700)
        val endY = config.getInt("endY", 500)
        val duration = config.getLong("duration", 500L)
        val delayAfter = config.getLong("delayAfter", 1000L)
        
        return try {
            // 🎯 使用增强版无障碍管理器
            val success = EnhancedAccessibilityManager.swipe(startX, startY, endX, endY, duration)
            if (!success) {
                throw Exception("滑动执行失败，无障碍服务可能未运行")
            }
            
            // 等待动作后延迟
            if (delayAfter > 0) {
                kotlinx.coroutines.delay(delayAfter)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun validate(config: ActionConfig): ValidationResult {
        val startX = config.getInt("startX")
        val startY = config.getInt("startY")
        val endX = config.getInt("endX")
        val endY = config.getInt("endY")
        val duration = config.getLong("duration", 500L)
        
        val errors = mutableListOf<String>()
        
        if (startX < 0) errors.add("起点X坐标不能为负数")
        if (startY < 0) errors.add("起点Y坐标不能为负数")
        if (endX < 0) errors.add("终点X坐标不能为负数")
        if (endY < 0) errors.add("终点Y坐标不能为负数")
        if (duration <= 0) errors.add("滑动时长必须大于0")
        
        // 验证起点和终点是否相同（无意义的滑动）
        if (startX == endX && startY == endY) {
            errors.add("起点和终点不能相同")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.VALID
        } else {
            ValidationResult.invalid(*errors.toTypedArray())
        }
    }
    
    @Composable
    override fun ConfigUI(config: ActionConfig, onConfigChanged: (ActionConfig) -> Unit) {
        androidx.compose.material3.Text("滑动动作配置 (从(${config.getInt("startX")},${config.getInt("startY")}) 到 (${config.getInt("endX")},${config.getInt("endY")}), 时长: ${config.getLong("duration", 500L)}ms)")
    }
}

/**
 * 等待动作实现（保持不变）
 */
class WaitAction : IAction {
    override val type: String = "wait"
    override val displayName: String = "等待"
    override val description: String = "等待指定时间"
    
    override suspend fun execute(context: Context, config: ActionConfig): Boolean {
        val duration = config.getLong("duration", 1000L)
        
        return try {
            kotlinx.coroutines.delay(duration)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun validate(config: ActionConfig): ValidationResult {
        val duration = config.getLong("duration", 1000L)
        
        return if (duration > 0) {
            ValidationResult.VALID
        } else {
            ValidationResult.invalid("等待时间必须大于0")
        }
    }
    
    @Composable
    override fun ConfigUI(config: ActionConfig, onConfigChanged: (ActionConfig) -> Unit) {
        androidx.compose.material3.Text("等待动作配置 (${config.getLong("duration", 1000L)}ms)")
    }
}

/**
 * 长按动作实现（新增 - 增强功能）
 */
class LongClickAction : IAction {
    override val type: String = "longClick"
    override val displayName: String = "长按"
    override val description: String = "在指定位置执行长按操作"
    
    override suspend fun execute(context: Context, config: ActionConfig): Boolean {
        val x = config.getInt("x", 500)
        val y = config.getInt("y", 500)
        val duration = config.getLong("duration", 1000L)  // 默认长按1秒
        val delayAfter = config.getLong("delayAfter", 1000L)
        
        return try {
            // 🎯 使用增强版无障碍管理器（支持长按）
            val success = EnhancedAccessibilityManager.longClick(x, y, duration)
            if (!success) {
                throw Exception("长按执行失败，无障碍服务可能未运行")
            }
            
            // 等待动作后延迟
            if (delayAfter > 0) {
                kotlinx.coroutines.delay(delayAfter)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun validate(config: ActionConfig): ValidationResult {
        val x = config.getInt("x")
        val y = config.getInt("y")
        val duration = config.getLong("duration", 1000L)
        
        val errors = mutableListOf<String>()
        
        if (x < 0) errors.add("X坐标不能为负数")
        if (y < 0) errors.add("Y坐标不能为负数")
        if (duration < 100) errors.add("长按时长至少100ms")
        if (duration > 10000) errors.add("长按时长不能超过10秒")
        
        return if (errors.isEmpty()) {
            ValidationResult.VALID
        } else {
            ValidationResult.invalid(*errors.toTypedArray())
        }
    }
    
    @Composable
    override fun ConfigUI(config: ActionConfig, onConfigChanged: (ActionConfig) -> Unit) {
        androidx.compose.material3.Text("长按动作配置 (X: ${config.getInt("x")}, Y: ${config.getInt("y")}, 时长: ${config.getLong("duration", 1000L)}ms)")
    }
}

/**
 * 查找点击动作实现（新增 - 增强功能）
 */
class FindAndClickAction : IAction {
    override val type: String = "findAndClick"
    override val displayName: String = "查找点击"
    override val description: String = "查找指定文本或描述的节点并点击"
    
    override suspend fun execute(context: Context, config: ActionConfig): Boolean {
        val text = config.getString("text", "")
        val description = config.getString("description", "")
        val timeout = config.getLong("timeout", 5000L)
        val delayAfter = config.getLong("delayAfter", 1000L)
        
        return try {
            if (text.isEmpty() && description.isEmpty()) {
                throw Exception("必须指定要查找的文本或描述")
            }
            
            // 🎯 使用增强版无障碍管理器（支持查找点击）
            val success = EnhancedAccessibilityManager.findAndClick(
                text = if (text.isNotEmpty()) text else null,
                description = if (description.isNotEmpty()) description else null
            )
            
            if (!success) {
                throw Exception("未找到匹配的节点或点击失败")
            }
            
            // 等待动作后延迟
            if (delayAfter > 0) {
                kotlinx.coroutines.delay(delayAfter)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun validate(config: ActionConfig): ValidationResult {
        val text = config.getString("text", "")
        val description = config.getString("description", "")
        val timeout = config.getLong("timeout", 5000L)
        
        val errors = mutableListOf<String>()
        
        if (text.isEmpty() && description.isEmpty()) {
            errors.add("必须指定要查找的文本或描述")
        }
        
        if (timeout <= 0) {
            errors.add("超时时间必须大于0")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.VALID
        } else {
            ValidationResult.invalid(*errors.toTypedArray())
        }
    }
    
    @Composable
    override fun ConfigUI(config: ActionConfig, onConfigChanged: (ActionConfig) -> Unit) {
        val text = config.getString("text", "")
        val description = config.getString("description", "")
        
        androidx.compose.material3.Text(
            "查找点击动作配置 (${if (text.isNotEmpty()) "文本: $text" else "描述: $description"})"
        )
    }
}

/**
 * 输入文本动作实现（新增 - 增强功能）
 */
class InputTextAction : IAction {
    override val type: String = "inputText"
    override val displayName: String = "输入文本"
    override val description: String = "查找节点并输入指定文本"
    
    override suspend fun execute(context: Context, config: ActionConfig): Boolean {
        val text = config.getString("text", "")
        val targetText = config.getString("targetText", "")
        val targetDescription = config.getString("targetDescription", "")
        val delayAfter = config.getLong("delayAfter", 1000L)
        
        return try {
            if (text.isEmpty()) {
                throw Exception("必须指定要输入的文本")
            }
            
            // 🎯 使用增强版无障碍管理器（支持输入文本）
            val success = EnhancedAccessibilityManager.findAndInput(
                text = text,
                targetText = if (targetText.isNotEmpty()) targetText else null
            )
            
            if (!success) {
                throw Exception("未找到输入框或输入失败")
            }
            
            // 等待动作后延迟
            if (delayAfter > 0) {
                kotlinx.coroutines.delay(delayAfter)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun validate(config: ActionConfig): ValidationResult {
        val text = config.getString("text", "")
        
        val errors = mutableListOf<String>()
        
        if (text.isEmpty()) {
            errors.add("必须指定要输入的文本")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.VALID
        } else {
            ValidationResult.invalid(*errors.toTypedArray())
        }
    }
    
    @Composable
    override fun ConfigUI(config: ActionConfig, onConfigChanged: (ActionConfig) -> Unit) {
        val text = config.getString("text", "")
        val targetText = config.getString("targetText", "")
        
        androidx.compose.material3.Text(
            "输入文本动作配置 (文本: \"$text\"${if (targetText.isNotEmpty()) ", 目标: \"$targetText\"" else ""})"
        )
    }
}

/**
 * 返回动作实现（新增 - 增强功能）
 */
class BackAction : IAction {
    override val type: String = "back"
    override val displayName: String = "返回"
    override val description: String = "执行返回键操作"
    
    override suspend fun execute(context: Context, config: ActionConfig): Boolean {
        val delayAfter = config.getLong("delayAfter", 1000L)
        
        return try {
            // 🎯 使用增强版无障碍管理器（支持全局操作）
            val success = EnhancedAccessibilityManager.performGlobalAction(
                EnhancedAccessibilityService.GlobalAction.BACK
            )
            
            if (!success) {
                throw Exception("返回操作失败")
            }
            
            // 等待动作后延迟
            if (delayAfter > 0) {
                kotlinx.coroutines.delay(delayAfter)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun validate(config: ActionConfig): ValidationResult {
        // 返回动作没有特殊验证要求
        return ValidationResult.VALID
    }
    
    @Composable
    override fun ConfigUI(config: ActionConfig, onConfigChanged: (ActionConfig) -> Unit) {
        androidx.compose.material3.Text("返回动作配置")
    }
}

/**
 * 主页动作实现（新增 - 增强功能）
 */
class HomeAction : IAction {
    override val type: String = "home"
    override val displayName: String = "主页"
    override val description: String = "执行主页键操作"
    
    override suspend fun execute(context: Context, config: ActionConfig): Boolean {
        val delayAfter = config.getLong("delayAfter", 1000L)
        
        return try {
            // 🎯 使用增强版无障碍管理器（支持全局操作）
            val success = EnhancedAccessibilityManager.performGlobalAction(
                EnhancedAccessibilityService.GlobalAction.HOME
            )
            
            if (!success) {
                throw Exception("主页操作失败")
            }
            
            // 等待动作后延迟
            if (delayAfter > 0) {
                kotlinx.coroutines.delay(delayAfter)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun validate(config: ActionConfig): ValidationResult {
        // 主页动作没有特殊验证要求
        return ValidationResult.VALID
    }
    
    @Composable
    override fun ConfigUI(config: ActionConfig, onConfigChanged: (ActionConfig) -> Unit) {
        androidx.compose.material3.Text("主页动作配置")
    }
}