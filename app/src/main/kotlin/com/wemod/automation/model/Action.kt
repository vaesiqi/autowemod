// ===========================================
// 文件：app/src/main/kotlin/com/wemod/automation/model/Action.kt
// 修复版：使用特定参数类型替代Any类型，解决序列化问题
// ===========================================

package com.wemod.automation.model

import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.util.UUID

// ===== 参数值类型定义 =====

/**
 * 动作参数值的密封类，支持多种类型
 */
@Serializable(with = ActionParamSerializer::class)
sealed class ActionParam {
    @Serializable
    data class StringParam(val value: String) : ActionParam()
    
    @Serializable
    data class IntParam(val value: Int) : ActionParam()
    
    @Serializable
    data class LongParam(val value: Long) : ActionParam()
    
    @Serializable
    data class BooleanParam(val value: Boolean) : ActionParam()
    
    @Serializable
    data class FloatParam(val value: Float) : ActionParam()
    
    @Serializable
    data class DoubleParam(val value: Double) : ActionParam()
    
    /**
     * 获取原始值
     */
    fun getValue(): Any = when (this) {
        is StringParam -> value
        is IntParam -> value
        is LongParam -> value
        is BooleanParam -> value
        is FloatParam -> value
        is DoubleParam -> value
    }
    
    /**
     * 转换为字符串
     */
    override fun toString(): String {
        return getValue().toString()
    }
}

/**
 * ActionParam序列化器
 */
object ActionParamSerializer : KSerializer<ActionParam> {
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ActionParam") {
        element<String>("type")
        element<String>("value")
    }
    
    override fun serialize(encoder: Encoder, value: ActionParam) {
        encoder.encodeStructure(descriptor) {
            when (value) {
                is ActionParam.StringParam -> {
                    encodeStringElement(descriptor, 0, "string")
                    encodeStringElement(descriptor, 1, value.value)
                }
                is ActionParam.IntParam -> {
                    encodeStringElement(descriptor, 0, "int")
                    encodeStringElement(descriptor, 1, value.value.toString())
                }
                is ActionParam.LongParam -> {
                    encodeStringElement(descriptor, 0, "long")
                    encodeStringElement(descriptor, 1, value.value.toString())
                }
                is ActionParam.BooleanParam -> {
                    encodeStringElement(descriptor, 0, "boolean")
                    encodeStringElement(descriptor, 1, value.value.toString())
                }
                is ActionParam.FloatParam -> {
                    encodeStringElement(descriptor, 0, "float")
                    encodeStringElement(descriptor, 1, value.value.toString())
                }
                is ActionParam.DoubleParam -> {
                    encodeStringElement(descriptor, 0, "double")
                    encodeStringElement(descriptor, 1, value.value.toString())
                }
            }
        }
    }
    
    override fun deserialize(decoder: Decoder): ActionParam {
        return decoder.decodeStructure(descriptor) {
            var type = ""
            var valueStr = ""
            
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> type = decodeStringElement(descriptor, 0)
                    1 -> valueStr = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("意外的索引: $index")
                }
            }
            
            when (type) {
                "string" -> ActionParam.StringParam(valueStr)
                "int" -> ActionParam.IntParam(valueStr.toIntOrNull() ?: 0)
                "long" -> ActionParam.LongParam(valueStr.toLongOrNull() ?: 0L)
                "boolean" -> ActionParam.BooleanParam(valueStr.toBoolean())
                "float" -> ActionParam.FloatParam(valueStr.toFloatOrNull() ?: 0f)
                "double" -> ActionParam.DoubleParam(valueStr.toDoubleOrNull() ?: 0.0)
                else -> ActionParam.StringParam(valueStr)
            }
        }
    }
}

// ===== 辅助函数 =====

/**
 * 创建字符串参数
 */
fun stringParam(value: String): ActionParam = ActionParam.StringParam(value)

/**
 * 创建整数参数
 */
fun intParam(value: Int): ActionParam = ActionParam.IntParam(value)

/**
 * 创建长整数参数
 */
fun longParam(value: Long): ActionParam = ActionParam.LongParam(value)

/**
 * 创建布尔参数
 */
fun booleanParam(value: Boolean): ActionParam = ActionParam.BooleanParam(value)

/**
 * 创建浮点数参数
 */
fun floatParam(value: Float): ActionParam = ActionParam.FloatParam(value)

/**
 * 创建双精度参数
 */
fun doubleParam(value: Double): ActionParam = ActionParam.DoubleParam(value)

/**
 * 将Map<String, Any>转换为Map<String, ActionParam>
 */
fun Map<String, Any>.toActionParamMap(): Map<String, ActionParam> {
    return mapValues { (_, value) ->
        when (value) {
            is String -> stringParam(value)
            is Int -> intParam(value)
            is Long -> longParam(value)
            is Boolean -> booleanParam(value)
            is Float -> floatParam(value)
            is Double -> doubleParam(value)
            else -> stringParam(value.toString())
        }
    }
}

/**
 * 将Map<String, ActionParam>转换为Map<String, Any>
 */
fun Map<String, ActionParam>.toAnyMap(): Map<String, Any> {
    return mapValues { (_, param) -> param.getValue() }
}

