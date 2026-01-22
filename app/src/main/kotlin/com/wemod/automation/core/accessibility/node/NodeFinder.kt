// NodeFinder.kt 保持不变（没有修改）
// 文件：app/src/main/kotlin/com/wemod/automation/core/accessibility/node/NodeFinder.kt
// ===========================================

package com.wemod.automation.core.accessibility.node

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 节点查找器 - 高级节点搜索功能
 */
class NodeFinder {
    
    companion object {
        private const val TAG = "NodeFinder"
    }
    
    /**
     * 查找节点（多重条件）
     */
    fun findNode(
        root: AccessibilityNodeInfo,
        text: String? = null,
        description: String? = null,
        className: String? = null,
        clickable: Boolean = true,
        visible: Boolean = true
    ): AccessibilityNodeInfo? {
        return findNodeRecursive(root, text, description, className, clickable, visible)
    }
    
    /**
     * 查找所有匹配节点
     */
    fun findAllNodes(
        root: AccessibilityNodeInfo,
        text: String? = null,
        description: String? = null,
        className: String? = null,
        clickable: Boolean = true
    ): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findAllNodesRecursive(root, text, description, className, clickable, result)
        return result
    }
    
    /**
     * 通过ID查找节点
     */
    fun findNodeById(
        root: AccessibilityNodeInfo,
        viewId: String
    ): AccessibilityNodeInfo? {
        return findNodeByIdRecursive(root, viewId)
    }
    
    /**
     * 通过坐标查找节点
     */
    fun findNodeByPosition(
        root: AccessibilityNodeInfo,
        x: Int,
        y: Int
    ): AccessibilityNodeInfo? {
        return findNodeByPositionRecursive(root, x, y)
    }
    
    /**
     * 查找可滚动容器
     */
    fun findScrollableContainers(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findScrollableRecursive(root, result)
        return result
    }
    
    /**
     * 查找输入框
     */
    fun findInputFields(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findInputFieldsRecursive(root, result)
        return result
    }
    
    /**
     * 转储节点层次结构（调试用）
     */
    fun dumpHierarchy(root: AccessibilityNodeInfo?): String {
        if (root == null) return "Root is null"
        
        val stringBuilder = StringBuilder()
        dumpNodeRecursive(root, stringBuilder, 0)
        return stringBuilder.toString()
    }
    
    // ===== 私有递归方法 =====
    
    private fun findNodeRecursive(
        node: AccessibilityNodeInfo,
        text: String?,
        description: String?,
        className: String?,
        clickable: Boolean,
        visible: Boolean
    ): AccessibilityNodeInfo? {
        // 检查当前节点是否匹配
        if (isNodeMatch(node, text, description, className, clickable, visible)) {
            return node
        }
        
        // 递归检查子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeRecursive(child, text, description, className, clickable, visible)
            if (found != null) {
                return found
            }
            child.recycle()
        }
        
        return null
    }
    
    private fun findAllNodesRecursive(
        node: AccessibilityNodeInfo,
        text: String?,
        description: String?,
        className: String?,
        clickable: Boolean,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        // 检查当前节点是否匹配
        if (isNodeMatch(node, text, description, className, clickable, true)) {
            result.add(AccessibilityNodeInfo.obtain(node))
        }
        
        // 递归检查子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findAllNodesRecursive(child, text, description, className, clickable, result)
            child.recycle()
        }
    }
    
    private fun findNodeByIdRecursive(
        node: AccessibilityNodeInfo,
        viewId: String
    ): AccessibilityNodeInfo? {
        // 检查当前节点的ID
        node.viewIdResourceName?.let { idName ->
            if (idName.contains(viewId)) {
                return node
            }
        }
        
        // 递归检查子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByIdRecursive(child, viewId)
            if (found != null) {
                return found
            }
            child.recycle()
        }
        
        return null
    }
    
    private fun findNodeByPositionRecursive(
        node: AccessibilityNodeInfo,
        x: Int,
        y: Int
    ): AccessibilityNodeInfo? {
        // 检查点是否在节点范围内
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        
        if (bounds.contains(x, y)) {
            // 继续在子节点中查找更精确的匹配
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                val found = findNodeByPositionRecursive(child, x, y)
                if (found != null) {
                    return found
                }
                child.recycle()
            }
            // 没有更精确的子节点匹配，返回当前节点
            return node
        }
        
        return null
    }
    
    private fun findScrollableRecursive(
        node: AccessibilityNodeInfo,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.isScrollable) {
            result.add(AccessibilityNodeInfo.obtain(node))
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findScrollableRecursive(child, result)
            child.recycle()
        }
    }
    
    private fun findInputFieldsRecursive(
        node: AccessibilityNodeInfo,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (node.isEditable || node.className?.toString()?.contains("EditText") == true) {
            result.add(AccessibilityNodeInfo.obtain(node))
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findInputFieldsRecursive(child, result)
            child.recycle()
        }
    }
    
    private fun dumpNodeRecursive(
        node: AccessibilityNodeInfo,
        builder: StringBuilder,
        depth: Int
    ) {
        val indent = "  ".repeat(depth)
        
        builder.append("$indent├─ ")
        builder.append("Class: ${node.className?.toString() ?: "unknown"}\n")
        
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { text ->
            builder.append("${indent}│  Text: \"$text\"\n")
        }
        
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { desc ->
            builder.append("${indent}│  Desc: \"$desc\"\n")
        }
        
        node.viewIdResourceName?.let { id ->
            builder.append("${indent}│  ID: $id\n")
        }
        
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        builder.append("${indent}│  Bounds: [${bounds.left},${bounds.top}-${bounds.right},${bounds.bottom}]\n")
        
        builder.append("${indent}│  Clickable: ${node.isClickable}, Visible: ${node.isVisibleToUser}\n")
        
        // 递归处理子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                dumpNodeRecursive(child, builder, depth + 1)
                child.recycle()
            }
        }
    }
    
    private fun isNodeMatch(
        node: AccessibilityNodeInfo,
        text: String?,
        description: String?,
        className: String?,
        clickable: Boolean,
        visible: Boolean
    ): Boolean {
        // 可见性检查
        if (visible && !node.isVisibleToUser) {
            return false
        }
        
        // 可点击性检查
        if (clickable && !node.isClickable) {
            return false
        }
        
        // 文本匹配
        text?.let { 
            val nodeText = node.text?.toString()
            if (nodeText == null || !nodeText.contains(it, ignoreCase = true)) {
                return false
            }
        }
        
        // 描述匹配
        description?.let {
            val nodeDesc = node.contentDescription?.toString()
            if (nodeDesc == null || !nodeDesc.contains(it, ignoreCase = true)) {
                return false
            }
        }
        
        // 类名匹配
        className?.let {
            val nodeClass = node.className?.toString()
            if (nodeClass == null || !nodeClass.contains(it, ignoreCase = true)) {
                return false
            }
        }
        
        return true
    }
}