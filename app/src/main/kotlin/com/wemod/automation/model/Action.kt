package com.wemod.automation.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 基础动作接口
 */
@Immutable
@Serializable
sealed class Action {
    abstract val id: String
    abstract val name: String
    abstract val enabled: Boolean
    abstract val delayAfter: Long
    
    /**
     * 点击动作
     */
    @Serializable
    data class ClickAction(
        override val id: String = "",
        override val name: String = "点击",
        override val enabled: Boolean = true,
        val x: Int = 0,
        val y: Int = 0,
        override val delayAfter: Long = 1000
    ) : Action() {
        init {
            if (this.id.isEmpty()) {
                // Kotlin data class 不允许在构造函数中修改属性
                // 这里使用 copy 创建新实例
                @Suppress("LeakingThis")
                val newId = generateId()
                this.copy(id = newId)
            }
        }
    }
    
    /**
     * 滑动动作
     */
    @Serializable
    data class SwipeAction(
        override val id: String = "",
        override val name: String = "滑动",
        override val enabled: Boolean = true,
        val startX: Int = 0,
        val startY: Int = 0,
        val endX: Int = 0,
        val endY: Int = 0,
        val duration: Long = 500,
        override val delayAfter: Long = 1000
    ) : Action() {
        init {
            if (this.id.isEmpty()) {
                @Suppress("LeakingThis")
                val newId = generateId()
                this.copy(id = newId)
            }
        }
    }
    
    /**
     * 等待动作
     */
    @Serializable
    data class WaitAction(
        override val id: String = "",
        override val name: String = "等待",
        override val enabled: Boolean = true,
        val duration: Long = 1000,
        override val delayAfter: Long = 0
    ) : Action() {
        init {
            if (this.id.isEmpty()) {
                @Suppress("LeakingThis")
                val newId = generateId()
                this.copy(id = newId)
            }
        }
    }
    
    companion object {
        fun createDefaultClick(): ClickAction {
            return ClickAction(
                id = generateId(),
                name = "点击",
                x = 500,
                y = 500
            )
        }
        
        fun createDefaultSwipe(): SwipeAction {
            return SwipeAction(
                id = generateId(),
                name = "滑动",
                startX = 300,
                startY = 500,
                endX = 700,
                endY = 500
            )
        }
        
        fun createDefaultWait(): WaitAction {
            return WaitAction(
                id = generateId(),
                name = "等待",
                duration = 1000
            )
        }
        
    // 修改前：private fun generateId(): String
    // 修改后：
    internal fun generateId(): String = UUID.randomUUID().toString().take(8)

        
        // private fun generateId(): String {
            // return "action_${System.currentTimeMillis()}_${(0..1000).random()}"
        // }
    }
}