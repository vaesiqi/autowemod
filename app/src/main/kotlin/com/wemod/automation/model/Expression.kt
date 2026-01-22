// ===========================================
// 文件：app/src/main/kotlin/com/wemod/automation/model/Expression.kt
// 重构：表达式系统 - 为条件判断和变量计算做准备
// ===========================================

package com.wemod.automation.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 表达式系统 - 为条件判断和变量计算做准备（修复版）
 */
@Serializable
sealed class Expression {
    
    /**
     * 字面量表达式
     */
    @Serializable
    data class Literal(
        @Contextual
        val value: Any
    ) : Expression()
    
    /**
     * 变量引用表达式
     */
    @Serializable
    data class Variable(val name: String) : Expression()
    
    /**
     * 二元运算表达式
     */
    @Serializable
    data class BinaryOperation(
        val left: Expression,
        val operator: Operator,
        val right: Expression
    ) : Expression()
    
    /**
     * 函数调用表达式
     */
    @Serializable
    data class FunctionCall(
        val functionName: String,
        val arguments: List<Expression>
    ) : Expression()
    
    /**
     * 一元运算表达式
     */
    @Serializable
    data class UnaryOperation(
        val operator: UnaryOperator,
        val operand: Expression
    ) : Expression()
    
    /**
     * 属性访问表达式
     */
    @Serializable
    data class PropertyAccess(
        val target: Expression,
        val propertyName: String
    ) : Expression()
    
    /**
     * 数组访问表达式
     */
    @Serializable
    data class ArrayAccess(
        val array: Expression,
        val index: Expression
    ) : Expression()
    
    /**
     * 三元条件表达式
     */
    @Serializable
    data class Conditional(
        val condition: Expression,
        val thenExpr: Expression,
        val elseExpr: Expression
    ) : Expression()
    
    /**
     * 二元运算符
     */
    @Serializable
    enum class Operator {
        // 算术运算
        ADD,        // +
        SUBTRACT,   // -
        MULTIPLY,   // *
        DIVIDE,     // /
        MODULO,     // %
        
        // 比较运算
        EQUAL,              // ==
        NOT_EQUAL,          // !=
        GREATER_THAN,       // >
        GREATER_EQUAL,      // >=
        LESS_THAN,          // <
        LESS_EQUAL,         // <=
        
        // 逻辑运算
        AND,        // &&
        OR,         // ||
        
        // 位运算
        BITWISE_AND,    // &
        BITWISE_OR,     // |
        BITWISE_XOR,    // ^
        LEFT_SHIFT,     // <<
        RIGHT_SHIFT,    // >>
        
        // 字符串运算
        CONCAT      // + (字符串连接)
    }
    
    /**
     * 一元运算符
     */
    @Serializable
    enum class UnaryOperator {
        NEGATE,     // - (负号)
        NOT,        // ! (逻辑非)
        BITWISE_NOT // ~ (按位取反)
    }
    
    /**
     * 表达式求值上下文
     */
    data class EvaluationContext(
        val variables: Map<String, Any> = emptyMap(),
        val functions: Map<String, (List<Any>) -> Any> = emptyMap()
    )
    
