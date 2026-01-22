// ===========================================
// 文件：app/src/main/kotlin/com/wemod/automation/model/Script.kt
// 修改：使用 ActionData 替代 Action
// ===========================================

package com.wemod.automation.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 脚本模型
 * 修改：使用 ActionData 替代 Action
 */
@Serializable
data class Script(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "未命名脚本",
    val createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis(),
    val actions: MutableList<ActionData> = mutableListOf()  // ✅ 修改为 ActionData
) {
    
    /**
     * 添加动作到脚本
     */
    fun addAction(action: ActionData) {
        // 确保动作有ID
        val actionWithId = if (action.id.isEmpty()) {
            action.copy(id = generateActionId())  // ✅ 使用新的生成方法
        } else {
            action
        }
        
        actions.add(actionWithId)
        modifiedAt = System.currentTimeMillis()
    }
    
    /**
     * 批量添加动作
     */
    fun addActions(vararg newActions: ActionData) {
        newActions.forEach { addAction(it) }
    }
    
    /**
     * 删除指定ID的动作
     */
    fun removeAction(actionId: String): Boolean {
        return actions.removeIf { it.id == actionId }
            .also { if (it) modifiedAt = System.currentTimeMillis() }
    }
    
    /**
     * 移动动作位置
     */
    fun moveAction(fromIndex: Int, toIndex: Int) {
        if (fromIndex in actions.indices && toIndex in actions.indices && fromIndex != toIndex) {
            val action = actions.removeAt(fromIndex)
            actions.add(toIndex, action)
            modifiedAt = System.currentTimeMillis()
        }
    }
    
    /**
     * 上移动作
     */
    fun moveActionUp(actionId: String): Boolean {
        val index = actions.indexOfFirst { it.id == actionId }
        if (index > 0) {
            moveAction(index, index - 1)
            return true
        }
        return false
    }
    
    /**
     * 下移动作
     */
    fun moveActionDown(actionId: String): Boolean {
        val index = actions.indexOfFirst { it.id == actionId }
        if (index != -1 && index < actions.size - 1) {
            moveAction(index, index + 1)
            return true
        }
        return false
    }
    
    /**
     * 更新动作
     */
    fun updateAction(updatedAction: ActionData): Boolean {
        val index = actions.indexOfFirst { it.id == updatedAction.id }
        if (index != -1) {
            actions[index] = updatedAction
            modifiedAt = System.currentTimeMillis()
            return true
        }
        return false
    }
    
    /**
     * 获取指定ID的动作
     */
    fun getAction(actionId: String): ActionData? {
        return actions.find { it.id == actionId }
    }
    
    /**
     * 获取指定索引的动作
     */
    fun getActionAt(index: Int): ActionData? {
        return actions.getOrNull(index)
    }
    
    /**
     * 清空所有动作
     */
    fun clearActions() {
        actions.clear()
        modifiedAt = System.currentTimeMillis()
    }
    
    /**
     * 检查脚本是否为空
     */
    fun isEmpty(): Boolean = actions.isEmpty()
    
    /**
     * 获取动作数量
     */
    fun size(): Int = actions.size
    
    /**
     * 获取脚本中启用的动作数量
     */
    fun enabledActionsCount(): Int {
        return actions.count { it.enabled }
    }
    
    /**
     * 获取脚本中禁用的动作数量
     */
    fun disabledActionsCount(): Int {
        return actions.count { !it.enabled }
    }
    
    /**
     * 启用/禁用所有动作
     */
    fun setAllActionsEnabled(enabled: Boolean) {
        actions.replaceAll { action ->
            action.copy(enabled = enabled)
        }
        modifiedAt = System.currentTimeMillis()
    }
    
    /**
     * 获取指定类型的动作列表
     */
    fun getActionsByType(type: String): List<ActionData> {
        return actions.filter { it.type == type }
    }
    
    /**
     * 获取脚本中所有动作类型的统计
     */
    fun getActionTypeCounts(): Map<String, Int> {
        return actions.groupingBy { it.type }.eachCount()
    }
    
    /**
     * 获取动作类型统计的字符串表示
     */
    fun getActionTypeSummary(): String {
        val counts = getActionTypeCounts()
        return if (counts.isEmpty()) {
            "无动作"
        } else {
            counts.entries.joinToString("、") { (type, count) ->
                "${getActionTypeDisplayName(type)}($count)"
            }
        }
    }
    
    /**
     * 获取动作类型的显示名称
     */
    private fun getActionTypeDisplayName(type: String): String {
        return when (type) {
            "click" -> "点击"
            "swipe" -> "滑动"
            "wait" -> "等待"
            else -> type
        }
    }
    
    /**
     * 估算脚本执行时间（毫秒）
     */
    fun estimateExecutionTime(): Long {
        return actions.sumOf { action ->
            val baseTime = when (action.type) {
                "click" -> 100L      // 点击大约100ms
                "swipe" -> action.getParam("duration", 500L) + 200L  // 滑动时间+额外开销
                "wait" -> action.getParam("duration", 1000L)
                else -> 200L         // 其他动作默认200ms
            }
            baseTime + action.delayAfter
        }
    }
    
    /**
     * 获取脚本执行时间的人类可读字符串
     */
    fun getExecutionTimeDisplay(): String {
        val totalMs = estimateExecutionTime()
        return when {
            totalMs < 1000 -> "${totalMs}毫秒"
            totalMs < 60000 -> "${totalMs / 1000}秒"
            else -> "${totalMs / 60000}分${(totalMs % 60000) / 1000}秒"
        }
    }
    
    /**
     * 验证脚本中的所有动作
     */
    fun validateActions(): List<Pair<ActionData, ActionValidator.ValidationResult>> {
        return actions.map { action ->
            action to ActionValidator.validate(action)
        }
    }
    
    /**
     * 获取所有无效的动作
     */
    fun getInvalidActions(): List<ActionData> {
        return actions.filter { action ->
            !ActionValidator.validate(action).isValid
        }
    }
    
    /**
     * 检查脚本是否有效（所有动作都有效）
     */
    fun isValid(): Boolean {
        return getInvalidActions().isEmpty()
    }
    
    /**
     * 复制脚本
     */
    fun copy(
        newId: String = UUID.randomUUID().toString(),
        newName: String = "${name} - 副本",
        includeActions: Boolean = true
    ): Script {
        return Script(
            id = newId,
            name = newName,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            actions = if (includeActions) {
                actions.map { it.copy(id = generateActionId()) }.toMutableList()
            } else {
                mutableListOf()
            }
        )
    }
    
    /**
     * 将脚本转换为调试字符串
     */
    fun toDebugString(): String {
        return buildString {
            appendLine("脚本: $name (ID: $id)")
            appendLine("创建时间: ${java.util.Date(createdAt)}")
            appendLine("修改时间: ${java.util.Date(modifiedAt)}")
            appendLine("动作数量: ${actions.size}")
            appendLine("预估执行时间: ${getExecutionTimeDisplay()}")
            appendLine("动作详情:")
            
            if (actions.isEmpty()) {
                appendLine("  - 无动作")
            } else {
                actions.forEachIndexed { index, action ->
                    append("  ${index + 1}. ${action.name} [${action.type}]")
                    if (!action.enabled) append(" (禁用)")
                    appendLine()
                    
                    // 显示动作参数
                    action.params.forEach { (key, value) ->
                        appendLine("      $key: $value")
                    }
                    
                    if (action.delayAfter > 0) {
                        appendLine("      延迟: ${action.delayAfter}ms")
                    }
                }
            }
        }
    }
    
    /**
     * 生成动作ID
     */
    private fun generateActionId(): String {
        return UUID.randomUUID().toString().take(8)
    }
    
    companion object {
        /**
         * 创建空脚本
         */
        fun createEmpty(): Script {
            return Script(name = "新脚本")
        }
        
        /**
         * 创建示例脚本
         */
        fun createSample(): Script {
            return Script(
                name = "示例脚本",
                actions = mutableListOf(
                    ActionData.createClick(
                        name = "点击屏幕中心",
                        x = 500,
                        y = 500
                    ),
                    ActionData.createWait(
                        name = "等待1秒",
                        duration = 1000L
                    ),
                    ActionData.createSwipe(
                        name = "水平滑动",
                        startX = 300,
                        startY = 500,
                        endX = 700,
                        endY = 500,
                        duration = 500L
                    )
                )
            )
        }
        
        /**
         * 创建测试脚本
         */
        fun createTestScript(): Script {
            return Script(
                name = "测试脚本",
                actions = mutableListOf(
                    ActionData.createClick(name = "测试点击", x = 500, y = 500),
                    ActionData.createWait(name = "测试等待", duration = 500L),
                    ActionData.createSwipe(
                        name = "测试滑动",
                        startX = 300,
                        startY = 500,
                        endX = 700,
                        endY = 500
                    )
                )
            )
        }
        
        /**
         * 从动作列表创建脚本
         */
        fun createFromActions(name: String, actions: List<ActionData>): Script {
            return Script(
                name = name,
                actions = actions.toMutableList()
            )
        }
        
        /**
         * 从旧版脚本转换（兼容性）
         */
        @Deprecated("仅用于从旧版本迁移", replaceWith = ReplaceWith("新的 Script 对象"))
        fun fromLegacy(legacyScript: Any?): Script? {
            // 这里应该实现从旧版 Script 到新版 Script 的转换
            // 由于我们直接重构，暂时返回 null
            return null
        }
    }
}

