package com.wemod.automation.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 脚本模型
 */
@Serializable
data class Script(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "未命名脚本",
    val createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis(),
    val actions: MutableList<Action> = mutableListOf()
) {
    fun addAction(action: Action) {
        // 确保动作有ID
        val actionWithId = when (action) {
            is Action.ClickAction -> if (action.id.isEmpty()) action.copy(id = Action.generateId()) else action
            is Action.SwipeAction -> if (action.id.isEmpty()) action.copy(id = Action.generateId()) else action
            is Action.WaitAction -> if (action.id.isEmpty()) action.copy(id = Action.generateId()) else action
        }
        
        actions.add(actionWithId)
        modifiedAt = System.currentTimeMillis()
    }
    
    fun removeAction(actionId: String): Boolean {
        return actions.removeIf { it.id == actionId }
            .also { if (it) modifiedAt = System.currentTimeMillis() }
    }
    
    fun moveAction(fromIndex: Int, toIndex: Int) {
        if (fromIndex in actions.indices && toIndex in actions.indices) {
            val action = actions.removeAt(fromIndex)
            actions.add(toIndex, action)
            modifiedAt = System.currentTimeMillis()
        }
    }
    
    fun updateAction(updatedAction: Action): Boolean {
        val index = actions.indexOfFirst { it.id == updatedAction.id }
        if (index != -1) {
            actions[index] = updatedAction
            modifiedAt = System.currentTimeMillis()
            return true
        }
        return false
    }
    
    fun getAction(actionId: String): Action? {
        return actions.find { it.id == actionId }
    }
    
    fun clearActions() {
        actions.clear()
        modifiedAt = System.currentTimeMillis()
    }
    
    fun isEmpty(): Boolean = actions.isEmpty()
    
    fun size(): Int = actions.size
    
    companion object {
        fun createEmpty(): Script {
            return Script(name = "新脚本")
        }
        
        fun createSample(): Script {
            return Script(
                name = "示例脚本",
                actions = mutableListOf(
                    Action.createDefaultClick(),
                    Action.createDefaultWait(),
                    Action.createDefaultSwipe()
                )
            )
        }
        
        fun createFromActions(name: String, actions: List<Action>): Script {
            return Script(
                name = name,
                actions = actions.toMutableList()
            )
        }
    }
}