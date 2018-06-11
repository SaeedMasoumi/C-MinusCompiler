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

data class Item(var isVoid: Boolean = false, val isInt: Boolean = false,
        var address: Int = 0,
        var isVariable:Boolean = false,
        var returnAddress: Int = 0, var callbackAddress: Int = 0, var inputAddress: Int = 0,
        var args: MutableList<String> = mutableListOf(), var size: Int = 0)

data class Scope(private val map: MutableMap<String, Item> = mutableMapOf()) {
    fun insert(token: String, item: Item) {
        map[token] = item
    }

    fun print() {
        map.forEach { t, s ->
            println("$t : $s")
        }
    }

    fun lookup(currentFunction: String) = map[currentFunction]

}

data class SymbolTable(private val scopes: MutableList<Scope> = mutableListOf()) {
    fun increase() {
        scopes.add(Scope())
    }

    fun insert(token: Symbol, item: Item) {
        scopes.last().insert(token.name, item)
    }

    fun print() {
        scopes.forEach {
            it.print()
        }
    }

    fun decrease() {
        scopes.removeAt(scopes.size - 1)
    }

    fun getLast() = scopes.last()
    
    fun get(token: String): Item? {
        var item: Item? = null
        scopes.forEach {
            if (item == null)
                item = it.lookup(token)
        }
        if (item == null) {
            Logger.log("$token is not declared")
        }
        return item
    }
}