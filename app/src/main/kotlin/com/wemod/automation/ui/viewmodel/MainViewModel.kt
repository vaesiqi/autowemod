package com.wemod.automation.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wemod.automation.data.repository.ScriptRepository
import com.wemod.automation.model.Script
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主界面ViewModel
 */
class MainViewModel(private val context: Context) : ViewModel() {
    
    private val scriptRepository = ScriptRepository(context)
    
    // 脚本列表状态
    private val _scripts = MutableStateFlow<List<Script>>(emptyList())
    val scripts: StateFlow<List<Script>> = _scripts.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 当前选中的脚本
    private val _selectedScript = MutableStateFlow<Script?>(null)
    val selectedScript: StateFlow<Script?> = _selectedScript.asStateFlow()
    
    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadScripts()
    }
    
    /**
     * 加载所有脚本
     */
    fun loadScripts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val loadedScripts = scriptRepository.getAllScripts()
                _scripts.value = loadedScripts
                if (loadedScripts.isEmpty()) {
                    // 如果没有脚本，创建一个示例
                    createSampleScript()
                }
            } catch (e: Exception) {
                _errorMessage.value = "加载脚本失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 创建新脚本
     */
    fun createNewScript(): String {
        return try {
            val newScript = Script.createEmpty()
            if (scriptRepository.saveScript(newScript)) {
                loadScripts() // 重新加载列表
                newScript.id
            } else {
                throw Exception("保存脚本失败")
            }
        } catch (e: Exception) {
            _errorMessage.value = "创建脚本失败: ${e.message}"
            ""
        }
    }
    
    /**
     * 创建示例脚本
     */
    private fun createSampleScript() {
        viewModelScope.launch {
            val sampleScript = Script.createSample()
            if (scriptRepository.saveScript(sampleScript)) {
                loadScripts()
            }
        }
    }
    
    /**
     * 删除脚本
     */
    fun deleteScript(scriptId: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            
            try {
                if (scriptRepository.deleteScript(scriptId)) {
                    loadScripts()
                    
                    // 如果删除的是当前选中的脚本，清空选择
                    if (_selectedScript.value?.id == scriptId) {
                        _selectedScript.value = null
                    }
                } else {
                    throw Exception("删除失败")
                }
            } catch (e: Exception) {
                _errorMessage.value = "删除脚本失败: ${e.message}"
            }
        }
    }
    
    /**
     * 选择脚本
     */
    fun selectScript(scriptId: String) {
        viewModelScope.launch {
            _selectedScript.value = scriptRepository.loadScript(scriptId)
        }
    }
    
    /**
     * 更新脚本
     */
    fun updateScript(script: Script): Boolean {
        return try {
            val success = scriptRepository.saveScript(script)
            if (success) {
                loadScripts()
                _selectedScript.value = script
            }
            success
        } catch (e: Exception) {
            _errorMessage.value = "更新脚本失败: ${e.message}"
            false
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 获取脚本数量
     */
    fun getScriptCount(): Int = _scripts.value.size
    
    /**
     * 检查是否有脚本
     */
    fun hasScripts(): Boolean = _scripts.value.isNotEmpty()
    
    /**
     * 导出脚本
     */
    fun exportScript(scriptId: String): String? {
        return try {
            val script = scriptRepository.loadScript(scriptId)
            script?.let { scriptRepository.exportScript(it) }
        } catch (e: Exception) {
            _errorMessage.value = "导出脚本失败: ${e.message}"
            null
        }
    }
    
    /**
     * 导入脚本
     */
    fun importScript(jsonString: String): Boolean {
        return try {
            val script = scriptRepository.importScript(jsonString)
            script?.let {
                scriptRepository.saveScript(it)
                loadScripts()
                true
            } ?: false
        } catch (e: Exception) {
            _errorMessage.value = "导入脚本失败: ${e.message}"
            false
        }
    }
}