// ===== 脚本统计信息扩展 =====

/**
 * 脚本统计信息
 */
data class ScriptStats(
    val totalActions: Int,
    val enabledActions: Int,
    val disabledActions: Int,
    val estimatedDuration: Long,  // 预估执行时间（毫秒）
    val actionTypeCounts: Map<String, Int>,
    val isValid: Boolean,
    val invalidActions: List<ActionData>
)

/**
 * 计算脚本统计信息
 */
fun Script.calculateStats(): ScriptStats {
    val enabledActions = actions.count { it.enabled }
    val disabledActions = actions.count { !it.enabled }
    val estimatedDuration = estimateExecutionTime()
    val invalidActions = getInvalidActions()
    
    return ScriptStats(
        totalActions = actions.size,
        enabledActions = enabledActions,
        disabledActions = disabledActions,
        estimatedDuration = estimatedDuration,
        actionTypeCounts = getActionTypeCounts(),
        isValid = invalidActions.isEmpty(),
        invalidActions = invalidActions
    )
}

/**
 * 脚本组（用于组织相关脚本）
 */
data class ScriptGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val scripts: MutableList<Script> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis()
) {
    fun addScript(script: Script): Boolean {
        if (scripts.any { it.id == script.id }) {
            return false
        }
        scripts.add(script)
        modifiedAt = System.currentTimeMillis()
        return true
    }
    
    fun removeScript(scriptId: String): Boolean {
        return scripts.removeIf { it.id == scriptId }
            .also { if (it) modifiedAt = System.currentTimeMillis() }
    }
    
    fun getScript(scriptId: String): Script? {
        return scripts.find { it.id == scriptId }
    }
    
    fun containsScript(scriptId: String): Boolean {
        return scripts.any { it.id == scriptId }
    }
    
    val scriptCount: Int
        get() = scripts.size
    
    val totalActionsCount: Int
        get() = scripts.sumOf { it.actions.size }
}

// ===== 脚本排序扩展 =====

/**
 * 脚本排序方式
 */
enum class ScriptSortOrder {
    NAME_ASC,      // 名称升序
    NAME_DESC,     // 名称降序
    DATE_NEWEST,   // 最新修改
    DATE_OLDEST,   // 最早修改
    ACTION_COUNT,  // 动作数量
    DURATION       // 执行时长
}

/**
 * 脚本列表排序
 */
fun List<Script>.sortedBy(order: ScriptSortOrder): List<Script> {
    return when (order) {
        ScriptSortOrder.NAME_ASC -> sortedBy { it.name }
        ScriptSortOrder.NAME_DESC -> sortedByDescending { it.name }
        ScriptSortOrder.DATE_NEWEST -> sortedByDescending { it.modifiedAt }
        ScriptSortOrder.DATE_OLDEST -> sortedBy { it.modifiedAt }
        ScriptSortOrder.ACTION_COUNT -> sortedByDescending { it.actions.size }
        ScriptSortOrder.DURATION -> sortedByDescending { it.estimateExecutionTime() }
    }
}