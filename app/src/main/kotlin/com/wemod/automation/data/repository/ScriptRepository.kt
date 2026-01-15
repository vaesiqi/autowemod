package com.wemod.automation.data.repository

import android.content.Context
import android.util.Log
import com.wemod.automation.model.Script
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 脚本仓库 - 负责脚本的保存和加载
 */
class ScriptRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "ScriptRepository"
        private const val SCRIPTS_DIR = "scripts"
        private const val FILE_EXTENSION = ".axs" // AutoX Script
    }
    
    private val scriptsDir: File by lazy {
        File(context.filesDir, SCRIPTS_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    
    /**
     * 保存脚本
     */
    fun saveScript(script: Script): Boolean {
        return try {
            val scriptFile = File(scriptsDir, "${script.id}$FILE_EXTENSION")
            val jsonString = json.encodeToString(script)
            scriptFile.writeText(jsonString)
            Log.d(TAG, "脚本保存成功: ${script.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "保存脚本失败", e)
            false
        }
    }
    
    /**
     * 加载脚本
     */
    fun loadScript(scriptId: String): Script? {
        return try {
            val scriptFile = File(scriptsDir, "$scriptId$FILE_EXTENSION")
            if (!scriptFile.exists()) {
                Log.w(TAG, "脚本文件不存在: $scriptId")
                return null
            }
            
            val jsonString = scriptFile.readText()
            json.decodeFromString<Script>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "加载脚本失败", e)
            null
        }
    }
    
    /**
     * 删除脚本
     */
    fun deleteScript(scriptId: String): Boolean {
        return try {
            val scriptFile = File(scriptsDir, "$scriptId$FILE_EXTENSION")
            if (scriptFile.exists()) {
                scriptFile.delete()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "删除脚本失败", e)
            false
        }
    }
    
    /**
     * 获取所有脚本
     */
    fun getAllScripts(): List<Script> {
        return try {
            scriptsDir.listFiles()?.filter { it.extension == FILE_EXTENSION.substring(1) }
                ?.mapNotNull { file ->
                    try {
                        val jsonString = file.readText()
                        json.decodeFromString<Script>(jsonString)
                    } catch (e: Exception) {
                        Log.e(TAG, "解析脚本文件失败: ${file.name}", e)
                        null
                    }
                } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "获取脚本列表失败", e)
            emptyList()
        }
    }
    
    /**
     * 检查脚本是否存在
     */
    fun scriptExists(scriptId: String): Boolean {
        val scriptFile = File(scriptsDir, "$scriptId$FILE_EXTENSION")
        return scriptFile.exists()
    }
    
    /**
     * 获取脚本文件路径
     */
    fun getScriptFilePath(scriptId: String): String? {
        val scriptFile = File(scriptsDir, "$scriptId$FILE_EXTENSION")
        return if (scriptFile.exists()) scriptFile.absolutePath else null
    }
    
    /**
     * 导出脚本为JSON字符串
     */
    fun exportScript(script: Script): String {
        return json.encodeToString(script)
    }
    
    /**
     * 从JSON字符串导入脚本
     */
    fun importScript(jsonString: String): Script? {
        return try {
            json.decodeFromString<Script>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "导入脚本失败", e)
            null
        }
    }
}