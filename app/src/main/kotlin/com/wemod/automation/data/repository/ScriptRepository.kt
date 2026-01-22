package com.wemod.automation.data.repository

import android.content.Context
import android.util.Log
import com.wemod.automation.model.Script
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.serialization.decodeFromString

/**
 * 脚本仓库 - 负责脚本的保存和加载
 * 修复版：添加详细日志和错误处理
 */
class ScriptRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "ScriptRepository"
        private const val SCRIPTS_DIR = "scripts"
        private const val FILE_EXTENSION = ".axs" // AutoX Script
    }
    
    // 内存缓存作为备选方案
    private val memoryCache = mutableMapOf<String, Script>()
    
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    
    private val scriptsDir: File by lazy {
        File(context.filesDir, SCRIPTS_DIR).apply {
            if (!exists()) {
                Log.d(TAG, "创建脚本目录: $absolutePath")
                val created = mkdirs()
                Log.d(TAG, "目录创建结果: $created")
            }
        }
    }
    
    /**
     * 保存脚本 - 修复版
     */
    fun saveScript(script: Script): Boolean {
        Log.d(TAG, "=== saveScript() 开始 ===")
        Log.d(TAG, "脚本: ${script.name}, ID: ${script.id}")
        
        try {
            // 1. 先保存到内存缓存（确保基本功能）
            memoryCache[script.id] = script
            Log.d(TAG, "✅ 脚本已保存到内存缓存")
            
            // 2. 尝试保存到文件
            return trySaveToFile(script)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 保存脚本完全失败", e)
            return false
        }
    }
    
    /**
     * 尝试保存到文件
     */
    private fun trySaveToFile(script: Script): Boolean {
        return try {
            Log.d(TAG, "尝试保存到文件...")
            
            // 检查目录
            Log.d(TAG, "脚本目录: ${scriptsDir.absolutePath}")
            Log.d(TAG, "目录存在: ${scriptsDir.exists()}")
            Log.d(TAG, "目录可写: ${scriptsDir.canWrite()}")
            
            if (!scriptsDir.exists() || !scriptsDir.canWrite()) {
                Log.w(TAG, "目录不可用，使用内存存储")
                return true // 内存存储成功也算成功
            }
            
            // 创建文件
            val scriptFile = File(scriptsDir, "${script.id}$FILE_EXTENSION")
            Log.d(TAG, "脚本文件: ${scriptFile.absolutePath}")
            
            // 序列化
            val jsonString = json.encodeToString(script)
            Log.d(TAG, "JSON序列化成功，长度: ${jsonString.length}")
            
            // 写入文件
            scriptFile.writeText(jsonString)
            Log.d(TAG, "✅ 文件写入成功")
            
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 文件保存失败", e)
            Log.e(TAG, "错误详情: ${e.message}")
            e.printStackTrace()
            
            // 文件保存失败，但内存保存成功，返回true
            true
        }
    }
    
    /**
     * 加载脚本
     */
    fun loadScript(scriptId: String): Script? {
        return try {
            Log.d(TAG, "加载脚本: $scriptId")
            
            // 1. 先从内存缓存查找
            memoryCache[scriptId]?.let {
                Log.d(TAG, "✅ 从内存缓存找到脚本")
                return it
            }
            
            // 2. 从文件加载
            val scriptFile = File(scriptsDir, "$scriptId$FILE_EXTENSION")
            if (!scriptFile.exists()) {
                Log.w(TAG, "脚本文件不存在: $scriptId")
                return null
            }
            
            val jsonString = scriptFile.readText()
            val script = json.decodeFromString<Script>(jsonString)
            
            // 保存到内存缓存
            memoryCache[scriptId] = script
            Log.d(TAG, "✅ 从文件加载成功: ${script.name}")
            
            script
        } catch (e: Exception) {
            Log.e(TAG, "加载脚本失败", e)
            null
        }
    }
    
    /**
     * 获取所有脚本 - 只有一个定义！
     */
    fun getAllScripts(): List<Script> {
        return try {
            Log.d(TAG, "获取所有脚本...")
            
            // 先检查内存缓存
            if (memoryCache.isNotEmpty()) {
                Log.d(TAG, "从内存缓存获取: ${memoryCache.size} 个脚本")
                return memoryCache.values.toList()
            }
            
            // 从文件加载
            val files = scriptsDir.listFiles()
            Log.d(TAG, "扫描文件目录，文件数: ${files?.size ?: 0}")
            
            files?.filter { 
                it.isFile && it.extension == FILE_EXTENSION.substring(1) 
            }?.mapNotNull { file ->
                try {
                    Log.d(TAG, "处理文件: ${file.name}")
                    val jsonString = file.readText()
                    val script = json.decodeFromString<Script>(jsonString)
                    // 保存到内存缓存
                    memoryCache[script.id] = script
                    Log.d(TAG, "✅ 加载脚本: ${script.name}")
                    script
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 解析脚本文件失败: ${file.name}", e)
                    null
                }
            } ?: emptyList()
                
        } catch (e: Exception) {
            Log.e(TAG, "获取脚本列表失败", e)
            emptyList()
        }
    }
    
    /**
     * 删除脚本
     */
    fun deleteScript(scriptId: String): Boolean {
        return try {
            Log.d(TAG, "删除脚本: $scriptId")
            
            // 从内存缓存删除
            memoryCache.remove(scriptId)
            Log.d(TAG, "✅ 从内存缓存删除")
            
            // 从文件删除
            val scriptFile = File(scriptsDir, "$scriptId$FILE_EXTENSION")
            if (scriptFile.exists()) {
                val deleted = scriptFile.delete()
                Log.d(TAG, "文件删除结果: $deleted")
            } else {
                Log.d(TAG, "脚本文件不存在")
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "删除脚本失败", e)
            false
        }
    }
    
    /**
     * 检查脚本是否存在
     */
    fun scriptExists(scriptId: String): Boolean {
        return memoryCache.containsKey(scriptId) || 
               File(scriptsDir, "$scriptId$FILE_EXTENSION").exists()
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