// ===== 主要动作数据类 =====

/**
 * 动作数据模型 - 使用特定参数类型
 */
@Serializable
data class ActionData(
    val id: String,
    val name: String,
    val type: String,                // 动作类型：click, swipe, wait 等
    val params: Map<String, ActionParam> = emptyMap(),  // ✅ 使用特定类型
    val enabled: Boolean = true,
    val delayAfter: Long = 1000L
) {
    
    companion object {
        // ===== 快捷创建方法 =====
        
        /**
         * 创建点击动作
         */
        fun createClick(
            id: String = generateId(),
            name: String = "点击",
            x: Int = 500,
            y: Int = 500,
            enabled: Boolean = true,
            delayAfter: Long = 1000L
        ): ActionData {
            return ActionData(
                id = id,
                name = name,
                type = "click",
                params = mapOf(
                    "x" to intParam(x),
                    "y" to intParam(y)
                ),
                enabled = enabled,
                delayAfter = delayAfter
            )
        }
        
        /**
         * 创建滑动动作
         */
        fun createSwipe(
            id: String = generateId(),
            name: String = "滑动",
            startX: Int = 300,
            startY: Int = 500,
            endX: Int = 700,
            endY: Int = 500,
            duration: Long = 500L,
            enabled: Boolean = true,
            delayAfter: Long = 1000L
        ): ActionData {
            return ActionData(
                id = id,
                name = name,
                type = "swipe",
                params = mapOf(
                    "startX" to intParam(startX),
                    "startY" to intParam(startY),
                    "endX" to intParam(endX),
                    "endY" to intParam(endY),
                    "duration" to longParam(duration)
                ),
                enabled = enabled,
                delayAfter = delayAfter
            )
        }
        
        /**
         * 创建等待动作
         */
        fun createWait(
            id: String = generateId(),
            name: String = "等待",
            duration: Long = 1000L,
            enabled: Boolean = true,
            delayAfter: Long = 0L
        ): ActionData {
            return ActionData(
                id = id,
                name = name,
                type = "wait",
                params = mapOf("duration" to longParam(duration)),
                enabled = enabled,
                delayAfter = delayAfter
            )
        }
        
        // ===== 工具方法 =====
        
        /**
         * 生成动作ID
         */
        private fun generateId(): String = UUID.randomUUID().toString().take(8)
    }
    
    // ===== 数据类辅助方法 =====
    
    /**
     * 复制动作并更新参数（使用ActionParam）
     */
    fun copyWithParams(newParams: Map<String, ActionParam>): ActionData {
        return this.copy(params = newParams)
    }
    
    /**
     * 复制动作并更新参数（使用Any）
     */
    fun copyWithParamsAny(newParams: Map<String, Any>): ActionData {
        return copyWithParams(newParams.toActionParamMap())
    }
    
    /**
     * 启用/禁用动作
     */
    fun copyWithEnabled(enabled: Boolean): ActionData {
        return this.copy(enabled = enabled)
    }
    
    /**
     * 获取参数值（带类型安全）
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getParam(key: String, defaultValue: T): T {
        val param = params[key] ?: return defaultValue
        return param.getValue() as? T ?: defaultValue
    }
    
    /**
     * 获取参数值作为ActionParam
     */
    fun getParamAsActionParam(key: String): ActionParam? {
        return params[key]
    }
    
    /**
     * 获取字符串参数
     */
    fun getStringParam(key: String, defaultValue: String = ""): String {
        return getParam(key, defaultValue)
    }
    
    /**
     * 获取整数参数
     */
    fun getIntParam(key: String, defaultValue: Int = 0): Int {
        return getParam(key, defaultValue)
    }
    
    /**
     * 获取长整数参数
     */
    fun getLongParam(key: String, defaultValue: Long = 0L): Long {
        return getParam(key, defaultValue)
    }
    
    /**
     * 获取布尔参数
     */
    fun getBooleanParam(key: String, defaultValue: Boolean = false): Boolean {
        return getParam(key, defaultValue)
    }
    
    /**
     * 获取浮点数参数
     */
    fun getFloatParam(key: String, defaultValue: Float = 0f): Float {
        return getParam(key, defaultValue)
    }
    
    /**
     * 获取双精度参数
     */
    fun getDoubleParam(key: String, defaultValue: Double = 0.0): Double {
        return getParam(key, defaultValue)
    }
    
    /**
     * 获取所有参数作为Map<String, Any>
     */
    fun getParamsAsAnyMap(): Map<String, Any> {
        return params.toAnyMap()
    }
    
    /**
     * 检查动作是否有效
     */
    fun isValid(): Boolean {
        return when (type) {
            "click" -> {
                val x = getIntParam("x")
                val y = getIntParam("y")
                x >= 0 && y >= 0
            }
            "swipe" -> {
                val startX = getIntParam("startX")
                val startY = getIntParam("startY")
                val endX = getIntParam("endX")
                val endY = getIntParam("endY")
                val duration = getLongParam("duration")
                startX >= 0 && startY >= 0 && endX >= 0 && endY >= 0 && duration > 0
            }
            "wait" -> {
                val duration = getLongParam("duration")
                duration > 0
            }
            else -> true  // 其他类型暂不验证
        }
    }
    
    /**
     * 获取动作的简要描述
     */
    fun getDescription(): String {
        return when (type) {
            "click" -> {
                val x = getIntParam("x")
                val y = getIntParam("y")
                "点击($x, $y)"
            }
            "swipe" -> {
                val startX = getIntParam("startX")
                val startY = getIntParam("startY")
                val endX = getIntParam("endX")
                val endY = getIntParam("endY")
                val duration = getLongParam("duration")
                "滑动($startX,$startY)→($endX,$endY) ${duration}ms"
            }
            "wait" -> {
                val duration = getLongParam("duration")
                "等待${duration}ms"
            }
            else -> "$type"
        }
    }
    
    /**
     * 转换为JSON字符串（用于调试）
     */
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}

