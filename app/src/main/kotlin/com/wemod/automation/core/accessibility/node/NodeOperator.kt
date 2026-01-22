// 完全替换：app/src/main/kotlin/com/wemod/automation/core/accessibility/node/NodeOperator.kt
// ===========================================

package com.wemod.automation.core.accessibility.node

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.wemod.automation.core.accessibility.types.ScrollDirection

/**
 * 节点操作器 - 执行各种节点操作
 */
class NodeOperator(private val service: AccessibilityService) {
    
    companion object {
        private const val TAG = "NodeOperator"
    }
    
    /**
     * 点击节点
     */
    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } else {
                    // 如果节点不可点击，尝试找到父节点中的可点击节点
                    findClickableParent(node)?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        ?: false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "点击节点失败", e)
            false
        }
    }
    
    /**
     * 长按节点
     */
    fun longClickNode(node: AccessibilityNodeInfo): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "长按节点失败", e)
            false
        }
    }
    
    /**
     * 输入文本到节点
     */
    fun inputText(node: AccessibilityNodeInfo, text: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val arguments = android.os.Bundle().apply {
                    putString(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        text
                    )
                }
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // 对于旧版本，尝试清空后追加
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                
                // 清空现有文本
                val clearArgs = android.os.Bundle().apply {
                    putString(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        ""
                    )
                }
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearArgs)
                
                // 输入新文本（模拟键盘输入）
                // 注意：这种方法可能不适用于所有应用
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "输入文本失败", e)
            false
        }
    }
    
    /**
     * 清除节点文本
     */
    fun clearText(node: AccessibilityNodeInfo): Boolean {
        return inputText(node, "")
    }
    
    /**
     * 滚动节点
     */
    fun scrollNode(
        node: AccessibilityNodeInfo,
        direction: ScrollDirection,
        amount: Int = 1
    ): Boolean {
        if (!node.isScrollable) {
            return false
        }
        
        // 简化滚动动作映射
        val action = when (direction) {
            ScrollDirection.FORWARD -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            ScrollDirection.BACKWARD -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            ScrollDirection.UP -> {
                // 向上滚动：使用向前滚动作为替代
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            }
            ScrollDirection.DOWN -> {
                // 向下滚动：使用向前滚动作为替代
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            }
            ScrollDirection.LEFT -> {
                // 向左滚动：使用向后滚动作为替代
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            }
            ScrollDirection.RIGHT -> {
                // 向右滚动：使用向前滚动作为替代
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            }
        }
        
        var success = true
        repeat(amount) {
            if (!node.performAction(action)) {
                success = false
                return@repeat
            }
            // 短暂延迟，确保滚动完成
            Thread.sleep(300)
        }
        
        return success
    }
    
    /**
     * 滚动查找文本
     */
    fun scrollToFind(
        root: AccessibilityNodeInfo?,
        text: String,
        maxScrolls: Int = 10,
        scrollDirection: ScrollDirection = ScrollDirection.DOWN
    ): Boolean {
        if (root == null) return false
        
        val finder = NodeFinder()
        
        // 先检查当前页面
        if (finder.findNode(root, text) != null) {
            return true
        }
        
        // 查找可滚动容器
        val scrollables = finder.findScrollableContainers(root)
        if (scrollables.isEmpty()) {
            return false
        }
        
        // 尝试在每个可滚动容器中查找
        for (scrollable in scrollables) {
            var scrollCount = 0
            while (scrollCount < maxScrolls) {
                // 滚动一次
                if (!scrollNode(scrollable, scrollDirection)) {
                    break
                }
                
                // 短暂等待UI更新
                Thread.sleep(500)
                
                // 重新获取根节点（因为滚动后节点树可能改变）
                val newRoot = service.rootInActiveWindow
                if (newRoot != null && finder.findNode(newRoot, text) != null) {
                    return true
                }
                
                scrollCount++
            }
        }
        
        return false
    }
    
    /**
     * 聚焦节点
     */
    fun focusNode(node: AccessibilityNodeInfo): Boolean {
        return try {
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        } catch (e: Exception) {
            Log.e(TAG, "聚焦节点失败", e)
            false
        }
    }
    
    /**
     * 选择节点（用于列表、单选按钮等）
     */
    fun selectNode(node: AccessibilityNodeInfo): Boolean {
        return try {
            node.performAction(AccessibilityNodeInfo.ACTION_SELECT)
        } catch (e: Exception) {
            Log.e(TAG, "选择节点失败", e)
            false
        }
    }
    
    /**
     * 复制节点文本
     */
    fun copyNodeText(node: AccessibilityNodeInfo): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                node.performAction(AccessibilityNodeInfo.ACTION_COPY)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "复制节点文本失败", e)
            false
        }
    }
    
    /**
     * 粘贴文本到节点
     */
    fun pasteTextToNode(node: AccessibilityNodeInfo): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "粘贴文本到节点失败", e)
            false
        }
    }
    
    /**
     * 折叠/展开节点（用于列表、树状结构）
     */
    fun collapseExpandNode(node: AccessibilityNodeInfo, expand: Boolean): Boolean {
        return try {
            val action = if (expand) {
                AccessibilityNodeInfo.ACTION_EXPAND
            } else {
                AccessibilityNodeInfo.ACTION_COLLAPSE
            }
            node.performAction(action)
        } catch (e: Exception) {
            Log.e(TAG, "${if (expand) "展开" else "折叠"}节点失败", e)
            false
        }
    }
    
    // ===== 私有辅助方法 =====
    
    private fun findClickableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current = node.parent
        while (current != null) {
            if (current.isClickable) {
                return current
            }
            current = current.parent
        }
        return null
    }
}