    companion object {
        // ===== 创建表达式 =====
        
        /**
         * 创建字面量表达式
         */
        fun literal(value: Any): Expression = Literal(value)
        
        /**
         * 创建变量表达式
         */
        fun variable(name: String): Expression = Variable(name)
        
        /**
         * 创建二元运算表达式
         */
        fun binary(left: Expression, operator: Operator, right: Expression): Expression {
            return BinaryOperation(left, operator, right)
        }
        
        /**
         * 创建一元运算表达式
         */
        fun unary(operator: UnaryOperator, operand: Expression): Expression {
            return UnaryOperation(operator, operand)
        }
        
        /**
         * 创建函数调用表达式
         */
        fun call(functionName: String, vararg arguments: Expression): Expression {
            return FunctionCall(functionName, arguments.toList())
        }
        
        /**
         * 创建加法表达式
         */
        fun add(left: Expression, right: Expression): Expression {
            return binary(left, Operator.ADD, right)
        }
        
        /**
         * 创建减法表达式
         */
        fun subtract(left: Expression, right: Expression): Expression {
            return binary(left, Operator.SUBTRACT, right)
        }
        
        /**
         * 创建相等比较表达式
         */
        fun equal(left: Expression, right: Expression): Expression {
            return binary(left, Operator.EQUAL, right)
        }
        
        /**
         * 创建不等比较表达式
         */
        fun notEqual(left: Expression, right: Expression): Expression {
            return binary(left, Operator.NOT_EQUAL, right)
        }
        
        /**
         * 创建逻辑与表达式
         */
        fun and(left: Expression, right: Expression): Expression {
            return binary(left, Operator.AND, right)
        }
        
        /**
         * 创建逻辑或表达式
         */
        fun or(left: Expression, right: Expression): Expression {
            return binary(left, Operator.OR, right)
        }
        
        /**
         * 创建条件表达式
         */
        fun condition(
            condition: Expression,
            thenExpr: Expression,
            elseExpr: Expression
        ): Expression {
            return Conditional(condition, thenExpr, elseExpr)
        }
        
        // ===== 表达式求值 =====
        
        /**
         * 计算表达式值
         */
        fun evaluate(
            expression: Expression,
            context: EvaluationContext = EvaluationContext()
        ): Result<Any> {
            return try {
                Result.success(evaluateInternal(expression, context))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        
        /**
         * 内部求值方法
         */
        private fun evaluateInternal(
            expression: Expression,
            context: EvaluationContext
        ): Any {
            return when (expression) {
                is Literal -> expression.value
                is Variable -> {
                    val value = context.variables[expression.name]
                    if (value == null) {
                        throw EvaluationException("变量未定义: ${expression.name}")
                    }
                    value
                }
                is BinaryOperation -> {
                    val leftVal = evaluateInternal(expression.left, context)
                    val rightVal = evaluateInternal(expression.right, context)
                    evaluateBinaryOperation(leftVal, rightVal, expression.operator)
                }
                is UnaryOperation -> {
                    val operandVal = evaluateInternal(expression.operand, context)
                    evaluateUnaryOperation(operandVal, expression.operator)
                }
                is FunctionCall -> {
                    val func = context.functions[expression.functionName]
                    if (func == null) {
                        throw EvaluationException("函数未定义: ${expression.functionName}")
                    }
                    val args = expression.arguments.map { evaluateInternal(it, context) }
                    func(args)
                }
                is PropertyAccess -> {
                    val targetVal = evaluateInternal(expression.target, context)
                    getProperty(targetVal, expression.propertyName)
                }
                is ArrayAccess -> {
                    val arrayVal = evaluateInternal(expression.array, context)
                    val indexVal = evaluateInternal(expression.index, context)
                    getArrayElement(arrayVal, indexVal)
                }
                is Conditional -> {
                    val condVal = evaluateInternal(expression.condition, context)
                    if (isTruthy(condVal)) {
                        evaluateInternal(expression.thenExpr, context)
                    } else {
                        evaluateInternal(expression.elseExpr, context)
                    }
                }
            }
        }
        
        /**
         * 计算二元运算
         */
        private fun evaluateBinaryOperation(left: Any, right: Any, operator: Operator): Any {
            return when (operator) {
                // 算术运算
                Operator.ADD -> when {
                    left is Number && right is Number -> 
                        left.toDouble() + right.toDouble()
                    left is String && right is String -> 
                        left + right
                    left is String -> 
                        left + right.toString()
                    right is String -> 
                        left.toString() + right
                    else -> 
                        throw EvaluationException("不支持的操作数类型: $left + $right")
                }
                Operator.SUBTRACT -> arithmeticOperation(left, right) { a, b -> a - b }
                Operator.MULTIPLY -> arithmeticOperation(left, right) { a, b -> a * b }
                Operator.DIVIDE -> arithmeticOperation(left, right) { a, b -> a / b }
                Operator.MODULO -> arithmeticOperation(left, right) { a, b -> a % b }
                
                // 比较运算
                Operator.EQUAL -> left == right
                Operator.NOT_EQUAL -> left != right
                Operator.GREATER_THAN -> compareOperation(left, right) > 0
                Operator.GREATER_EQUAL -> compareOperation(left, right) >= 0
                Operator.LESS_THAN -> compareOperation(left, right) < 0
                Operator.LESS_EQUAL -> compareOperation(left, right) <= 0
                
                // 逻辑运算
                Operator.AND -> isTruthy(left) && isTruthy(right)
                Operator.OR -> isTruthy(left) || isTruthy(right)
                
                // 位运算
                Operator.BITWISE_AND -> bitwiseOperation(left, right) { a, b -> a and b }
                Operator.BITWISE_OR -> bitwiseOperation(left, right) { a, b -> a or b }
                Operator.BITWISE_XOR -> bitwiseOperation(left, right) { a, b -> a xor b }
                Operator.LEFT_SHIFT -> bitwiseOperation(left, right) { a, b -> a shl b }
                Operator.RIGHT_SHIFT -> bitwiseOperation(left, right) { a, b -> a shr b }
                
                // 字符串连接
                Operator.CONCAT -> left.toString() + right.toString()
            }
        }
        
        /**
         * 计算一元运算
         */
        private fun evaluateUnaryOperation(operand: Any, operator: UnaryOperator): Any {
            return when (operator) {
                UnaryOperator.NEGATE -> when (operand) {
                    is Number -> -operand.toDouble()
                    else -> throw EvaluationException("不支持的操作数类型: -$operand")
                }
                UnaryOperator.NOT -> !isTruthy(operand)
                UnaryOperator.BITWISE_NOT -> when (operand) {
                    is Int -> operand.inv()
                    is Long -> operand.inv()
                    else -> throw EvaluationException("不支持的操作数类型: ~$operand")
                }
            }
        }
        
        /**
         * 算术运算辅助函数
         */
        private fun arithmeticOperation(
            left: Any,
            right: Any,
            operation: (Double, Double) -> Double
        ): Any {
            return when {
                left is Number && right is Number -> 
                    operation(left.toDouble(), right.toDouble())
                else -> 
                    throw EvaluationException("不支持的操作数类型: $left, $right")
            }
        }
        
        /**
         * 比较运算辅助函数
         */
        private fun compareOperation(left: Any, right: Any): Int {
            return when {
                left is Comparable<*> && right is Comparable<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    (left as Comparable<Any>).compareTo(right as Any)
                }
                else -> throw EvaluationException("不可比较的操作数: $left, $right")
            }
        }
        
        /**
         * 位运算辅助函数
         */
        private fun bitwiseOperation(
            left: Any,
            right: Any,
            operation: (Int, Int) -> Int
        ): Int {
            return when {
                left is Int && right is Int -> operation(left, right)
                else -> throw EvaluationException("不支持的操作数类型: $left, $right")
            }
        }
        
        /**
         * 获取对象属性
         */
        private fun getProperty(target: Any, propertyName: String): Any {
            return when (target) {
                is Map<*, *> -> target[propertyName] ?: 
                    throw EvaluationException("属性不存在: $propertyName")
                else -> throw EvaluationException("不支持属性访问的对象类型: ${target::class}")
            }
        }
        
        /**
         * 获取数组元素
         */
        private fun getArrayElement(array: Any, index: Any): Any {
            return when {
                array is List<*> && index is Int -> 
                    array.getOrNull(index) ?: 
                        throw EvaluationException("数组索引越界: $index")
                array is String && index is Int -> 
                    array.getOrNull(index) ?: 
                        throw EvaluationException("字符串索引越界: $index")
                else -> throw EvaluationException("不支持数组访问的类型: ${array::class}[${index::class}]")
            }
        }
        
        /**
         * 判断值是否为真
         */
        private fun isTruthy(value: Any): Boolean {
            return when (value) {
                is Boolean -> value
                is Number -> value.toDouble() != 0.0
                is String -> value.isNotEmpty()
                is Collection<*> -> value.isNotEmpty()
                is Array<*> -> value.isNotEmpty()
                null -> false
                else -> true
            }
        }
    }
}

/**
 * 条件表达式
 */
@Serializable
data class Condition(
    val expression: Expression,
    val thenActions: List<ActionData> = emptyList(),
    val elseActions: List<ActionData> = emptyList(),
    val description: String = ""
) {
    /**
     * 验证条件是否有效
     */
    fun isValid(): Boolean {
        return try {
            // 尝试解析表达式
            val result = Expression.evaluate(expression)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取条件描述 - 修改方法名避免冲突
     */
    fun getDisplayDescription(): String {
        return description.ifEmpty { "条件判断" }
    }
}

/**
 * 循环表达式
 */
@Serializable
data class Loop(
    val count: Int = 1,
    val whileCondition: Expression? = null,
    val actions: List<ActionData> = emptyList(),
    val description: String = ""
) {
    /**
     * 检查是否是无限循环
     */
    val isInfinite: Boolean
        get() = count <= 0 && whileCondition == null
    
    /**
     * 检查是否有效
     */
    fun isValid(): Boolean {
        // 必须有循环条件或循环次数
        if (count <= 0 && whileCondition == null) {
            return false
        }
        
        // 检查循环条件是否有效
        whileCondition?.let { condition ->
            val result = Expression.evaluate(condition)
            if (result.isFailure) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * 获取循环描述 - 修改方法名避免冲突
     */
    fun getDisplayDescription(): String {
        return description.ifEmpty {
            when {
                count > 0 -> "循环 $count 次"
                whileCondition != null -> "条件循环"
                else -> "无限循环"
            }
        }
    }
}

/**
 * 变量定义
 */
@Serializable
data class VariableDef(
    val name: String,
    @Contextual
    val value: Any,
    val type: VariableType = VariableType.AUTO,
    val mutable: Boolean = true,
    val description: String = ""
) {
    /**
     * 检查变量名是否有效
     */
    fun isNameValid(): Boolean {
        return name.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))
    }
    
    /**
     * 获取变量类型
     */
    fun getActualType(): VariableType {
        if (type != VariableType.AUTO) {
            return type
        }
        
        return when (value) {
            is String -> VariableType.STRING
            is Number -> VariableType.NUMBER
            is Boolean -> VariableType.BOOLEAN
            is List<*> -> VariableType.ARRAY
            is Map<*, *> -> VariableType.OBJECT
            else -> VariableType.AUTO
        }
    }
}

/**
 * 变量类型
 */
@Serializable
enum class VariableType {
    AUTO,       // 自动推断
    STRING,     // 字符串
    NUMBER,     // 数字
    BOOLEAN,    // 布尔值
    ARRAY,      // 数组
    OBJECT ;     // 对象
    
    companion object {
        /**
         * 从字符串获取变量类型
         */
        fun fromString(typeStr: String): VariableType? {
            return values().find { it.name.equals(typeStr, ignoreCase = true) }
        }
        
        /**
         * 获取类型默认值
         */
        fun defaultValue(type: VariableType): Any {
            return when (type) {
                STRING -> ""
                NUMBER -> 0
                BOOLEAN -> false
                ARRAY -> emptyList<Any>()
                OBJECT -> emptyMap<String, Any>()
                else -> Unit
            }
        }
    }
}

/**
 * 表达式求值异常
 */
class EvaluationException(message: String) : Exception(message)

/**
 * 变量管理器
 */
class VariableManager {
    private val variables = mutableMapOf<String, VariableDef>()
    private val contexts = mutableListOf<MutableMap<String, Any>>()
    
    init {
        // 创建全局上下文
        contexts.add(mutableMapOf())
    }
    
    /**
     * 定义变量
     */
    fun define(variable: VariableDef): Boolean {
        if (!variable.isNameValid()) {
            return false
        }
        
        variables[variable.name] = variable
        setValue(variable.name, variable.value)
        return true
    }
    
    /**
     * 获取变量值
     */
    fun getValue(name: String): Any? {
        // 从最近的上下文开始查找
        for (context in contexts.reversed()) {
            if (context.containsKey(name)) {
                return context[name]
            }
        }
        return null
    }
    
    /**
     * 设置变量值
     */
    fun setValue(name: String, value: Any): Boolean {
        val variable = variables[name]
        if (variable == null || !variable.mutable) {
            return false
        }
        
        // 类型检查
        val actualType = variable.getActualType()
        if (!isTypeCompatible(value, actualType)) {
            return false
        }
        
        // 设置到当前上下文
        contexts.last()[name] = value
        return true
    }
    
    /**
     * 检查类型兼容性
     */
    private fun isTypeCompatible(value: Any, type: VariableType): Boolean {
        return when (type) {
            VariableType.STRING -> value is String
            VariableType.NUMBER -> value is Number
            VariableType.BOOLEAN -> value is Boolean
            VariableType.ARRAY -> value is List<*>
            VariableType.OBJECT -> value is Map<*, *>
            VariableType.AUTO -> true
        }
    }
    
    /**
     * 创建新上下文
     */
    fun pushContext() {
        contexts.add(mutableMapOf())
    }
    
    /**
     * 弹出上下文
     */
    fun popContext(): Boolean {
        if (contexts.size > 1) {
            contexts.removeAt(contexts.size - 1)
            return true
        }
        return false
    }
    
    /**
     * 获取所有变量
     */
    fun getAllVariables(): Map<String, VariableDef> {
        return variables.toMap()
    }
    
    /**
     * 清空所有变量
     */
    fun clear() {
        variables.clear()
        contexts.clear()
        contexts.add(mutableMapOf())
    }
    
    /**
     * 创建表达式求值上下文
     */
    fun createEvaluationContext(): Expression.EvaluationContext {
        val varValues = mutableMapOf<String, Any>()
        
        // 收集所有上下文中的变量值
        contexts.forEach { context ->
            varValues.putAll(context)
        }
        
        return Expression.EvaluationContext(
            variables = varValues,
            functions = builtInFunctions()
        )
    }
    
    /**
     * 内置函数
     */
    private fun builtInFunctions(): Map<String, (List<Any>) -> Any> {
        return mapOf(
            "length" to { args ->
                if (args.size != 1) throw EvaluationException("length() 需要一个参数")
                when (val arg = args[0]) {
                    is String -> arg.length
                    is List<*> -> arg.size
                    is Array<*> -> arg.size
                    else -> throw EvaluationException("length() 不支持的类型")
                }
            },
            
            "toString" to { args ->
                if (args.size != 1) throw EvaluationException("toString() 需要一个参数")
                args[0].toString()
            },
            
            "toInt" to { args ->
                if (args.size != 1) throw EvaluationException("toInt() 需要一个参数")
                when (val arg = args[0]) {
                    is Number -> arg.toInt()
                    is String -> arg.toIntOrNull() ?: throw EvaluationException("无法转换为整数")
                    else -> throw EvaluationException("toInt() 不支持的类型")
                }
            },
            
            "round" to { args ->
                if (args.size != 1) throw EvaluationException("round() 需要一个参数")
                when (val arg = args[0]) {
                    is Number -> kotlin.math.round(arg.toDouble()).toInt()
                    else -> throw EvaluationException("round() 不支持的类型")
                }
            },
            
            "contains" to { args ->
                if (args.size != 2) throw EvaluationException("contains() 需要两个参数")
                val container = args[0]
                val element = args[1]
                when (container) {
                    is String -> container.contains(element.toString())
                    is List<*> -> container.contains(element)
                    else -> throw EvaluationException("contains() 不支持的类型")
                }
            }
        )
    }
}