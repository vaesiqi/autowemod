package com.wemod.automation.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 项目管理
 */
@Serializable
data class Project(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "新项目",
    val createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis(),
    val scripts: MutableList<Script> = mutableListOf()
) {
    fun addScript(script: Script) {
        scripts.add(script)
        modifiedAt = System.currentTimeMillis()
    }
    
    fun removeScript(scriptId: String): Boolean {
        return scripts.removeIf { it.id == scriptId }
            .also { if (it) modifiedAt = System.currentTimeMillis() }
    }
    
    fun getScript(scriptId: String): Script? {
        return scripts.find { it.id == scriptId }
    }
    
    fun updateScript(updatedScript: Script): Boolean {
        val index = scripts.indexOfFirst { it.id == updatedScript.id }
        if (index != -1) {
            scripts[index] = updatedScript
            modifiedAt = System.currentTimeMillis()
            return true
        }
        return false
    }
    
    companion object {
        fun createEmpty(): Project {
            return Project(name = "新项目")
        }
        
        fun createSample(): Project {
            return Project(
                name = "示例项目",
                scripts = mutableListOf(
                    Script.createSample()
                )
            )
        }
    }
}