package io.saeid.compiler

import io.saeid.compiler.SymbolType.DIGIT
import io.saeid.compiler.SymbolType.ID
import io.saeid.compiler.SymbolType.RESERVED

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

data class Symbol(val name: String, val type: SymbolType = RESERVED, val scope: Int = -1) {

    fun typeToTableName(): String {
        return when (type) {
            DIGIT -> "NUM"
            ID -> "ID"
            else -> {
                name
            }
        }

    }
}

enum class SymbolType {
    DIGIT, ID, ANY, RESERVED
}

typealias RawRules = List<List<String>>

typealias Table = Map<Int, Map<String, String>>

data class Rule(val left: String, val right: List<String>)

data class Reduce(val rule: Rule, val cur: Symbol, val prev: Symbol, val old: Symbol)

data class SymbolScope(var address: Int, var isVar: Boolean = false,
        var isInt: Boolean = false, var callbackAddress: Int = 0,
        var returnAddress: Int = 0,
        var inputAddress: Int = 0,
        var size: Int = 0,
        var arguments: MutableList<String> = mutableListOf(),
        var isVoid: Boolean = false, val isReference: Boolean = false
)

data class SymbolScopeTable(
        private val symbols: MutableMap<String, SymbolScope> = mutableMapOf()) {
    fun insert(token: Symbol, scope: SymbolScope) {
        if (symbols.keys.contains(token.name))
            Logger.error("multiple declaration of $token")
        symbols[token.name] = scope
    }

    fun lookup(token: String) = symbols[token]
    fun update(token: String, arguments: MutableList<String> = mutableListOf(), size: Int = -1) {
        if (size != -1)
            symbols[token]?.size = size
        if (arguments.isNotEmpty()) {
            symbols[token]?.arguments = arguments
        }
    }

}

data class SymbolTable(val scopes: MutableList<SymbolScopeTable> = mutableListOf()) {
    fun insert(token: Symbol, scope: SymbolScope) {
        scopes.last().insert(token, scope)
    }

    fun increase() {
        scopes.add(SymbolScopeTable())
    }

    fun lookup(token: String): SymbolScope? {
        var ss: SymbolScope? = null
        scopes.reversed().forEach {
            if (ss == null) {
                ss = it.lookup(token)
            }
        }
        if (ss == null) {
            Logger.error("$token was not declared")
        }
        return ss
    }

    fun update(token: String, size: Int) {
        scopes.last().update(token, size = size)
    }
}