// ===== 扩展函数 =====

/**
 * 获取点击坐标
 */
fun ActionData.getClickPosition(): Pair<Int, Int>? {
    if (type != "click") return null
    val x = getIntParam("x", 0)
    val y = getIntParam("y", 0)
    return Pair(x, y)
}

/**
 * 滑动参数数据类
 */
data class SwipeParams(
    val startX: Int = 300,
    val startY: Int = 500,
    val endX: Int = 700,
    val endY: Int = 500,
    val duration: Long = 500L
)

/**
 * 获取滑动参数
 */
fun ActionData.getSwipeParams(): SwipeParams? {
    if (type != "swipe") return null
    return SwipeParams(
        startX = getIntParam("startX", 300),
        startY = getIntParam("startY", 500),
        endX = getIntParam("endX", 700),
        endY = getIntParam("endY", 500),
        duration = getLongParam("duration", 500L)
    )
}

/**
 * 获取等待时间
 */
fun ActionData.getWaitDuration(): Long? {
    if (type != "wait") return null
    return getLongParam("duration", 1000L)
}

// ===== 动作验证器 =====

object ActionValidator {
    /**
     * 验证动作数据
     */
    fun validate(action: ActionData): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 基础验证
        if (action.id.isEmpty()) errors.add("动作ID不能为空")
        if (action.name.isEmpty()) errors.add("动作名称不能为空")
        if (action.type.isEmpty()) errors.add("动作类型不能为空")
        
        // 类型特定验证
        when (action.type) {
            "click" -> {
                val x = action.getIntParam("x")
                val y = action.getIntParam("y")
                if (x < 0) errors.add("点击X坐标不能为负数")
                if (y < 0) errors.add("点击Y坐标不能为负数")
                if (x > 5000) errors.add("点击X坐标超出范围(0-5000)")
                if (y > 5000) errors.add("点击Y坐标超出范围(0-5000)")
            }
            "swipe" -> {
                val startX = action.getIntParam("startX")
                val startY = action.getIntParam("startY")
                val endX = action.getIntParam("endX")
                val endY = action.getIntParam("endY")
                val duration = action.getLongParam("duration")
                
                if (startX < 0) errors.add("滑动起点X坐标不能为负数")
                if (startY < 0) errors.add("滑动起点Y坐标不能为负数")
                if (endX < 0) errors.add("滑动终点X坐标不能为负数")
                if (endY < 0) errors.add("滑动终点Y坐标不能为负数")
                if (duration <= 0) errors.add("滑动时长必须大于0")
            }
            "wait" -> {
                val duration = action.getLongParam("duration")
                if (duration <= 0) errors.add("等待时间必须大于0")
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )
}

// ===== 动作类型工具 =====

/**
 * 动作类型工具类
 */
object ActionTypes {
    const val CLICK = "click"
    const val SWIPE = "swipe"
    const val WAIT = "wait"
    const val LONG_CLICK = "long_click"
    const val SCROLL = "scroll"
    const val INPUT_TEXT = "input_text"
    
    /**
     * 获取动作类型的显示名称
     */
    fun getDisplayName(type: String): String {
        return when (type) {
            CLICK -> "点击"
            SWIPE -> "滑动"
            WAIT -> "等待"
            LONG_CLICK -> "长按"
            SCROLL -> "滚动"
            INPUT_TEXT -> "输入文本"
            else -> type
        }
    }
    
    /**
     * 获取动作类型的图标名称（用于UI）
     */
    fun getIconName(type: String): String {
        return when (type) {
            CLICK -> "touch_app"
            SWIPE -> "swap_horiz"
            WAIT -> "timer"
            LONG_CLICK -> "touch_app"  // 使用相同图标
            SCROLL -> "swap_vert"
            INPUT_TEXT -> "keyboard"
            else -> "help"
        }
    }
    
    /**
     * 检查是否为基本动作类型
     */
    fun isBasicAction(type: String): Boolean {
        return type in listOf(CLICK, SWIPE, WAIT)
    }
    
    /**
     * 获取所有动作类型
     */
    fun getAllTypes(): List<String> {
        return listOf(CLICK, SWIPE, WAIT, LONG_CLICK, SCROLL, INPUT_TEXT)
    }
}