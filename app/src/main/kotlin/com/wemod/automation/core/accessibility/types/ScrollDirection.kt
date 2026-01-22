// 新建：app/src/main/kotlin/com/wemod/automation/core/accessibility/types/ScrollDirection.kt
// ===========================================

package com.wemod.automation.core.accessibility.types

/**
 * 统一的滚动方向枚举
 */
enum class ScrollDirection {
    FORWARD,    // 向前滚动（列表）
    BACKWARD,   // 向后滚动（列表）
    UP,         // 向上滚动
    DOWN,       // 向下滚动
    LEFT,       // 向左滚动
    RIGHT       // 向右滚动
}